package service;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3DirectoryEntry;
import nsdfs.slave.SlaveNode;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import util.Constants;
import util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Project: HadoopDFS
 * Package: service
 * Author:  Novemser
 * 2017/1/4
 */
@Service
public class ScpService {
    private Connection connection;

    @Value("${persistent.local.tmp.dir}")
    private String localTempDir;// = "F:\\NovaStar";

    private boolean authWithSlave(SlaveNode slaveNode) {
        try {
            boolean flag = connection.authenticateWithPassword(slaveNode.getUsername(), slaveNode.getPassword());
            if (flag) {
                Util.printlnTime("SCP认证 " + slaveNode + " 成功");
            }
            return flag;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean getFile(SlaveNode slaveNode, String remoteFile) {
        connection = new Connection(slaveNode.getSlaveIP(), Constants.defaultPort);

        try {
            connection.connect();
            boolean isAuthenticated = authWithSlave(slaveNode);
            if (isAuthenticated) {
                SCPClient scpClient = connection.createSCPClient();
                scpClient.get(remoteFile, localTempDir);
                Util.printlnTime("下载成功");
                return true;
            } else {
                Util.printerrTime("认证失败!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Util.printerrTime("下载失败 " + e.getMessage());
        } finally {
            connection.close();
        }

        return false;
    }

    public boolean putFile(SlaveNode slaveNode, String fileName, String remoteTargetDirectory) {
        connection = new Connection(slaveNode.getSlaveIP(), Constants.defaultPort);

        try {
            connection.connect();

            boolean isAuthenticated = authWithSlave(slaveNode);
            if (isAuthenticated) {
                SCPClient scpClient = connection.createSCPClient();
                scpClient.put(localTempDir + "\\" + fileName, remoteTargetDirectory);
                Util.printlnTime("上传成功");
                return true;
            } else {
                Util.printerrTime("认证失败!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Util.printerrTime("上传失败 " + ex.getMessage());
        } finally {
            connection.close();
        }

        return false;
    }

    public List<SFTPv3DirectoryEntry> lsBySlave(SlaveNode slaveNode) {
        connection = new Connection(slaveNode.getSlaveIP(), Constants.defaultPort);
        boolean isAuthenticated = authWithSlave(slaveNode);
        List<SFTPv3DirectoryEntry> directoryEntries = new ArrayList<>();
        try {
            if (isAuthenticated) {
                SFTPv3Client client = new SFTPv3Client(connection);
                Vector vector = client.ls(slaveNode.getPathFolder());
                for (Object obj : vector) {
                    SFTPv3DirectoryEntry entry = (SFTPv3DirectoryEntry) obj;
                    directoryEntries.add(entry);
                    System.out.println(entry.filename);
//                    System.out.println(entry.longEntry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Util.printerrTime("ls失败 " + e.getMessage());
        } finally {
            connection.close();
        }

        return directoryEntries;
    }

    public boolean renameBlock(String src, String des) {
        File file = new File(localTempDir + "\\" + src);
        return file.renameTo(new File(localTempDir + "\\" + des));
    }

    public void clearDirectory() {
        try {
            FileUtils.deleteDirectory(new File(localTempDir));
            new File(localTempDir).mkdir();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
