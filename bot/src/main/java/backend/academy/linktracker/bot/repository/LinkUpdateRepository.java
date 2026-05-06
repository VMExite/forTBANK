package backend.academy.linktracker.bot.repository;

import backend.academy.linktracker.bot.model.LinkUpdate;

public interface LinkUpdateRepository {
    void save(LinkUpdate entity);

    boolean existsByEventId(Long eventId);
}
