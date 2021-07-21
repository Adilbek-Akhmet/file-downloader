package sp.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import sp.dto.FileRequest;

import sp.dto.FileResponse;
import sp.model.AppFile;

import sp.service.impl.FileServiceImpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

@Slf4j
@Controller
@AllArgsConstructor
public class FileController {

    private final FileServiceImpl fileService;

    @GetMapping("upload")
    public String saveFile(Model model) {
        model.addAttribute("file", new FileRequest());
        return "upload";
    }

    @PostMapping("upload")
    public String save(@RequestParam("multipart") MultipartFile file, @ModelAttribute("file") FileRequest fileRequest, Model model)
            throws FileNotFoundException, ParseException {

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

    @GetMapping( "/${download.prefix}/{url}")
    public Object downloadFile(@PathVariable String url) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        if (fileService.findOneByLoginAndUrl(name, url).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        AppFile appFile = fileService.findOneByLoginAndUrl(name, url).get();

        String contentType;
        try {
            contentType = Files.probeContentType(Path.of(appFile.getFilePath()));
        } catch (IOException e) {
            log.warn("Файл недоступно c path-ом {} для пользователья {}", appFile.getFilePath(), appFile.getUsername());
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
        Path filePath = Path.of(appFile.getFilePath());

        byte[] data;
        try {
            data = Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.warn("Файл недоступно c path-ом {} для пользователья {}", appFile.getFilePath(), appFile.getUsername());
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }

        log.info("{} скачал файл с path-ом {}", appFile.getUsername(), appFile.getFilePath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath.getFileName() + "\"")
                .body(new ByteArrayResource(data));
    }
}
