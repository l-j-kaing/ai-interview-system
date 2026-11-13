package known.my.service.impl;

import known.my.common.exception.BaseException;
import known.my.common.properties.FileStorageProperties;
import known.my.common.result.ResultCode;
import known.my.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("pdf");

    private final FileStorageProperties fileStorageProperties;
    /**
     * 保存文件
     */
    @Override
    public String save(MultipartFile file, Long userId, String fileId) {

        if (file == null || file.isEmpty()) {
            throw new BaseException(ResultCode.NOT_FOUND,"上传文件不能为空");
        }

        if (userId == null || fileId == null || fileId.isBlank()) {
            throw new BaseException(ResultCode.FORBIDDEN,"存储参数不能为空");
        }

        // 获取文件扩展名
        String extension = StringUtils
                .getFilenameExtension(file.getOriginalFilename());

        if (extension == null || !ALLOWED_EXTENSIONS.contains(
                extension.toLowerCase())) {
            throw new BaseException(ResultCode.FILE_TYPE_NOT_SUPPORTED,"不支持的文件类型");
        }

        // 使用用户ID分目录，使用后端生成的fileId作为文件名
        String relativePath = userId + "/" + fileId + "."
                + extension.toLowerCase();

        Path rootPath = Paths.get(fileStorageProperties.getLocalRoot()).toAbsolutePath().normalize();
        Path targetPath = rootPath.resolve(relativePath).normalize();

        // 防止路径越界
        if (!targetPath.startsWith(rootPath)) {
            throw new BaseException(ResultCode.FORBIDDEN,"非法文件存储路径");
        }

        try {
            // 创建用户目录
            Files.createDirectories(targetPath.getParent());

            // 保存文件
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(
                        inputStream,
                        targetPath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            log.info("文件保存成功，storagePath={}", relativePath);

            return relativePath;

        } catch (IOException e) {
            try {
                Files.deleteIfExists(targetPath);
            } catch (IOException cleanupException) {
                e.addSuppressed(cleanupException);
                log.error("文件保存失败后清理残留文件失败，fileId={}", fileId, cleanupException);
            }
            log.error("文件保存失败，fileId={}", fileId, e);
            throw new BaseException(ResultCode.INTERNAL_ERROR,"文件保存失败", e);
        }
    }

    /**
     * 加载文件
     */
    @Override
    public Resource load(String storagePath) {

        Path targetPath = resolvePath(storagePath);

        if (!Files.exists(targetPath) || !Files.isRegularFile(targetPath)) {
            throw new BaseException(ResultCode.NOT_FOUND,"文件不存在");
        }

        return new FileSystemResource(targetPath);
    }
    /**
     * 删除文件
     */

    @Override
    public void delete(String storagePath) {

        Path targetPath = resolvePath(storagePath);

        try {
            Files.deleteIfExists(targetPath);
            log.info("文件删除完成，storagePath={}", storagePath);

        } catch (IOException e) {
            log.error("文件删除失败，storagePath={}", storagePath, e);
            throw new BaseException(ResultCode.INTERNAL_ERROR,"文件删除失败", e);
        }
    }

    /**
     * 将数据库中的相对路径转换成安全的绝对路径
     */
    private Path resolvePath(String storagePath) {

        if (storagePath == null || storagePath.isBlank()) {
            throw new BaseException(ResultCode.INTERNAL_ERROR,"文件存储路径不能为空");
        }

        Path rootPath = Paths.get(fileStorageProperties.getLocalRoot()).toAbsolutePath().normalize();
        Path relativePath = Paths.get(storagePath);

        if (relativePath.isAbsolute()) {
            throw new BaseException(ResultCode.INTERNAL_ERROR,"存储路径必须是相对路径");
        }

        Path targetPath = rootPath.resolve(relativePath).normalize();

        if (!targetPath.startsWith(rootPath)) {
            throw new BaseException(ResultCode.INTERNAL_ERROR,"非法文件存储路径");
        }

        return targetPath;
    }
}