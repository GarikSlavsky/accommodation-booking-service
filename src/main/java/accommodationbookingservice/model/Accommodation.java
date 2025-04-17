package accommodationbookingservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "accommodations")
@SQLDelete(sql = "UPDATE accommodations SET is_deleted = true WHERE id = ?")
@SQLRestriction(value = "is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class Accommodation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AccommodationType type;

    @Column(nullable = false)
    private String location;

    private String size;

    @ElementCollection
    private List<String> amenities;

    @Column(nullable = false)
    private BigDecimal dailyRate;

    private Integer availability;

    @Column(nullable = false)
    private boolean isDeleted = false;

    public enum AccommodationType {
        HOUSE,
        APARTMENT,
        CONDO,
        VACATION_HOME
    }

    public Accommodation(Long id) {
        this.id = id;
    }
}
