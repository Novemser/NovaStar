package service;

import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Project: HadoopDFS
 * Package: service
 * Author:  Novemser
 * 2016/12/29
 */

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({"file:src/main/webapp/WEB-INF/master-dispatcher.xml"})
public class MasterServiceTest {
    @Autowired
    private MasterService masterService;

    @Test
    public void testRequestForUpload1() throws Exception {
        JSONObject object = new JSONObject();
        object.put("name", "test.txt");
        object.put("fileSize", 2023);
        object.put("blockSize", 64 * 1024 * 1024);
        object.put("blockNum", 1);
        System.out.println("CreationStart");
        JSONObject res = masterService.requestForUpload(object);
        System.out.println("CreationFinished");

        System.out.println(res.toJSONString());
    }

    @Test
    public void testRequestForUpload2() throws Exception {
        JSONObject object = new JSONObject();
        object.put("name", "test2.dat");
        object.put("fileSize", 20333333);
        object.put("blockSize", 64 * 1024 * 1024);
        object.put("blockNum", 2);
        System.out.println("CreationStart");
        JSONObject res = masterService.requestForUpload(object);
        System.out.println("CreationFinished");


        System.out.println(res.toJSONString());
    }

}