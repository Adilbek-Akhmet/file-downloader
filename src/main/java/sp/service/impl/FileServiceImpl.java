package sp.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sp.dto.DownloadPrefix;
import sp.dto.FileRequest;
import sp.dto.FileResponse;
import sp.model.AppFile;
import sp.model.FolderStorage;
import sp.repository.FileRepository;


import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class FileServiceImpl {

    private final FileRepository fileRepository;
    private final FileFolderStorageServiceImpl fileFolderStorageService;
    private final FolderStorage folderStorage;
    private final DownloadPrefix downloadPrefix;

    public FileResponse saveUser(FileRequest fileRequest) throws IOException, ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date format = simpleDateFormat.parse(fileRequest.getTime());

        Instant instant = format.toInstant();

        String url = UUID.randomUUID().toString();

        AppFile appFile = AppFile.builder()
                .fileName(fileRequest.getFile().getOriginalFilename())
                .expiredAt(instant)
                .url(url)
                .username(fileRequest.getLogin())
                .password(fileRequest.getPassword())
                .build();

        String filePath = folderStorage.getPath()
                .concat(File.separatorChar + fileRequest.getLogin() + File.separatorChar + appFile.getFileName());

        if (!fileFolderStorageService.exists(filePath)) {
            log.info("Папка с именем {} не существует на хранилище", appFile.getUsername());
            fileFolderStorageService.copyToFolderStorage(fileRequest.getFile(), appFile.getUsername());
        }

        fileRepository.save(appFile);
        log.info("{} дан доступ к файлу с path-ом {}", appFile.getUsername(), filePath);

        return new FileResponse(fileRequest.getLogin(), fileRequest.getPassword(),
                downloadPrefix.getDomain() + "/" + downloadPrefix.getPrefix() + "/" + appFile.getUrl());
    }

    public Optional<AppFile> findOneByLoginAndUrl(String login, String url) {
        return fileRepository.findByUsernameAndUrl(login, url);
    }

    public Optional<AppFile> findOneByLogin(String login) {
        return fileRepository.findByUsername(login);
    }

    public List<AppFile> findAll() {
        return fileRepository.findAll();
    }
}
