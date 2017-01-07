package service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import nsdfs.Block;
import nsdfs.FileBlock;
import nsdfs.FileMetadata;
import nsdfs.ResourceManager;
import nsdfs.slave.SlaveNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import util.Constants;
import util.FileStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Project: HadoopDFS
 * Package: service
 * Author:  Novemser
 * 2016/12/29
 */
@Service
public class MasterService {

    private final ResourceManager manager;
    private ConcurrentHashMap<String, FileMetadata> uploadFileList = new ConcurrentHashMap<>();

    @Autowired
    public MasterService(ResourceManager manager) {
        this.manager = manager;
    }

    public Object getFileNamespace() {
        return manager.getMetadataList();
    }

    public JSONObject requestForUpload(JSONObject request) {
        String fileName = request.getString("name");
        String folder = request.getString("currentFolder");

        folder = (null == folder) ? "/" : folder;

        int fileSize = request.getInteger("fileSize");
        int blockSize = request.getInteger("blockSize");
        int blockNum = request.getInteger("blockNum");
//        JSONObject result = new JSONObject();

        // 先检查一下slave的状态
        manager.checkSlaveStatus();

        // 创建文件元数据
        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(fileName);
        metadata.setFileSize(fileSize);
        metadata.setAbsPath(folder);
        metadata.setCreateTime(new Date());
        metadata.setFileId(UUID.randomUUID().toString());
        metadata.setRealBlockCount(blockNum);

        //TODO:实现分配 每个Block一共存储N个地方
        Random random = new Random();
        JSONObject blockInfo = new JSONObject();
        blockInfo.put("fileId", metadata.getFileId());
        for (int i = 1; i <= blockNum; i++) {
            JSONArray blockList = new JSONArray();

            // 从随机的起始节点依次往后取N个节点存储
            int nodeIndex = 0;
            SlaveNode node, lastNode = null;
            Block block;
            FileBlock fileBlock;
            for (int j = 0; j < Constants.REDUNDANT_COUNT; j++) {
                // 计算下一个节点的位置
                if (j == 0) {
                    nodeIndex = random.nextInt() % manager.getAvailableNodeCount();
                    if (nodeIndex < 0)
                        nodeIndex = -nodeIndex;
                } else {
                    nodeIndex = nodeIndex + 1;
                    nodeIndex = nodeIndex % manager.getAvailableNodeCount();
                }

                // 根据计算生成的索引找到下一个node
                node = manager.getNextAvailSlave(nodeIndex);

                // 如果这个节点已经存储过了就不用再存一遍了
                if (node.equals(lastNode))
                    continue;

                lastNode = node;
                block = manager.createNextBlock();
                // 设置block的信息
                block.setSlaveName(node.getSlaveName());
                block.setSlaveIP(node.getSlaveIP());
                block.setPassword(node.getPassword());
                block.setUsername(node.getUsername());
                block.setFolderPath(node.getPathFolder());
                block.setBlockSize(blockSize);
                block.setBlockName("block_" + block.getBlockId());
                block.setAbsPath(block.getFolderPath() + "/" + block.getBlockName());

                blockList.add(block);
                // 创建fileBlock用于维护namespace
                fileBlock = new FileBlock(block, i);
                metadata.addFileBlock(fileBlock, j);
            }


            blockInfo.put("block" + i, blockList);
        }

        // 先暂时放在请求队列里
        uploadFileList.put(metadata.getFileId(), metadata);
//        manager.addFileMetadata(metadata);
        return blockInfo;
    }

    public JSONObject showSlaveStatus() {
        return manager.checkSlaveStatus();
    }

    public JSONObject verifyFileExists(String fileId) {
        JSONObject result = new JSONObject();
        FileMetadata metadata = uploadFileList.get(fileId);
        FileStatus status = manager.verifyFileIntegrity(metadata);

        switch (status) {
            case OK:
                manager.addFileMetadata(metadata);
                uploadFileList.remove(fileId);
                break;
            case BROKEN:
                uploadFileList.remove(fileId);
                break;
            case NOT_FOUND:
                break;
        }

        result.put("status", status);
        return result;
    }

    public JSONObject renameFile(JSONObject request) {
        JSONObject result = new JSONObject();

        String fileId = request.getString("fileId");
        String fileName = request.getString("fileName");
        boolean succeed = manager.renameFileMetadata(fileId, fileName);

        if (succeed)
            result.put("status", "OK");
        else
            result.put("status", "NOT_FOUND");

        return result;
    }

    public JSONObject deleteFile(JSONObject request) {
        JSONObject result = new JSONObject();

        String fileId = request.getString("fileId");
        boolean succeed = manager.deleteFileMetadata(fileId);
        if (succeed)
            result.put("status", "OK");
        else
            result.put("status", "NOT_FOUND");

        return result;
    }

    public JSONObject getFileById(String fileId) {
        JSONObject result = new JSONObject();
        FileMetadata metadata = manager.getFileById(fileId);

        if (null == metadata)
            result.put("file", "NOT_FOUND");
        else
            result.put("file", metadata);

        return result;
    }

    public JSONObject calculateSpaceUsage() {
        JSONObject result = new JSONObject();
        long total = manager.calculateTotalSpace();
        long used = manager.calculateUsedSpace();
        long free = total - used;
        result.put("total", total);
        result.put("used", used);
        result.put("free", free);
        return result;
    }

    public JSONObject formatAllSlaves() {
        JSONObject result = new JSONObject();
        manager.formatDisk();
        manager.setMetadataList(new ArrayList<>());
        result.put("status", "OK");
        return result;
    }
}
