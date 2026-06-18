# Backend Test Guide

This project uses two complementary test tools:

1. **Maven + JUnit** for automated backend code tests.
2. **Postman/Newman** for automated API flow tests.

## 1. Check backend code with Maven/JUnit

Run from the project root:

```powershell
npm run test:backend
```

Or run directly from backend:

```powershell
cd backend
.\mvnw.cmd test
```

Expected successful result:

```text
BUILD SUCCESS
Tests run: ..., Failures: 0, Errors: 0
```

Detailed JUnit/Surefire reports are generated in:

```text
backend/target/surefire-reports/
```

## 2. Run only the full business-flow JUnit test

Run:

```powershell
npm run test:backend:flow
```

This runs:

```text
FullMangaWorkflowFlowTest
```

It verifies the main happy path:

```text
Mangaka creates proposal
-> manuscript metadata attaches
-> Mangaka submits proposal
-> Editor forwards to Board
-> Board approves by majority
-> Mangaka creates chapter/page/region/task
-> Assistant starts and submits task
-> Mangaka approves task
```

## 3. Start backend for API/Newman flow testing

Newman tests call the real HTTP API, so the backend must be running first.

Recommended local profile:

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Wait until the server is ready on:

```text
http://localhost:8080
```

## 4. Run Postman/Newman API flow test

In another terminal, from the project root:

```powershell
npm run test:api
```

This runs:

```text
postman/manga-workflow-flow.postman_collection.json
```

using:

```text
postman/local.postman_environment.json
```

Newman checks every API step with assertions for:

- HTTP status code
- important response fields
- workflow status changes
- IDs needed by later steps

## 5. Read Newman reports

After running `npm run test:api`, reports are generated here:

```text
postman/reports/manga-workflow-report.html
postman/reports/manga-workflow-report.json
```

Open the HTML report in a browser to show test evidence during demo/defense.

## 6. What to say when asked how the flow was verified

You can say:

> Backend code is verified using Maven + JUnit tests. The full business flow is also tested using a dedicated `FullMangaWorkflowFlowTest`. For real API verification, we use a Postman collection executed by Newman. Newman runs the API sequence from Mangaka proposal creation through Editor review, Board approval, production task assignment, Assistant submission, and Mangaka approval. Each step has assertions for status code and workflow state. The generated HTML report is used as test evidence.

## 7. Common failures

### Backend is not running

If `npm run test:api` shows connection refused, start backend first:

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

### Port 8080 is busy

Stop the old backend process, then start it again.

### Newman is missing

Install project test dependencies:

```powershell
npm install
```

Then run:

```powershell
npm run test:api
```
