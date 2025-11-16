import java.io.*;
import java.util.*;
import java.time.LocalDate;

/**
 * City Library Digital Management System
 * Demonstrates:
 * - File Handling (Binary Serialization)
 * - Collections Framework
 * - Comparable and Comparator
 * - Buffered I/O
 * - Generics
 *
 * Compile:  javac LibraryApp.java
 * Run:      java LibraryApp
 */

public class LibraryApp {

    private static final String BOOKS_FILE = "books.dat";
    private static final String MEMBERS_FILE = "members.dat";
    private static final String LOG_FILE = "transactions.log";

    public static void main(String[] args) {

        Library library = new Library();
        library.loadState();

        Scanner sc = new Scanner(System.in);
        boolean exit = false;

        System.out.println("\n=== City Library Digital Management System ===");

        while (!exit) {

            System.out.println("\n1. Add Book");
            System.out.println("2. Add Member");
            System.out.println("3. Issue Book");
            System.out.println("4. Return Book");
            System.out.println("5. Show All Books");
            System.out.println("6. Show All Members");
            System.out.println("7. Sort Books (by Title)");
            System.out.println("8. Sort Members (by Name)");
            System.out.println("9. Save & Exit");
            System.out.print("➤ Enter choice: ");

            switch (sc.nextLine().trim()) {

                case "1": addBook(sc, library); break;
                case "2": addMember(sc, library); break;
                case "3": issueBook(sc, library); break;
                case "4": returnBook(sc, library); break;
                case "5": library.printBooks(); break;
                case "6": library.printMembers(); break;
                case "7": library.sortBooks(); break;
                case "8": library.sortMembers(); break;

                case "9":
                    library.saveState();
                    System.out.println("✔ Saved. Exiting...");
                    exit = true;
                    break;

                default: System.out.println("Invalid choice.");
            }
        }
    }

    // ----------------------------- INTERACTION FUNCTIONS -----------------------------

    static void addBook(Scanner sc, Library library) {
        System.out.print("Book ID: ");
        String id = sc.nextLine();

        System.out.print("Title: ");
        String title = sc.nextLine();

        System.out.print("Author: ");
        String author = sc.nextLine();

        System.out.print("Total copies: ");
        int copies = Integer.parseInt(sc.nextLine());

        Book b = new Book(id, title, author, copies);

        if (library.addBook(b)) System.out.println("✔ Book added.");
        else System.out.println("✘ Book ID already exists.");
    }

    static void addMember(Scanner sc, Library library) {
        System.out.print("Member ID: ");
        String id = sc.nextLine();

        System.out.print("Name: ");
        String name = sc.nextLine();

        Member m = new Member(id, name);

        if (library.addMember(m)) System.out.println("✔ Member added.");
        else System.out.println("✘ Member ID already exists.");
    }

    static void issueBook(Scanner sc, Library library) {
        System.out.print("Member ID: ");
        String mid = sc.nextLine();

        System.out.print("Book ID: ");
        String bid = sc.nextLine();

        if (library.issueBook(mid, bid)) System.out.println("✔ Issued.");
        else System.out.println("✘ Issue failed.");
    }

    static void returnBook(Scanner sc, Library library) {
        System.out.print("Member ID: ");
        String mid = sc.nextLine();

        System.out.print("Book ID: ");
        String bid = sc.nextLine();

        if (library.returnBook(mid, bid)) System.out.println("✔ Returned.");
        else System.out.println("✘ Return failed.");
    }

    // ----------------------------- BOOK CLASS -----------------------------

    static class Book implements Serializable, Comparable<Book> {

        private String id, title, author;
        private int total, available;

        public Book(String id, String title, String author, int total) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.total = total;
            this.available = total;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public int getAvailable() { return available; }

        public boolean issue() {
            if (available > 0) {
                available--;
                return true;
            }
            return false;
        }

        public boolean receive() {
            if (available < total) {
                available++;
                return true;
            }
            return false;
        }

        @Override
        public int compareTo(Book o) {
            return this.title.compareToIgnoreCase(o.title);
        }

        @Override
        public String toString() {
            return String.format("Book[%s] \"%s\" by %s | %d/%d available",
                    id, title, author, available, total);
        }
    }

    // ----------------------------- MEMBER CLASS -----------------------------

    static class Member implements Serializable {

        private String id, name;
        private List<String> issuedBooks = new ArrayList<>();

        public Member(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public List<String> getIssuedBooks() { return issuedBooks; }

        public boolean borrow(String bookId) {
            if (issuedBooks.size() >= 5) return false;
            issuedBooks.add(bookId);
            return true;
        }

        public boolean giveBack(String bookId) {
            return issuedBooks.remove(bookId);
        }

        @Override
        public String toString() {
            return String.format("Member[%s] %s | Books issued: %d",
                    id, name, issuedBooks.size());
        }
    }

    // ----------------------------- LIBRARY CLASS -----------------------------

    static class Library {

        private Map<String, Book> books = new HashMap<>();
        private Map<String, Member> members = new HashMap<>();

        // Add Book
        public boolean addBook(Book b) {
            if (books.containsKey(b.getId())) return false;
            books.put(b.getId(), b);
            log("Added Book: " + b);
            return true;
        }

        // Add Member
        public boolean addMember(Member m) {
            if (members.containsKey(m.getId())) return false;
            members.put(m.getId(), m);
            log("Added Member: " + m);
            return true;
        }

        // Issue Book
        public boolean issueBook(String memberId, String bookId) {

            Member m = members.get(memberId);
            Book b = books.get(bookId);

            if (m == null || b == null) return false;
            if (!b.issue()) return false;
            if (!m.borrow(bookId)) { b.receive(); return false; }

            log("Issued: Book " + bookId + " to Member " + memberId);
            return true;
        }

        // Return Book
        public boolean returnBook(String memberId, String bookId) {

            Member m = members.get(memberId);
            Book b = books.get(bookId);

            if (m == null || b == null) return false;
            if (!m.giveBack(bookId)) return false;

            b.receive();
            log("Returned: Book " + bookId + " by Member " + memberId);
            return true;
        }

        // Print Books
        public void printBooks() {
            if (books.isEmpty()) System.out.println("No books.");
            else books.values().forEach(System.out::println);
        }

        // Print Members
        public void printMembers() {
            if (members.isEmpty()) System.out.println("No members.");
            else members.values().forEach(System.out::println);
        }

        // Sorting
        public void sortBooks() {
            List<Book> list = new ArrayList<>(books.values());
            Collections.sort(list);
            list.forEach(System.out::println);
        }

        public void sortMembers() {
            List<Member> list = new ArrayList<>(members.values());
            list.sort(Comparator.comparing(Member::getName, String.CASE_INSENSITIVE_ORDER));
            list.forEach(System.out::println);
        }

        // ----------------------------- FILE HANDLING -----------------------------

        public void saveState() {
            saveObject(books, BOOKS_FILE);
            saveObject(members, MEMBERS_FILE);
            log("State Saved");
        }

        @SuppressWarnings("unchecked")
        public void loadState() {
            Object b = loadObject(BOOKS_FILE);
            Object m = loadObject(MEMBERS_FILE);

            if (b instanceof Map) books = (Map<String, Book>) b;
            if (m instanceof Map) members = (Map<String, Member>) m;
        }

        void saveObject(Object obj, String filename) {
            try (ObjectOutputStream oos =
                    new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename)))) {
                oos.writeObject(obj);
            } catch (Exception e) {
                System.out.println("Error saving: " + filename);
            }
        }

        Object loadObject(String filename) {
            try (ObjectInputStream ois =
                    new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
                return ois.readObject();
            } catch (Exception e) {
                return null;
            }
        }

        // ----------------------------- LOGGING -----------------------------

        void log(String msg) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
                bw.write(LocalDate.now() + " - " + msg);
                bw.newLine();
            } catch (Exception ignored) {}
        }
    }
}
