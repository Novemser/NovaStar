package nsdfs;

import util.Constants;

import java.io.Serializable;
import java.util.*;

/**
 * Project: HadoopDFS
 * Package: nsdfs
 * Author:  Novemser
 * 2016/12/29
 */
public class FileMetadata implements Serializable {
    private String fileId;

    private Date createTime;

    private int realBlockCount;

    private List<List<FileBlock>> fileBlocks;

    private String fileName;

    private int fileSize;

    private String absPath;

    public FileMetadata() {
        fileBlocks = new ArrayList<>();
        for (int i = 0; i < Constants.REDUNDANT_COUNT; i++) {
            fileBlocks.add(new ArrayList<>());
        }
    }

    public boolean removeFileBlock(FileBlock blockToRemove) {
        if (null == blockToRemove)
            return false;

        List<List<FileBlock>> blocks = fileBlocks;
        int idI = -1, idJ = -1;

        for (int i = 0; i < blocks.size(); i++) {
            List<FileBlock> tmpList = blocks.get(i);
            for (int j = 0; j < tmpList.size(); j++) {
                FileBlock tmpBlock = tmpList.get(j);
                if (tmpBlock.equals(blockToRemove)) {
                    idI = i;
                    idJ = j;
                    break;
                }
            }
        }

        FileBlock removedBlock = null;
        // Found
        if (idI != -1 && idJ != -1) {
            removedBlock = fileBlocks.get(idI).remove(idJ);
        }

        return blockToRemove.equals(removedBlock);
    }

    public int getRealBlockCount() {
        return realBlockCount;
    }

    public void setRealBlockCount(int realBlockCount) {
        this.realBlockCount = realBlockCount;
    }

    public void addFileBlock(FileBlock block, int index) {
        fileBlocks.get(index).add(block);
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public List<Block> getDownloadBlocks() {
        List<Block> blocks = new ArrayList<>();
        Set<Integer> indexes = new HashSet<>();
        for (FileBlock block : getAllFileBlocks()) {
            int idx = block.getIndex();

            if (indexes.contains(idx))
                continue;

            blocks.add(block.getBlock());
            indexes.add(idx);
        }

        return blocks;
    }

    public List<FileBlock> getAllFileBlocks() {
        List<FileBlock> fileBlocksList = new ArrayList<>();
        for (List<FileBlock> bl : fileBlocks) {
            for (FileBlock fileBlock : bl) {
                fileBlocksList.add(fileBlock);
            }
        }
        return fileBlocksList;
    }

//    public void setFileBlocks(List<FileBlock> fileBlocks) {
//        this.fileBlocks = fileBlocks;
//    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getAbsPath() {
        return absPath;
    }

    public void setAbsPath(String absPath) {
        this.absPath = absPath;
    }
}
