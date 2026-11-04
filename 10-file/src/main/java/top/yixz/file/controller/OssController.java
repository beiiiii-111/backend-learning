package top.yixz.file.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.yixz.file.service.OssService;

@RestController
@RequestMapping("/oss")
@RequiredArgsConstructor
public class OssController {

    private final OssService ossService;

    /** 文件上传，Apifox 用 form-data，key 为 file */
    @PostMapping("/upload")
    public String upload(MultipartFile file) {
        String url = ossService.upload(file);
        return url != null ? url : "上传失败";
    }
}
