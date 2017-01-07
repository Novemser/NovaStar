package service;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3DirectoryEntry;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
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
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Project: HadoopDFS
 * Package: service
 * Author:  Novemser
 * 2017/1/4
 */
@Service
public class SSHService {
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
                initSlaveDir(slaveNode);
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

    public void clearSlaveDir(SlaveNode slaveNode) {
        connection = new Connection(slaveNode.getSlaveIP(), Constants.defaultPort);

        try {
            connection.connect();
            boolean isAuthenticated = authWithSlave(slaveNode);
            if (isAuthenticated) {
                SFTPv3Client client = new SFTPv3Client(connection);
                Vector vector = client.ls(slaveNode.getPathFolder());
                for (Object obj : vector) {
                    SFTPv3DirectoryEntry entry = (SFTPv3DirectoryEntry) obj;
                    String filePath = slaveNode.getPathFolder() + "/" + entry.filename;
                    Util.printlnTime("Remove " + filePath);
                    if (!entry.filename.equals(".") && !entry.filename.equals(".."))
                        client.rm(filePath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Util.printerrTime("remove失败 " + e.getMessage());
        } finally {
            connection.close();
        }
    }

    public List<SFTPv3DirectoryEntry> lsBySlave(SlaveNode slaveNode) {
        connection = new Connection(slaveNode.getSlaveIP(), Constants.defaultPort);

        List<SFTPv3DirectoryEntry> directoryEntries = new ArrayList<>();
        try {
            connection.connect();
            boolean isAuthenticated = authWithSlave(slaveNode);
            if (isAuthenticated) {
                SFTPv3Client client = new SFTPv3Client(connection);
                Vector vector = client.ls(slaveNode.getPathFolder());
                for (Object obj : vector) {
                    SFTPv3DirectoryEntry entry = (SFTPv3DirectoryEntry) obj;
                    directoryEntries.add(entry);
                    System.out.println(entry.filename);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Util.printerrTime("ls失败 " + e.getMessage());
            System.out.println(slaveNode.getPathFolder());
        } finally {
            connection.close();
        }

        return directoryEntries;
    }

    public boolean renameBlock(String src, String des) {
        File file = new File(localTempDir + "\\" + src);
        return file.renameTo(new File(localTempDir + "\\" + des));
    }

    public void clearLocalDirectory() {
        try {
            FileUtils.deleteDirectory(new File(localTempDir));
            new File(localTempDir).mkdir();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initSlaveDir(SlaveNode slaveNode) {
        connection = new Connection(slaveNode.getSlaveIP(), Constants.defaultPort);

        try {
            connection.connect();
            boolean isAuthenticated = authWithSlave(slaveNode);
            if (isAuthenticated) {
                SFTPv3Client client = new SFTPv3Client(connection);
                client.mkdir(slaveNode.getPathFolder(), 755);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /***
     * 返回Slave node的剩余空间
     * @param slaveNode
     * @return
     */
    public int getSlaveAvailableSpace(SlaveNode slaveNode) {
        int space = -1;
        final SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier((s, i, publicKey) -> true);
        try {

            ssh.connect(slaveNode.getSlaveIP());
            ssh.authPassword(slaveNode.getUsername(), slaveNode.getPassword());
            try (Session session = ssh.startSession()) {
                final Session.Command cmd = session.exec("df " + slaveNode.getPathFolder());
                Scanner scanner = new Scanner(IOUtils.readFully(cmd.getInputStream()).toString());
                scanner.nextLine();
                String line = scanner.nextLine();
                Matcher matcher = Pattern.compile("\\d+").matcher(line);
                int cnt = 0;
                while (matcher.find()) {
                    cnt++;
                    if (cnt == 3) {
                        space = Integer.parseInt(matcher.group());
                        Util.printlnTime(slaveNode + " space " + space);
                    }
                }
                cmd.join(5, TimeUnit.SECONDS);
//                System.out.println("\n** exit status: " + cmd.getExitStatus());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ssh.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return space;
    }
}
