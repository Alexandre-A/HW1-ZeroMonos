package tqs.boundary.dto;

import tqs.data.Booking.Booking;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for returning detailed booking information (with status history)
 * Used when citizen checks their booking or staff views details
 */
public class BookingDetailedResponseDTO {

    private Long id;
    private String accessToken;
    private String municipality;
    private LocalDate collectionDate;
    private String timeSlot;
    private String currentStatus;
    private List<BulkItemDTO> items;
    private List<StatusHistoryDTO> statusHistory;

    // Constructors
    public BookingDetailedResponseDTO() {
    }

    // Static factory method to create DTO from entity with status history
    public static BookingDetailedResponseDTO fromEntity(Booking booking) {
        return fromEntity(booking, true);
    }

    // Static factory method with option to include/exclude status history
    public static BookingDetailedResponseDTO fromEntity(Booking booking, boolean includeStatusHistory) {
        if (booking == null) {
            return null;
        }
        
        BookingDetailedResponseDTO dto = new BookingDetailedResponseDTO();
        dto.setId(booking.getId());
        dto.setAccessToken(booking.getAccessToken());
        dto.setMunicipality(booking.getMunicipality());
        dto.setCollectionDate(booking.getCollectionDate());
        dto.setTimeSlot(booking.getTimeSlot());
        dto.setCurrentStatus(booking.getCurrentStatus().name());
        
        // Convert items
        List<BulkItemDTO> itemDTOs = booking.getBulkItems().stream()
                .map(BulkItemDTO::fromEntity)
                .toList();
        dto.setItems(itemDTOs);
        
        // Convert status history (sorted by timestamp) - only if requested
        if (includeStatusHistory) {
            List<StatusHistoryDTO> historyDTOs = booking.getStatusHistories().stream()
                    .map(StatusHistoryDTO::fromEntity)
                    .sorted((h1, h2) -> h2.getTimestamp().compareTo(h1.getTimestamp())) // Most recent first
                    .toList();
            dto.setStatusHistory(historyDTOs);
        }
        
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

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

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public List<BulkItemDTO> getItems() {
        return items;
    }

    public void setItems(List<BulkItemDTO> items) {
        this.items = items;
    }

    public List<StatusHistoryDTO> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<StatusHistoryDTO> statusHistory) {
        this.statusHistory = statusHistory;
    }

    @Override
    public String toString() {
        return "BookingDetailedResponseDTO{" +
                "id=" + id +
                ", accessToken='" + accessToken + '\'' +
                ", municipality='" + municipality + '\'' +
                ", collectionDate=" + collectionDate +
                ", timeSlot='" + timeSlot + '\'' +
                ", currentStatus='" + currentStatus + '\'' +
                ", items=" + items +
                ", statusHistory=" + statusHistory +
                '}';
    }
}
