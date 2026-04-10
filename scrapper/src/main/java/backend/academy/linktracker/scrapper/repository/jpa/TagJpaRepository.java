package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.model.entity.TagEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
public interface TagJpaRepository extends JpaRepository<TagEntity, Long> { }
