package mate.academy.accommodationbookingservice.repository;

import java.util.Optional;
import mate.academy.accommodationbookingservice.model.Accommodation;
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
