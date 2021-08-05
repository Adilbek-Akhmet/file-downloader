package sp.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "folder.storage")
public class FolderStorage {

    private String path;

}
