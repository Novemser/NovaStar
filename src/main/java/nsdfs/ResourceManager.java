package nsdfs;

import ch.ethz.ssh2.SFTPv3DirectoryEntry;
import com.alibaba.fastjson.JSONObject;
import nsdfs.slave.SlaveNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import service.PersistentService;
import service.ScpService;
import util.FileStatus;
import util.Util;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Project: HadoopDFS
 * Package: nsdfs
 * Author:  Novemser
 * 2016/12/29
 */

@Component
public class ResourceManager implements Serializable {

    private List<FileMetadata> metadataList = new ArrayList<>();
    private List<SlaveNode> slaveNodes = new ArrayList<>();

    @Autowired
    private ScpService scpService;

    @Value("#{'${node.ip}'.split(',')}")
    private List<String> slaveIps;

    @Value("${persistent.metaDataPath}")
    private String metaDataPath;

    @Value("${node.status.interval}")
    private int statusShowInterval;

    @Value("${node.username}")
    private String username;

    @Value("${node.password}")
    private String password;

    @Value("${node.dir}")
    private String pathFolder;

    public void initSlaveList() {
        for (int i = 0; i < slaveIps.size(); i++) {
            SlaveNode node = new SlaveNode("Slave" + i, slaveIps.get(i),
                    username,
                    password,
                    pathFolder);

            addSlave(node);
        }
        listenOnSlaveStatus();
    }

    public SlaveNode getSlaveByIP(String ip) {
        SlaveNode node = null;
        for (SlaveNode slaveNode : slaveNodes) {
            if (slaveNode.getSlaveIP().equals(ip))
                node = slaveNode;
        }
        return node;
    }

    public int getAvailableNodeCount() {
        int count = 0;
        for (SlaveNode slaveNode : slaveNodes) {
            if (slaveNode.getStatus().equals(SlaveNode.STATUS.ALIVE))
                count++;
        }
        return count;
    }

    private void listenOnSlaveStatus() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                checkSlaveStatus();
            }
        };

        timer.schedule(timerTask, 0, statusShowInterval);
    }

    public JSONObject checkSlaveStatus() {
        JSONObject result = new JSONObject();

        CountDownLatch latch = new CountDownLatch(slaveNodes.size()); // 用于进程同步
        for (SlaveNode slaveNode : slaveNodes) {
            // 新起一个线程去发ICMP包
            new Thread(() -> {
                boolean isAlive = slaveNode.checkIsAlive();

                // JSONObject不是线程安全的，需要同步代码块
                synchronized (this) {
                    result.put(slaveNode.getSlaveName(), isAlive);
                }

                if (isAlive) {
                    if (slaveNode.getStatus() == SlaveNode.STATUS.ALIVE) {
                        Util.printlnTime(slaveNode + " 连接正常");
                    } else {
                        Util.printlnTime(slaveNode + " 恢复连接");
                        slaveNode.resetOutCount();
                    }

                    slaveNode.setStatus(SlaveNode.STATUS.ALIVE);
                } else {
                    Util.printerrTime(slaveNode + " 失去连接");
                    slaveNode.setStatus(SlaveNode.STATUS.DIED);
                    // 增加一次失联的计数
                    slaveNode.addOutCount();

                    // 当失去连接次数==2次以后，将该节点从可用节点中删去
                    // 并且将这个节点存储的所有数据都保存在别的节点上
                    if (slaveNode.getOutCount() == 1) {
                        migrantData(slaveNode);
                    }

                }

                latch.countDown(); // 释放一个信号量
            }).start();

        }

        // 等待所有ICMP结束
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    /***
     * 创建一个新的block，为他分配UUID，并将创建好的block加入管理队列中
     * @return 创建好的block
     */
    public Block createNextBlock() {
        UUID uuid = UUID.randomUUID();
        String id = uuid.toString();
        Block block = new Block();
        block.setBlockId(id);
        return block;
    }

    public void addSlave(SlaveNode node) {
        slaveNodes.add(node);
    }

    /***
     * 保存一个文件
     * 并写回到文件中
     * @param metadata 新的文件
     */
    public void addFileMetadata(FileMetadata metadata) {
        metadataList.add(metadata);
        PersistentService.persistMetadata(this, metaDataPath);
    }

    public void migrantData(SlaveNode srcNode) {
        new Thread(() -> {
            HashMap<FileBlock, FileMetadata> blocksToTransfer = new HashMap<>();

            Util.printerrTime(srcNode + " 失效, 开始转移数据....");
            for (FileMetadata metadata : metadataList) {
                for (FileBlock fileBlock : metadata.getAllFileBlocks()) {
                    Block block = fileBlock.getBlock();
                    boolean hasToMove =
                            block.getSlaveIP().equals(srcNode.getSlaveIP()) &&
                                    block.getSlaveName().equals(srcNode.getSlaveName());

                    if (hasToMove) {
                        blocksToTransfer.put(fileBlock, metadata);
                    }

                }
            }

            // 节点数大于2的时候才会转移
            if (getAvailableNodeCount() > 2) {
                // 对每一个需要转移的block统计剩下的2个block存放在何处
                // 之后选择一个新的节点进行存储
                for (Map.Entry<FileBlock, FileMetadata> entry : blocksToTransfer.entrySet()) {
                    FileBlock deadFileBlock = entry.getKey();
                    FileMetadata metadata = entry.getValue();
//                    System.out.println(metadata.getFileName() + ": Block" + fileBlock.getIndex());

                    List<String> otherBlockIPList = new ArrayList<>();
                    FileBlock availOtherBlock = null;
                    // 统计其余block所存放在的物理区域
                    for (FileBlock otherBlock : metadata.getAllFileBlocks()) {

                        if (otherBlock.getIndex() == deadFileBlock.getIndex() &&
                                !otherBlock.equals(deadFileBlock)) {
                            otherBlockIPList.add(otherBlock.getBlock().getSlaveIP());

                            if (null == availOtherBlock)
                                availOtherBlock = otherBlock;
                        }
                    }

                    List<SlaveNode> availableNodes = getAvailableSlaveNodes();
                    SlaveNode nodeToMigrant = null;
                    SlaveNode nodeFromMigrant = null;
                    // 找出一个没有存储过block的slaveNode
                    for (SlaveNode slaveNodeToChoose : availableNodes) {
                        if (!otherBlockIPList.contains(slaveNodeToChoose.getSlaveIP())) {
                            nodeToMigrant = slaveNodeToChoose;
                        } else if (slaveNodeToChoose.getSlaveIP().equals(availOtherBlock.getBlock().getSlaveIP())) {
                            nodeFromMigrant = slaveNodeToChoose;
                        }
                    }
                    // 出错，没有找到满足条件的slave
                    if (null == nodeToMigrant || null == availOtherBlock || null == nodeFromMigrant)
                        return;

                    // 将block的数据拿过来然后放在新的slave上
                    // 把otherBlockSample的数据取过来
                    boolean status = scpService.getFile(nodeFromMigrant, availOtherBlock.getBlock().getAbsPath());

                    if (status) {
                        // 重命名成需要的blockName
                        scpService.renameBlock(availOtherBlock.getBlock().getBlockName(), deadFileBlock.getBlock().getBlockName());
                        // 在新的节点上复制数据
                        scpService.putFile(nodeToMigrant, deadFileBlock.getBlock().getBlockName(), nodeToMigrant.getPathFolder());


                        // TODO:更新namespace
                        Block deadBlock = deadFileBlock.getBlock();
                        deadBlock.setSlaveIP(nodeToMigrant.getSlaveIP());
                        deadBlock.setSlaveName(nodeToMigrant.getSlaveName());
                        deadBlock.setFolderPath(nodeToMigrant.getPathFolder());
                        deadBlock.setAbsPath(deadBlock.getFolderPath() + "/" + deadBlock.getBlockName());
                    }

                }
                PersistentService.persistMetadata(this, metaDataPath);

            }
            // TODO:否则就是挂掉了 暂时先不管
        }).start();

    }

    public List<FileMetadata> getMetadataList() {
        return metadataList;
    }

    public void setMetadataList(List<FileMetadata> metadataList) {
        this.metadataList = metadataList;
        System.out.println("加载NameSpace文件...");
        System.out.println("成功载入" + metadataList.size() + "条记录");
    }

    public List<SlaveNode> getAvailableSlaveNodes() {
        List<SlaveNode> result = new ArrayList<>();
        for (SlaveNode slaveNode : getSlaveNodes()) {
            if (slaveNode.getStatus().equals(SlaveNode.STATUS.ALIVE))
                result.add(slaveNode);
        }
        return result;
    }

    public List<SlaveNode> getSlaveNodes() {
        return slaveNodes;
    }

    public int getSlaveCount() {
        return slaveNodes.size();
    }

    /***
     * 返回下一个可用SlaveNode
     * 当某一个node挂掉以后选择其后的node
     * @param index 索引
     * @return 可用node, 如果无可用Node则返回null
     */
    public SlaveNode getSlave(int index) {
        SlaveNode node = slaveNodes.get(index++);

        while (node.getStatus().equals(SlaveNode.STATUS.DIED) && slaveNodes.size() > 1)
            node = slaveNodes.get((index++) % slaveNodes.size());

        if (node.getStatus().equals(SlaveNode.STATUS.DIED))
            return null;

        return node;
    }

    public FileStatus verifyFileIntegrity(String fileId) {
        FileMetadata fileToVerify = null;
        for (FileMetadata metadata : getMetadataList()) {
            if (metadata.getFileId().equals(fileId)) {
                fileToVerify = metadata;
            }
        }

        if (null == fileToVerify)
            return FileStatus.NOT_FOUND;

        Map<SlaveNode, List<SFTPv3DirectoryEntry>> slaveLSDirs = new HashMap<>();

        // 验证所有Block
        for (FileBlock fileBlock : fileToVerify.getAllFileBlocks()) {
            Block block = fileBlock.getBlock();

            SlaveNode storageNode = getSlaveByIP(block.getSlaveIP());

            if (null != storageNode) {
                // TODO:根据文件名验证文件存在
                if (!slaveLSDirs.containsKey(storageNode))
                    slaveLSDirs.put(storageNode, scpService.lsBySlave(storageNode));

                List<SFTPv3DirectoryEntry> directoryEntries = slaveLSDirs.get(storageNode);
                boolean exist = false;
                for (SFTPv3DirectoryEntry entry : directoryEntries) {
                    if (entry.filename.equals(block.getBlockName())) {
                        exist = true;
                    }
                }

                if (!exist)
                    return FileStatus.BROKEN;
            }
        }


        return FileStatus.OK;
    }

    public boolean deleteFileMetadata(String fileId) {
        FileMetadata dataToRemove = null;
        for (FileMetadata metadata : metadataList) {
            if (metadata.getFileId().equals(fileId)) {
                dataToRemove = metadata;
            }
        }

        if (null != dataToRemove) {
            metadataList.remove(dataToRemove);
            PersistentService.persistMetadata(this, metaDataPath);

            return true;
        }

        return false;
    }

    public boolean renameFileMetadata(String fileId, String newFileName) {
        for (FileMetadata metadata : metadataList) {
            if (metadata.getFileId().equals(fileId)) {
                metadata.setFileName(newFileName);
                return true;
            }
        }

        return false;
    }

    public FileMetadata getFileById(String fileId) {

        for (FileMetadata metadata : metadataList) {
            if (metadata.getFileId().equals(fileId)) {
                return metadata;
            }
        }

        return null;
    }

    public void formatDisk() {
        for (SlaveNode slaveNode : getAvailableSlaveNodes()) {
            scpService.clearSlaveDir(slaveNode);
        }
        PersistentService.persistMetadata(this, metaDataPath);

    }
}
