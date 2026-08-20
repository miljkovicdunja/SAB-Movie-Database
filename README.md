# SAB Project — Movie Database Operations (JDBC)

A Java project implementing a set of predefined interfaces for interacting with a Microsoft SQL Server database of movies, genres, ratings, tags, users, and watchlists. Built as part of a university Database Systems (SAB) course.

This is a student project built for a university Database Systems course, following a fixed interface template provided by the instructors — the overall architecture (single `DB` connection class, direct JDBC calls) reflects that assignment structure rather than a from-scratch design choice.

## Tech Stack

- **Language:** Java
- **Database:** Microsoft SQL Server (via JDBC)
- **Testing:** JUnit 4 (via provided public test JAR)

## Project Structure

```
md210500/
├── student/                    # Implementation of the required interfaces
│   ├── DB.java                 # Database connection setup
│   ├── md210500_GeneralOperations.java
│   ├── md210500_MoviesOperations.java
│   ├── md210500_GenresOperations.java
│   ├── md210500_RatingsOperations.java
│   ├── md210500_TagsOperations.java
│   ├── md210500_UsersOperations.java
│   └── md210500_WatchlistsOperations.java
├── libraries/                  # Required JARs (interfaces, test suite, JDBC driver)
├── md210500.sql                # Database schema/data (MySQL-style dump)
├── md210500-tsql.sql           # Database schema/data (T-SQL, for SQL Server)
└── dokumentacija/              # Generated Javadoc for the interfaces 
```

## Setup

### 1. Set Up the Database

Install Microsoft SQL Server (Express edition works fine for local development). Create a database named `md210500` and import the schema from `md210500-tsql.sql`.

### 2. Import the Project into IntelliJ (or another IDE)

1. Open the project folder in IntelliJ.
2. Go to **File → Project Structure → Libraries**, and add both `.jar` files from the `libraries/` folder:
   - `SAB_projekat_2026_javni_test.jar` (contains the interfaces to implement and the public test suite)
   - `mssql-jdbc-7.0.0.jre8.jar` (JDBC driver for SQL Server)
3. Make sure the `student` package is marked as a source root.

### 3. Configure the Database Connection

Connection settings are defined in `student/DB.java`:

```java
private static final String username = "sa";
private static final String password = "123";
private static final String database = "md210500";
private static final int port = 1433;
private static final String serverName = "localhost";
```

Update these values to match your local SQL Server setup before running.

### 4. Run

Use the provided test suite (from the JAR) or a `StudentMain` class to execute the implemented operations against the database.