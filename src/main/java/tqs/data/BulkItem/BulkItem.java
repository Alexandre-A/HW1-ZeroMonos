package tqs.data.BulkItem;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tqs.data.Booking.Booking;

import org.hibernate.validator.constraints.Length;

/**
 * Entity representing a bulk item to be collected
 */
@Entity
@Table(name = "bulk_items")
public class BulkItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Item name is mandatory")
    @Length(max = 30, message = "Name must not exceed 30 characters")
    @Column(nullable = false, length = 30)
    private String name;

    @Length(max = 100, message = "Description must not exceed 100 characters")
    @Column(length = 100)
    private String description;

    @NotNull(message = "Weight is mandatory")
    @Positive(message = "Weight must be positive")
    @Column(nullable = false)
    private Float weight;

    @NotNull(message = "Volume is mandatory")
    @Positive(message = "Volume must be positive")
    @Column(nullable = false)
    private Float volume;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Constructors
    public BulkItem() {
    }

    public BulkItem(String name, String description, Float weight, Float volume) {
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.volume = volume;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BulkItem)) return false;
        BulkItem bulkItem = (BulkItem) o;
        return id != null && id.equals(bulkItem.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "BulkItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", weight=" + weight +
                ", volume=" + volume +
                '}';
    }
}
