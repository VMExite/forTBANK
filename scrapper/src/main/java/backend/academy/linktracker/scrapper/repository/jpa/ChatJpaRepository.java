package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.model.entity.ChatEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
public interface ChatJpaRepository extends JpaRepository<ChatEntity, Long> {
    @EntityGraph(attributePaths = {"links", "links.tags"})
    Optional<ChatEntity> findWithGraphByChatId(Long chatId);
}
