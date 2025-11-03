package tqs.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring configuration for Cucumber tests.
 * This class is required when running Cucumber tests in a Spring Boot project,
 * even though our Playwright tests don't directly use Spring components.
 * 
 * The webEnvironment = NONE setting means no web server is started,
 * as we're testing the UI through Playwright's browser automation.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CucumberSpringConfiguration {
    // This class doesn't need any methods.
    // It just provides the Spring context configuration for Cucumber.
}
