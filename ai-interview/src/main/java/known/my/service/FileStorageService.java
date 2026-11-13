package known.my.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * 保存用户上传的原始文件
     *
     * @return 文件相对于根目录的存储路径
     */
    String save(MultipartFile file, Long userId, String fileId);

    /**
     * 根据相对路径读取文件
     */
    Resource load(String storagePath);

    /**
     * 根据相对路径删除文件
     */
    void delete(String storagePath);
}