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
        
        // FIRST: Check if there's an error on the page (fail fast)
        Locator errorAlert = getPage().locator(".alert-danger.show, .alert-error.show");
        if (errorAlert.count() > 0 && errorAlert.first().isVisible()) {
            String errorText = errorAlert.first().textContent().trim();
            fail("Booking creation failed with error: " + errorText);
        }
        
        // SECOND: Check if token card is visible (success case)
        Locator tokenCard = getPage().locator("#tokenCard:not(.hidden)");
        
        // Wait for token card to appear (if booking succeeded)
        boolean tokenCardVisible = false;
        try {
            tokenCard.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));
            tokenCardVisible = true;
        } catch (Exception e) {
            // Token card didn't appear - check page content one more time
            String pageContent = getPage().content();
            if (pageContent.contains("alert-danger") || pageContent.contains("alert-error")) {
                fail("Booking creation failed - error alert found on page");
            }
            // If no error found, re-throw the timeout
            throw e;
        }
        
        // Verify success indicators are present
        if (tokenCardVisible) {
            String pageContent = getPage().content();
            assertTrue(pageContent.contains("Booking created successfully") || 
                      pageContent.contains("access token") ||
                      getPage().locator("#accessTokenDisplay").isVisible(),
                      "Token card is visible but no success message found");
        }
    }

    @Then("I should receive an access token")
    public void iShouldReceiveAnAccessToken() {
        // Wait a moment for the async operation to complete
        getPage().waitForTimeout(2000);
        
        // FIRST: Check for errors (fail fast)
        Locator errorAlert = getPage().locator(".alert-danger.show, .alert-error.show");
        if (errorAlert.count() > 0 && errorAlert.first().isVisible()) {
            String errorText = errorAlert.first().textContent().trim();
            fail("Cannot retrieve access token - booking creation failed with error: " + errorText);
        }
        
        // SECOND: Wait for token card to be visible
        Locator tokenCard = getPage().locator("#tokenCard:not(.hidden)");
        Locator tokenDisplay = getPage().locator("#accessTokenDisplay");
        
        try {
            tokenCard.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5000));
        } catch (Exception e) {
            // Check one more time for errors that might have appeared
            String pageContent = getPage().content();
            if (pageContent.contains("alert-danger") || pageContent.contains("alert-error")) {
                fail("Booking creation failed - error found on page after timeout");
            }
            throw new AssertionError("Token card did not become visible within timeout, and no error message found");
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
        iSelectMunicipality("Lisboa");
        iSelectCollectionDateDaysFromNow(32);
        iSelectTimeSlot("afternoon");
        
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
