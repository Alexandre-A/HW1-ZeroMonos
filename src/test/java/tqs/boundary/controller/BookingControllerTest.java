package tqs.boundary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.boundary.dto.BookingRequestDTO;
import tqs.boundary.dto.BulkItemDTO;
import tqs.data.Booking.Booking;
import tqs.data.BulkItem.BulkItem;
import tqs.service.BookingService;
import tqs.service.MunicipalityService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@DisplayName("BookingController Integration Tests")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private MunicipalityService municipalityService;

    private Booking testBooking;
    private BookingRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        // Create test booking
        testBooking = new Booking("Porto", LocalDate.now().plusDays(5), "morning");
        testBooking.addBulkItem(new BulkItem("Sofa", "Large sofa", 2.5f, 3.0f));
        testBooking.addBulkItem(new BulkItem("Table", "Wooden table", 1.2f, 1.5f));

        // Create valid request DTO
        List<BulkItemDTO> items = new ArrayList<>();
        items.add(new BulkItemDTO("Sofa", "Large sofa", 2.5f, 3.0f));
        items.add(new BulkItemDTO("Table", "Wooden table", 1.2f, 1.5f));

        validRequest = new BookingRequestDTO();
        validRequest.setMunicipality("Porto");
        validRequest.setCollectionDate(LocalDate.now().plusDays(5));
        validRequest.setTimeSlot("morning");
        validRequest.setItems(items);
    }

    @Test
    @DisplayName("POST /api/bookings - Should create booking successfully")
    void testCreateBooking_Success() throws Exception {
        when(municipalityService.isValidMunicipality(anyString())).thenReturn(true);
        when(bookingService.createBooking(anyString(), any(LocalDate.class), anyString(), anyList()))
                .thenReturn(testBooking);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.municipality").value("Porto"))
                .andExpect(jsonPath("$.currentStatus").value("RECEIVED"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.collectionDate").exists())
                .andExpect(jsonPath("$.timeSlot").value("morning"));

        verify(municipalityService).isValidMunicipality(anyString());
        verify(bookingService).createBooking(anyString(), any(LocalDate.class), anyString(), anyList());
    }

    @Test
    @DisplayName("POST /api/bookings - Should return 400 when municipality is blank")
    void testCreateBooking_BlankMunicipality() throws Exception {
        validRequest.setMunicipality("");

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/bookings - Should return 400 when collection date is in the past")
    void testCreateBooking_PastDate() throws Exception {
        validRequest.setCollectionDate(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/bookings - Should return 400 when time slot is invalid")
    void testCreateBooking_InvalidTimeSlot() throws Exception {
        validRequest.setTimeSlot("invalid-slot");

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/bookings - Should return 400 when items list is empty")
    void testCreateBooking_EmptyItems() throws Exception {
        validRequest.setItems(new ArrayList<>());

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/bookings - Should return 409 when capacity is exceeded")
    void testCreateBooking_CapacityExceeded() throws Exception {
        when(municipalityService.isValidMunicipality(anyString())).thenReturn(true);
        when(bookingService.createBooking(anyString(), any(LocalDate.class), anyString(), anyList()))
                .thenThrow(new IllegalStateException("Municipality has reached booking capacity for this date"));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Municipality has reached booking capacity for this date"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /api/bookings/{token} - Should return booking by token")
    void testGetBookingByToken_Success() throws Exception {
        String token = testBooking.getAccessToken();
        when(bookingService.findByAccessToken(token)).thenReturn(Optional.of(testBooking));

        mockMvc.perform(get("/api/bookings/{token}", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.municipality").value("Porto"))
                .andExpect(jsonPath("$.currentStatus").value("RECEIVED"))
                .andExpect(jsonPath("$.accessToken").value(token));

        verify(bookingService).findByAccessToken(token);
    }

    @Test
    @DisplayName("GET /api/bookings/{token} - Should return 404 when token not found")
    void testGetBookingByToken_NotFound() throws Exception {
        String invalidToken = "invalid-token";
        when(bookingService.findByAccessToken(invalidToken)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/bookings/{token}", invalidToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Booking not found with token: " + invalidToken))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/bookings/{token}/details - Should return detailed booking info")
    void testGetBookingDetails_Success() throws Exception {
        // Transition booking through states to create history
        testBooking.assign();
        testBooking.start();

        String token = testBooking.getAccessToken();
        when(bookingService.findByAccessToken(token)).thenReturn(Optional.of(testBooking));

        mockMvc.perform(get("/api/bookings/{token}/details", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.municipality").value("Porto"))
                .andExpect(jsonPath("$.currentStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.statusHistory").isArray())
                .andExpect(jsonPath("$.statusHistory", hasSize(3))) // RECEIVED, ASSIGNED, IN_PROGRESS
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(2)));

        verify(bookingService).findByAccessToken(token);
    }

    @Test
    @DisplayName("GET /api/bookings/{token}/details - Should return 404 when token not found")
    void testGetBookingDetails_NotFound() throws Exception {
        String invalidToken = "invalid-token";
        when(bookingService.findByAccessToken(invalidToken)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/bookings/{token}/details", invalidToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Booking not found with token: " + invalidToken));
    }

    @Test
    @DisplayName("PUT /api/bookings/{token}/cancel - Should cancel booking successfully")
    void testCancelBooking_Success() throws Exception {
        String token = testBooking.getAccessToken();
        Booking cancelledBooking = new Booking("Porto", LocalDate.now().plusDays(5), "morning");
        cancelledBooking.cancel();

        when(bookingService.findByAccessToken(token)).thenReturn(Optional.of(testBooking));
        when(bookingService.cancelBooking(any())).thenReturn(cancelledBooking);

        mockMvc.perform(put("/api/bookings/{token}/cancel", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("CANCELLED"))
                .andExpect(jsonPath("$.municipality").value("Porto"));

        verify(bookingService).findByAccessToken(token);
    }

    @Test
    @DisplayName("PUT /api/bookings/{token}/cancel - Should return 404 when token not found")
    void testCancelBooking_NotFound() throws Exception {
        String invalidToken = "invalid-token";
        when(bookingService.findByAccessToken(invalidToken)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/bookings/{token}/cancel", invalidToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Booking not found with token: " + invalidToken));
    }

    @Test
    @DisplayName("PUT /api/bookings/{token}/cancel - Should return 400 when trying to cancel from invalid state")
    void testCancelBooking_InvalidStateTransition() throws Exception {
        String token = testBooking.getAccessToken();

        when(bookingService.findByAccessToken(token)).thenReturn(Optional.of(testBooking));
        when(bookingService.cancelBooking(any()))
                .thenThrow(new tqs.data.state.InvalidStateTransitionException(
                        "COMPLETED", "cancel"));

        mockMvc.perform(put("/api/bookings/{token}/cancel", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }
}
