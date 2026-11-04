package top.yixz.file.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.yixz.file.service.OssService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/oss")
@RequiredArgsConstructor
public class OssController {

    private final OssService ossService;

    /** 单图上传，Apifox 用 form-data，key 为 file */
    @PostMapping("/upload")
    public String upload(MultipartFile file) {
        String url = ossService.upload(file);
        return url != null ? url : "上传失败";
    }

    /** 多图上传，form-data 里 key 为 files，同时选多张 */
    @PostMapping("/upload/batch")
    public List<String> uploadBatch(MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = ossService.upload(file);
            if (url != null) {
                urls.add(url);
            }
        }
        return urls;
    }
}
