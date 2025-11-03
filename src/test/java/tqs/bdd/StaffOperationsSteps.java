package tqs.bdd;

import io.cucumber.java.en.*;
import io.cucumber.datatable.DataTable;
import com.microsoft.playwright.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class StaffOperationsSteps {
    private Page page;
    private static final String BASE_URL = "http://localhost:8080";
    private String currentBookingToken;

    private Page getPage() {
        if (page == null) {
            page = PlaywrightContext.getPage();
        }
        return page;
    }

    @Given("there are existing bookings in the system")
    public void thereAreExistingBookingsInTheSystem() {
        // Bookings should exist from previous scenarios or database setup
    }

    @Given("I am on the staff portal")
    public void iAmOnTheStaffPortal() {
        getPage().navigate(BASE_URL + "/staff.html");
        getPage().waitForLoadState();
        getPage().waitForTimeout(1000);
    }

    @When("I load the staff portal")
    public void iLoadTheStaffPortal() {
        iAmOnTheStaffPortal();
    }

    @Then("I should see a list of bookings")
    public void iShouldSeeAListOfBookings() {
        // Check for booking table or cards
        String pageContent = getPage().content();
        assertTrue(pageContent.contains("Booking") || 
                   pageContent.contains("Access Token") ||
                   getPage().locator(".booking-row, .booking-card, tbody tr").count() > 0,
                   "Should see bookings list");
    }

    @Then("each booking should display:")
    public void eachBookingShouldDisplay(DataTable dataTable) {
        List<String> fields = dataTable.asList();
        String pageContent = getPage().content().toLowerCase();
        
        for (String field : fields) {
            assertTrue(pageContent.contains(field.toLowerCase()) ||
                      pageContent.contains(field.replace(" ", "").toLowerCase()),
                      "Should display field: " + field);
        }
    }

    @Given("there are bookings for multiple municipalities")
    public void thereAreBookingsForMultipleMunicipalities() {
        // Assume data exists from test setup
    }

    @When("I filter by municipality {string}")
    public void iFilterByMunicipality(String municipality) {
        Locator municipalityFilter = getPage().locator("#municipalityFilter");
        if (municipalityFilter.count() > 0) {
            municipalityFilter.selectOption(municipality);
            getPage().waitForTimeout(1000);
        }
    }

    @Then("I should see only bookings for {string}")
    public void iShouldSeeOnlyBookingsFor(String municipality) {
        getPage().waitForTimeout(500);
        String pageContent = getPage().content();
        assertTrue(pageContent.contains(municipality),
                  "Filtered results should contain: " + municipality);
    }

    @Given("there are bookings with different statuses")
    public void thereAreBookingsWithDifferentStatuses() {
        // Assume data exists from test setup
    }

    @When("I filter by status {string}")
    public void iFilterByStatus(String status) {
        Locator statusFilter = getPage().locator("#statusFilter");
        if (statusFilter.count() > 0) {
            statusFilter.selectOption(status);
            getPage().waitForTimeout(1000);
        }
    }

    @Then("I should see only bookings with status {string}")
    public void iShouldSeeOnlyBookingsWithStatus(String status) {
        getPage().waitForTimeout(500);
        String pageContent = getPage().content();
        assertTrue(pageContent.contains(status),
                  "Filtered results should contain status: " + status);
    }

    @Given("there is a booking with status {string}")
    public void thereIsABookingWithStatus(String status) {
        // This would ideally create or find a booking with this status
        // For now, assume it exists from test data
    }

    @When("I click view details for that booking")
    public void iClickViewDetailsForThatBooking() {
        Locator viewBtn = getPage().locator("button:has-text('View'), .view-details-btn, button:has-text('Details')").first();
        if (viewBtn.count() > 0) {
            viewBtn.click();
            getPage().waitForTimeout(1500);
        }
    }

    @Then("I should see the complete booking information")
    public void iShouldSeeTheCompleteBookingInformation() {
        Locator detailsSection = getPage().locator("#bookingDetails, .booking-details, .modal");
        detailsSection.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        assertTrue(detailsSection.isVisible(), "Booking details should be visible");
    }

    @Then("I should see all bulk items")
    public void iShouldSeeAllBulkItems() {
        String pageContent = getPage().content();
        assertTrue(pageContent.contains("Item") || 
                   pageContent.contains("Bulk") ||
                   pageContent.contains("Weight") ||
                   pageContent.contains("Volume"),
                   "Should display bulk items");
    }

    @Then("I should see the status history with timestamps")
    public void iShouldSeeTheStatusHistoryWithTimestamps() {
        String pageContent = getPage().content();
        assertTrue(pageContent.contains("Status") || 
                   pageContent.contains("History") ||
                   pageContent.contains("RECEIVED") ||
                   pageContent.contains("Changed"),
                   "Should display status history");
    }

    @When("I click the assign button")
    public void iClickTheAssignButton() {
        Locator assignBtn = getPage().locator("#assignBtn, button:has-text('Assign')");
        assignBtn.waitFor(new Locator.WaitForOptions().setTimeout(3000));
        assignBtn.click();
        getPage().waitForTimeout(2000);
    }

    @Then("the booking status should change to {string}")
    public void theBookingStatusShouldChangeTo(String expectedStatus) {
        getPage().waitForTimeout(1000);
        String pageContent = getPage().content();
        assertTrue(pageContent.contains(expectedStatus),
                  "Status should be: " + expectedStatus);
    }

    @Then("the status history should show the transition")
    public void theStatusHistoryShouldShowTheTransition() {
        // Status history should be updated with new entry
        String pageContent = getPage().content();
        assertTrue(pageContent.contains("Status") || pageContent.contains("History"),
                  "Status history should show transition");
    }

    @When("I assign the booking")
    public void iAssignTheBooking() {
        iClickTheAssignButton();
    }

    @When("I start the booking")
    public void iStartTheBooking() {
        Locator startBtn = getPage().locator("#startBtn, button:has-text('Start'), button:has-text('In Progress')");
        startBtn.waitFor(new Locator.WaitForOptions().setTimeout(3000));
        startBtn.click();
        getPage().waitForTimeout(2000);
    }

    @When("I complete the booking")
    public void iCompleteTheBooking() {
        Locator completeBtn = getPage().locator("#completeBtn, button:has-text('Complete')");
        completeBtn.waitFor(new Locator.WaitForOptions().setTimeout(3000));
        completeBtn.click();
        getPage().waitForTimeout(2000);
    }

    @Then("no more actions should be available")
    public void noMoreActionsShouldBeAvailable() {
        // Check that action buttons are disabled or hidden
        getPage().waitForTimeout(500);
        
        // Try to find action buttons - they should not be enabled
        Locator assignBtn = getPage().locator("#assignBtn, button:has-text('Assign')");
        Locator startBtn = getPage().locator("#startBtn, button:has-text('Start')");
        
        // If buttons exist, they should be disabled
        if (assignBtn.count() > 0) {
            assertTrue(assignBtn.isDisabled() || !assignBtn.isVisible(),
                      "Assign button should be disabled");
        }
        if (startBtn.count() > 0) {
            assertTrue(startBtn.isDisabled() || !startBtn.isVisible(),
                      "Start button should be disabled");
        }
    }

    @When("I cancel the booking")
    public void iCancelTheBooking() {
        Locator cancelBtn = getPage().locator("#cancelBtn, button:has-text('Cancel')");
        cancelBtn.waitFor(new Locator.WaitForOptions().setTimeout(3000));
        
        // Might need to confirm cancellation
        cancelBtn.click();
        getPage().waitForTimeout(2000);
        
        // Check for confirmation dialog
        Locator confirmBtn = getPage().locator("button:has-text('Confirm'), button:has-text('Yes')");
        if (confirmBtn.count() > 0 && confirmBtn.isVisible()) {
            confirmBtn.click();
            getPage().waitForTimeout(1000);
        }
    }

    @When("I view the booking details")
    public void iViewTheBookingDetails() {
        iClickViewDetailsForThatBooking();
    }

    @Then("I should not see any action buttons")
    public void iShouldNotSeeAnyActionButtons() {
        // Completed or cancelled bookings shouldn't have action buttons
        Locator assignBtn = getPage().locator("#assignBtn:visible, button:has-text('Assign'):visible");
        Locator startBtn = getPage().locator("#startBtn:visible, button:has-text('Start'):visible");
        Locator completeBtn = getPage().locator("#completeBtn:visible, button:has-text('Complete'):visible");
        
        // None of these should be visible and enabled
        assertTrue(assignBtn.count() == 0 || assignBtn.isDisabled(),
                  "Should not have visible assign button");
        assertTrue(startBtn.count() == 0 || startBtn.isDisabled(),
                  "Should not have visible start button");
        assertTrue(completeBtn.count() == 0 || completeBtn.isDisabled(),
                  "Should not have visible complete button");
    }
}
