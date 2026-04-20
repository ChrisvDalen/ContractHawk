# Acceptance checks

## API checks

- Upload valid contract returns 201
- Upload invalid file returns 400
- Get contracts returns uploaded contracts
- Get contract details returns expected metadata
- Get latest analysis returns most recent analysis

## Analysis checks

- Upload triggers asynchronous analysis
- Valid contract reaches COMPLETED
- Invalid contract reaches FAILED or COMPLETED with validSpec false, depending on implementation choice
- Retry attempts are limited
- Failed messages end in dead-letter flow after retries are exhausted

## Breaking change checks

- First version has breakingChangesDetected = false
- Removing a path sets breakingChangesDetected = true
- Removing a method sets breakingChangesDetected = true

## Architecture checks

- Controllers do not access repositories directly
- Domain does not depend on controllers
- No cyclic dependencies between feature packages

## Observability checks

- Health endpoint is available
- Upload metric increments
- Analysis success/failure metrics increment
