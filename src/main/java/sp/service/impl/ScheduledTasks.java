package sp.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp.model.AppFile;
import sp.repository.FileRepository;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ScheduledTasks {

    private final FileRepository fileRepository;
    private final FileFolderStorageServiceImpl fileFolderStorageService;

    @Transactional
    @Scheduled(fixedRate = 1000*60)
    public void reportCurrentTime() {

        List<AppFile> appFiles = fileRepository.deleteAllByExpiredAtBefore(Instant.now());

        if (appFiles.size() != 0) {
            for (AppFile appFile: appFiles) {
                log.info("Username {} с файл path-ом {} был удален", appFile.getUsername(), appFile.getFilePath());
                fileFolderStorageService.delete(appFile.getFilePath());
            }
        }
    }
}
