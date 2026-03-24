package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.model.Chat;

public interface ChatJdbcRepository extends JdbcRepository<Chat, Long> {
    boolean existsById(Long id);
}
