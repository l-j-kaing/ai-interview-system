package known.my.common.utils;

import known.my.common.properties.FileStorageProperties;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.nio.file.Paths;
@RequiredArgsConstructor
public class FileStorageUtil {
    private final FileStorageProperties fileStorageProperties;
    public Path getAbsolutePath(String relativePath) {
        return Paths.get(fileStorageProperties.getLocalRoot())
                .resolve(relativePath)
                .normalize();
    }
}
