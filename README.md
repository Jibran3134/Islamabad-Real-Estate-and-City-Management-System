# Islamabad Real Estate and City Management System

A JavaFX and SQL Server management system for real estate and city authority oversight in Islamabad.

## Features
- **Property Listings**: Search and manage property listings with advanced filters.
- **Bidding System**: Live online bidding for premium properties.
- **User Management**: Role-based access for Admins, Agents, Authorities, and Buyers.
- **Sector Management**: Authority-led capacity definition and sector freezing.

## Tech Stack
- **UI**: JavaFX 26.0.1
- **Language**: Java 26
- **Database**: Microsoft SQL Server
- **Build Tool**: Maven

## Build and Run
Use the project scripts from the project root:

```powershell
.\build.ps1
.\run.ps1
```

The scripts use the Maven wrapper. Make sure Java 26 is available through `JAVA_HOME` or on your `PATH`.

You can also call Maven directly if Java is configured in your terminal:

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd javafx:run
```

If you use IntelliJ IDEA, import the project as a Maven project from `pom.xml`. The JavaFX dependencies are resolved by Maven, so you do not need to manually add the JavaFX SDK jars to the module classpath.

## Folder Structure
- `src/main/java`: Java source code.
- `src/main/resources/fxml`: JavaFX FXML views.
- `src/main/resources/css`: JavaFX styles.
- `pom.xml`: Maven configuration.
