import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Library Management System
 * A menu-driven Java console application based on the supplied project PDF.
 *
 * Compile: javac LibraryManagementSystem.java
 * Run:     java LibraryManagementSystem
 */
public class LibraryManagementSystem {
    private static final Scanner sc = new Scanner(System.in);
    private static final Library library = new Library();

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ", 1, 6);
            try {
                switch (choice) {
                    case 1 -> addBook();
                    case 2 -> addMember();
                    case 3 -> borrowBook();
                    case 4 -> returnBook();
                    case 5 -> displayAvailableBooks();
                    case 6 -> {
                        running = false;
                        System.out.println("Thank you for using the Library Management System!");
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (LibraryException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
        sc.close();
    }

    private static void printMenu() {
        System.out.println("\n========================================");
        System.out.println("       LIBRARY MANAGEMENT SYSTEM");
        System.out.println("========================================");
        System.out.println("1. Add Book");
        System.out.println("2. Add Member");
        System.out.println("3. Borrow Book");
        System.out.println("4. Return Book");
        System.out.println("5. Display Available Books");
        System.out.println("6. Exit");
    }

    private static void addBook() {
        String title = readText("Book title: ");
        String author = readText("Author name: ");
        String country = readText("Author country: ");
        String category = readText("Category: ");
        int copies = readInt("Number of copies (1-50): ", 1, 50);
        Book book = library.addBook(title, author, country, category, copies);
        System.out.println("Book added successfully. ID: " + book.id);
        System.out.println("Stock: " + book.stockText());
    }

    private static void addMember() {
        int type = readInt("Membership (1 = Regular, 2 = Premium): ", 1, 2);
        String name = readText("Member name: ");
        String email = readText("Email: ");
        String phone = readText("Phone: ");
        Member member = library.addMember(type == 2, name, email, phone);
        System.out.println("Member registered. ID: " + member.id);
        System.out.println("Type: " + member.membershipType()
                + " | Max books: " + member.maxBooks()
                + " | Loan period: " + member.loanDays() + " days"
                + " | Fine: Rs. " + member.finePerDay() + " per day");
    }

    private static void borrowBook() throws LibraryException {
        if (library.books.isEmpty() || library.members.isEmpty()) {
            throw new LibraryException("Add at least one book and one member first.");
        }
        displayBooks(library.books.values());
        String memberId = readText("Member ID (for example M001): ").toUpperCase();
        String bookId = readText("Book ID (for example B101): ").toUpperCase();
        LocalDate date = readDate("Borrow date (yyyy-MM-dd, Enter for today): ");

        BorrowTransaction transaction = library.borrowBook(memberId, bookId, date);
        System.out.println("\n--------------- BORROW RECEIPT ---------------");
        System.out.println("Transaction ID : " + transaction.id);
        System.out.println("Member         : " + transaction.member.name + " (" + transaction.member.id + ")");
        System.out.println("Membership     : " + transaction.member.membershipType());
        System.out.println("Book           : " + transaction.book.title + " (" + transaction.book.id + ")");
        System.out.println("Author         : " + transaction.book.author.name);
        System.out.println("Borrow date    : " + transaction.borrowDate);
        System.out.println("Due date       : " + transaction.dueDate);
        System.out.println("----------------------------------------------");
        System.out.println("Updated stock  : " + transaction.book.stockText());
    }

    private static void returnBook() throws LibraryException {
        String transactionId = readText("Borrow transaction ID (for example TXN1001): ").toUpperCase();
        LocalDate date = readDate("Return date (yyyy-MM-dd, Enter for today): ");
        ReturnTransaction transaction = library.returnBook(transactionId, date);

        System.out.println("\n--------------- RETURN RECEIPT ---------------");
        System.out.println("Return ID      : " + transaction.id);
        System.out.println("Borrow ID      : " + transaction.borrow.id);
        System.out.println("Member         : " + transaction.borrow.member.name);
        System.out.println("Book           : " + transaction.borrow.book.title);
        System.out.println("Due date       : " + transaction.borrow.dueDate);
        System.out.println("Return date    : " + transaction.returnDate);
        System.out.println("Days late      : " + transaction.daysLate);
        System.out.printf("Fine payable   : Rs. %.2f%n", transaction.fine);
        System.out.println("----------------------------------------------");
        System.out.println("Updated stock  : " + transaction.borrow.book.stockText());
    }

    private static void displayAvailableBooks() {
        List<Book> available = new ArrayList<>();
        for (Book book : library.books.values()) {
            if (book.availableCopies > 0) available.add(book);
        }
        if (available.isEmpty()) {
            System.out.println("No books are currently available.");
        } else {
            displayBooks(available);
        }
    }

    private static void displayBooks(Collection<Book> books) {
        System.out.printf("%-7s %-25s %-20s %-15s %s%n", "ID", "TITLE", "AUTHOR", "CATEGORY", "STOCK");
        System.out.println("--------------------------------------------------------------------------");
        for (Book b : books) {
            System.out.printf("%-7s %-25s %-20s %-15s %s%n",
                    b.id, shorten(b.title, 25), shorten(b.author.name, 20),
                    shorten(b.category, 15), b.availableCopies + "/" + b.totalCopies);
        }
    }

    private static String shorten(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }

    private static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = sc.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("Input cannot be empty. Please try again.");
        }
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(sc.nextLine().trim());
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {
                // Show the same validation message below.
            }
            System.out.println("Enter a whole number between " + min + " and " + max + ".");
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = sc.nextLine().trim();
            if (value.isEmpty()) return LocalDate.now();
            try {
                return LocalDate.parse(value);
            } catch (Exception e) {
                System.out.println("Enter a valid date in yyyy-MM-dd format.");
            }
        }
    }

    private static class Author {
        String name;
        String country;

        Author(String name, String country) {
            this.name = name;
            this.country = country;
        }
    }

    private static class Book {
        String id;
        String title;
        Author author;
        String category;
        int totalCopies;
        int availableCopies;

        Book(String id, String title, Author author, String category, int copies) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.category = category;
            this.totalCopies = copies;
            this.availableCopies = copies;
        }

        String stockText() {
            return availableCopies + " of " + totalCopies + " copies available";
        }
    }

    private static class Member {
        String id;
        String name;
        String email;
        String phone;
        List<BorrowTransaction> activeLoans = new ArrayList<>();

        Member(String id, String name, String email, String phone) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
        }

        int maxBooks() { return 3; }
        int loanDays() { return 14; }
        double finePerDay() { return 5.0; }
        String membershipType() { return "Regular"; }
    }

    private static class PremiumMember extends Member {
        PremiumMember(String id, String name, String email, String phone) {
            super(id, name, email, phone);
        }

        @Override int maxBooks() { return 6; }
        @Override int loanDays() { return 30; }
        @Override double finePerDay() { return 2.0; }
        @Override String membershipType() { return "Premium"; }
    }

    private static class BorrowTransaction {
        String id;
        Book book;
        Member member;
        LocalDate borrowDate;
        LocalDate dueDate;
        boolean returned;

        BorrowTransaction(String id, Book book, Member member, LocalDate date) {
            this.id = id;
            this.book = book;
            this.member = member;
            this.borrowDate = date;
            this.dueDate = date.plusDays(member.loanDays());
        }
    }

    private static class ReturnTransaction {
        String id;
        BorrowTransaction borrow;
        LocalDate returnDate;
        long daysLate;
        double fine;

        ReturnTransaction(String id, BorrowTransaction borrow, LocalDate date) {
            this.id = id;
            this.borrow = borrow;
            this.returnDate = date;
            this.daysLate = Math.max(0, ChronoUnit.DAYS.between(borrow.dueDate, date));
            this.fine = daysLate * borrow.member.finePerDay();
        }
    }

    private static class Library {
        Map<String, Book> books = new LinkedHashMap<>();
        Map<String, Member> members = new LinkedHashMap<>();
        Map<String, BorrowTransaction> borrows = new LinkedHashMap<>();
        int bookCounter = 100;
        int memberCounter = 0;
        int borrowCounter = 1000;
        int returnCounter = 5000;

        Book addBook(String title, String authorName, String country, String category, int copies) {
            String id = "B" + (++bookCounter);
            Book book = new Book(id, title, new Author(authorName, country), category, copies);
            books.put(id, book);
            return book;
        }

        Member addMember(boolean premium, String name, String email, String phone) {
            String id = String.format("M%03d", ++memberCounter);
            Member member = premium
                    ? new PremiumMember(id, name, email, phone)
                    : new Member(id, name, email, phone);
            members.put(id, member);
            return member;
        }

        BorrowTransaction borrowBook(String memberId, String bookId, LocalDate date)
                throws LibraryException {
            Member member = members.get(memberId);
            if (member == null) throw new LibraryException("Member not found: " + memberId);
            Book book = books.get(bookId);
            if (book == null) throw new LibraryException("Book not found: " + bookId);
            if (member.activeLoans.size() >= member.maxBooks()) {
                throw new LibraryException("Borrowing limit reached for this member.");
            }
            for (BorrowTransaction t : member.activeLoans) {
                if (t.book.id.equals(book.id)) {
                    throw new LibraryException("This member has already borrowed this book.");
                }
            }
            if (book.availableCopies <= 0) throw new LibraryException("This book is out of stock.");
            book.availableCopies--;
            BorrowTransaction transaction =
                    new BorrowTransaction("TXN" + (++borrowCounter), book, member, date);
            borrows.put(transaction.id, transaction);
            member.activeLoans.add(transaction);
            return transaction;
        }

        ReturnTransaction returnBook(String borrowId, LocalDate date) throws LibraryException {
            BorrowTransaction borrow = borrows.get(borrowId);
            if (borrow == null) throw new LibraryException("Borrow transaction not found: " + borrowId);
            if (borrow.returned) throw new LibraryException("This transaction has already been returned.");
            if (date.isBefore(borrow.borrowDate)) {
                throw new LibraryException("Return date cannot be before the borrow date.");
            }
            ReturnTransaction transaction =
                    new ReturnTransaction("RET" + (++returnCounter), borrow, date);
            borrow.returned = true;
            if (borrow.book.availableCopies < borrow.book.totalCopies) borrow.book.availableCopies++;
            borrow.member.activeLoans.remove(borrow);
            return transaction;
        }
    }

    private static class LibraryException extends Exception {
        LibraryException(String message) {
            super(message);
        }
    }
}
