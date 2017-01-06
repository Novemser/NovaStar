package nsdfs;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import service.SSHService;

/**
 * Project: HadoopDFS
 * Package: nsdfs
 * Author:  Novemser
 * 2017/1/4
 */
public class ResourceManagerTest {
    @Autowired
    private ResourceManager manager;

    @Test
    public void migrantData() throws Exception {

        manager.migrantData(null);
    }

    @Test
    public void rename() throws Exception {
        SSHService SSHService = new SSHService();
        Assert.assertTrue( SSHService.renameBlock("pacc.txt", "pacc.txt"));
    }

    @Test
    public void testDelete() throws Exception {
        SSHService SSHService = new SSHService();
        SSHService.clearLocalDirectory();
    }
}