package tqs.boundary.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for creating a new booking
 * Represents what the client SENDS when creating a booking
 */
public class BookingRequestDTO {

    @NotBlank(message = "Municipality is required")
    private String municipality;

    @NotNull(message = "Collection date is required")
    @Future(message = "Collection date must be in the future")
    private LocalDate collectionDate;

    @NotBlank(message = "Time slot is required")
    @Pattern(regexp = "morning|afternoon|evening", message = "Time slot must be: morning, afternoon, or evening")
    private String timeSlot;

    @NotEmpty(message = "At least one bulk item is required")
    @Valid
    private List<BulkItemDTO> items;

    // Constructors
    public BookingRequestDTO() {
    }

    public BookingRequestDTO(String municipality, LocalDate collectionDate, String timeSlot, List<BulkItemDTO> items) {
        this.municipality = municipality;
        this.collectionDate = collectionDate;
        this.timeSlot = timeSlot;
        this.items = items;
    }

    // Getters and Setters
    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public LocalDate getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(LocalDate collectionDate) {
        this.collectionDate = collectionDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public List<BulkItemDTO> getItems() {
        return items;
    }

    public void setItems(List<BulkItemDTO> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "BookingRequestDTO{" +
                "municipality='" + municipality + '\'' +
                ", collectionDate=" + collectionDate +
                ", timeSlot='" + timeSlot + '\'' +
                ", items=" + items +
                '}';
    }
}