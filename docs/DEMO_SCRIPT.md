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
2. Chon manuscript file.
3. He thong validate file type/size va hien AI summary preview.
4. Mangaka confirm summary truoc khi upload/final submit.
5. Tao proposal moi.
6. Upload manuscript.
7. Bam `Save & Submit to Tantou`.
8. Neu can, mo checkpoint voi giao vien de giai thich summary va file rules.
9. Login Tantou Editor.
10. Mo proposal vua submit.
11. Download manuscript neu can.
12. Chon mot trong hai nhanh:
   - `Request Revision`: Mangaka update file va resubmit.
   - `Forward to Board`: dua sang Board vote.
13. Login lan luot 3 Board accounts.
14. Moi Board member vote Approve/Reject.
15. Sau 3 vote, he thong tu chot Approved/Rejected.
16. Neu Approved, login lai Mangaka.
17. Tao chapter/page/region/task.
18. Assign task cho Assistant.
19. Login Assistant.
20. Start task.
21. Submit work.
22. Login lai Mangaka.
23. Approve hoac Request redo task.
24. Neu giao vien hoi ve V1, giai thich day la ban rut gon va AI summary chi la feature nho ho tro demo.

## 5. Checklist ket qua

- Proposal status doi dung qua tung buoc.
- Board moi nguoi chi vote mot lan.
- Du 3 vote thi auto decision.
- Assistant task status doi dung.
- UI khong bao loi do.
- Backend khong crash.
