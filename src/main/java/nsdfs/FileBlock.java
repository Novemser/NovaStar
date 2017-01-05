package nsdfs;

import java.io.Serializable;

/**
 * Project: HadoopDFS
 * Package: nsdfs
 * Author:  Novemser
 * 2016/12/29
 */
public class FileBlock implements Serializable {
    private Block block;
    private int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public FileBlock(Block block, int index) {
        this.block = block;
        this.index = index;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }
}
