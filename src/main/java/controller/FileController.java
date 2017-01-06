package controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.MasterService;

/**
 * Project: HadoopDFS
 * Package: controller
 * Author:  Novemser
 * 2016/12/29
 */
@RestController
@RequestMapping("/api")
public class FileController {

    private final MasterService service;

    @Autowired
    public FileController(MasterService service) {
        this.service = service;
    }

    @GetMapping("/file/{fileId}")
    public JSONObject getFile(@PathVariable String fileId) {
        return service.getFileById(fileId);
    }

    @GetMapping("/files")
    public Object getFileNameSpace() {
        return service.getFileNamespace();
    }

    @PostMapping("/upload")
    public JSONObject requestForUpload(@RequestBody JSONObject requestBody) {
        return service.requestForUpload(requestBody);
    }

    @PostMapping("/delete")
    public JSONObject requestForDelete(@RequestBody JSONObject requestBody) {
        return service.deleteFile(requestBody);
    }

    @PostMapping("/rename")
    public JSONObject rename(@RequestBody JSONObject requestBody) {
        return service.renameFile(requestBody);
    }

    @GetMapping("/verify/{fileId}")
    public JSONObject verifyFile(@PathVariable String fileId) {
        return service.verifyFileExists(fileId);
    }

    @GetMapping("/slave/status")
    public JSONObject showSlaveStatus() {
        return service.showSlaveStatus();
    }

    @GetMapping("/space")
    public JSONObject getSpace() {
        return service.calculateSpaceUsage();
    }

    @GetMapping("/format")
    public JSONObject formatDir() {
        return service.formatAllSlaves();
    }
}
