package sp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sp.model.AppFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<AppFile, Long> {

    Optional<AppFile> findByUsername(String username);

    Optional<AppFile> findByUsernameAndUrl(String username, String url);

    List<AppFile> deleteAllByExpiredAtBefore(Instant time);
}
