package tqs.boundary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tqs.data.BulkItem.BulkItem;

/**
 * DTO for BulkItem
 * Used in API requests/responses
 */
public class BulkItemDTO {

    @NotBlank(message = "Item name is required")
    private String name;

    private String description;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Float weight;

    @NotNull(message = "Volume is required")
    @Positive(message = "Volume must be positive")
    private Float volume;

    public BulkItemDTO() {
    }

    public BulkItemDTO(String name, String description, Float weight, Float volume) {
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.volume = volume;
    }

    // Static factory method to create DTO from entity
    public static BulkItemDTO fromEntity(BulkItem item) {
        if (item == null) {
            return null;
        }
        return new BulkItemDTO(
            item.getName(),
            item.getDescription(),
            item.getWeight(),
            item.getVolume()
        );
    }

    // Method to convert DTO to entity
    public BulkItem toEntity() {
        return new BulkItem(name, description, weight, volume);
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public Float getVolume() {
        return volume;
    }

    public void setVolume(Float volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "BulkItemDTO{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", weight=" + weight +
                ", volume=" + volume +
                '}';
    }
}