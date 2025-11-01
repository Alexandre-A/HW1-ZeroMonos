package tqs.data;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import tqs.data.Booking.Booking;
import tqs.data.Booking.BookingRepository;
import tqs.data.BulkItem.BulkItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for BookingRepository
 */
@DataJpaTest
@DisplayName("BookingRepository Integration Tests")
class BookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    private Booking booking1;
    private Booking booking2;
    private Booking booking3;

    @BeforeEach
    void setUp() {
        // Create test bookings
        booking1 = new Booking("Porto", LocalDate.of(2025, 11, 15), "Morning (9:00-12:00)");
        booking2 = new Booking("Porto", LocalDate.of(2025, 11, 16), "Afternoon (14:00-17:00)");
        booking3 = new Booking("Lisbon", LocalDate.of(2025, 11, 15), "Morning (9:00-12:00)");
        
        booking2.setCurrentStatus(BookingStatus.ASSIGNED);
        booking3.setCurrentStatus(BookingStatus.COMPLETED);

        entityManager.persistAndFlush(booking1);
        entityManager.persistAndFlush(booking2);
        entityManager.persistAndFlush(booking3);
    }

    @AfterEach
    void tearDown() {
        entityManager.clear();
    }

    @Test
    @DisplayName("Should save and retrieve booking by ID")
    void testSaveAndFindById() {
        Booking newBooking = new Booking("Braga", LocalDate.now(), "Evening");
        Booking savedBooking = bookingRepository.save(newBooking);

        Optional<Booking> found = bookingRepository.findById(savedBooking.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getMunicipality()).isEqualTo("Braga");
        assertThat(found.get().getAccessToken()).isNotNull();
    }

    @Test
    @DisplayName("Should find booking by access token")
    void testFindByAccessToken() {
        String token = booking1.getAccessToken();

        Optional<Booking> found = bookingRepository.findByAccessToken(token);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(booking1.getId());
        assertThat(found.get().getMunicipality()).isEqualTo("Porto");
    }

    @Test
    @DisplayName("Should return empty when access token not found")
    void testFindByAccessTokenNotFound() {
        Optional<Booking> found = bookingRepository.findByAccessToken("non-existent-token");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find bookings by municipality")
    void testFindByMunicipality() {
        List<Booking> portoBookings = bookingRepository.findByMunicipality("Porto");

        assertThat(portoBookings).hasSize(2);
        assertThat(portoBookings).extracting(Booking::getMunicipality)
                                  .containsOnly("Porto");
    }

    @Test
    @DisplayName("Should find bookings by status")
    void testFindByCurrentStatus() {
        List<Booking> receivedBookings = bookingRepository.findByCurrentStatus(BookingStatus.RECEIVED);

        assertThat(receivedBookings).hasSize(1);
        assertThat(receivedBookings.get(0).getId()).isEqualTo(booking1.getId());
    }

    @Test
    @DisplayName("Should find bookings by municipality and status")
    void testFindByMunicipalityAndCurrentStatus() {
        List<Booking> result = bookingRepository.findByMunicipalityAndCurrentStatus(
                "Porto", BookingStatus.ASSIGNED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(booking2.getId());
    }

    @Test
    @DisplayName("Should find bookings by collection date")
    void testFindByCollectionDate() {
        LocalDate targetDate = LocalDate.of(2025, 11, 15);
        List<Booking> bookings = bookingRepository.findByCollectionDate(targetDate);

        assertThat(bookings).hasSize(2);
        assertThat(bookings).extracting(Booking::getCollectionDate)
                           .containsOnly(targetDate);
    }

    @Test
    @DisplayName("Should find bookings by municipality and date")
    void testFindByMunicipalityAndCollectionDate() {
        List<Booking> result = bookingRepository.findByMunicipalityAndCollectionDate(
                "Porto", LocalDate.of(2025, 11, 15));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(booking1.getId());
    }

    @Test
    @DisplayName("Should check if access token exists")
    void testExistsByAccessToken() {
        String existingToken = booking1.getAccessToken();
        String nonExistingToken = "non-existent-token";

        assertThat(bookingRepository.existsByAccessToken(existingToken)).isTrue();
        assertThat(bookingRepository.existsByAccessToken(nonExistingToken)).isFalse();
    }

    @Test
    @DisplayName("Should update booking status")
    void testUpdateBookingStatus() {
        booking1.setCurrentStatus(BookingStatus.IN_PROGRESS);
        bookingRepository.save(booking1);
        entityManager.flush();

        Booking updated = bookingRepository.findById(booking1.getId()).orElseThrow();
        assertThat(updated.getCurrentStatus()).isEqualTo(BookingStatus.IN_PROGRESS);
    }

    //@Test
    //@DisplayName("Should delete booking")
    //void testDeleteBooking() {
    //    Long bookingId = booking1.getId();
    //    bookingRepository.delete(booking1);
    //    entityManager.flush();
//
    //    Optional<Booking> found = bookingRepository.findById(bookingId);
    //    assertThat(found).isEmpty();
    //}

    @Test
    @DisplayName("Should find all bookings")
    void testFindAll() {
        List<Booking> allBookings = bookingRepository.findAll();

        assertThat(allBookings).hasSize(3);
    }

    @Test
    @DisplayName("Should generate unique access tokens")
    void testUniqueAccessTokens() {
        Booking b1 = new Booking("City1", LocalDate.now(), "Slot1");
        Booking b2 = new Booking("City2", LocalDate.now(), "Slot2");
        
        bookingRepository.save(b1);
        bookingRepository.save(b2);

        assertThat(b1.getAccessToken()).isNotEqualTo(b2.getAccessToken());
        assertThat(bookingRepository.existsByAccessToken(b1.getAccessToken())).isTrue();
        assertThat(bookingRepository.existsByAccessToken(b2.getAccessToken())).isTrue();
    }

    @Test
    @DisplayName("Should return empty list for non-existent municipality")
    void testFindByNonExistentMunicipality() {
        List<Booking> result = bookingRepository.findByMunicipality("NonExistent");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle booking with multiple bulk items")
    void testBookingWithBulkItems() {
        Booking booking = new Booking("Coimbra", LocalDate.now(), "Morning");
        BulkItem item1 = new BulkItem("Sofa", "Old sofa", 50.0f, 2.5f);
        BulkItem item2 = new BulkItem("Mattress", "King size", 30.0f, 1.8f);
        
        booking.addBulkItem(item1);
        booking.addBulkItem(item2);
        
        Booking saved = bookingRepository.save(booking);
        entityManager.flush();
        entityManager.clear();

        Booking retrieved = bookingRepository.findById(saved.getId()).orElseThrow();
        assertThat(retrieved.getBulkItems()).hasSize(2);
    }
}
