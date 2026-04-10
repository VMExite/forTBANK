package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.model.entity.LinkEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
public interface LinkJpaRepository extends JpaRepository<LinkEntity, Long> {
    Optional<LinkEntity> findByUrl(String url);
    List<LinkEntity> findByUrlIn(Collection<String> urls);
}
