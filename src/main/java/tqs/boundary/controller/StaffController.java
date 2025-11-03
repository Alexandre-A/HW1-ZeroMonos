package tqs.boundary.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.boundary.dto.BookingDetailedResponseDTO;
import tqs.data.Booking.Booking;
import tqs.data.BookingStatus;
import tqs.service.BookingService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * REST Controller for staff operations
 * Handles booking state transitions and listing
 */
@RestController
@RequestMapping("/api/staff/bookings")
public class StaffController {

    private static final Logger logger = LoggerFactory.getLogger(StaffController.class);

    private final BookingService bookingService;

    public StaffController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * List all bookings
     * GET /api/staff/bookings
     */
    @GetMapping
    public ResponseEntity<List<BookingDetailedResponseDTO>> getAllBookings() {
        logger.debug("Listing all bookings");

        List<BookingDetailedResponseDTO> bookings = bookingService.getAllBookings().stream()
                .map(b -> BookingDetailedResponseDTO.fromEntity(b, false))
                .toList();

        return ResponseEntity.ok(bookings);
    }

    /**
     * Get booking details by ID (with status history)
     * GET /api/staff/bookings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingDetailedResponseDTO> getBookingById(@PathVariable Long id) {
        logger.debug("Finding booking with ID: {}", id);

        // Service methods throw IllegalArgumentException if not found
        // This is caught by GlobalExceptionHandler and returns 404
        Booking booking = bookingService.getAllBookings().stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + id));

        BookingDetailedResponseDTO response = BookingDetailedResponseDTO.fromEntity(booking);
        return ResponseEntity.ok(response);
    }

    /**
     * Summary of bookings for operations dashboard
     * GET /api/staff/bookings/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        logger.debug("Generating bookings summary for dashboard");

        List<Booking> bookings = bookingService.getAllBookings();

        Map<String, Long> byStatus = bookings.stream()
                .collect(Collectors.groupingBy(b -> b.getCurrentStatus().name(), Collectors.counting()));

        Map<String, Long> byMunicipality = bookings.stream()
                .collect(Collectors.groupingBy(Booking::getMunicipality, Collectors.counting()));

        Map<String, Object> result = new HashMap<>();
        result.put("total", bookings.size());
        result.put("byStatus", byStatus);
        result.put("byMunicipality", byMunicipality);

        return ResponseEntity.ok(result);
    }

    /**
     * Get bookings by municipality
     * GET /api/staff/bookings/municipality/{municipality}
     */
    @GetMapping("/municipality/{municipality}")
    public ResponseEntity<List<BookingDetailedResponseDTO>> getBookingsByMunicipality(@PathVariable String municipality) {
        logger.debug("Finding bookings for municipality: {}", municipality);

        List<BookingDetailedResponseDTO> bookings = bookingService.getBookingsByMunicipality(municipality).stream()
                .map(b -> BookingDetailedResponseDTO.fromEntity(b, false))
                .toList();

        return ResponseEntity.ok(bookings);
    }

    /**
     * Get bookings by status
     * GET /api/staff/bookings/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookingDetailedResponseDTO>> getBookingsByStatus(@PathVariable BookingStatus status) {
        logger.debug("Finding bookings with status: {}", status);

        List<BookingDetailedResponseDTO> bookings = bookingService.getBookingsByStatus(status).stream()
                .map(b -> BookingDetailedResponseDTO.fromEntity(b, false))
                .toList();

        return ResponseEntity.ok(bookings);
    }

    /**
     * Get bookings by municipality and status
     * GET /api/staff/bookings/municipality/{municipality}/status/{status}
     */
    @GetMapping("/municipality/{municipality}/status/{status}")
    public ResponseEntity<List<BookingDetailedResponseDTO>> getBookingsByMunicipalityAndStatus(
            @PathVariable String municipality, 
            @PathVariable BookingStatus status) {
        logger.debug("Finding bookings for municipality: {} with status: {}", municipality, status);

        List<BookingDetailedResponseDTO> bookings = bookingService.getBookingsByMunicipalityAndStatus(municipality, status).stream()
                .map(b -> BookingDetailedResponseDTO.fromEntity(b, false))
                .toList();

        return ResponseEntity.ok(bookings);
    }

    /**
     * Assign a booking to a collection team
     * PUT /api/staff/bookings/{id}/assign
     */
    @PutMapping("/{id}/assign")
    public ResponseEntity<BookingDetailedResponseDTO> assignBooking(@PathVariable Long id) {
        logger.info("Assigning booking with ID: {}", id);

        // Service handles "not found" internally
        Booking assigned = bookingService.assignBooking(id);
        BookingDetailedResponseDTO response = BookingDetailedResponseDTO.fromEntity(assigned, false);

        return ResponseEntity.ok(response);
    }

    /**
     * Start collection for a booking
     * PUT /api/staff/bookings/{id}/start
     */
    @PutMapping("/{id}/start")
    public ResponseEntity<BookingDetailedResponseDTO> startCollection(@PathVariable Long id) {
        logger.info("Starting collection for booking with ID: {}", id);

        Booking started = bookingService.startBooking(id);
        BookingDetailedResponseDTO response = BookingDetailedResponseDTO.fromEntity(started, false);

        return ResponseEntity.ok(response);
    }

    /**
     * Complete collection for a booking
     * PUT /api/staff/bookings/{id}/complete
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<BookingDetailedResponseDTO> completeCollection(@PathVariable Long id) {
        logger.info("Completing collection for booking with ID: {}", id);

        Booking completed = bookingService.completeBooking(id);
        BookingDetailedResponseDTO response = BookingDetailedResponseDTO.fromEntity(completed, false);

        return ResponseEntity.ok(response);
    }

    /**
     * Cancel a booking (staff side)
     * PUT /api/staff/bookings/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingDetailedResponseDTO> cancelBooking(@PathVariable Long id) {
        logger.info("Cancelling booking with ID: {}", id);

        Booking cancelled = bookingService.cancelBooking(id);
        BookingDetailedResponseDTO response = BookingDetailedResponseDTO.fromEntity(cancelled, false);

        return ResponseEntity.ok(response);
    }
}
