package tqs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Service for fetching and validating Portuguese municipalities from external API
 */
@Service
public class MunicipalityService {

    private static final Logger logger = LoggerFactory.getLogger(MunicipalityService.class);
    
    private final RestTemplate restTemplate;
    private final String apiUrl;

    public MunicipalityService(
            RestTemplate restTemplate,
            @Value("${municipality.api.url:https://json.geoapi.pt/municipios}") String apiUrl) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
    }

    @Cacheable(value = "municipalities", unless = "#result.isEmpty()")
    public List<String> getAvailableMunicipalities() {
        logger.info("Fetching municipalities from external API");
        
        try {
            String[] municipalities = restTemplate.getForObject(apiUrl, String[].class);
            
            if (municipalities == null || municipalities.length == 0) {
                logger.warn("External API returned empty municipality list");
                return Collections.emptyList();
            }
            
            logger.info("Successfully fetched {} municipalities", municipalities.length);
            return Arrays.asList(municipalities);
            
        } catch (RestClientException e) {
            logger.error("Failed to fetch municipalities from external API: {}", e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Unexpected error fetching municipalities: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean isValidMunicipality(String municipalityName) {
        if (municipalityName == null || municipalityName.trim().isEmpty()) {
            return false;
        }

        List<String> municipalities = getAvailableMunicipalities();
        
        // If API failed, we can't validate - return true to allow booking
        // (Alternative: could return false for strict validation)
        if (municipalities.isEmpty()) {
            logger.warn("Cannot validate municipality - API unavailable, allowing booking");
            return true;
        }

        boolean isValid = municipalities.stream()
                .anyMatch(m -> m.equalsIgnoreCase(municipalityName.trim()));
        
        if (!isValid) {
            logger.debug("Municipality validation failed");
        }
        
        return isValid;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}
