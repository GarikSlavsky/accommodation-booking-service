package mate.academy.accommodationbookingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.accommodationbookingservice.dto.accommodation.AccommodationRequestDto;
import mate.academy.accommodationbookingservice.dto.accommodation.AccommodationResponseDto;
import mate.academy.accommodationbookingservice.service.accommodation.AccommodationService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Accommodation management", description = "Endpoints for managing accommodations.")
@RestController
@RequestMapping("/accommodations")
@RequiredArgsConstructor
public class AccommodationController {
    private final AccommodationService accommodationService;

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Permits the addition of new accommodations.")
    public AccommodationResponseDto addAccommodation(
            @RequestBody @Valid AccommodationRequestDto requestDto) {

        return accommodationService.addAccommodation(requestDto);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieves detailed information about a specific accommodation.")
    public AccommodationResponseDto getAccommodationById(@PathVariable Long id) {
        return accommodationService.getAccommodationById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Provides a list of available accommodations.")
    public List<AccommodationResponseDto> getAllAccommodations(Pageable pageable) {
        return accommodationService.getAllAccommodations(pageable);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Updates all details of a specific accommodation.")
    public AccommodationResponseDto updateAccommodation(
            @PathVariable Long id,
            @RequestBody @Valid AccommodationRequestDto requestDto) {

        return accommodationService.updateAccommodation(id, requestDto);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Enables the removal of accommodations by marking them as deleted.")
    public void deleteAccommodation(@PathVariable Long id) {
        accommodationService.deleteAccommodation(id);
    }
}
