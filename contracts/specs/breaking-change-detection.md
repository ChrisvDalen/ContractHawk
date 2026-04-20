# Feature: Detect breaking changes

## Goal

The system compares a newly uploaded contract version against the latest previous version of the same service.

## Scenario: No previous version exists

Given this is the first uploaded version for a service
When the analysis runs
Then previous version exists is false
And breakingChangesDetected is false

## Scenario: Removed path is a breaking change

Given a previous version exists for the same service
And the previous version contains path /orders/{id}
And the new version does not contain path /orders/{id}
When the comparison runs
Then breakingChangesDetected is true
And the summary contains the removed path

## Scenario: Removed HTTP method is a breaking change

Given a previous version exists for the same service
And the previous version contains GET on /orders
And the new version no longer contains GET on /orders
When the comparison runs
Then breakingChangesDetected is true
And the summary contains the removed method

## Scenario: Added path is not a breaking change

Given a previous version exists for the same service
When the new version adds a new path
Then breakingChangesDetected is false unless a removal also occurred
