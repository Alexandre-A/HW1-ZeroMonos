package tqs.boundary.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for error responses
 * Standardized error format for API
 */
public class ErrorResponseDTO {

    private String message;
    private LocalDateTime timestamp;
    private String path;
    private int status;
    private Map<String, String> errors; // For validation errors

    // Constructors
    public ErrorResponseDTO() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponseDTO(String message, String path, int status) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.path = path;
        this.status = status;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return "ErrorResponseDTO{" +
                "message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", path='" + path + '\'' +
                ", status=" + status +
                ", errors=" + errors +
                '}';
    }
}