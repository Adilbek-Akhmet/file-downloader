package sp.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sp.dto.DownloadPrefix;
import sp.dto.FileRequest;

import sp.dto.FileResponse;
import sp.model.AppFile;

import sp.model.FolderStorage;
import sp.model.Role;
import sp.service.impl.FileServiceImpl;

import java.io.*;
import java.nio.file.Path;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


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
    public String save(@RequestParam("multipart") MultipartFile file, @ModelAttribute("file") FileRequest fileRequest,
                       RedirectAttributes redirectAttributes)
            throws IOException, ParseException {

        if (fileRequest.getLogin().length() < 3) {
            redirectAttributes.addFlashAttribute("message", "Имя пользователя должен состоять из три или более символов");
            return "redirect:/upload";
        }

        if (fileRequest.getPassword().length() < 3) {
            redirectAttributes.addFlashAttribute("message", "Пароль должен состоять из три или более символов");
            return "redirect:/upload";
        }

        if (fileService.findOneByLogin(fileRequest.getLogin()).isPresent()) {
            redirectAttributes.addFlashAttribute("message", "Введите другое имя пользователя!");
            return "redirect:/upload";
        }

        if (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(fileRequest.getTime()).before(java.util.Date.from(Instant.now()))) {
            redirectAttributes.addFlashAttribute("message", "Неверная дата!");
            return "redirect:/upload";
        }


        fileRequest.setFile(file);
        FileResponse fileResponse = fileService.saveUser(fileRequest);
        fileResponse.setPassword(fileRequest.getPassword());
        redirectAttributes.addFlashAttribute("response", fileResponse);
        redirectAttributes.addFlashAttribute("file", new FileRequest());
        return "redirect:/upload";
    }

    @GetMapping("list")
    public String listAllFiles(Model model, @AuthenticationPrincipal UserDetails userDetail) {
        boolean isAdmin = userDetail.getAuthorities().contains(new SimpleGrantedAuthority(Role.ADMIN.name()));
        model.addAttribute("isAdmin", isAdmin);

        List<AppFile> reverseSorted;
        if (!isAdmin) {
            reverseSorted = fileService.findAllByUser(userDetail.getUsername()).stream()
                    .sorted(Comparator.comparing(AppFile::getId).reversed())
                    .collect(Collectors.toList());
        } else {
            reverseSorted = fileService.findAll().stream()
                    .sorted(Comparator.comparing(AppFile::getId).reversed())
                    .collect(Collectors.toList());
        }

        model.addAttribute("downloadPrefix", downloadPrefix);
        model.addAttribute("list", reverseSorted);
        return "listOfFiles";
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

        log.info("FileUser {} скачал файл с path-ом {}", appFile.getUsername(), filePathFull);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath.getFileName() + "\"")
                .body(resource);
    }
}
