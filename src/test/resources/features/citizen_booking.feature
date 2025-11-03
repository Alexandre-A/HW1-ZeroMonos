@citizen
Feature: Citizen Booking Management
  As a citizen
  I want to book bulk waste collection
  So that I can dispose of large items conveniently

  Background:
    Given the booking application is running
    And I am on the citizen portal

  Scenario: Create a new booking successfully
    When I select municipality "Porto"
    And I select collection date 30 days from now
    And I select time slot "morning"
    And I add a bulk item with:
      | name        | Old Sofa              |
      | description | Large leather sofa    |
      | weight      | 50.0                  |
      | volume      | 2.5                   |
    And I submit the booking
    Then I should see a success message
    And I should receive an access token
    And the booking status should be "RECEIVED"

  Scenario: Check booking status with valid token
    Given I have created a booking with token saved
    When I enter my access token in the status check field
    And I click check status
    Then I should see my booking details
    And the status should be "RECEIVED"
    And I should see the status history

  Scenario: Cancel a booking
    Given I have created a booking with token saved
    When I enter my access token in the cancel field
    And I click cancel booking
    Then I should see a cancellation confirmation
    When I check the status again
    Then the status should be "CANCELLED"

  Scenario Outline: Create bookings for different municipalities
    When I select municipality "<municipality>"
    And I select collection date 30 days from now
    And I select time slot "<timeSlot>"
    And I add a bulk item with:
      | name        | Test Item       |
      | description | Test desc       |
      | weight      | 10.0            |
      | volume      | 1.0             |
    And I submit the booking
    Then I should see a success message
    And the booking municipality should be "<municipality>"

    Examples:
      | municipality | timeSlot  |
      | Porto        | morning   |
      | Lisboa       | afternoon |
      | Coimbra      | evening   |

  Scenario: Check status with invalid token
    When I enter an invalid token "invalid-token-123"
    And I click check status
    Then I should see an error message containing "not found"