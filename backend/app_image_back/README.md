# APP_IMAGE_BACK

## Overview

The `APP_IMAGE_BACK` module serves as the backend implementation for managing images within a web application. Built using Java (Spring Boot) and SQL, this backend handles server requests, database interactions, image processing, and more.

## Purpose

The purpose of this module is to provide functionalities for uploading, storing, processing, and retrieving images within a web application. It enables users to perform various actions such as uploading images, applying filters, comparing image histograms, and interacting with the database.

## Components

### ImageController

The `ImageController` handles incoming HTTP requests related to image management. It contains methods for uploading, deleting, and retrieving images, as well as applying filters and comparing image histograms.

### ImageDao

The `ImageDao` establishes a connection between the server and the database using SQL queries. It provides methods for storing and retrieving image data from the database.

### ImageProcessing

The `ImageProcessing` component includes methods for applying filters and performing image processing operations such as resizing, cropping, and rotating images.

## Usage

To use the backend module, follow these steps:

1. Clone the repository and navigate to the `APP_IMAGE_BACK` directory.
2. Build the backend using Maven:
```
mvn clean install
```
3. Run the backend using Spring Boot:
```
mvn spring-boot:run
```
4. The endpoints will now be available through the 8181 port : for example, http://localhost:8181/images will return a JSON with the metadata of the images on the server

* Once the project built, you can access the documentation located in `backend/app_image_back/target/apidocs/index.html`

## Database Access

The backend connects to a PostgreSQL database with the following access settings:

`spring.datasource.url=jdbc:postgresql://pgsql:5432/zsaidalichei`\
`spring.datasource.username=zsaidalichei`\
`spring.datasource.password=Zeyadsaid_000`\

## Testing

Automated tests for the backend functionality can be found in the `test` directory, specifically in the `ImageControllerTests.java` file. These tests utilize MockMvc to simulate HTTP requests and verify the behavior of the endpoints.

## Deployment

To deploy the backend, follow standard deployment procedures for Spring Boot applications. Ensure that environment-specific configurations are updated accordingly, such as database credentials and server ports.
