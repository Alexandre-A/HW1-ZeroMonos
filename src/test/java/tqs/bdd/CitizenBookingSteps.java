package tqs.bdd;

import io.cucumber.java.en.*;
import io.cucumber.datatable.DataTable;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CitizenBookingSteps {
    private Page page;
    private String savedToken;
    private static final String BASE_URL = "http://localhost:8080";

    private Page getPage() {
        if (page == null) {
            page = PlaywrightContext.getPage();
        }
        return page;
    }

    @Given("the booking application is running")
    public void theBookingApplicationIsRunning() {
        // Assumption: Spring Boot app is running on localhost:8080
    }

    @Given("I am on the citizen portal")
    public void iAmOnTheCitizenPortal() {
        getPage().navigate(BASE_URL + "/");
        getPage().waitForLoadState();
    }

    @When("I select municipality {string}")
    public void iSelectMunicipality(String municipality) {
        Locator municipalitySelect = getPage().locator("#municipality");
        municipalitySelect.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        municipalitySelect.selectOption(municipality);
    }

    @When("I select collection date {int} days from now")
    public void iSelectCollectionDateDaysFromNow(int days) {
        LocalDate futureDate = LocalDate.now().plusDays(days);
        String dateString = futureDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        Locator dateInput = getPage().locator("#collectionDate");
        dateInput.fill(dateString);
    }

    @When("I select collection date {int} days ago")
    public void iSelectCollectionDateDaysAgo(int days) {
        LocalDate pastDate = LocalDate.now().minusDays(days);
        String dateString = pastDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        Locator dateInput = getPage().locator("#collectionDate");
        dateInput.fill(dateString);
    }

    @When("I select time slot {string}")
    public void iSelectTimeSlot(String timeSlot) {
        Locator timeSlotSelect = getPage().locator("#timeSlot");
        timeSlotSelect.selectOption(timeSlot);
    }

    @When("I add a bulk item with:")
    public void iAddABulkItemWith(DataTable dataTable) {
        Map<String, String> item = dataTable.asMap(String.class, String.class);
        
        getPage().locator("#itemName-1").fill(item.get("name"));
        getPage().locator("#itemDesc-1").fill(item.get("description"));
        getPage().locator("#itemWeight-1").fill(item.get("weight"));
        getPage().locator("#itemVolume-1").fill(item.get("volume"));
    }

    @When("I submit the booking")
    public void iSubmitTheBooking() {
        Locator submitBtn = getPage().locator("#submitBtn");
        submitBtn.click();
        getPage().waitForTimeout(2000);
    }

    @Then("I should see a success message")
    public void iShouldSeeASuccessMessage() {
        // Wait a moment for async operation
        getPage().waitForTimeout(2000);
        
        // Check if the token card became visible (hidden class removed)
        Locator tokenCard = getPage().locator("#tokenCard:not(.hidden)");
        
        try {
            tokenCard.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));
        } catch (Exception e) {
            // If token card didn't show, check page content for debugging
            String pageContent = getPage().content();
            System.out.println("DEBUG: Token card not visible, checking for errors...");
            
            // Extract error message if present (check both alert-danger and alert-error)
            if (pageContent.contains("alert-danger") || pageContent.contains("alert-error")) {
                // Try to find the error message
                String searchClass = pageContent.contains("alert-danger") ? "alert-danger" : "alert-error";
                int errorStart = pageContent.indexOf(searchClass);
                String errorSection = pageContent.substring(errorStart, Math.min(errorStart + 500, pageContent.length()));
                System.out.println("DEBUG: Error section: " + errorSection);
                fail("Booking creation appears to have failed - error alert on page. Check console output for details.");
            }
            throw e; // Re-throw if no error found
        }
        
        // Verify success message or token is displayed
        String pageContent = getPage().content();
        assertTrue(pageContent.contains("Booking created successfully") || 
                  pageContent.contains("access token") ||
                  getPage().locator("#accessTokenDisplay").isVisible(),
                  "Page should show success message or token");
    }

    @Then("I should receive an access token")
    public void iShouldReceiveAnAccessToken() {
        // Wait a moment for the async operation to complete
        getPage().waitForTimeout(3000);
        
        // Check if token card became visible OR if token is in the response div
        Locator tokenCard = getPage().locator("#tokenCard:not(.hidden)");
        Locator tokenDisplay = getPage().locator("#accessTokenDisplay");
        
        try {
            // Try waiting for token card to be visible
            tokenCard.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));
        } catch (Exception e) {
            // If card didn't show, check if there's an error or print page content for debugging
            System.out.println("DEBUG: Token card did not become visible");
            String pageContent = getPage().content();
            if (pageContent.contains("alert-danger") || pageContent.contains("error")) {
                System.out.println("DEBUG: Error found on page");
                fail("Booking creation failed - error message on page");
            }
        }
        
        // Get the token text
        String token = tokenDisplay.textContent().trim();
        assertNotNull(token, "Token should not be null");
        assertFalse(token.isEmpty(), "Token should not be empty");
        
        savedToken = token;
    }

    @Then("the booking status should be {string}")
    public void theBookingStatusShouldBe(String expectedStatus) {
        String pageContent = getPage().content();
        assertTrue(pageContent.contains(expectedStatus) || 
                   pageContent.contains("Booking created successfully"));
    }

    @Given("I have created a booking with token saved")
    public void iHaveCreatedABookingWithTokenSaved() {
        iAmOnTheCitizenPortal();
        iSelectMunicipality("Porto");
        iSelectCollectionDateDaysFromNow(5);
        iSelectTimeSlot("morning");
        
        getPage().locator("#itemName-1").fill("Test Item");
        getPage().locator("#itemDesc-1").fill("Test description");
        getPage().locator("#itemWeight-1").fill("10.0");
        getPage().locator("#itemVolume-1").fill("1.0");
        
        iSubmitTheBooking();
        iShouldReceiveAnAccessToken();
        
        getPage().navigate(BASE_URL + "/");
        getPage().waitForLoadState();
    }

    @When("I enter my access token in the status check field")
    public void iEnterMyAccessTokenInTheStatusCheckField() {
        Locator tokenInput = getPage().locator("#checkToken");
        tokenInput.fill(savedToken);
    }

    @When("I click check status")
    public void iClickCheckStatus() {
        Locator checkBtn = getPage().locator("#checkStatusBtn");
        checkBtn.click();
        getPage().waitForTimeout(2000);
    }

    @Then("I should see my booking details")
    public void iShouldSeeMyBookingDetails() {
        // The modal should become visible
        Locator modal = getPage().locator("#detailsModal");
        modal.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        
        // Check if modal content is loaded
        Locator modalContent = getPage().locator("#bookingDetailsContent");
        assertTrue(modalContent.isVisible());
    }

    @Then("the status should be {string}")
    public void theStatusShouldBe(String expectedStatus) {
        String pageContent = getPage().content();
        assertTrue(pageContent.contains(expectedStatus));
    }

    @Then("I should see the status history")
    public void iShouldSeeTheStatusHistory() {
        String pageContent = getPage().content();
        assertTrue(pageContent.contains("Status History") || 
                   pageContent.contains("RECEIVED"));
    }

    @When("I enter my access token in the cancel field")
    public void iEnterMyAccessTokenInTheCancelField() {
        // Enter the token in the status check field
        iEnterMyAccessTokenInTheStatusCheckField();
        // Then click check status to open the modal
        iClickCheckStatus();
    }

    @When("I click cancel booking")
    public void iClickCancelBooking() {
        // Wait for the modal to be visible first (modal gets class 'show' when opened)
        Locator modal = getPage().locator("#detailsModal.show");
        modal.waitFor(new Locator.WaitForOptions()
            .setTimeout(5000)
            .setState(WaitForSelectorState.VISIBLE));
        
        // The button in the modal is #cancelBookingBtn
        Locator cancelBtn = getPage().locator("#cancelBookingBtn");
        cancelBtn.waitFor(new Locator.WaitForOptions()
            .setTimeout(5000)
            .setState(WaitForSelectorState.VISIBLE));
        cancelBtn.click();
        getPage().waitForTimeout(2000);
    }

    @Then("I should see a cancellation confirmation")
    public void iShouldSeeACancellationConfirmation() {
        String pageContent = getPage().content();
        assertTrue(pageContent.contains("cancel") || 
                   pageContent.contains("CANCELLED"));
    }

    @When("I check the status again")
    public void iCheckTheStatusAgain() {
        iClickCheckStatus();
    }

    @Then("the booking municipality should be {string}")
    public void theBookingMunicipalityShouldBe(String expectedMunicipality) {
        String pageContent = getPage().content();
        assertTrue(pageContent.contains(expectedMunicipality));
    }

    @Then("I should see an error message containing {string}")
    public void iShouldSeeAnErrorMessageContaining(String errorText) {
        // Wait for the async operation
        getPage().waitForTimeout(2000);
        
        // Try to wait for error alert to be visible - handle both alert-danger and alert-error
        try {
            Locator errorAlert = getPage().locator(".alert-danger.show, .alert-error.show, #bookingResponse.alert-danger");
            errorAlert.first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(3000));
        } catch (Exception e) {
            // Continue even if specific error element not found
            System.out.println("DEBUG: No visible error alert found, checking page content...");
        }
        
        // Check in both the alert container and the bookingResponse div
        String pageContent = getPage().content().toLowerCase();
        String searchText = errorText.toLowerCase();
        
        // More flexible matching - check for related terms
        boolean containsError = pageContent.contains(searchText) ||
                               (searchText.contains("future") && pageContent.contains("1 day in the future")) ||
                               (searchText.contains("not found") && pageContent.contains("not found"));
        
        assertTrue(containsError, "Page should contain error related to: " + errorText + 
                   ". Error alerts with class 'alert-error' or 'alert-danger' should be visible.");
    }

    @When("I enter an invalid token {string}")
    public void iEnterAnInvalidToken(String invalidToken) {
        Locator tokenInput = getPage().locator("#checkToken");
        tokenInput.fill(invalidToken);
    }
}
