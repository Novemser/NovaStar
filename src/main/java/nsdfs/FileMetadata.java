package nsdfs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private List<FileBlock> fileBlocks;

    private String fileName;

    private int fileSize;

    private String absPath;

    public int getRealBlockCount() {
        return realBlockCount;
    }

    public void setRealBlockCount(int realBlockCount) {
        this.realBlockCount = realBlockCount;
    }

    public void addFileBlock(FileBlock block) {
        fileBlocks.add(block);
    }

    public FileMetadata() {
        fileBlocks = new ArrayList<FileBlock>();
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

    public List<FileBlock> getFileBlocks() {
        return fileBlocks;
    }

    public void setFileBlocks(List<FileBlock> fileBlocks) {
        this.fileBlocks = fileBlocks;
    }

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
