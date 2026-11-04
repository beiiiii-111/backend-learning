package top.yixz.file.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.yixz.file.config.OssConfig;
import top.yixz.file.service.OssService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class OssServiceImpl implements OssService {

    @Resource
    private OssConfig ossConfig;

    private static final Map<String, String> CONTENT_TYPES = Map.of(
            ".jpg", "image/jpeg",
            ".jpeg", "image/jpeg",
            ".png", "image/png",
            ".gif", "image/gif",
            ".webp", "image/webp"
    );

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("上传失败：文件为空");
            return null;
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            log.warn("上传失败：文件名缺少后缀，originalFilename={}", originalFilename);
            return null;
        }

        // 新文件名 = UUID + 原始后缀，防止重名覆盖
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + suffix;
        String objectName = ossConfig.getDir() + newFileName;

        OSS ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(), ossConfig.getAccessKey(), ossConfig.getSecretKey());
        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType(CONTENT_TYPES.getOrDefault(suffix.toLowerCase(), "application/octet-stream"));
            ossClient.putObject(ossConfig.getBucket(), objectName, inputStream, meta);
        } catch (IOException e) {
            log.error("读取上传文件流失败", e);
            return null;
        } finally {
            ossClient.shutdown();
        }

        String url = "https://" + ossConfig.getBucket() + "." + ossConfig.getEndpoint() + "/" + objectName;
        log.info("文件上传成功：{}", url);
        return url;
    }
}
