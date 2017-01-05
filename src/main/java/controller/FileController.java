package controller;

import com.alibaba.fastjson.JSONObject;
import nsdfs.FileMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.MasterService;

import java.util.List;

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
        return null;
    }

    @GetMapping("/files")
    public List<FileMetadata> getFileNameSpace() {
        return service.getFileNamespace();
    }

    @PostMapping("/upload")
    public JSONObject requestForUpload(@RequestBody JSONObject requestBody) {
        return service.requestForUpload(requestBody);
    }

    @PostMapping("/delete")
    public JSONObject requestForDelete(@RequestBody JSONObject requestBody) {
        return null;
    }

    @PostMapping("/rename")
    public JSONObject rename(@RequestBody JSONObject requestBody) {
        return null;
    }

    @GetMapping("/verify/{fileId}")
    public JSONObject verifyFile(@PathVariable String fileId) {
        return service.verifyFileExists(fileId);
    }

    @GetMapping("/slave/status")
    public JSONObject showSlaveStatus() {
        return service.showSlaveStatus();
    }


}
