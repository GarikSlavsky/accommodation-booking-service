package accommodation.booking.service.repository;

import accommodation.booking.service.model.Accommodation;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    @EntityGraph(attributePaths = "amenities")
    Optional<Accommodation> findById(Long accommodationId);

    @EntityGraph(attributePaths = "amenities")
    Page<Accommodation> findAll(Pageable pageable);
}
