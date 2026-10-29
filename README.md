# Library Management System (Java)

A menu-driven Java console application based on the supplied project specification.

## Features
- Add books and track total/available copies
- Register regular and premium members
- Borrow and return books
- Enforce stock and borrowing limits
- Calculate late-return fines
- Print borrow/return receipts
- Validate menu choices, numbers, and dates

## Membership rules
- Regular: up to 3 books, 14 days, Rs. 5 per late day
- Premium: up to 6 books, 30 days, Rs. 2 per late day

## Requirements
- Java 14 or later (uses switch arrow syntax)

## Run
Open a terminal in this folder and run:

```bash
javac LibraryManagementSystem.java
java LibraryManagementSystem
```

Book IDs begin at `B101`, member IDs begin at `M001`, and borrow transaction IDs begin at `TXN1001`.
