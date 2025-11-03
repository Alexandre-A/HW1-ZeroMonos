package tqs.boundary.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.boundary.dto.BookingDetailedResponseDTO;
import tqs.boundary.dto.BookingRequestDTO;
import tqs.boundary.dto.BulkItemDTO;
import tqs.data.Booking.Booking;
import tqs.data.BulkItem.BulkItem;
import tqs.service.BookingService;
import tqs.service.MunicipalityService;

import java.util.List;

/**
 * REST Controller for citizen-facing booking operations
 * Handles booking creation and status checking
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    private static final String BOOKING_NOT_FOUND = "Booking not found with token: ";

    private final BookingService bookingService;
    private final MunicipalityService municipalityService;

    public BookingController(BookingService bookingService, MunicipalityService municipalityService) {
        this.bookingService = bookingService;
        this.municipalityService = municipalityService;
    }

    /**
     * Get list of available municipalities
     * GET /api/bookings/municipalities
     */
    @GetMapping("/municipalities")
    public ResponseEntity<List<String>> getAvailableMunicipalities() {
        logger.debug("Fetching available municipalities");
        List<String> municipalities = municipalityService.getAvailableMunicipalities();
        return ResponseEntity.ok(municipalities);
    }

    /**
     * Create a new booking
     * POST /api/bookings
     */
    @PostMapping
    public ResponseEntity<BookingDetailedResponseDTO> createBooking(@Valid @RequestBody BookingRequestDTO request) {
        logger.info("Received booking creation request");

        // Validate municipality
        if (!municipalityService.isValidMunicipality(request.getMunicipality())) {
            logger.warn("Invalid municipality in booking request");
            throw new IllegalArgumentException("Invalid municipality. Please select a valid Portuguese municipality.");
        }

        // Convert DTOs to entities
        List<BulkItem> items = request.getItems().stream()
                .map(BulkItemDTO::toEntity)
                .toList();

        // Create booking
        Booking booking = bookingService.createBooking(
                request.getMunicipality(),
                request.getCollectionDate(),
                request.getTimeSlot(),
                items
        );

        // Convert to DTO (without status history for creation response)
        BookingDetailedResponseDTO response = BookingDetailedResponseDTO.fromEntity(booking, false);

        logger.info("Booking created successfully with ID: {}", booking.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get booking by access token
     * GET /api/bookings/{token}
     */
    @GetMapping("/{token}")
    public ResponseEntity<BookingDetailedResponseDTO> getBookingByToken(@PathVariable String token) {
        logger.debug("Finding booking with token: {}", token);

        Booking booking = bookingService.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException(BOOKING_NOT_FOUND + token));

        // Return without status history (simple view)
        BookingDetailedResponseDTO response = BookingDetailedResponseDTO.fromEntity(booking, false);
        return ResponseEntity.ok(response);
    }

    /**
     * Get detailed booking information (with status history)
     * GET /api/bookings/{token}/details
     */
    @GetMapping("/{token}/details")
    public ResponseEntity<BookingDetailedResponseDTO> getBookingDetails(@PathVariable String token) {
        logger.debug("Finding booking details for token: {}", token);

        Booking booking = bookingService.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException(BOOKING_NOT_FOUND + token));

        BookingDetailedResponseDTO response = BookingDetailedResponseDTO.fromEntity(booking);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel a booking (changes state to CANCELLED)
     * PUT /api/bookings/{token}/cancel
     */
    @PutMapping("/{token}/cancel")
    public ResponseEntity<BookingDetailedResponseDTO> cancelBooking(@PathVariable String token) {
        logger.info("Received booking cancellation request");

        Booking booking = bookingService.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException(BOOKING_NOT_FOUND + token));

        Booking cancelled = bookingService.cancelBooking(booking.getId());
        BookingDetailedResponseDTO response = BookingDetailedResponseDTO.fromEntity(cancelled, false);

        logger.info("Booking cancelled successfully");
        return ResponseEntity.ok(response);
    }
}
