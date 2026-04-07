package backend.academy.linktracker.scrapper.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "chat")
public class Chat {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "chat_id")
    private Long chatId;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "chat_link",
            joinColumns = @JoinColumn(name = "chat_id", referencedColumnName = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "link_id", referencedColumnName = "link_id"))
    @Builder.Default
    private Set<Link> links = new HashSet<>();
}
