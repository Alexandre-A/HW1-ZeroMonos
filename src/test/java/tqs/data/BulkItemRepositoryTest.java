package tqs.data;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import tqs.data.Booking.Booking;
import tqs.data.BulkItem.BulkItem;
import tqs.data.BulkItem.BulkItemRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for BulkItemRepository
 */
@DataJpaTest
@DisplayName("BulkItemRepository Integration Tests")
class BulkItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BulkItemRepository bulkItemRepository;

    private Booking booking1;
    private Booking booking2;
    private BulkItem item1;
    private BulkItem item2;
    private BulkItem item3;

    @BeforeEach
    void setUp() {
        // Create test bookings
        booking1 = new Booking("Porto", LocalDate.now(), "Morning");
        booking2 = new Booking("Lisbon", LocalDate.now(), "Afternoon");

        entityManager.persist(booking1);
        entityManager.persist(booking2);

        // Create test bulk items
        item1 = new BulkItem("Sofa", "Old leather sofa", 50.0f, 2.5f);
        item2 = new BulkItem("Mattress", "King size mattress", 30.0f, 1.8f);
        item3 = new BulkItem("Refrigerator", "Old white fridge", 70.0f, 0.8f);

        booking1.addBulkItem(item1);
        booking1.addBulkItem(item2);
        booking2.addBulkItem(item3);

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);
        entityManager.flush();
    }

    @AfterEach
    void tearDown() {
        entityManager.clear();
    }

    @Test
    @DisplayName("Should save and retrieve bulk item by ID")
    void testSaveAndFindById() {
        BulkItem newItem = new BulkItem("Washing Machine", "Front load", 60.0f, 0.6f);
        newItem.setBooking(booking1);
        
        BulkItem saved = bulkItemRepository.save(newItem);
        entityManager.flush();

        Optional<BulkItem> found = bulkItemRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Washing Machine");
        assertThat(found.get().getWeight()).isEqualTo(60.0f);
    }

    @Test
    @DisplayName("Should find bulk items by booking")
    void testFindByBooking() {
        List<BulkItem> items = bulkItemRepository.findByBooking(booking1);

        assertThat(items).hasSize(2);
        assertThat(items).containsExactlyInAnyOrder(item1, item2);
    }

    @Test
    @DisplayName("Should find bulk items by booking ID")
    void testFindByBookingId() {
        List<BulkItem> items = bulkItemRepository.findByBookingId(booking1.getId());

        assertThat(items).hasSize(2);
        assertThat(items).extracting(BulkItem::getName)
                        .containsExactlyInAnyOrder("Sofa", "Mattress");
    }

    @Test
    @DisplayName("Should count bulk items for a booking")
    void testCountByBooking() {
        long count1 = bulkItemRepository.countByBooking(booking1);
        long count2 = bulkItemRepository.countByBooking(booking2);

        assertThat(count1).isEqualTo(2);
        assertThat(count2).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find bulk items by name containing search term")
    void testFindByNameContainingIgnoreCase() {
        List<BulkItem> items = bulkItemRepository.findByNameContainingIgnoreCase("sofa");

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getName()).isEqualTo("Sofa");
    }

    @Test
    @DisplayName("Should find bulk items with case insensitive search")
    void testFindByNameCaseInsensitive() {
        List<BulkItem> lower = bulkItemRepository.findByNameContainingIgnoreCase("refrigerator");
        List<BulkItem> upper = bulkItemRepository.findByNameContainingIgnoreCase("REFRIGERATOR");
        List<BulkItem> mixed = bulkItemRepository.findByNameContainingIgnoreCase("ReFrIgErAtOr");

        assertThat(lower).hasSize(1);
        assertThat(upper).hasSize(1);
        assertThat(mixed).hasSize(1);
        assertThat(lower).isEqualTo(upper).isEqualTo(mixed);
    }

    @Test
    @DisplayName("Should find bulk items with partial name match")
    void testFindByPartialName() {
        List<BulkItem> items = bulkItemRepository.findByNameContainingIgnoreCase("at");

        // Should find "Mattress" and "Refrigerator" (both contain "at")
        assertThat(items).hasSize(2);
        assertThat(items).extracting(BulkItem::getName)
                        .allMatch(name -> name.toLowerCase().contains("at"));
    }

    @Test
    @DisplayName("Should update bulk item details")
    void testUpdateBulkItem() {
        item1.setName("Updated Sofa");
        item1.setWeight(55.0f);
        bulkItemRepository.save(item1);
        entityManager.flush();

        BulkItem updated = bulkItemRepository.findById(item1.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Sofa");
        assertThat(updated.getWeight()).isEqualTo(55.0f);
    }

    //@Test
    //@DisplayName("Should delete bulk item")
    //void testDeleteBulkItem() {
    //    Long itemId = item1.getId();
    //    bulkItemRepository.deleteById(itemId);
    //    entityManager.flush();
    //    entityManager.clear();
//
    //    assertThat(bulkItemRepository.existsById(itemId)).isFalse();
    //}

    @Test
    @DisplayName("Should cascade delete bulk items when booking is deleted")
    void testCascadeDelete() {
        Long item1Id = item1.getId();
        Long item2Id = item2.getId();
        
        entityManager.remove(booking1);
        entityManager.flush();

        assertThat(bulkItemRepository.findById(item1Id)).isEmpty();
        assertThat(bulkItemRepository.findById(item2Id)).isEmpty();
        assertThat(bulkItemRepository.findById(item3.getId())).isPresent(); // Other booking's item still exists
    }

    @Test
    @DisplayName("Should find all bulk items")
    void testFindAll() {
        List<BulkItem> allItems = bulkItemRepository.findAll();

        assertThat(allItems).hasSize(3);
    }

    @Test
    @DisplayName("Should return empty list for non-existent booking")
    void testFindByNonExistentBooking() {
        Booking nonExistentBooking = new Booking("Test", LocalDate.now(), "Test");
        nonExistentBooking.setId(999L);

        List<BulkItem> items = bulkItemRepository.findByBooking(nonExistentBooking);

        assertThat(items).isEmpty();
    }

    @Test
    @DisplayName("Should save bulk item with minimum required fields")
    void testSaveWithMinimumFields() {
        BulkItem item = new BulkItem("Chair", null, 5.0f, 0.1f);
        item.setBooking(booking1);
        
        BulkItem saved = bulkItemRepository.save(item);
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Chair");
        assertThat(saved.getDescription()).isNull();
    }

    @Test
    @DisplayName("Should handle multiple items with same name")
    void testMultipleItemsWithSameName() {
        BulkItem sofa1 = new BulkItem("Sofa", "2-seater", 40.0f, 2.0f);
        BulkItem sofa2 = new BulkItem("Sofa", "3-seater", 50.0f, 2.5f);
        
        sofa1.setBooking(booking1);
        sofa2.setBooking(booking2);
        
        bulkItemRepository.save(sofa1);
        bulkItemRepository.save(sofa2);
        entityManager.flush();

        List<BulkItem> sofas = bulkItemRepository.findByNameContainingIgnoreCase("Sofa");
        assertThat(sofas).hasSizeGreaterThanOrEqualTo(3); // At least the original sofa + 2 new ones
    }
}
