package tqs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.data.Booking.Booking;
import tqs.data.Booking.BookingRepository;
import tqs.data.BulkItem.BulkItem;
import tqs.data.BookingStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingValidationService Tests")
class BookingValidationServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingValidationService validationService;

    private LocalDate futureDate;
    private LocalDate pastDate;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        futureDate = LocalDate.now().plusDays(7);
        pastDate = LocalDate.now().minusDays(1);
        today = LocalDate.now();
    }

    // Test 1: Valid future date
    @Test
    @DisplayName("Should accept valid future date")
    void testValidateBookingDate_FutureDate() {
        boolean result = validationService.validateBookingDate(futureDate);
        assertThat(result).isTrue();
    }

    // Test 2: Past date
    @Test
    @DisplayName("Should reject past date")
    void testValidateBookingDate_PastDate() {
        boolean result = validationService.validateBookingDate(pastDate);
        assertThat(result).isFalse();
    }

    // Test 3: Today's date
    @Test
    @DisplayName("Should reject today's date (need advance booking)")
    void testValidateBookingDate_Today() {
        boolean result = validationService.validateBookingDate(today);
        assertThat(result).isFalse();
    }

    // Test 4: Date too far in future
    @Test
    @DisplayName("Should reject date more than 90 days in future")
    void testValidateBookingDate_TooFarInFuture() {
        LocalDate farFutureDate = LocalDate.now().plusDays(100);
        boolean result = validationService.validateBookingDate(farFutureDate);
        assertThat(result).isFalse();
    }

    // Test 5: Minimum valid date (tomorrow)
    @Test
    @DisplayName("Should accept minimum valid date (tomorrow)")
    void testValidateBookingDate_Tomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        boolean result = validationService.validateBookingDate(tomorrow);
        assertThat(result).isTrue();
    }

    // Test 6: Maximum valid date (90 days)
    @Test
    @DisplayName("Should accept maximum valid date (90 days)")
    void testValidateBookingDate_MaxDate() {
        LocalDate maxDate = LocalDate.now().plusDays(90);
        boolean result = validationService.validateBookingDate(maxDate);
        assertThat(result).isTrue();
    }

    // Test 7: Can accept booking - no existing bookings
    @Test
    @DisplayName("Should accept booking when no existing bookings")
    void testCanAcceptBooking_NoExistingBookings() {
        when(bookingRepository.findByMunicipality("Porto")).thenReturn(List.of());
        boolean result = validationService.canAcceptBooking("Porto", futureDate);
        assertThat(result).isTrue();
        verify(bookingRepository).findByMunicipality("Porto");
    }

    // Test 8: Can accept booking - capacity available
    @Test
    @DisplayName("Should accept booking when capacity available")
    void testCanAcceptBooking_CapacityAvailable() {
        // Given - 5 bookings for the same date (under capacity of 10)
        List<Booking> existingBookings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Booking booking = new Booking("Porto", futureDate, "morning");
            booking.setCurrentStatus(BookingStatus.RECEIVED);
            existingBookings.add(booking);
        }
        when(bookingRepository.findByMunicipality("Porto")).thenReturn(existingBookings);

        boolean result = validationService.canAcceptBooking("Porto", futureDate);
        assertThat(result).isTrue();
    }

    // Test 9: Can accept booking - capacity exceeded
    @Test
    @DisplayName("Should reject booking when daily capacity exceeded")
    void testCanAcceptBooking_CapacityExceeded() {
        // Given - 10 bookings for the same date (at capacity limit)
        List<Booking> existingBookings = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Booking booking = new Booking("Porto", futureDate, "morning");
            booking.setCurrentStatus(BookingStatus.ASSIGNED);
            existingBookings.add(booking);
        }
        when(bookingRepository.findByMunicipality("Porto")).thenReturn(existingBookings);

        boolean result = validationService.canAcceptBooking("Porto", futureDate);
        assertThat(result).isFalse();
        verify(bookingRepository).findByMunicipality("Porto");
    }

    // Test 10: Can accept booking - ignore cancelled bookings
    @Test
    @DisplayName("Should not count cancelled bookings toward capacity")
    void testCanAcceptBooking_IgnoreCancelled() {
        // Given - 8 active bookings + 5 cancelled bookings for same date
        List<Booking> existingBookings = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Booking booking = new Booking("Porto", futureDate, "morning");
            booking.setCurrentStatus(BookingStatus.RECEIVED);
            existingBookings.add(booking);
        }
        for (int i = 0; i < 5; i++) {
            Booking booking = new Booking("Porto", futureDate, "morning");
            booking.setCurrentStatus(BookingStatus.CANCELLED);
            existingBookings.add(booking);
        }
        when(bookingRepository.findByMunicipality("Porto")).thenReturn(existingBookings);

        boolean result = validationService.canAcceptBooking("Porto", futureDate);
        assertThat(result).isTrue();
    }

    // Test 11: Can accept booking - ignore completed bookings
    @Test
    @DisplayName("Should not count completed bookings toward capacity")
    void testCanAcceptBooking_IgnoreCompleted() {
        // Given - 9 active bookings + 3 completed bookings for same date
        List<Booking> existingBookings = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            Booking booking = new Booking("Porto", futureDate, "morning");
            booking.setCurrentStatus(BookingStatus.IN_PROGRESS);
            existingBookings.add(booking);
        }
        for (int i = 0; i < 3; i++) {
            Booking booking = new Booking("Porto", futureDate, "morning");
            booking.setCurrentStatus(BookingStatus.COMPLETED);
            existingBookings.add(booking);
        }
        when(bookingRepository.findByMunicipality("Porto")).thenReturn(existingBookings);

        boolean result = validationService.canAcceptBooking("Porto", futureDate);
        assertThat(result).isTrue();
    }

    // Test 12: Can accept booking - only count bookings for same date
    @Test
    @DisplayName("Should only count bookings for the same date")
    void testCanAcceptBooking_OnlySameDate() {
        // Given - 10 bookings but for different dates
        List<Booking> existingBookings = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Booking booking = new Booking("Porto", futureDate.plusDays(i), "morning");
            booking.setCurrentStatus(BookingStatus.RECEIVED);
            existingBookings.add(booking);
        }
        when(bookingRepository.findByMunicipality("Porto")).thenReturn(existingBookings);

        boolean result = validationService.canAcceptBooking("Porto", futureDate);
        assertThat(result).isTrue();
    }

    // Test: Validate bulk items - valid list
    @Test
    @DisplayName("Should accept booking with at least one item")
    void testValidateBulkItems_Valid() {
        // Given
        List<BulkItem> items = List.of(
            new BulkItem("Mattress", "Old mattress", 20f, 2f)
        );

        // When
        boolean result = validationService.validateBulkItems(items);

        // Then
        assertThat(result).isTrue();
    }

    // Test: Validate bulk items - empty list
    @Test
    @DisplayName("Should reject booking with no items")
    void testValidateBulkItems_Empty() {
        // Given
        List<BulkItem> items = List.of();

        boolean result = validationService.validateBulkItems(items);
        assertThat(result).isFalse();
    }


    // Test: Multiple items validation
    @Test
    @DisplayName("Should accept booking with multiple items")
    void testValidateBulkItems_Multiple() {
        // Given
        List<BulkItem> items = List.of(
            new BulkItem("Mattress", "Old mattress", 20f, 2f),
            new BulkItem("Fridge", "Old refrigerator", 50f, 1.5f),
            new BulkItem("Sofa", "Old sofa", 30f, 3f)
        );

        // When
        boolean result = validationService.validateBulkItems(items);

        // Then
        assertThat(result).isTrue();
    }
}
