# Feature: Analyze contract asynchronously

## Goal

The system processes uploaded contracts in the background and stores analysis results.

## Scenario: Valid contract analysis

Given a contract exists with status PENDING
When the worker consumes the analysis job
Then the analysis status becomes PROCESSING
And the file is parsed
And the system determines whether the spec is valid
And the system counts paths
And the system counts operations
And the system stores the analysis result
And the analysis status becomes COMPLETED

## Scenario: Failed analysis with retry

Given a contract exists with status PENDING
And the worker encounters a temporary processing error
When the analysis fails
Then the message is retried up to the configured retry limit
And the final failure is persisted if all retries are exhausted
And the message is routed to the dead-letter queue
And the analysis status becomes FAILED
