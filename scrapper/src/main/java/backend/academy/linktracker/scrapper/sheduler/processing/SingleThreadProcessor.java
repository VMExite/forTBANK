package backend.academy.linktracker.scrapper.sheduler.processing;

import java.util.Collection;

public interface SingleThreadProcessor<T> {
    void process(Collection<T> elements);
}
