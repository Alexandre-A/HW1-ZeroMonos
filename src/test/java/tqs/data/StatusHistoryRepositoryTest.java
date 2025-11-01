package tqs.data;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import tqs.data.Booking.Booking;
import tqs.data.StatusHistory.StatusHistory;
import tqs.data.StatusHistory.StatusHistoryRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for StatusHistoryRepository
 */
@DataJpaTest
@DisplayName("StatusHistoryRepository Integration Tests")
class StatusHistoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;

    private Booking booking1;
    private Booking booking2;
    private StatusHistory history1;
    private StatusHistory history2;
    private StatusHistory history3;

    @BeforeEach
    void setUp() {
        // Create test bookings
        booking1 = new Booking("Porto", LocalDate.now(), "Morning");
        booking2 = new Booking("Lisbon", LocalDate.now(), "Afternoon");

        entityManager.persist(booking1);
        entityManager.persist(booking2);

        // Create test status histories
        history1 = new StatusHistory(BookingStatus.RECEIVED, booking1);
        history2 = new StatusHistory(BookingStatus.ASSIGNED, booking1);
        history3 = new StatusHistory(BookingStatus.RECEIVED, booking2);

        booking1.addStatusHistory(history1);
        booking1.addStatusHistory(history2);
        booking2.addStatusHistory(history3);

        entityManager.persist(history1);
        entityManager.persist(history2);
        entityManager.persist(history3);
        entityManager.flush();
    }

    @AfterEach
    void tearDown() {
        entityManager.clear();
    }

    @Test
    @DisplayName("Should save and retrieve status history by ID")
    void testSaveAndFindById() {
        StatusHistory newHistory = new StatusHistory(BookingStatus.IN_PROGRESS, booking1);
        
        StatusHistory saved = statusHistoryRepository.save(newHistory);
        entityManager.flush();

        Optional<StatusHistory> found = statusHistoryRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(BookingStatus.IN_PROGRESS);
        assertThat(found.get().getDatetime()).isNotNull();
    }

    @Test
    @DisplayName("Should find status histories by booking ordered by datetime")
    void testFindByBookingOrderByDatetimeAsc() {
        List<StatusHistory> histories = statusHistoryRepository.findByBookingOrderByDatetimeAsc(booking1);

        assertThat(histories).hasSize(2);
        assertThat(histories).containsExactly(history1, history2);
        
        // Verify ordering
        for (int i = 0; i < histories.size() - 1; i++) {
            assertThat(histories.get(i).getDatetime())
                .isBeforeOrEqualTo(histories.get(i + 1).getDatetime());
        }
    }

    @Test
    @DisplayName("Should find status histories by booking ID")
    void testFindByBookingIdOrderByDatetimeAsc() {
        List<StatusHistory> histories = statusHistoryRepository
                .findByBookingIdOrderByDatetimeAsc(booking1.getId());

        assertThat(histories).hasSize(2);
        assertThat(histories).extracting(StatusHistory::getStatus)
                           .containsExactly(BookingStatus.RECEIVED, BookingStatus.ASSIGNED);
    }

    @Test
    @DisplayName("Should find status histories by status")
    void testFindByStatus() {
        List<StatusHistory> received = statusHistoryRepository.findByStatus(BookingStatus.RECEIVED);

        assertThat(received).hasSize(2);
        assertThat(received).containsExactlyInAnyOrder(history1, history3);
    }

    @Test
    @DisplayName("Should find status histories in date range")
    void testFindByDatetimeBetween() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        List<StatusHistory> histories = statusHistoryRepository.findByDatetimeBetween(start, end);

        assertThat(histories).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should count status histories for a booking")
    void testCountByBooking() {
        long count1 = statusHistoryRepository.countByBooking(booking1);
        long count2 = statusHistoryRepository.countByBooking(booking2);

        assertThat(count1).isEqualTo(2);
        assertThat(count2).isEqualTo(1);
    }

    @Test
    @DisplayName("Should automatically set datetime on creation")
    void testAutomaticDatetimeSet() {
        StatusHistory newHistory = new StatusHistory(BookingStatus.COMPLETED, booking1);
        
        StatusHistory saved = statusHistoryRepository.save(newHistory);
        entityManager.flush();

        assertThat(saved.getDatetime()).isNotNull();
        assertThat(saved.getDatetime()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should track complete status workflow")
    void testCompleteWorkflow() {
        Booking testBooking = new Booking("Coimbra", LocalDate.now(), "Evening");
        entityManager.persist(testBooking);

        StatusHistory step1 = new StatusHistory(BookingStatus.RECEIVED, testBooking);
        StatusHistory step2 = new StatusHistory(BookingStatus.ASSIGNED, testBooking);
        StatusHistory step3 = new StatusHistory(BookingStatus.IN_PROGRESS, testBooking);
        StatusHistory step4 = new StatusHistory(BookingStatus.COMPLETED, testBooking);

        statusHistoryRepository.save(step1);
        statusHistoryRepository.save(step2);
        statusHistoryRepository.save(step3);
        statusHistoryRepository.save(step4);
        entityManager.flush();

        List<StatusHistory> workflow = statusHistoryRepository
                .findByBookingOrderByDatetimeAsc(testBooking);

        assertThat(workflow).hasSize(4);
        assertThat(workflow).extracting(StatusHistory::getStatus)
                           .containsExactly(
                               BookingStatus.RECEIVED,
                               BookingStatus.ASSIGNED,
                               BookingStatus.IN_PROGRESS,
                               BookingStatus.COMPLETED
                           );
    }

    //@Test
    //@DisplayName("Should delete status history")
    //void testDeleteStatusHistory() {
    //    Long historyId = history1.getId();
    //    statusHistoryRepository.deleteById(historyId);
    //    entityManager.flush();
    //    entityManager.clear();
//
    //    assertThat(statusHistoryRepository.existsById(historyId)).isFalse();
    //}

    @Test
    @DisplayName("Should cascade delete status histories when booking is deleted")
    void testCascadeDelete() {
        Long history1Id = history1.getId();
        Long history2Id = history2.getId();
        Long history3Id = history3.getId();
        
        entityManager.remove(booking1);
        entityManager.flush();

        assertThat(statusHistoryRepository.findById(history1Id)).isEmpty();
        assertThat(statusHistoryRepository.findById(history2Id)).isEmpty();
        assertThat(statusHistoryRepository.findById(history3Id)).isPresent(); // Other booking's history still exists
    }

    @Test
    @DisplayName("Should find all status histories")
    void testFindAll() {
        List<StatusHistory> allHistories = statusHistoryRepository.findAll();

        assertThat(allHistories).hasSize(3);
    }

    @Test
    @DisplayName("Should handle cancelled booking status")
    void testCancelledStatus() {
        StatusHistory cancelled = new StatusHistory(BookingStatus.CANCELLED, booking1);
        statusHistoryRepository.save(cancelled);
        entityManager.flush();

        List<StatusHistory> cancelledHistories = statusHistoryRepository
                .findByStatus(BookingStatus.CANCELLED);

        assertThat(cancelledHistories).hasSize(1);
        assertThat(cancelledHistories.get(0).getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should maintain order even with simultaneous status changes")
    void testMultipleStatusChanges() {
        StatusHistory h1 = new StatusHistory(BookingStatus.IN_PROGRESS, booking1);
        StatusHistory h2 = new StatusHistory(BookingStatus.COMPLETED, booking1);
        
        statusHistoryRepository.save(h1);
        statusHistoryRepository.save(h2);
        entityManager.flush();

        List<StatusHistory> histories = statusHistoryRepository
                .findByBookingOrderByDatetimeAsc(booking1);

        assertThat(histories).hasSizeGreaterThanOrEqualTo(4);
        // Verify chronological order
        for (int i = 0; i < histories.size() - 1; i++) {
            assertThat(histories.get(i).getDatetime())
                .isBeforeOrEqualTo(histories.get(i + 1).getDatetime());
        }
    }

    @Test
    @DisplayName("Should return empty list for non-existent booking")
    void testFindByNonExistentBooking() {
        Booking nonExistent = new Booking("Test", LocalDate.now(), "Test");
        nonExistent.setId(999L);

        List<StatusHistory> histories = statusHistoryRepository.findByBookingOrderByDatetimeAsc(nonExistent);

        assertThat(histories).isEmpty();
    }

    @Test
    @DisplayName("Should find histories in narrow time range")
    void testNarrowTimeRange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusMinutes(5);
        LocalDateTime end = now.plusMinutes(5);

        List<StatusHistory> histories = statusHistoryRepository.findByDatetimeBetween(start, end);

        assertThat(histories).isNotEmpty();
        histories.forEach(h -> {
            assertThat(h.getDatetime()).isAfterOrEqualTo(start);
            assertThat(h.getDatetime()).isBeforeOrEqualTo(end);
        });
    }
}
