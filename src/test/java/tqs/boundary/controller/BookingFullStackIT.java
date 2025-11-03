package tqs.boundary.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import tqs.boundary.dto.BookingDetailedResponseDTO;
import tqs.boundary.dto.BookingRequestDTO;
import tqs.boundary.dto.BookingDetailedResponseDTO;
import tqs.boundary.dto.BulkItemDTO;
import tqs.data.Booking.Booking;
import tqs.data.Booking.BookingRepository;
import tqs.data.BookingStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-Stack Integration Tests for Booking System
 * 
 * Tests the ENTIRE application stack:
 * - Real HTTP requests (TestRestTemplate)
 * - Real controllers
 * - Real services
 * - Real repositories
 * - Real database (H2 in-memory)
 * 
 * Unlike @WebMvcTest, NO mocks are used!
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Booking Full-Stack Integration Tests")
class BookingFullStackIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Should create booking and persist to database (full stack)")
    void whenCreateBooking_thenPersistToDatabase() {
        // Arrange
        BookingRequestDTO request = createValidBookingRequest("Porto");

        // Act - Make real HTTP POST request
        ResponseEntity<BookingDetailedResponseDTO> response = restTemplate.postForEntity(
                "/api/bookings",
                request,
                BookingDetailedResponseDTO.class
        );

        // Assert HTTP response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMunicipality()).isEqualTo("Porto");
        assertThat(response.getBody().getCurrentStatus()).isEqualTo("RECEIVED");
        assertThat(response.getBody().getAccessToken()).isNotNull();

        // Assert database state (real DB query!)
        List<Booking> bookingsInDb = bookingRepository.findAll();
        assertThat(bookingsInDb).hasSize(1);
        assertThat(bookingsInDb.get(0).getMunicipality()).isEqualTo("Porto");
        assertThat(bookingsInDb.get(0).getCurrentStatus()).isEqualTo(BookingStatus.RECEIVED);
    }

    @Test
    @DisplayName("Should retrieve booking by token from database (full stack)")
    void whenGetBookingByToken_thenReturnBookingFromDatabase() {
        // Arrange - Create a booking first
        BookingRequestDTO request = createValidBookingRequest("Lisboa");
        ResponseEntity<BookingDetailedResponseDTO> createResponse = restTemplate.postForEntity(
                "/api/bookings",
                request,
                BookingDetailedResponseDTO.class
        );
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        String token = createResponse.getBody().getAccessToken();
        assertThat(token).isNotNull();

        // Verify booking exists in database
        List<Booking> bookingsInDb = bookingRepository.findAll();
        assertThat(bookingsInDb).hasSize(1);
        assertThat(bookingsInDb.get(0).getAccessToken()).isEqualTo(token);

        // Act - Make real HTTP GET request (try both String and Map response)
        ResponseEntity<String> errorResponse = restTemplate.getForEntity(
                "/api/bookings/" + token,
                String.class
        );
        
        // Debug: Print what we got
        System.out.println("Response status: " + errorResponse.getStatusCode());
        System.out.println("Response body: " + errorResponse.getBody());
        
        ResponseEntity<BookingDetailedResponseDTO> response = restTemplate.getForEntity(
                "/api/bookings/" + token,
                BookingDetailedResponseDTO.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMunicipality()).isEqualTo("Lisboa");
        assertThat(response.getBody().getAccessToken()).isEqualTo(token);
    }

    @Test
    @DisplayName("Should return 404 for non-existent token")
    void whenGetBookingWithInvalidToken_thenReturn404() {
        // Act
        ResponseEntity<BookingDetailedResponseDTO> response = restTemplate.getForEntity(
                "/api/bookings/invalid-token-123",
                BookingDetailedResponseDTO.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should retrieve detailed booking with status history (full stack)")
    void whenGetBookingDetails_thenReturnCompleteHistory() {
        // Arrange - Create booking
        BookingRequestDTO request = createValidBookingRequest("Coimbra");
        ResponseEntity<BookingDetailedResponseDTO> createResponse = restTemplate.postForEntity(
                "/api/bookings",
                request,
                BookingDetailedResponseDTO.class
        );
        String token = createResponse.getBody().getAccessToken();

        // Act - Get detailed booking info
        ResponseEntity<BookingDetailedResponseDTO> response = restTemplate.getForEntity(
                "/api/bookings/" + token + "/details",
                BookingDetailedResponseDTO.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMunicipality()).isEqualTo("Coimbra");
        assertThat(response.getBody().getStatusHistory()).hasSize(1);  // RECEIVED
        assertThat(response.getBody().getStatusHistory().get(0).getStatus()).isEqualTo("RECEIVED");
        assertThat(response.getBody().getItems()).hasSize(2);
    }

    @Test
    @DisplayName("Should cancel booking and update in database (full stack)")
    void whenCancelBooking_thenUpdateDatabaseState() {
        // Arrange - Create booking
        BookingRequestDTO request = createValidBookingRequest("Braga");
        ResponseEntity<BookingDetailedResponseDTO> createResponse = restTemplate.postForEntity(
                "/api/bookings",
                request,
                BookingDetailedResponseDTO.class
        );
        String token = createResponse.getBody().getAccessToken();

        // Act - Cancel booking via HTTP PUT
        restTemplate.put("/api/bookings/" + token + "/cancel", null);

        // Assert - Verify database state changed
        List<Booking> bookingsInDb = bookingRepository.findAll();
        assertThat(bookingsInDb).hasSize(1);
        assertThat(bookingsInDb.get(0).getCurrentStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should list all bookings (staff endpoint - full stack)")
    void whenGetAllBookings_thenReturnAllFromDatabase() {
        // Arrange - Create multiple bookings
        restTemplate.postForEntity("/api/bookings", createValidBookingRequest("Porto"), BookingDetailedResponseDTO.class);
        restTemplate.postForEntity("/api/bookings", createValidBookingRequest("Lisboa"), BookingDetailedResponseDTO.class);
        restTemplate.postForEntity("/api/bookings", createValidBookingRequest("Faro"), BookingDetailedResponseDTO.class);

        // Act - Get all bookings via staff endpoint
        ResponseEntity<List<BookingDetailedResponseDTO>> response = restTemplate.exchange(
                "/api/staff/bookings",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BookingDetailedResponseDTO>>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(3);
        assertThat(response.getBody())
                .extracting(BookingDetailedResponseDTO::getMunicipality)
                .containsExactlyInAnyOrder("Porto", "Lisboa", "Faro");
    }

    @Test
    @DisplayName("Should filter bookings by municipality (full stack)")
    void whenGetBookingsByMunicipality_thenReturnFilteredResults() {
        // Arrange
        restTemplate.postForEntity("/api/bookings", createValidBookingRequest("Porto"), BookingDetailedResponseDTO.class);
        restTemplate.postForEntity("/api/bookings", createValidBookingRequest("Porto"), BookingDetailedResponseDTO.class);
        restTemplate.postForEntity("/api/bookings", createValidBookingRequest("Lisboa"), BookingDetailedResponseDTO.class);

        // Act
        ResponseEntity<List<BookingDetailedResponseDTO>> response = restTemplate.exchange(
                "/api/staff/bookings/municipality/Porto",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BookingDetailedResponseDTO>>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody())
                .allMatch(booking -> booking.getMunicipality().equals("Porto"));
    }

    @Test
    @DisplayName("Should filter bookings by status (full stack)")
    void whenGetBookingsByStatus_thenReturnFilteredResults() {
        // Arrange - Create two bookings
        ResponseEntity<BookingDetailedResponseDTO> booking1 = restTemplate.postForEntity(
                "/api/bookings",
                createValidBookingRequest("Porto"),
                BookingDetailedResponseDTO.class
        );
        restTemplate.postForEntity(
                "/api/bookings",
                createValidBookingRequest("Lisboa"),
                BookingDetailedResponseDTO.class
        );

        // Cancel one booking
        restTemplate.put("/api/bookings/" + booking1.getBody().getAccessToken() + "/cancel", null);

        // Act - Get bookings by status
        ResponseEntity<List<BookingDetailedResponseDTO>> receivedResponse = restTemplate.exchange(
                "/api/staff/bookings/status/RECEIVED",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BookingDetailedResponseDTO>>() {}
        );

        ResponseEntity<List<BookingDetailedResponseDTO>> cancelledResponse = restTemplate.exchange(
                "/api/staff/bookings/status/CANCELLED",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BookingDetailedResponseDTO>>() {}
        );

        // Assert
        assertThat(receivedResponse.getBody()).hasSize(1);
        assertThat(receivedResponse.getBody().get(0).getCurrentStatus()).isEqualTo("RECEIVED");

        assertThat(cancelledResponse.getBody()).hasSize(1);
        assertThat(cancelledResponse.getBody().get(0).getCurrentStatus()).isEqualTo("CANCELLED");
    }

    /**
     * Helper method to create a valid booking request DTO
     */
    private BookingRequestDTO createValidBookingRequest(String municipality) {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setMunicipality(municipality);
        request.setCollectionDate(LocalDate.now().plusDays(5));
        request.setTimeSlot("morning");

        List<BulkItemDTO> items = new ArrayList<>();
        items.add(new BulkItemDTO("Sofa", "Large sofa", 2.5f, 3.0f));
        items.add(new BulkItemDTO("Table", "Wooden table", 1.2f, 1.5f));
        request.setItems(items);

        return request;
    }
}
