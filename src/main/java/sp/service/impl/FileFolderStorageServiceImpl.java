package sp.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sp.model.FolderStorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class FileFolderStorageServiceImpl {

    private final FolderStorage folderStorage;


    public void copyToFolderStorage(MultipartFile multipartFile) throws FileNotFoundException {

        Path path = Path.of(folderStorage.getPath());

        Path fileLocation = path.resolve(
                Paths.get(Objects.requireNonNull(multipartFile.getOriginalFilename())))
                .normalize().toAbsolutePath();

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, fileLocation,
                    StandardCopyOption.REPLACE_EXISTING);
            log.info("Файл {} загружен папку, расположение файла: {}", multipartFile.getOriginalFilename(), path);
        } catch (IOException e) {
            log.warn("Файл {} НЕ загружен папку", multipartFile.getOriginalFilename());
            throw new FileNotFoundException("Файл НЕ загружен папку");
        }
    }

    public boolean exists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public void delete(String filePath) {
        File file = new File(filePath);

        if (file.exists()) {
            file.delete();
        }
    }
}
