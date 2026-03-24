package backend.academy.linktracker.scrapper.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long linkId;

    @URL
    @Column(nullable = false)
    private String url;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "link_tag",
            joinColumns = @JoinColumn(name = "link_id", referencedColumnName = "linkId"),
            inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "tagId"))
    private List<Tag> tags;

    @Column(nullable = false)
    private final OffsetDateTime lastUpdate = OffsetDateTime.now();

    @ManyToMany
    @JoinTable(
            name = "chat_link",
            joinColumns = @JoinColumn(name = "link_id", referencedColumnName = "linkId"),
            inverseJoinColumns = @JoinColumn(name = "chat_id", referencedColumnName = "chatId"))
    private List<Chat> chats = new ArrayList<>();
}
