@dashboard
Feature: Operations Dashboard
  As a staff manager
  I want to view operational metrics
  So that I can monitor the booking system performance

  Background:
    Given the booking application is running
    And there are bookings with various statuses and municipalities
    And I am on the operations dashboard

  Scenario: View dashboard summary
    When I load the dashboard
    Then I should see the total number of bookings
    And I should see a status distribution chart
    And I should see a municipality distribution chart
    And I should see the last updated timestamp

  Scenario: Dashboard auto-refresh
    Given I am viewing the dashboard
    When I wait for 60 seconds
    Then the dashboard should automatically refresh
    And the last updated timestamp should change

  Scenario: Manual dashboard refresh
    Given I am viewing the dashboard
    When I click the refresh button
    Then the data should be reloaded
