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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "link")
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "link_id")
    private Long linkId;

    @URL
    @Column(name = "url", length = 2048, nullable = false, unique = true)
    @EqualsAndHashCode.Include
    @NotNull
    private String url;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "link_tag",
            joinColumns = @JoinColumn(name = "link_id", referencedColumnName = "link_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "tag_id"))
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @Column(name = "last_update", nullable = false)
    @Builder.Default
    private OffsetDateTime lastUpdate = OffsetDateTime.now();

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "chat_link",
            joinColumns = @JoinColumn(name = "link_id", referencedColumnName = "link_id"),
            inverseJoinColumns = @JoinColumn(name = "chat_id", referencedColumnName = "chat_id"))
    @Builder.Default
    private Set<Chat> chats = new HashSet<>();
}
