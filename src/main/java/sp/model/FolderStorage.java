package sp.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

@Data
@ConfigurationProperties(prefix = "folder.storage")
public class FolderStorage {

    private String path;

}
