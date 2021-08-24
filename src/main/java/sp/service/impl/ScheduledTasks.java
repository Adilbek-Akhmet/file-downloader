package sp.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp.model.AppFile;
import sp.model.FolderStorage;
import sp.repository.FileRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ScheduledTasks {

    private final FileRepository fileRepository;
    private final FolderStorage folderStorage;
    private final FileFolderStorageServiceImpl fileFolderStorageService;

    @Async
    @Transactional
    @Scheduled(fixedRate = 1000*60)
    public void reportCurrentTime() throws IOException {

        List<AppFile> appFiles = fileRepository.findAllByExpiredAtBefore(java.util.Date.from(Instant.now()));

        if (appFiles.size() != 0) {
            for (AppFile appFile: appFiles) {
                String filePath = folderStorage.getPath()
                        .concat(File.separatorChar + appFile.getUsername() + File.separatorChar + appFile.getFileName());
                Path parentPath = Path.of(filePath).getParent();


                boolean isDeleted = fileFolderStorageService.delete(parentPath.toString());

                if (isDeleted) {
                    log.info("FileUSER {} с файл path-ом {} был удален", appFile.getUsername(), parentPath);
                    fileRepository.delete(appFile);
                }
            }
        }
    }
}
