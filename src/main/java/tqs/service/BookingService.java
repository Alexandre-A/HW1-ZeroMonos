package tqs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tqs.data.Booking.Booking;
import tqs.data.Booking.BookingRepository;
import tqs.data.BookingStatus;
import tqs.data.BulkItem.BulkItem;
import tqs.data.StatusHistory.StatusHistoryRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for orchestrating booking operations
 * This service orchestrates business logic, delegates validation to BookingValidationService
 */
@Service
@Transactional
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final BookingValidationService validationService;

    public BookingService(BookingRepository bookingRepository,
                          StatusHistoryRepository statusHistoryRepository,
                          BookingValidationService validationService) {
        this.bookingRepository = bookingRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.validationService = validationService;
    }

    public Booking createBooking(String municipality, LocalDate collectionDate, String timeSlot, List<BulkItem> items) {
        logger.info("Creating booking for a specific municipality and date");

        // Validate items (must have at least one)
        if (!validationService.validateBulkItems(items)) {
            logger.warn("Cannot create booking without items");
            throw new IllegalArgumentException("At least one bulk item is required for booking");
        }

        // Delegate validation to validation service
        if (!validationService.validateBookingDate(collectionDate)) {
            logger.warn("Invalid booking date");
            throw new IllegalArgumentException("Collection date must be at least 1 day in the future and within 90 days");
        }

        // Check capacity
        if (!validationService.canAcceptBooking(municipality, collectionDate)) {
            logger.warn("Capacity exceeded for municipality on the specified date");
            throw new IllegalStateException("Municipality has reached booking capacity for this date");
        }

        // Create booking entity
        Booking booking = new Booking(municipality, collectionDate, timeSlot);
        
        // Add bulk items 
        items.forEach(booking::addBulkItem);

        Booking saved = bookingRepository.save(booking);
        logger.info("Booking created with token: {}", saved.getAccessToken());
        
        return saved;
    }


    public Optional<Booking> findByAccessToken(String accessToken) {
        logger.debug("Finding booking by token: {}", accessToken);
        return bookingRepository.findByAccessToken(accessToken);
    }

 
    public List<Booking> getBookingsByMunicipality(String municipality) {
        logger.debug("Finding bookings for municipality: {}", municipality);
        return bookingRepository.findByMunicipality(municipality);
    }


    public List<Booking> getBookingsByMunicipalityAndStatus(String municipality, BookingStatus status) {
        logger.debug("Finding bookings for municipality: {} with status: {}", municipality, status);
        return bookingRepository.findByMunicipalityAndCurrentStatus(municipality, status);
    }


    public Booking assignBooking(Long bookingId) {
        logger.info("Assigning booking: {}", bookingId);
        Booking booking = getBookingOrThrow(bookingId);
        booking.assign();
        return bookingRepository.save(booking);
    }


    public Booking startBooking(Long bookingId) {
        logger.info("Starting booking: {}", bookingId);
        Booking booking = getBookingOrThrow(bookingId);
        booking.start();
        return bookingRepository.save(booking);
    }


    public Booking completeBooking(Long bookingId) {
        logger.info("Completing booking: {}", bookingId);
        Booking booking = getBookingOrThrow(bookingId);
        booking.complete();
        return bookingRepository.save(booking);
    }


    public Booking cancelBooking(Long bookingId) {
        logger.info("Cancelling booking: {}", bookingId);
        Booking booking = getBookingOrThrow(bookingId);
        booking.cancel();
        return bookingRepository.save(booking);
    }


    public List<Booking> getAllBookings() {
        logger.debug("Finding all bookings");
        return bookingRepository.findAll();
    }


    public List<Booking> getBookingsByStatus(BookingStatus status) {
        logger.debug("Finding bookings with status: {}", status);
        return bookingRepository.findByCurrentStatus(status);
    }


    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));
    }
}