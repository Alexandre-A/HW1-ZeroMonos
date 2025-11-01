package tqs.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tqs.data.Booking.Booking;
import tqs.data.BulkItem.BulkItem;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for BulkItem entity
 */
@DisplayName("BulkItem Entity Tests")
class BulkItemTest {

    private BulkItem bulkItem;
    private Booking booking;

    @BeforeEach
    void setUp() {
        booking = new Booking("Porto", LocalDate.now(), "Morning");
        bulkItem = new BulkItem("Sofa", "Old leather sofa", 50.0f, 2.5f);
    }

    @Test
    @DisplayName("Should create bulk item with valid data")
    void testCreateBulkItem() {
        assertThat(bulkItem).isNotNull();
        assertThat(bulkItem.getName()).isEqualTo("Sofa");
        assertThat(bulkItem.getDescription()).isEqualTo("Old leather sofa");
        assertThat(bulkItem.getWeight()).isEqualTo(50.0f);
        assertThat(bulkItem.getVolume()).isEqualTo(2.5f);
    }

    @Test
    @DisplayName("Should create bulk item without description")
    void testCreateBulkItemWithoutDescription() {
        BulkItem item = new BulkItem("Mattress", null, 30.0f, 1.8f);
        
        assertThat(item.getName()).isEqualTo("Mattress");
        assertThat(item.getDescription()).isNull();
        assertThat(item.getWeight()).isEqualTo(30.0f);
        assertThat(item.getVolume()).isEqualTo(1.8f);
    }

    @Test
    @DisplayName("Should set booking reference")
    void testSetBooking() {
        bulkItem.setBooking(booking);
        
        assertThat(bulkItem.getBooking()).isEqualTo(booking);
    }

    @Test
    @DisplayName("Should properly set and get all fields")
    void testGettersAndSetters() {
        BulkItem item = new BulkItem();
        
        item.setName("Refrigerator");
        item.setDescription("Old white fridge");
        item.setWeight(70.0f);
        item.setVolume(0.8f);
        item.setBooking(booking);

        assertThat(item.getName()).isEqualTo("Refrigerator");
        assertThat(item.getDescription()).isEqualTo("Old white fridge");
        assertThat(item.getWeight()).isEqualTo(70.0f);
        assertThat(item.getVolume()).isEqualTo(0.8f);
        assertThat(item.getBooking()).isEqualTo(booking);
    }

    @Test
    @DisplayName("Should have proper toString representation")
    void testToString() {
        String str = bulkItem.toString();
        
        assertThat(str).contains("BulkItem");
        assertThat(str).contains("Sofa");
        assertThat(str).contains("Old leather sofa");
        assertThat(str).contains("50.0");
        assertThat(str).contains("2.5");
    }

    @Test
    @DisplayName("Should handle different item types")
    void testDifferentItemTypes() {
        BulkItem sofa = new BulkItem("Sofa", "3-seater", 45.0f, 2.0f);
        BulkItem mattress = new BulkItem("Mattress", "Queen size", 25.0f, 1.5f);
        BulkItem fridge = new BulkItem("Refrigerator", "Double door", 80.0f, 0.9f);
        BulkItem washingMachine = new BulkItem("Washing Machine", "Front load", 60.0f, 0.6f);

        assertThat(sofa.getName()).isEqualTo("Sofa");
        assertThat(mattress.getName()).isEqualTo("Mattress");
        assertThat(fridge.getName()).isEqualTo("Refrigerator");
        assertThat(washingMachine.getName()).isEqualTo("Washing Machine");
    }

    @Test
    @DisplayName("Should handle small and large items")
    void testVariousSizes() {
        BulkItem smallItem = new BulkItem("Chair", "Dining chair", 5.0f, 0.1f);
        BulkItem largeItem = new BulkItem("Wardrobe", "3-door wardrobe", 150.0f, 5.0f);

        assertThat(smallItem.getWeight()).isLessThan(largeItem.getWeight());
        assertThat(smallItem.getVolume()).isLessThan(largeItem.getVolume());
    }

    @Test
    @DisplayName("Should accept items with maximum name length")
    void testMaxNameLength() {
        // Maximum length is 30 characters
        String maxName = "A".repeat(30);
        BulkItem item = new BulkItem(maxName, "Test item", 10.0f, 0.5f);
        
        assertThat(item.getName()).hasSize(30);
        assertThat(item.getName()).isEqualTo(maxName);
    }

    @Test
    @DisplayName("Should accept items with maximum description length")
    void testMaxDescriptionLength() {
        // Maximum length is 100 characters
        String maxDesc = "B".repeat(100);
        BulkItem item = new BulkItem("Item", maxDesc, 10.0f, 0.5f);
        
        assertThat(item.getDescription()).hasSize(100);
        assertThat(item.getDescription()).isEqualTo(maxDesc);
    }

    @Test
    @DisplayName("Should handle decimal values for weight and volume")
    void testDecimalValues() {
        BulkItem item = new BulkItem("Test", "Test item", 45.75f, 2.333f);
        
        assertThat(item.getWeight()).isEqualTo(45.75f);
        assertThat(item.getVolume()).isEqualTo(2.333f);
    }

    @Test
    @DisplayName("Should properly handle equals and hashCode")
    void testEqualsAndHashCode() {
        BulkItem item1 = new BulkItem("Sofa", "Old sofa", 50.0f, 2.5f);
        BulkItem item2 = new BulkItem("Mattress", "Queen size", 30.0f, 1.8f);
        
        // Test equals with same object
        assertThat(item1.equals(item1)).isTrue();
        
        // Test equals with different type
        assertThat(item1.equals("not a bulk item")).isFalse();
        
        // Test equals with null
        assertThat(item1.equals(null)).isFalse();
        
        // Test hashCode consistency
        assertThat(item1.hashCode()).isEqualTo(item1.hashCode());
        assertThat(item2.hashCode()).isEqualTo(item2.hashCode());
    }

    @Test
    @DisplayName("Should properly set id")
    void testIdHandling() {
        BulkItem item = new BulkItem("Chair", "Dining chair", 5.0f, 0.1f);
        
        // Test setting ID
        item.setId(1L);
        assertThat(item.getId()).isEqualTo(1L);
        
        // Test with different ID
        item.setId(999L);
        assertThat(item.getId()).isEqualTo(999L);
    }

    @Test
    @DisplayName("Should properly update booking reference")
    void testUpdateBookingReference() {
        BulkItem item = new BulkItem("Table", "Coffee table", 15.0f, 0.3f);
        
        // Initially no booking
        assertThat(item.getBooking()).isNull();
        
        // Set first booking
        item.setBooking(booking);
        assertThat(item.getBooking()).isEqualTo(booking);
        
        // Update to another booking
        Booking anotherBooking = new Booking("Lisbon", LocalDate.now(), "Evening");
        item.setBooking(anotherBooking);
        assertThat(item.getBooking()).isEqualTo(anotherBooking);
    }
}
