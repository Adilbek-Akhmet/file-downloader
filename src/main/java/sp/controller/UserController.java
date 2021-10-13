package sp.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sp.dto.UserDTO;
import sp.model.User;
import sp.model.Admin;
import sp.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Controller
public class UserController {

    private final Admin admin;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(Admin admin, UserRepository userRepository, @Qualifier("v2") PasswordEncoder passwordEncoder) {
        this.admin = admin;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/user/create")
    public String createGet(Model model) {
        model.addAttribute("user", new UserDTO());
        return "createUser";
    }

    @PostMapping("/user/create")
    public String createPost(@ModelAttribute("user") UserDTO userDTO, RedirectAttributes redirectAttributes) {

        if (userDTO.getUsername().length() < 3) {
            redirectAttributes.addFlashAttribute("message", "Имя пользователя должен состоять из три или более символов");
            return "redirect:/user/create";
        }

        if (userDTO.getPassword().length() < 3 || userDTO.getReTypePassword().length() < 3) {
            redirectAttributes.addFlashAttribute("message", "Пароль должен состоять из три или более символов");
            return "redirect:/user/create";
        }

        if (!userDTO.getPassword().equals(userDTO.getReTypePassword())) {
            redirectAttributes.addFlashAttribute("message", "Пароли не совпадают");
            return "redirect:/user/create";
        }

        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()
                || admin.getUsername().equals(userDTO.getUsername())) {
            redirectAttributes.addFlashAttribute("message", "Введите другое имя пользователя!");
            return "redirect:/user/create";
        }

        userRepository.save(new User(userDTO.getUsername(), passwordEncoder.encode(userDTO.getPassword())));

        redirectAttributes.addFlashAttribute("response", "Пользаватель успешно создан");
        return "redirect:/user/create";
    }

    @GetMapping("/user/list")
    public String listAllUsers(Model model) {
        List<User> list = new ArrayList<>(userRepository.findAll());
        model.addAttribute("list", list);
        model.addAttribute("userDto", new UserDTO());
        return "userList";
    }

    @PostMapping("/user/update")
    public String userProfileUpdatePost(@ModelAttribute("userDto") UserDTO userDto) {

        if (userDto.getUsername().length() < 3) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }

        if (userDto.getPassword().length() < 3 || userDto.getReTypePassword().length() < 3) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }

        if (!userDto.getPassword().equals(userDto.getReTypePassword())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByUsername(userDto.getUsername())
                .orElseThrow(() -> new IllegalStateException("пользователь не существует"));
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userRepository.save(user);
        return "redirect:/user/list";
    }

    @GetMapping("/user/profile/update")
    public String userProfileUpdate(Model model) {
        model.addAttribute("userDto", new UserDTO());
        return "changePassword";
    }

    @PostMapping("/user/profile/update")
    public String updateUser(@ModelAttribute("userDto") UserDTO userDto, RedirectAttributes redirectAttributes) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new IllegalStateException("пользователь не существует"));

        if (!passwordEncoder.matches(userDto.getCurrentPassword(), user.getPassword())) {
            redirectAttributes.addFlashAttribute("message", "Неверный текущий пароль");
            return "redirect:/user/profile/update";
        }

        if (userDto.getPassword().length() < 3 || userDto.getReTypePassword().length() < 3) {
            redirectAttributes.addFlashAttribute("message", "Пароль должен состоять из три или более символов");
            return "redirect:/user/profile/update";
        }

        if (!userDto.getPassword().equals(userDto.getReTypePassword())) {
            redirectAttributes.addFlashAttribute("message", "Пароли не совпадают");
            return "redirect:/user/profile/update";
        }


        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("response", "Пароль был успешно изменен");
        return "redirect:/user/profile/update";
    }


    

}
