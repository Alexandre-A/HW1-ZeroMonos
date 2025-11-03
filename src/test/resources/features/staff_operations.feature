@staff
Feature: Staff Operations Management
  As a staff member
  I want to manage booking requests
  So that I can efficiently process bulk waste collections

  Background:
    Given the booking application is running
    And there are existing bookings in the system
    And I am on the staff portal

  Scenario: View all bookings
    When I load the staff portal
    Then I should see a list of bookings

  Scenario: Filter bookings by municipality
    Given there are bookings for multiple municipalities
    When I filter by municipality "Porto"
    Then I should see only bookings for "Porto"

  Scenario Outline: Filter bookings by status
    Given there are bookings with different statuses
    When I filter by status "<status>"
    Then I should see only bookings with status "<status>"

    Examples:
      | status      |
      | RECEIVED    |
      | ASSIGNED    |
      | IN_PROGRESS |
      | COMPLETED   |
      | CANCELLED   |

  Scenario: View booking details
    Given there is a booking with status "RECEIVED"
    When I click view details for that booking
    Then I should see the complete booking information
    And I should see all bulk items
    And I should see the status history with timestamps

  Scenario: Assign a received booking
    Given there is a booking with status "RECEIVED"
    When I click view details for that booking
    And I click the assign button
    Then the booking status should change to "ASSIGNED"
    And the status history should show the transition
