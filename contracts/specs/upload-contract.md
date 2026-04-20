# Feature: Upload contract

## Goal

A user can upload an OpenAPI contract so the system can analyze it asynchronously.

## Scenario: Successful upload

Given a valid OpenAPI file in json, yaml, or yml format
And a non-empty serviceName
And a non-empty version
When the user uploads the file
Then the system stores the file on local disk
And stores contract metadata in the database
And creates an analysis record with status PENDING
And publishes an analysis job to the queue
And returns HTTP 201

## Scenario: Empty file rejected

Given an empty upload file
When the user uploads the file
Then the system returns HTTP 400
And no contract is stored
And no analysis job is published

## Scenario: Unsupported file extension rejected

Given a file with unsupported extension
When the user uploads the file
Then the system returns HTTP 400
And no contract is stored
And no analysis job is published
