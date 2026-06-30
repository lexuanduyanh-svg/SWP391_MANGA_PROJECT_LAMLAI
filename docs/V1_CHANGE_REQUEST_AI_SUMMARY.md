# V1 Change Request - Manuscript Validation and AI Summary

## 1. Source request

User/teacher feedback added these requirements:

```text
- Rang buoc file dau vao cua Mangaka.
- Summary lai file duoc chon ve mat noi dung truoc khi upload.
- V1 sua be lai, them chuc nang AI summary.
- Tuong tac voi giao vien nhieu hon.
```

## 2. Interpretation

V1 should be smaller than the full workflow plan, but it should include one clear AI-assisted feature:

```text
Mangaka selects manuscript file
-> system validates file
-> system creates content summary preview
-> Mangaka confirms
-> upload/submit continues
```

This makes the project easier to explain to the teacher because the AI part has a clear business purpose: helping Mangaka and Tantou quickly understand manuscript content before review.

## 3. Functional requirements

### FR-V1-01 File input validation

Before final upload, the system must validate:

- Allowed file extensions.
- Allowed MIME/content type if available.
- Maximum file size.
- Empty/corrupted file detection where possible.

Suggested V1 allowed formats:

```text
.pdf
.doc
.docx
.txt
.png
.jpg
.jpeg
```

Suggested V1 size limit:

```text
25 MB per file
```

If validation fails, the UI must show a clear error and block final upload.

### FR-V1-02 AI/content summary preview

Before final upload/final submit, the system must show a short summary of the selected file.

V1 acceptable implementation levels:

1. Basic text extraction + simple rule-based summary.
2. Optional AI API/model call if available.
3. Fallback summary message if text cannot be extracted.

The summary does not need to be perfect in V1. It must be useful enough for demo and teacher discussion.

### FR-V1-03 Mangaka confirmation

Mangaka must see:

- File name.
- File type.
- File size.
- Summary preview.
- Warning messages if summary quality is low.

Mangaka should confirm before upload/submission.

### FR-V1-04 Store summary metadata

Store summary metadata with the proposal if possible:

```text
summary
summaryGeneratedAt
summaryStatus
summaryWarnings
```

If database work is not ready, keep this as response-only first, then persist later.

### FR-V1-05 Teacher interaction checkpoint

Before implementing beyond V1, the team should ask teacher to confirm:

- Are the allowed file types acceptable?
- Is the summary preview useful enough?
- Should AI summary be demo-only or persisted?
- Should summary happen before upload or after temporary upload?
- Does teacher prefer smaller V1 over full workflow scope?

Record feedback in this file or project meeting notes.

## 4. Proposed API

```text
POST /api/mangaka/proposals/preview-upload
```

Purpose:

```text
Validate selected manuscript and return summary preview before final upload.
```

Response fields:

```text
valid
summary
warningMessages
fileName
fileType
fileSize
summaryStatus
```

## 5. Frontend UI impact

Mangaka proposal form should add a pre-upload preview panel:

```text
Selected file
Validation result
AI summary preview
Confirm button
Upload/submit button enabled only after valid preview
```

## 6. Owner split

| Work item | Owner |
|---|---|
| Backend validation endpoint | Member 1 |
| Summary generation logic | Member 1, Member 5 support for explanation |
| DB fields for summary metadata | Member 3 |
| UI preview/confirm panel | Member 4 |
| Test/demo script/teacher questions | Member 5 |

## 7. Acceptance criteria

- Invalid file is blocked before final upload.
- Valid file shows a summary preview.
- Mangaka can confirm and continue submit.
- Summary behavior can be explained to teacher in under 2 minutes.
- Docs/API/test cases are updated.


