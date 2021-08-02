package sp.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import sp.dto.DownloadPrefix;
import sp.dto.FileRequest;

import sp.dto.FileResponse;
import sp.model.AppFile;

import sp.model.FolderStorage;
import sp.service.impl.FileServiceImpl;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Controller
@AllArgsConstructor
public class FileController {

    private final FileServiceImpl fileService;
    private final FolderStorage folderStorage;
    private final DownloadPrefix downloadPrefix;

    @GetMapping("upload")
    public String saveFile(Model model) {
        model.addAttribute("file", new FileRequest());
        return "upload";
    }

    @PostMapping("upload")
    public String save(@RequestParam("multipart") MultipartFile file, @ModelAttribute("file") FileRequest fileRequest, Model model)
            throws IOException, ParseException {

        if (fileService.findOneByLogin(fileRequest.getLogin()).isPresent()) {
            model.addAttribute("message", "Введите другое имя пользователя!");
            return "upload";
        }

        if (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(fileRequest.getTime()).before(Date.valueOf(LocalDate.now()))) {
            model.addAttribute("message", "Неверная дата!");
            return "upload";
        }


        fileRequest.setFile(file);
        FileResponse fileResponse = fileService.saveUser(fileRequest);
        fileResponse.setPassword(fileRequest.getPassword());
        model.addAttribute("response", fileResponse);
        model.addAttribute("file", new FileRequest());
        return "upload";
    }

    @GetMapping("list")
    public String listAllUsers(Model model) {
        if (fileService.findAll().size() == 0) {
            model.addAttribute("message", "Введите другое имя пользователя!");
            return "listOfUsers";
        }
        List<AppFile> reverseSorted = fileService.findAll().stream()
                .sorted(Comparator.comparing(AppFile::getId).reversed())
                .collect(Collectors.toList());
        model.addAttribute("downloadPrefix", downloadPrefix);
        model.addAttribute("list", reverseSorted);
        return "listOfUsers";
    }


    @GetMapping( "/${download.prefix}/{url}")
    public Object downloadFile(@PathVariable String url) throws FileNotFoundException {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();


        if (fileService.findOneByLoginAndUrl(name, url).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        AppFile appFile = fileService.findOneByLoginAndUrl(name, url).get();
        String filePathFull = folderStorage.getPath()
                .concat(File.separatorChar + appFile.getUsername() + File.separatorChar + appFile.getFileName());


        Path filePath = Path.of(filePathFull);


        InputStreamResource resource = new InputStreamResource(new FileInputStream(filePathFull));

        log.info("{} скачал файл с path-ом {}", appFile.getUsername(), filePathFull);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath.getFileName() + "\"")
                .body(resource);
    }
}
