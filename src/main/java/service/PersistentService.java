package service;

import nsdfs.FileMetadata;
import nsdfs.ResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import util.Util;

import java.io.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Project: HadoopDFS
 * Package: service
 * Author:  Novemser
 * 2017/1/2
 */
@Service
public class PersistentService {

    @Value("${persistent.metaDataPath}")
    private String metaDataPath;

    private final ResourceManager manager;

    @Autowired
    public PersistentService(ResourceManager manager) {
        this.manager = manager;

        // 延迟一段时间执行 确保配置文件被加载
        Timer timer = new Timer();
        TimerTask timerTask =new TimerTask() {
            @Override
            public void run() {
                manager.initSlaveList();
                initPersist();
            }
        };
        timer.schedule(timerTask, 5000);
    }


    @SuppressWarnings("unchecked")
    private void initPersist() {
        Object res = readObjectFromFile(metaDataPath);
        if (null != res)
            manager.setMetadataList((List<FileMetadata>) res);
        else {
            System.out.println("未找到NameSpace文件, 当上传文件后会自动生成...");
        }
    }

    public static void persistMetadata(ResourceManager manager, String metaDataPath) {
        Util.printlnTime(" 保存NameSpace文件到 " + metaDataPath);
        writeObjectToFile(manager.getMetadataList(), metaDataPath);
    }

    /**
     * Write object to file.
     *
     * @param obj      the obj
     * @param fileName the file name
     */
    public static void writeObjectToFile(Object obj, String fileName) {
        File file = new File(fileName);
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(obj);
            objOut.flush();
            objOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read object from file object.
     *
     * @param fileName the file name
     * @return the object
     */
    public static Object readObjectFromFile(String fileName) {
        Object temp = null;
        File file = new File(fileName);
        FileInputStream in;
        try {
            in = new FileInputStream(file);
            ObjectInputStream objIn = new ObjectInputStream(in);
            temp = objIn.readObject();
            objIn.close();
        } catch (IOException | ClassNotFoundException ignored) {

        }
        return temp;
    }
}
