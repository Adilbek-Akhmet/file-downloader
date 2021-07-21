package sp.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileRequest {

    private MultipartFile file;

    private String login;

    private String password;

    private String time;

}
