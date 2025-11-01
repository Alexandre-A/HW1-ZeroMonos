package tqs.data.Booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tqs.data.BookingStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Booking entity
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Optional<Booking> findByAccessToken(String accessToken);
    
    List<Booking> findByMunicipality(String municipality);
    
    List<Booking> findByCurrentStatus(BookingStatus status);
    
    List<Booking> findByMunicipalityAndCurrentStatus(String municipality, BookingStatus status);
    
    List<Booking> findByCollectionDate(LocalDate collectionDate);
    
    List<Booking> findByMunicipalityAndCollectionDate(String municipality, LocalDate collectionDate);
    
    boolean existsByAccessToken(String accessToken);
}
