# FitTrack — Online Fitness Tracking Application (Java)

A desktop fitness tracker built with Java Swing, JDBC and MySQL. Includes registration/login, password hashing, activity logging, goals, activity history, and a seven-day summary.

## Requirements
- JDK 17+
- Maven 3.8+
- MySQL 8+

## Setup
1. Create the database/tables by running `database/fitness_tracking.sql` in MySQL Workbench or the MySQL CLI.
2. Set database environment variables if your MySQL settings differ:
   - `FITTRACK_DB_URL` (default `jdbc:mysql://localhost:3306/fitness_tracking?useSSL=false&serverTimezone=UTC`)
   - `FITTRACK_DB_USER` (default `root`)
   - `FITTRACK_DB_PASSWORD` (default empty)
3. From the project directory, run:

   ```bash
   mvn clean compile
   mvn exec:java -Dexec.mainClass=com.fittrack.FitnessApp
   ```

   If the Maven Exec plugin is not installed/configured in your environment, add this plugin to `pom.xml` or run the compiled class with the dependency classpath from your IDE.

## Use
- Register with a name, email, and password of at least 8 characters.
- Log in with the same email/password.
- Use **Log activity** to add workouts, calories, steps, duration, and notes.
- Use **Goals** to add and review fitness targets.
- Use **Progress history** to view saved activity entries.

## Project structure
```text
online-fitness-tracking/
├── database/fitness_tracking.sql
├── src/main/java/com/fittrack/
│   ├── Database.java
│   ├── PasswordUtil.java
│   └── FitnessApp.java
├── pom.xml
└── README.md
```

## Notes
- The app is a learning/demo project, not a medical tool.
- Goal records are stored and displayed; current values are not automatically calculated from activities yet.
- For production, add account recovery, rate limiting, stronger deployment configuration, and automated tests.
