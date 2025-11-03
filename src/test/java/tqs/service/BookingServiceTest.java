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
import tqs.data.BookingStatus;
import tqs.data.BulkItem.BulkItem;
import tqs.data.StatusHistory.StatusHistoryRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private StatusHistoryRepository statusHistoryRepository;

    @Mock
    private BookingValidationService validationService;

    @InjectMocks
    private BookingService bookingService;

    private Booking testBooking;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        futureDate = LocalDate.now().plusDays(7);
        testBooking = new Booking("Porto", futureDate, "morning");
    }

    // Test 1: Create booking successfully
    @Test
    @DisplayName("Should create a new booking with valid data")
    void testCreateBooking_Success() {
        // Given
        BulkItem item = new BulkItem("Mattress", "Old mattress", 20f, 2f);
        List<BulkItem> items = List.of(item);
        
        when(validationService.validateBulkItems(items)).thenReturn(true);
        when(validationService.validateBookingDate(futureDate)).thenReturn(true);
        when(validationService.canAcceptBooking("Porto", futureDate)).thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // When
        Booking created = bookingService.createBooking("Porto", futureDate, "morning", items);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getMunicipality()).isEqualTo("Porto");
        assertThat(created.getCurrentStatus()).isEqualTo(BookingStatus.RECEIVED);
        assertThat(created.getAccessToken()).isNotNull();
        verify(bookingRepository).save(any(Booking.class));
        verify(validationService).validateBulkItems(items);
        verify(validationService).validateBookingDate(futureDate);
        verify(validationService).canAcceptBooking("Porto", futureDate);
    }

    //Reject booking with no items
    @Test
    @DisplayName("Should reject booking with no items")
    void testCreateBooking_NoItems() {
        // Given
        when(validationService.validateBulkItems(List.of())).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking("Porto", futureDate, "morning", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least one bulk item is required");
        verify(bookingRepository, never()).save(any());
    }

    // Test 2: Create booking with invalid date
    @Test
    @DisplayName("Should reject booking with past date")
    void testCreateBooking_PastDate() {
        // Given
        LocalDate pastDate = LocalDate.now().minusDays(1);
        BulkItem item = new BulkItem("Mattress", "Old mattress", 20f, 2f);
        List<BulkItem> items = List.of(item);
        
        when(validationService.validateBulkItems(items)).thenReturn(true);
        when(validationService.validateBookingDate(pastDate)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking("Porto", pastDate, "morning", items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("date");
        verify(bookingRepository, never()).save(any());
    }

    // Test 3: Create booking when capacity exceeded
    @Test
    @DisplayName("Should reject booking when municipality capacity exceeded")
    void testCreateBooking_CapacityExceeded() {
        // Given
        BulkItem item = new BulkItem("Mattress", "Old mattress", 20f, 2f);
        List<BulkItem> items = List.of(item);
        
        when(validationService.validateBulkItems(items)).thenReturn(true);
        when(validationService.validateBookingDate(futureDate)).thenReturn(true);
        when(validationService.canAcceptBooking("Porto", futureDate)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking("Porto", futureDate, "morning", items))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("capacity");
        verify(bookingRepository, never()).save(any());
    }

    // Test 5: Find booking by access token
    @Test
    @DisplayName("Should find booking by access token")
    void testFindByAccessToken_Found() {
        // Given
        String token = "TEST-TOKEN-123";
        when(bookingRepository.findByAccessToken(token)).thenReturn(Optional.of(testBooking));

        // When
        Optional<Booking> found = bookingService.findByAccessToken(token);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(testBooking);
        verify(bookingRepository).findByAccessToken(token);
    }

    // Test 6: Find booking by access token - not found
    @Test
    @DisplayName("Should return empty when token not found")
    void testFindByAccessToken_NotFound() {
        // Given
        when(bookingRepository.findByAccessToken(anyString())).thenReturn(Optional.empty());

        // When
        Optional<Booking> found = bookingService.findByAccessToken("INVALID-TOKEN");

        // Then
        assertThat(found).isEmpty();
        verify(bookingRepository).findByAccessToken("INVALID-TOKEN");
    }

    // Test 7: Get bookings by municipality
    @Test
    @DisplayName("Should get all bookings for a municipality")
    void testGetBookingsByMunicipality() {
        // Given
        Booking booking2 = new Booking("Porto", futureDate.plusDays(1), "afternoon");
        when(bookingRepository.findByMunicipality("Porto")).thenReturn(List.of(testBooking, booking2));

        // When
        List<Booking> bookings = bookingService.getBookingsByMunicipality("Porto");

        // Then
        assertThat(bookings).hasSize(2);
        assertThat(bookings).allMatch(b -> b.getMunicipality().equals("Porto"));
        verify(bookingRepository).findByMunicipality("Porto");
    }

    // Test 8: Update booking status - assign
    @Test
    @DisplayName("Should assign booking successfully")
    void testAssignBooking() {
        // Given
        Long bookingId = 1L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // When
        Booking updated = bookingService.assignBooking(bookingId);

        // Then
        assertThat(updated.getCurrentStatus()).isEqualTo(BookingStatus.ASSIGNED);
        verify(bookingRepository).findById(bookingId);
        verify(bookingRepository).save(testBooking);
    }

    // Test 9: Update booking status - start
    @Test
    @DisplayName("Should start booking successfully")
    void testStartBooking() {
        // Given
        Long bookingId = 1L;
        testBooking.setCurrentStatus(BookingStatus.ASSIGNED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // When
        Booking updated = bookingService.startBooking(bookingId);

        // Then
        assertThat(updated.getCurrentStatus()).isEqualTo(BookingStatus.IN_PROGRESS);
        verify(bookingRepository).save(testBooking);
    }

    // Test 10: Update booking status - complete
    @Test
    @DisplayName("Should complete booking successfully")
    void testCompleteBooking() {
        // Given
        Long bookingId = 1L;
        testBooking.setCurrentStatus(BookingStatus.IN_PROGRESS);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // When
        Booking updated = bookingService.completeBooking(bookingId);

        // Then
        assertThat(updated.getCurrentStatus()).isEqualTo(BookingStatus.COMPLETED);
        verify(bookingRepository).save(testBooking);
    }

    // Test 11: Update booking status - cancel
    @Test
    @DisplayName("Should cancel booking successfully")
    void testCancelBooking() {
        // Given
        Long bookingId = 1L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // When
        Booking updated = bookingService.cancelBooking(bookingId);

        // Then
        assertThat(updated.getCurrentStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(testBooking);
    }

    // Test 12: Update booking status - not found
    @Test
    @DisplayName("Should throw exception when booking not found for status update")
    void testUpdateStatus_NotFound() {
        // Given
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> bookingService.assignBooking(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
        verify(bookingRepository, never()).save(any());
    }

    // Test 13: Get bookings by municipality and status
    @Test
    @DisplayName("Should filter bookings by municipality and status")
    void testGetBookingsByMunicipalityAndStatus() {
        // Given
        when(bookingRepository.findByMunicipalityAndCurrentStatus("Porto", BookingStatus.RECEIVED))
                .thenReturn(List.of(testBooking));

        // When
        List<Booking> bookings = bookingService.getBookingsByMunicipalityAndStatus("Porto", BookingStatus.RECEIVED);

        // Then
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getCurrentStatus()).isEqualTo(BookingStatus.RECEIVED);
        verify(bookingRepository).findByMunicipalityAndCurrentStatus("Porto", BookingStatus.RECEIVED);
    }

    // Test 14: Get all bookings
    @Test
    @DisplayName("Should get all bookings")
    void testGetAllBookings() {
        // Given
        Booking booking2 = new Booking("Lisbon", futureDate, "afternoon");
        when(bookingRepository.findAll()).thenReturn(List.of(testBooking, booking2));

        // When
        List<Booking> bookings = bookingService.getAllBookings();

        // Then
        assertThat(bookings).hasSize(2);
        verify(bookingRepository).findAll();
    }

    // Test 15: Get bookings by status
    @Test
    @DisplayName("Should get bookings by status")
    void testGetBookingsByStatus() {
        // Given
        when(bookingRepository.findByCurrentStatus(BookingStatus.RECEIVED))
                .thenReturn(List.of(testBooking));

        // When
        List<Booking> bookings = bookingService.getBookingsByStatus(BookingStatus.RECEIVED);

        // Then
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getCurrentStatus()).isEqualTo(BookingStatus.RECEIVED);
        verify(bookingRepository).findByCurrentStatus(BookingStatus.RECEIVED);
    }
    
}
