package nsdfs.slave;

import util.Constants;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Project: HadoopDFS
 * Package: nsdfs.slave
 * Author:  Novemser
 * 2016/12/29
 */
public class SlaveNode implements Serializable, Cloneable {
    public enum STATUS {
        ALIVE,
        DIED
    }

    private String slaveName;
    private String slaveIP;
    private String username;
    private String password;
    private String pathFolder;

    // 默认是死亡的。。。。
    private STATUS status = STATUS.DIED;

    private AtomicInteger outCount;

    public int addOutCount() {
        return outCount.incrementAndGet();
    }

    public void resetOutCount() {
        outCount.set(0);
    }

    public int getOutCount() {
        return outCount.get();
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public String getPathFolder() {
        return pathFolder;
    }

    public boolean checkIsAlive() {
        try {
            return InetAddress.getByName(slaveIP).isReachable(Constants.TIME_OUT);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getSlaveName() {
        return slaveName;
    }

    public String getSlaveIP() {
        return slaveIP;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public SlaveNode(String slaveName,
                     String slaveIP,
                     String username,
                     String password,
                     String pathFolder) {
        this.slaveName = slaveName;
        this.slaveIP = slaveIP;
        this.username = username;
        this.password = password;
        this.pathFolder = pathFolder;

        outCount = new AtomicInteger(0);
    }

    @Override
    public SlaveNode clone() {
        return new SlaveNode(
                slaveName,
                slaveIP,
                username,
                password,
                pathFolder
        );
    }

    @Override
    public String toString() {
        return slaveName + " " + slaveIP;
    }
}
