# Object Recognition Module

The Object Recognition module is a Java library designed to facilitate object detection and labeling within images using the YOLO (You Only Look Once) deep learning algorithm implemented with OpenCV. This module provides functionalities to detect objects, draw bounding boxes around them, and label the detected objects with their corresponding class names and confidence scores.

This module is designed to be used with the version 3 of the YOLO model which can be found [here](https://pjreddie.com/darknet/yolo/)

## Features

- **Object Detection:** Detect objects within images using the YOLO deep learning algorithm.
- **Bounding Box Visualization:** Draw bounding boxes around detected objects.
- **Object Labeling:** Label detected objects with their corresponding class names and confidence scores.

## Usage

To use the Object Recognition module in your Java application, follow these steps:

1. **Instantiate the `ObjectsRecognition` Class:** Create an instance of the `ObjectsRecognition` class by providing the paths to the label file, model file, and configuration file.
2. **Detect Objects:** Use the `detectObjects` method to detect objects within an input image. This method returns a HashMap containing the detected objects along with their bounding boxes, class labels, and confidence scores.
3. **Visualize Detection Results:** Visualize the detection results by drawing bounding boxes around the detected objects and labeling them with their class names and confidence scores.

* Once the project built, you can access the documentation located in `backend/object_recognition/target/apidocs/index.html`

## Example

```java
// Instantiate ObjectsRecognition class
ObjectsRecognition recognizer = new ObjectsRecognition("labels.names", "model.weights", "model.cfg");

// Load input image
BufferedImage inputImage = ImageIO.read(new File("input.jpg"));

// Detect objects in the input image
HashMap<Frame, ArrayList<Map<String, Object>>> detectionResults = recognizer.detectObjects(inputImage, 1);

// Visualize detection results
BufferedImage outputImage = Labeling.labelImage(inputImage, detectionResults);
