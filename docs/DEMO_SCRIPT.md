# Demo Script

## 1. Muc tieu demo

Chung minh he thong quan ly workflow san xuat manga end-to-end:

```text
Mangaka -> Tantou Editor -> Editorial Board -> Production -> Assistant -> Mangaka review
```

## 2. Chuan bi

Backend:

```bash
cd src/backend
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Frontend:

```bash
cd src/frontend
npm install
npm run dev
```

URL:

```text
Frontend: http://localhost:5173
Backend: http://localhost:8080
```

## 3. Account demo

```text
admin@manga.local / Admin@123
mangaka@manga.local / Mangaka@123
assistant@manga.local / Assistant@123
editor@manga.local / Editor@123
board@manga.local / Board@123
board2@manga.local / Board2@123
board3@manga.local / Board3@123
```

## 4. Flow demo chinh

1. Login Mangaka.
2. Tao proposal moi.
3. Upload manuscript.
4. Bam `Save & Submit to Tantou`.
5. Login Tantou Editor.
6. Mo proposal vua submit.
7. Download manuscript neu can.
8. Chon mot trong hai nhanh:
   - `Request Revision`: Mangaka update file va resubmit.
   - `Forward to Board`: dua sang Board vote.
9. Login lan luot 3 Board accounts.
10. Moi Board member vote Approve/Reject.
11. Sau 3 vote, he thong tu chot Approved/Rejected.
12. Neu Approved, login lai Mangaka.
13. Tao chapter/page/region/task.
14. Assign task cho Assistant.
15. Login Assistant.
16. Start task.
17. Submit work.
18. Login lai Mangaka.
19. Approve hoac Request redo task.

## 5. Checklist ket qua

- Proposal status doi dung qua tung buoc.
- Board moi nguoi chi vote mot lan.
- Du 3 vote thi auto decision.
- Assistant task status doi dung.
- UI khong bao loi do.
- Backend khong crash.
