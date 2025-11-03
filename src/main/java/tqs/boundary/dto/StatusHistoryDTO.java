package tqs.boundary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import tqs.data.StatusHistory.StatusHistory;

import java.time.LocalDateTime;

/**
 * DTO for StatusHistory
 * Used to show status changes in booking details
 */
public class StatusHistoryDTO {

    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // Constructors
    public StatusHistoryDTO() {
    }

    public StatusHistoryDTO(String status, LocalDateTime timestamp) {
        this.status = status;
        this.timestamp = timestamp;
    }

    // Static factory method to create DTO from entity
    public static StatusHistoryDTO fromEntity(StatusHistory history) {
        if (history == null) {
            return null;
        }
        return new StatusHistoryDTO(
            history.getStatus().name(),
            history.getDatetime()
        );
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "StatusHistoryDTO{" +
                "status='" + status + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
