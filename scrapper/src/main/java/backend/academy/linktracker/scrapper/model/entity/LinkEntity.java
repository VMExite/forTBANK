package backend.academy.linktracker.scrapper.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "link")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long linkId;

    @EqualsAndHashCode.Include
    @Column(name = "url", nullable = false, unique = true)
    private String url;

    @Column(name = "last_update", nullable = false)
    private OffsetDateTime lastUpdate;

    @ManyToMany(mappedBy = "links", fetch = FetchType.LAZY)
    private Set<ChatEntity> chats = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "link_tag",
        joinColumns = @JoinColumn(name = "link_id", referencedColumnName = "link_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "tag_id")
    )
    private Set<TagEntity> tags = new HashSet<>();
}
