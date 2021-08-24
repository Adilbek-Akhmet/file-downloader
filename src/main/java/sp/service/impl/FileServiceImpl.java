package sp.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.*;

@Slf4j
@Service
public class FileServiceImpl {

    private final FileRepository fileRepository;
    private final FileFolderStorageServiceImpl fileFolderStorageService;
    private final FolderStorage folderStorage;
    private final DownloadPrefix downloadPrefix;
    private final PasswordEncoder passwordEncoder;

    public FileServiceImpl(FileRepository fileRepository, FileFolderStorageServiceImpl fileFolderStorageService, FolderStorage folderStorage,
                           DownloadPrefix downloadPrefix, @Qualifier("v1") PasswordEncoder passwordEncoder) {
        this.fileRepository = fileRepository;
        this.fileFolderStorageService = fileFolderStorageService;
        this.folderStorage = folderStorage;
        this.downloadPrefix = downloadPrefix;
        this.passwordEncoder = passwordEncoder;
    }

    public FileResponse saveUser(FileRequest fileRequest) throws IOException, ParseException {

        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Date date = simpleDateFormat.parse(fileRequest.getTime());

        String url = UUID.randomUUID().toString();

        AppFile appFile = AppFile.builder()
                .fileName(fileRequest.getFile().getOriginalFilename())
                .expiredAt(date)
                .url(url)
                .username(fileRequest.getLogin())
                .password(passwordEncoder.encode(fileRequest.getPassword()))
                .user(user)
                .build();

        String filePath = folderStorage.getPath()
                .concat(File.separatorChar + fileRequest.getLogin() + File.separatorChar + appFile.getFileName());

        if (!fileFolderStorageService.exists(filePath)) {
            fileFolderStorageService.copyToFolderStorage(fileRequest.getFile(), appFile.getUsername());
        }

        fileRepository.save(appFile);
        log.info("Ползователь {} дал  доступ к FileUSER {} к файлу {}", user, appFile.getUsername(), appFile.getFileName());

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

    public List<AppFile> findAllByUser(String user) {
        return fileRepository.findAllByUser(user);
    }
}
