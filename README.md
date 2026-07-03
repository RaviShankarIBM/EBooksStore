
<html lang="en">
<head>
<meta charset="UTF-8" />
<title>E-Book Store REST API</title>
<style>
  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: -apple-system, "Segoe UI", system-ui, sans-serif; font-size: 14px; line-height: 1.6; background: #ffffff; color: #1f2328; }
  .wrap { max-width: 760px; margin: 0 auto; padding: 32px 20px; }
  h1 { font-size: 22px; font-weight: 700; margin-bottom: 4px; }
  h2 { font-size: 16px; font-weight: 700; margin: 28px 0 10px; border-bottom: 1px solid #e5e7eb; padding-bottom: 6px; }
  h3 { font-size: 14px; font-weight: 600; margin: 18px 0 6px; }
  p { margin-bottom: 10px; color: #1f2328; }
  .subtitle { color: #57606a; margin-bottom: 24px; font-size: 13px; }
  .badge { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; letter-spacing: 0.4px; color: #fff; margin-right: 6px; }
  .GET    { background: #3b82d4; }
  .POST   { background: #16a34a; }
  .PUT    { background: #d97706; }
  .DELETE { background: #dc2626; }
  table { width: 100%; border-collapse: collapse; margin-bottom: 16px; font-size: 13px; }
  th { background: #f7f8fa; text-align: left; padding: 7px 10px; border: 1px solid #e5e7eb; font-weight: 600; }
  td { padding: 7px 10px; border: 1px solid #e5e7eb; vertical-align: top; }
  code { background: #f7f8fa; padding: 1px 5px; border-radius: 3px; font-size: 12px; font-family: "Consolas", monospace; border: 1px solid #e5e7eb; }
  pre { background: #f7f8fa; border: 1px solid #e5e7eb; border-radius: 5px; padding: 12px 14px; overflow-x: auto; font-size: 12px; line-height: 1.55; margin-bottom: 12px; font-family: "Consolas", monospace; }
  .endpoint-row { display: flex; align-items: flex-start; margin-bottom: 10px; gap: 10px; }
  .endpoint-path { font-family: "Consolas", monospace; font-size: 13px; }
  .endpoint-desc { color: #57606a; font-size: 12px; margin-top: 2px; }
  .section { background: #f7f8fa; border: 1px solid #e5e7eb; border-radius: 6px; padding: 14px 16px; margin-bottom: 16px; }
  .tree { font-family: "Consolas", monospace; font-size: 12px; line-height: 1.7; }
  .tag { display: inline-block; background: #eef2ff; color: #3b5fc0; border-radius: 3px; padding: 1px 6px; font-size: 11px; font-weight: 600; }
  footer { text-align: center; font-size: 12px; color: #57606a; border-top: 1px solid #e5e7eb; margin-top: 40px; padding-top: 16px; }
</style>
</head>
<body>
<div class="wrap">

  <h1>📚 E-Book Store REST API</h1>
  <p class="subtitle">Spring Boot 3.2 · Java 17 · JPA · H2 In-Memory Database</p>

  <h2>Project Structure</h2>
  <div class="section">
    <pre class="tree">ebook-store/
├── pom.xml
└── src/main/
    ├── resources/
    │   └── application.properties
    └── java/com/ebookstore/
        ├── EbookStoreApplication.java
        ├── config/
        │   └── DataSeeder.java            ← seeds 6 sample books on startup
        ├── entity/
        │   ├── Book.java
        │   ├── User.java
        │   └── UserBook.java              ← join table: user ↔ book
        ├── repository/
        │   ├── BookRepository.java
        │   ├── UserRepository.java
        │   └── UserBookRepository.java
        ├── service/
        │   ├── BookService.java
        │   ├── UserService.java
        │   └── UserBookService.java
        ├── controller/
        │   ├── BookController.java
        │   ├── UserController.java
        │   └── UserBookController.java
        ├── dto/
        │   ├── BookRequest.java
        │   ├── BookResponse.java
        │   ├── UserRegistrationRequest.java
        │   ├── UserResponse.java
        │   ├── UserBookResponse.java
        │   └── ApiResponse.java           ← generic wrapper {success, message, data}
        └── exception/
            ├── GlobalExceptionHandler.java
            ├── ResourceNotFoundException.java
            └── ResourceAlreadyExistsException.java</pre>
  </div>

  
  <h2>User Endpoints — <code>/api/users</code></h2>

  <div class="endpoint-row">
    <div><span class="badge POST">POST</span></div>
    <div><div class="endpoint-path">/api/users/register</div><div class="endpoint-desc">Register a new user</div></div>
  </div>
  <pre>{
  "name":     "Alice Smith",
  "email":    "alice@example.com",
  "password": "secret123"
}</pre>

  <div class="endpoint-row">
    <div><span class="badge GET">GET</span></div>
    <div><div class="endpoint-path">/api/users</div><div class="endpoint-desc">Get all registered users</div></div>
  </div>

  <div class="endpoint-row">
    <div><span class="badge GET">GET</span></div>
    <div><div class="endpoint-path">/api/users/{id}</div><div class="endpoint-desc">Get user by ID</div></div>
  </div>

  <div class="endpoint-row">
    <div><span class="badge GET">GET</span></div>
    <div><div class="endpoint-path">/api/users/email/{email}</div><div class="endpoint-desc">Get user by email</div></div>
  </div>

  <div class="endpoint-row">
    <div><span class="badge DELETE">DELETE</span></div>
    <div><div class="endpoint-path">/api/users/{id}</div><div class="endpoint-desc">Delete user</div></div>
  </div>

  
  <h2>Book Endpoints — <code>/api/books</code></h2>

  <div class="endpoint-row">
    <div><span class="badge POST">POST</span></div>
    <div><div class="endpoint-path">/api/books</div><div class="endpoint-desc">Add a book to the store</div></div>
  </div>
  <pre>{
  "title":       "Clean Code",
  "author":      "Robert C. Martin",
  "description": "A handbook of agile software craftsmanship",
  "price":       29.99,
  "genre":       "Technology",
  "available":   true
}</pre>

  <div class="endpoint-row">
    <div><span class="badge GET">GET</span></div>
    <div><div class="endpoint-path">/api/books</div><div class="endpoint-desc">Get all books (optional: <code>?title=</code>, <code>?author=</code>, <code>?genre=</code>)</div></div>
  </div>

  <div class="endpoint-row">
    <div><span class="badge GET">GET</span></div>
    <div><div class="endpoint-path">/api/books/available</div><div class="endpoint-desc">Get all available books</div></div>
  </div>

  <div class="endpoint-row">
    <div><span class="badge GET">GET</span></div>
    <div><div class="endpoint-path">/api/books/{id}</div><div class="endpoint-desc">Get a book by ID</div></div>
  </div>

  <div class="endpoint-row">
    <div><span class="badge PUT">PUT</span></div>
    <div><div class="endpoint-path">/api/books/{id}</div><div class="endpoint-desc">Update book details</div></div>
  </div>

  <div class="endpoint-row">
    <div><span class="badge DELETE">DELETE</span></div>
    <div><div class="endpoint-path">/api/books/{id}</div><div class="endpoint-desc">Remove a book</div></div>
  </div>

  
  <h2>User-Book Access Endpoints — <code>/api/users/{userId}/books</code></h2>

  <div class="endpoint-row">
    <div><span class="badge POST">POST</span></div>
    <div><div class="endpoint-path">/api/users/{userId}/books/{bookId}</div><div class="endpoint-desc">User accesses (purchases) a book from the store</div></div>
  </div>

  <div class="endpoint-row">
    <div><span class="badge GET">GET</span></div>
    <div><div class="endpoint-path">/api/users/{userId}/books</div><div class="endpoint-desc">Get all books in a user's library</div></div>
  </div>

  <div class="endpoint-row">
    <div><span class="badge DELETE">DELETE</span></div>
    <div><div class="endpoint-path">/api/users/{userId}/books/{bookId}</div><div class="endpoint-desc">Remove a book from user's library</div></div>
  </div>

  
  <h2>Response Format</h2>
  <p>All endpoints return a unified <code>ApiResponse&lt;T&gt;</code> wrapper:</p>
  <pre>{
  "success": true,
  "message": "Book accessed successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "userName": "Alice Smith",
    "bookId": 3,
    "bookTitle": "Design Patterns",
    "bookAuthor": "Gang of Four",
    "accessedAt": "2024-01-15T10:30:00"
  }
}</pre>

  
  <h2>HTTP Status Codes</h2>
  <table>
    <tr><th>Status</th><th>Scenario</th></tr>
    <tr><td><code>200 OK</code></td><td>GET / DELETE / PUT success</td></tr>
    <tr><td><code>201 Created</code></td><td>POST success (new resource created)</td></tr>
    <tr><td><code>400 Bad Request</code></td><td>Validation error or unavailable book</td></tr>
    <tr><td><code>404 Not Found</code></td><td>User or Book not found</td></tr>
    <tr><td><code>409 Conflict</code></td><td>Duplicate email / user already has book</td></tr>
    <tr><td><code>500 Internal Server Error</code></td><td>Unexpected error</td></tr>
  </table>

  
  <h2>How to Run</h2>
  <h3>Prerequisites</h3>
  <p>Java 17 ✅ is installed. Install Maven if not already:</p>
  <pre>winget install Apache.Maven</pre>

  <h3>Build &amp; Start</h3>
  <pre>cd ebook-store
mvn clean package -DskipTests
mvn spring-boot:run</pre>

  <p>Or run the jar:</p>
  <pre>java -jar target/ebook-store-1.0.0.jar</pre>

  <h3>H2 Console (Browser)</h3>
  <pre>http://localhost:8080/h2-console
JDBC URL:  jdbc:h2:mem:ebookdb
Username:  sa
Password:  (empty)</pre>

  <h3>Quick Test with curl</h3>
  <pre># Register a user
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@test.com","password":"pass123"}'

# Browse all books (6 pre-seeded)
curl http://localhost:8080/api/books

# Search by genre
curl "http://localhost:8080/api/books?genre=Technology"

# User (id=1) accesses book (id=1)
curl -X POST http://localhost:8080/api/users/1/books/1

# View user's library
curl http://localhost:8080/api/users/1/books</pre>

  <footer>Made with IBM Bob</footer>
</div>
</body>
</html>
