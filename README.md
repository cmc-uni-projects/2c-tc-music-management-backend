# CMCmp3 Spring Boot Application

This is a Spring Boot application for CMCmp3, a music streaming service.

## Prerequisites

- Java 21
- Maven
- MySQL

## Configuration

1.  **Database Configuration**:
    Open `src/main/resources/application.properties` and update the following properties to match your MySQL database configuration:
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/musicapp?allowPublicKeyRetrieval=true&useSSL=false
    spring.datasource.username=root
    spring.datasource.password=your-password
    ```

2.  **Firebase Configuration**:
    This project uses Firebase for file storage. You need to provide your Firebase service account credentials and bucket name.

    1.  Obtain your Firebase service account key file (`.json`) from the Firebase console.
    2.  Rename the file to `firebase-service-account.json` and place it in the `src/main/resources` directory.
    3.  Open `src/main/resources/application.properties` and update the following property with your Firebase Storage bucket name:
        ```properties
        firebase.bucket.name=your-bucket-name
        ```

## Build

To build the project, run the following command from the project root directory:

```bash
./mvnw clean install
```

## Run


To run the application, use the following command:


```bash

./mvnw spring-boot:run
```

The application will start on port `8080`.

