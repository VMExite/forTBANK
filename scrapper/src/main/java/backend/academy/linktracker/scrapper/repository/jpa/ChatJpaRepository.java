package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.model.entity.ChatEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
public interface ChatJpaRepository extends JpaRepository<ChatEntity, Long> {
    @EntityGraph(attributePaths = {"links", "links.tags"})
    Optional<ChatEntity> findWithGraphByChatId(Long chatId);

    @Query("""
        select c.chatId
        from ChatEntity c
        join c.links l
        where l.lastUpdate = :linkId
    """)
    List<Long> findChatIdsByLinkId(@Param("linkId") Long linkId);
}
