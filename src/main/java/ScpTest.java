import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3DirectoryEntry;
import nsdfs.slave.SlaveNode;
import service.ScpService;

import java.io.IOException;
import java.util.Vector;

/**
 * Project: HadoopDFS
 * Package: PACKAGE_NAME
 * Author:  Novemser
 * 2016/12/28
 */
public class ScpTest {
    private static String HOST = "192.168.52.132";
    private static int PORT = 22;
    private static String USER = "nova";//登录用户名
    private static String PASSWORD = "a19951106";//生成私钥的密码和登录密码，这两个共用这个密码
    private static Connection connection = new Connection(HOST, PORT);
//    private static String PRIVATEKEY = "C:\\Users\\ubuntu\\.ssh\\id_rsa";// 本机的私钥文件
    private static boolean usePassword = false;// 使用用户名和密码来进行登录验证。如果为true则通过用户名和密码登录，false则使用rsa免密码登录

    /**
     * ssh用户登录验证，使用用户名和密码来认证
     *
     * @param user
     * @param password
     * @return
     */
    public static boolean isAuthedWithPassword(String user, String password) {
        try {
            return connection.authenticateWithPassword(user, password);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isAuth() {
        return isAuthedWithPassword(USER, PASSWORD);
    }

    public static void getFile(String remoteFile, String path) {
        try {
            connection.connect();
            boolean isAuthed = isAuth();
            if (isAuthed) {
                System.out.println("认证成功!");
                SCPClient scpClient = connection.createSCPClient();
                scpClient.get(remoteFile, path);
            } else {
                System.out.println("认证失败!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

    public static void putFile(String localFile, String remoteTargetDirectory) {
        try {
            connection.connect();

            boolean isAuthed = isAuth();
            if (isAuthed) {
                SFTPv3Client client = new SFTPv3Client(connection);
                Vector vector = client.ls(remoteTargetDirectory);
                for (Object obj : vector) {
                    SFTPv3DirectoryEntry entry = (SFTPv3DirectoryEntry) obj;
                    System.out.println(entry.filename);
                    System.out.println(entry.longEntry);
                }
//                client.mkdir(remoteTargetDirectory, 755);
                SCPClient scpClient = connection.createSCPClient();
                scpClient.put(localFile, remoteTargetDirectory);
            } else {
                System.out.println("认证失败!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            connection.close();
        }
    }

    public static void main(String[] args) {
        ScpService scpService = new ScpService();
        SlaveNode node = new SlaveNode("Slave" + 1, HOST,
                "nova",
                "a19951106",
                "/home/nova/NovaStar");

        scpService.getFile(node, "/home/nova/hadoop-2.2.0/NOTICE.txt");
//        try {
//            // getFile("/home/users/ubuntu/error.txt", "c://");
//            putFile("G:\\pac.txt", "/home/nova/NovaStar");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
