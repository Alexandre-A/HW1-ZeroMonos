package tqs.data.Booking;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import tqs.data.BookingStatus;
import tqs.data.BulkItem.BulkItem;
import tqs.data.StatusHistory.StatusHistory;
import tqs.data.state.BookingState;
import tqs.data.state.BookingStateFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a booking for garbage collection service
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Municipality is mandatory")
    @Column(nullable = false)
    private String municipality;

    @NotNull(message = "Collection date is mandatory")
    @Column(nullable = false)
    private LocalDate collectionDate;

    @NotBlank(message = "Time slot is mandatory")
    @Column(nullable = false)
    private String timeSlot;

    @Column(unique = true, nullable = false, updatable = false)
    private String accessToken;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus currentStatus;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Transient
    private BookingState state;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BulkItem> bulkItems = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StatusHistory> statusHistories = new ArrayList<>();

    // Constructors
    public Booking() {
        this.accessToken = UUID.randomUUID().toString();
        this.currentStatus = BookingStatus.RECEIVED;
    }

    public Booking(String municipality, LocalDate collectionDate, String timeSlot) {
        this();
        this.municipality = municipality;
        this.collectionDate = collectionDate;
        this.timeSlot = timeSlot;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BookingStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(BookingStatus currentStatus) {
        this.currentStatus = currentStatus;
        this.state = null; // Reset state to force recreation
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    private BookingState getState() {
        if (state == null) {
            state = BookingStateFactory.getState(this);
        }
        return state;
    }

    // State transition methods - delegate to state object

    public void assign() {
        BookingStatus oldStatus = this.currentStatus;
        getState().assign();
        if (oldStatus != this.currentStatus) {
            addStatusHistory(new StatusHistory(this.currentStatus, this));
        }
    }

    public void start() {
        BookingStatus oldStatus = this.currentStatus;
        getState().start();
        if (oldStatus != this.currentStatus) {
            addStatusHistory(new StatusHistory(this.currentStatus, this));
        }
    }

    public void complete() {
        BookingStatus oldStatus = this.currentStatus;
        getState().complete();
        if (oldStatus != this.currentStatus) {
            addStatusHistory(new StatusHistory(this.currentStatus, this));
        }
    }

    public void cancel() {
        BookingStatus oldStatus = this.currentStatus;
        getState().cancel();
        if (oldStatus != this.currentStatus) {
            addStatusHistory(new StatusHistory(this.currentStatus, this));
        }
    }

    public List<BulkItem> getBulkItems() {
        return bulkItems;
    }

    public void setBulkItems(List<BulkItem> bulkItems) {
        this.bulkItems = bulkItems;
    }

    public List<StatusHistory> getStatusHistories() {
        return statusHistories;
    }

    public void setStatusHistories(List<StatusHistory> statusHistories) {
        this.statusHistories = statusHistories;
    }

    // Helper methods for bidirectional relationships
    public void addBulkItem(BulkItem bulkItem) {
        bulkItems.add(bulkItem);
        bulkItem.setBooking(this);
    }

    public void removeBulkItem(BulkItem bulkItem) {
        bulkItems.remove(bulkItem);
        bulkItem.setBooking(null);
    }

    public void addStatusHistory(StatusHistory statusHistory) {
        statusHistories.add(statusHistory);
        statusHistory.setBooking(this);
    }

    public void removeStatusHistory(StatusHistory statusHistory) {
        statusHistories.remove(statusHistory);
        statusHistory.setBooking(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;
        return id != null && id.equals(booking.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", municipality='" + municipality + '\'' +
                ", collectionDate=" + collectionDate +
                ", timeSlot='" + timeSlot + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", currentStatus=" + currentStatus +
                ", createdAt=" + createdAt +
                '}';
    }
}
