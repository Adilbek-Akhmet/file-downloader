package sp.exception;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@ControllerAdvice
public class FileSizeException {

    @ExceptionHandler({MultipartException.class, FileSizeLimitExceededException.class, IllegalStateException.class})
    public String handleMultipartException(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "Слишком большой размер файла");
        return "redirect:/upload";
    }
}
