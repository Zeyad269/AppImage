# BACKEND Implementation of the Web Application

## Implementation

The backend of this project consists of two modules:

1. [app_image_back](app_image_back/README.md): This module handles the core functionalities of the web application, including user authentication, image storage, and API endpoints for interacting with the frontend.

2. [object_recognition](object_recognition/README.md): This module provides object recognition capabilities using the YOLO (You Only Look Once) deep learning algorithm implemented with OpenCV. It offers functionalities to detect objects within images, draw bounding boxes around them, and label the detected objects with their corresponding class names and confidence scores.

Please refer to the respective README files for detailed information on each module.


## Usage

To use the backend module, follow these steps:

1. Clone the repository and navigate to the `BACKEND` directory.
2. Build the backend using Maven:
```
mvn clean install
```
3. Run the backend using Spring Boot:
```
mvn --projects backend/app_image_back spring-boot:run
```