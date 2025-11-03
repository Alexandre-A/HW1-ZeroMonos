package tqs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MunicipalityServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private MunicipalityService municipalityService;

    private static final String API_URL = "https://json.geoapi.pt/municipios";
    private static final String[] MOCK_MUNICIPALITIES = {
        "Lisboa", "Porto", "Coimbra", "Braga", "Faro", "Aveiro"
    };

    @BeforeEach
    void setUp() {
        municipalityService = new MunicipalityService(restTemplate, API_URL);
    }

    @Test
    void testGetAvailableMunicipalities_Success() {
        // Given
        when(restTemplate.getForObject(API_URL, String[].class))
                .thenReturn(MOCK_MUNICIPALITIES);

        // When
        List<String> municipalities = municipalityService.getAvailableMunicipalities();

        // Then
        assertThat(municipalities).hasSize(6);
        assertThat(municipalities).contains("Lisboa", "Porto", "Coimbra");
        verify(restTemplate, times(1)).getForObject(API_URL, String[].class);
    }

    @Test
    void testGetAvailableMunicipalities_ApiReturnsNull() {
        // Given
        when(restTemplate.getForObject(API_URL, String[].class))
                .thenReturn(null);

        // When
        List<String> municipalities = municipalityService.getAvailableMunicipalities();

        // Then
        assertThat(municipalities).isEmpty();
        verify(restTemplate, times(1)).getForObject(API_URL, String[].class);
    }

    @Test
    void testGetAvailableMunicipalities_ApiReturnsEmptyArray() {
        // Given
        when(restTemplate.getForObject(API_URL, String[].class))
                .thenReturn(new String[0]);

        // When
        List<String> municipalities = municipalityService.getAvailableMunicipalities();

        // Then
        assertThat(municipalities).isEmpty();
        verify(restTemplate, times(1)).getForObject(API_URL, String[].class);
    }

    @Test
    void testGetAvailableMunicipalities_NetworkError() {
        // Given
        when(restTemplate.getForObject(API_URL, String[].class))
                .thenThrow(new RestClientException("Network timeout"));

        // When
        List<String> municipalities = municipalityService.getAvailableMunicipalities();

        // Then
        assertThat(municipalities).isEmpty();
        verify(restTemplate, times(1)).getForObject(API_URL, String[].class);
    }

    @Test
    void testGetAvailableMunicipalities_UnexpectedException() {
        // Given
        when(restTemplate.getForObject(API_URL, String[].class))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When
        List<String> municipalities = municipalityService.getAvailableMunicipalities();

        // Then - Service should handle all exceptions gracefully
        assertThat(municipalities).isEmpty();
        verify(restTemplate, times(1)).getForObject(API_URL, String[].class);
    }

    @Test
    void testIsValidMunicipality_ValidName() {
        // Given
        when(restTemplate.getForObject(API_URL, String[].class))
                .thenReturn(MOCK_MUNICIPALITIES);

        // When
        boolean isValid = municipalityService.isValidMunicipality("Lisboa");

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void testIsValidMunicipality_ValidName_CaseInsensitive() {
        // Given
        when(restTemplate.getForObject(API_URL, String[].class))
                .thenReturn(MOCK_MUNICIPALITIES);

        // When
        boolean isValid = municipalityService.isValidMunicipality("LISBOA");

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void testIsValidMunicipality_ValidName_WithWhitespace() {
        // Given
        when(restTemplate.getForObject(API_URL, String[].class))
                .thenReturn(MOCK_MUNICIPALITIES);

        // When
        boolean isValid = municipalityService.isValidMunicipality("  Porto  ");

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void testIsValidMunicipality_InvalidName() {
        // Given
        when(restTemplate.getForObject(API_URL, String[].class))
                .thenReturn(MOCK_MUNICIPALITIES);

        // When
        boolean isValid = municipalityService.isValidMunicipality("InvalidCity");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void testIsValidMunicipality_NullName() {
        // When
        boolean isValid = municipalityService.isValidMunicipality(null);

        // Then
        assertThat(isValid).isFalse();
        verify(restTemplate, never()).getForObject(anyString(), eq(String[].class));
    }

    @Test
    void testIsValidMunicipality_EmptyName() {
        // When
        boolean isValid = municipalityService.isValidMunicipality("");

        // Then
        assertThat(isValid).isFalse();
        verify(restTemplate, never()).getForObject(anyString(), eq(String[].class));
    }

    @Test
    void testIsValidMunicipality_WhitespaceName() {
        // When
        boolean isValid = municipalityService.isValidMunicipality("   ");

        // Then
        assertThat(isValid).isFalse();
        verify(restTemplate, never()).getForObject(anyString(), eq(String[].class));
    }

    @Test
    void testIsValidMunicipality_ApiFailure_AllowsBooking() {
        // Given - API fails
        when(restTemplate.getForObject(API_URL, String[].class))
                .thenThrow(new RestClientException("API unavailable"));

        // When
        boolean isValid = municipalityService.isValidMunicipality("Lisboa");

        // Then - Should return true to allow booking when API is down
        assertThat(isValid).isTrue();
    }

    @Test
    void testGetApiUrl() {
        // When
        String url = municipalityService.getApiUrl();

        // Then
        assertThat(url).isEqualTo(API_URL);
    }

    @Test
    void testCustomApiUrl() {
        // Given
        String customUrl = "https://custom.api.com/municipalities";
        MunicipalityService customService = new MunicipalityService(restTemplate, customUrl);

        // When
        String url = customService.getApiUrl();

        // Then
        assertThat(url).isEqualTo(customUrl);
    }
}
