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

import java.io.FileNotFoundException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class FileServiceImpl {

    private final PasswordEncoder passwordEncoder;
    private final FileRepository fileRepository;
    private final FileFolderStorageServiceImpl fileFolderStorageService;
    private final FolderStorage folderStorage;
    private final DownloadPrefix downloadPrefix;

    public FileResponse saveUser(FileRequest fileRequest) throws FileNotFoundException, ParseException {

        Date format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(fileRequest.getTime());
        Instant instant = format.toInstant();



        String url = UUID.randomUUID().toString();

        AppFile appFile = new AppFile(folderStorage.getPath().concat(Objects.requireNonNull(fileRequest.getFile().getOriginalFilename())),
                instant, url, fileRequest.getLogin(), passwordEncoder.encode(fileRequest.getPassword()));

        if (!fileFolderStorageService.exists(appFile.getFilePath())) {
            log.info("Файл не существует на папке");
            fileFolderStorageService.copyToFolderStorage(fileRequest.getFile());
        }


        fileRepository.save(appFile);
        log.info("{} дан доступ к файлу с path-ом {}", appFile.getUsername(), appFile.getFilePath());

        return new FileResponse(fileRequest.getLogin(), fileRequest.getPassword(),
                downloadPrefix.getDomain() + "/" + downloadPrefix.getPrefix() + "/" + url);
    }

    public Optional<AppFile> findOneByLoginAndUrl(String login, String url) {
        return fileRepository.findByUsernameAndUrl(login, url);
    }

    public Optional<AppFile> findOneByLogin(String login) {
        return fileRepository.findByUsername(login);
    }
}
