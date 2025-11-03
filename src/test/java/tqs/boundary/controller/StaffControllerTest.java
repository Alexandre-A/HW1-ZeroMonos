package tqs.boundary.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.data.Booking.Booking;
import tqs.data.BookingStatus;
import tqs.data.BulkItem.BulkItem;
import tqs.service.BookingService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StaffController.class)
@DisplayName("StaffController Integration Tests")
class StaffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    private Booking pendingBooking;
    private Booking assignedBooking;
    private Booking inProgressBooking;
    private List<Booking> allBookings;

    @BeforeEach
    void setUp() throws Exception {
        // Create test bookings with different states
        pendingBooking = new Booking("Porto", LocalDate.now().plusDays(5), "morning");
        pendingBooking.addBulkItem(new BulkItem("Sofa", "Large sofa", 2.5f, 3.0f));
        setId(pendingBooking, 1L);

        assignedBooking = new Booking("Lisboa", LocalDate.now().plusDays(6), "afternoon");
        assignedBooking.addBulkItem(new BulkItem("Table", "Wooden table", 1.2f, 1.5f));
        assignedBooking.assign();
        setId(assignedBooking, 2L);

        inProgressBooking = new Booking("Porto", LocalDate.now().plusDays(7), "evening");
        inProgressBooking.addBulkItem(new BulkItem("Chair", "Office chair", 0.5f, 0.8f));
        inProgressBooking.assign();
        inProgressBooking.start();
        setId(inProgressBooking, 3L);

        allBookings = Arrays.asList(pendingBooking, assignedBooking, inProgressBooking);
    }

    // Helper method to set ID using reflection
    private void setId(Booking booking, Long id) throws Exception {
        var field = Booking.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(booking, id);
    }

    @Test
    @DisplayName("GET /api/staff/bookings - Should return all bookings")
    void testGetAllBookings_Success() throws Exception {
        when(bookingService.getAllBookings()).thenReturn(allBookings);

        mockMvc.perform(get("/api/staff/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].currentStatus").value("RECEIVED"))
                .andExpect(jsonPath("$[1].currentStatus").value("ASSIGNED"))
                .andExpect(jsonPath("$[2].currentStatus").value("IN_PROGRESS"));

        verify(bookingService).getAllBookings();
    }

    @Test
    @DisplayName("GET /api/staff/bookings - Should return empty list when no bookings exist")
    void testGetAllBookings_EmptyList() throws Exception {
        when(bookingService.getAllBookings()).thenReturn(List.of());

        mockMvc.perform(get("/api/staff/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/staff/bookings/{id} - Should return booking details by ID")
    void testGetBookingById_Success() throws Exception {
        // Add more state transitions to create history
        Booking detailedBooking = new Booking("Porto", LocalDate.now().plusDays(5), "morning");
        setId(detailedBooking, 10L); // Set ID for filtering
        detailedBooking.addBulkItem(new BulkItem("Sofa", "Large sofa", 2.5f, 3.0f));
        detailedBooking.addBulkItem(new BulkItem("Table", "Wooden table", 1.2f, 1.5f));
        detailedBooking.assign();
        detailedBooking.start();

        when(bookingService.getAllBookings()).thenReturn(List.of(detailedBooking));

        mockMvc.perform(get("/api/staff/bookings/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.municipality").value("Porto"))
                .andExpect(jsonPath("$.currentStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.statusHistory").isArray())
                .andExpect(jsonPath("$.statusHistory", hasSize(3)))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/staff/bookings/{id} - Should return 404 when ID not found")
    void testGetBookingById_NotFound() throws Exception {
        when(bookingService.getAllBookings()).thenReturn(allBookings);

        mockMvc.perform(get("/api/staff/bookings/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Booking not found with ID: 999"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/staff/bookings/municipality/{municipality} - Should return bookings for municipality")
    void testGetBookingsByMunicipality_Success() throws Exception {
        List<Booking> portoBookings = Arrays.asList(pendingBooking, inProgressBooking);
        when(bookingService.getBookingsByMunicipality("Porto")).thenReturn(portoBookings);

        mockMvc.perform(get("/api/staff/bookings/municipality/{municipality}", "Porto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].municipality").value("Porto"))
                .andExpect(jsonPath("$[1].municipality").value("Porto"));

        verify(bookingService).getBookingsByMunicipality("Porto");
    }

    @Test
    @DisplayName("GET /api/staff/bookings/municipality/{municipality} - Should return empty list for municipality with no bookings")
    void testGetBookingsByMunicipality_Empty() throws Exception {
        when(bookingService.getBookingsByMunicipality("Coimbra")).thenReturn(List.of());

        mockMvc.perform(get("/api/staff/bookings/municipality/{municipality}", "Coimbra"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/staff/bookings/status/{status} - Should return bookings by status")
    void testGetBookingsByStatus_Success() throws Exception {
        when(bookingService.getBookingsByStatus(BookingStatus.RECEIVED))
                .thenReturn(List.of(pendingBooking));

        mockMvc.perform(get("/api/staff/bookings/status/{status}", "RECEIVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].currentStatus").value("RECEIVED"));

        verify(bookingService).getBookingsByStatus(BookingStatus.RECEIVED);
    }

    @Test
    @DisplayName("GET /api/staff/bookings/status/{status} - Should handle multiple bookings with same status")
    void testGetBookingsByStatus_Multiple() throws Exception {
        Booking anotherPending = new Booking("Braga", LocalDate.now().plusDays(8), "morning");
        anotherPending.addBulkItem(new BulkItem("Desk", "Office desk", 1.5f, 2.0f));

        when(bookingService.getBookingsByStatus(BookingStatus.RECEIVED))
                .thenReturn(Arrays.asList(pendingBooking, anotherPending));

        mockMvc.perform(get("/api/staff/bookings/status/{status}", "RECEIVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].currentStatus").value("RECEIVED"))
                .andExpect(jsonPath("$[1].currentStatus").value("RECEIVED"));
    }

    @Test
    @DisplayName("GET /api/staff/bookings/municipality/{municipality}/status/{status} - Should filter by both criteria")
    void testGetBookingsByMunicipalityAndStatus_Success() throws Exception {
        when(bookingService.getBookingsByMunicipalityAndStatus("Porto", BookingStatus.RECEIVED))
                .thenReturn(List.of(pendingBooking));

        mockMvc.perform(get("/api/staff/bookings/municipality/{municipality}/status/{status}", 
                        "Porto", "RECEIVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].municipality").value("Porto"))
                .andExpect(jsonPath("$[0].currentStatus").value("RECEIVED"));

        verify(bookingService).getBookingsByMunicipalityAndStatus("Porto", BookingStatus.RECEIVED);
    }

    @Test
    @DisplayName("PUT /api/staff/bookings/{id}/assign - Should assign booking successfully")
    void testAssignBooking_Success() throws Exception {
        Booking assigned = new Booking("Porto", LocalDate.now().plusDays(5), "morning");
        assigned.assign();

        when(bookingService.assignBooking(1L)).thenReturn(assigned);

        mockMvc.perform(put("/api/staff/bookings/{id}/assign", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("ASSIGNED"))
                .andExpect(jsonPath("$.municipality").value("Porto"));

        verify(bookingService).assignBooking(1L);
    }

    @Test
    @DisplayName("PUT /api/staff/bookings/{id}/assign - Should return 404 when booking not found")
    void testAssignBooking_NotFound() throws Exception {
        when(bookingService.assignBooking(999L))
                .thenThrow(new IllegalArgumentException("Booking not found with id: 999"));

        mockMvc.perform(put("/api/staff/bookings/{id}/assign", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Booking not found with id: 999"));
    }

    @Test
    @DisplayName("PUT /api/staff/bookings/{id}/assign - Should return 400 when invalid state transition")
    void testAssignBooking_InvalidStateTransition() throws Exception {
        when(bookingService.assignBooking(1L))
                .thenThrow(new tqs.data.state.InvalidStateTransitionException(
                        "COMPLETED", "assign"));

        mockMvc.perform(put("/api/staff/bookings/{id}/assign", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("PUT /api/staff/bookings/{id}/start - Should start collection successfully")
    void testStartCollection_Success() throws Exception {
        Booking started = new Booking("Porto", LocalDate.now().plusDays(5), "afternoon");
        started.assign();
        started.start();

        when(bookingService.startBooking(1L)).thenReturn(started);

        mockMvc.perform(put("/api/staff/bookings/{id}/start", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("IN_PROGRESS"));

        verify(bookingService).startBooking(1L);
    }

    @Test
    @DisplayName("PUT /api/staff/bookings/{id}/start - Should return 404 when booking not found")
    void testStartCollection_NotFound() throws Exception {
        when(bookingService.startBooking(999L))
                .thenThrow(new IllegalArgumentException("Booking not found with id: 999"));

        mockMvc.perform(put("/api/staff/bookings/{id}/start", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/staff/bookings/{id}/complete - Should complete collection successfully")
    void testCompleteCollection_Success() throws Exception {
        Booking completed = new Booking("Porto", LocalDate.now().plusDays(5), "evening");
        completed.assign();
        completed.start();
        completed.complete();

        when(bookingService.completeBooking(1L)).thenReturn(completed);

        mockMvc.perform(put("/api/staff/bookings/{id}/complete", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("COMPLETED"));

        verify(bookingService).completeBooking(1L);
    }

    @Test
    @DisplayName("PUT /api/staff/bookings/{id}/complete - Should return 404 when booking not found")
    void testCompleteCollection_NotFound() throws Exception {
        when(bookingService.completeBooking(999L))
                .thenThrow(new IllegalArgumentException("Booking not found with id: 999"));

        mockMvc.perform(put("/api/staff/bookings/{id}/complete", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/staff/bookings/{id}/cancel - Should cancel booking successfully")
    void testCancelBooking_Success() throws Exception {
        Booking cancelled = new Booking("Porto", LocalDate.now().plusDays(5), "morning");
        cancelled.cancel();

        when(bookingService.cancelBooking(1L)).thenReturn(cancelled);

        mockMvc.perform(put("/api/staff/bookings/{id}/cancel", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("CANCELLED"));

        verify(bookingService).cancelBooking(1L);
    }

    @Test
    @DisplayName("PUT /api/staff/bookings/{id}/cancel - Should return 404 when booking not found")
    void testCancelBooking_NotFound() throws Exception {
        when(bookingService.cancelBooking(999L))
                .thenThrow(new IllegalArgumentException("Booking not found with id: 999"));

        mockMvc.perform(put("/api/staff/bookings/{id}/cancel", 999L))
                .andExpect(status().isNotFound());
    }
}
