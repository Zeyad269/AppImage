package pdl.object_recognition;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect2d;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.utils.Converters;

import nu.pattern.OpenCV;

/**
 * Provides functionality for object recognition using a pre-trained deep learning model.
 */
public class ObjectsRecognition {

    Net dnnNet;
    List<String> labels;
    boolean init = false;

    /**
     * Constructs an ObjectsRecognition instance.
     * @param labelsPath The path to the file containing class labels.
     * @param modelPath The path to the pre-trained model file.
     * @param cfgPath The path to the configuration file.
     */
    public ObjectsRecognition(String labelsPath, String modelPath, String cfgPath){
        OpenCV.loadLocally();

        try {
            Scanner scan = new Scanner(new FileReader(labelsPath));
            this.labels = new ArrayList<String>();
            while(scan.hasNextLine()) {
                labels.add(scan.nextLine());        
            }
            scan.close();
            
            this.dnnNet = Dnn.readNetFromDarknet(cfgPath,
            modelPath);

            this.dnnNet.setPreferableBackend(Dnn.DNN_BACKEND_CUDA);
            this.dnnNet.setPreferableTarget(Dnn.DNN_TARGET_CUDA);
            this.init = true;
        }
        catch (Exception e){
            e.printStackTrace();
            System.err.println("Objects recogniton not loaded properly");
        }
        
    }

    /**
     * Checks if the object recognition system is properly initialized.
     * @return False if initialized, true otherwise.
     */
    public boolean isEmpty(){
        return !this.init;
    }

    /**
     * Detects objects in a given image.
     * @param buffImg The BufferedImage to process.
     * @param maxLabelNb The maximum number of labels to return per frame.
     * @return A HashMap containing detected objects and their information within frames.
     */
    public HashMap<Frame, ArrayList<Map<String, Object>>> detectObjects(BufferedImage buffImg, int maxLabelNb) {

        if (!this.init) {
            return new HashMap<>();
        }
        
        byte[] pixels = ((DataBufferByte) buffImg.getData().getDataBuffer()).getData();
        Mat img = new Mat(buffImg.getHeight(), buffImg.getWidth(), CvType.CV_8UC3);
        img.put(0, 0, pixels);
        //  -- determine  the output layer names that we need from YOLO
        List<String> layerNames = this.dnnNet.getLayerNames();
        List<String> outputLayers = new ArrayList<String>();
        for (Integer i : dnnNet.getUnconnectedOutLayers().toList()) {
            outputLayers.add(layerNames.get(i - 1));
        }
        HashMap<Frame, ArrayList<Map<String, Object>>> result = forwardImageOverNetwork(img, dnnNet, outputLayers);

        ArrayList<Float> confList = new ArrayList<>();

        ArrayList<Frame> frameArray = new ArrayList<>();
        frameArray.addAll(result.keySet());

        if (frameArray.size() == 0) {
            return new HashMap<>();
        }
        for (Frame frame : frameArray) {
            confList.add(maxval(result.get(frame), "confidence"));
        }

        MatOfInt indices = getBBoxIndicesFromNonMaximumSuppression(frameArray, confList);
        if (indices.empty()) {
            return new HashMap<>();
        }

        List<Integer> indicesList = indices.toList();

        HashMap<Frame, ArrayList<Map<String, Object>>> resultMap = new HashMap<>();
        for (int i = 0; i < frameArray.size(); i++) {
            if (indicesList.contains(i)) {
                ArrayList<Map<String, Object>> sortedRes = result.get(frameArray.get(i));
                sortedRes.sort(Comparator.comparing(m -> (Float)m.get("confidence")));

                ArrayList<Map<String, Object>> res = new ArrayList<>(sortedRes.subList(0, Math.min(maxLabelNb, sortedRes.size())));
                resultMap.put(frameArray.get(i), res);
            }
        }
        return resultMap;  
    }  

    /**
     * Performs forward pass of the image through the network.
     * @param img The input image.
     * @param dnnNet The neural network.
     * @param outputLayers The list of output layers.
     * @return A HashMap containing detected objects and their information within frames.
     */
    private HashMap<Frame, ArrayList<Map<String, Object>>> forwardImageOverNetwork(Mat img,
                                                  Net dnnNet,
                                                  List<String> outputLayers) {
    // --We need to prepare some data structure  in order to store the data returned by the network  (ie, after Net.forward() call))
        // So, Initialize our lists of detected bounding boxes, confidences, and  class IDs, respectively
    // This is what this method will return:
        HashMap<Frame, ArrayList<Map<String, Object>>> result = new HashMap<>();

        // -- The input image to a neural network needs to be in a certain format called a blob.
        //  In this process, it scales the image pixel values to a target range of 0 to 1 using a scale factor of 1/255.
        // It also resizes the image to the given size of (416, 416) without cropping
        // Construct a blob from the input image and then perform a forward  pass of the YOLO object detector,
        // giving us our bounding boxes and  associated probabilities:
        
        Mat blob_from_image = Dnn.blobFromImage(img, 1 / 255.0, new Size(608, 608), // Here we supply the spatial size that the Convolutional Neural Network expects.
                new Scalar(new double[]{0.0, 0.0, 0.0}), true, false);
        dnnNet.setInput(blob_from_image);

        // -- the output from network's forward() method will contain a List of OpenCV Mat object, so lets prepare one
        List<Mat> outputs = new ArrayList<Mat>();
        
        // -- Finally, let pass forward throught network. The main work is done here:  
        dnnNet.forward(outputs, outputLayers);
        
        // --Each output of the network outs (ie, each row of the Mat from 'outputs') is represented by a vector of the number
        // of classes + 5 elements.  The first 4 elements represent center_x, center_y, width and height.
        // The fifth element represents the confidence that the bounding box encloses the object.
        // The remaining elements are the confidence levels (ie object types) associated with each class.
        // The box is assigned to the category corresponding to the highest score of the box:
        
        for(Mat output : outputs) {
            for (int i = 0; i < output.rows(); i++) {
                Mat row = output.row(i);
                List<Float> detect = new MatOfFloat(row).toList();
                List<Float> score = detect.subList(5, output.cols());
                int class_id = argmax(score);
                float conf = score.get(class_id);
                if (conf >= 0.5) {
                    int center_x = (int) (detect.get(0) * img.cols());
                    int center_y = (int) (detect.get(1) * img.rows());
                    int width = (int) (detect.get(2) * img.cols());
                    int height = (int) (detect.get(3) * img.rows());
                    int x = (center_x - width / 2);
                    int y = (center_y - height / 2);
                    Frame box = new Frame(x, y, x+width, y+height);
                    ArrayList<Map<String, Object>> valArray = new ArrayList<>();
                    HashMap<String, Object> valMap = new HashMap<>();
                    valMap.put("label", this.labels.get(class_id));
                    valMap.put("confidence", conf);
                    valArray.add(valMap); 
                    result.put(box, valArray);
                }
                
            }
        }
        return result;
    }

    /**
     * Finds the index of the maximum value in a list of floats.
     * @param array The list of floats.
     * @return The index of the maximum value.
     */
    private static int argmax(List<Float> array) {
        float max = array.get(0);
        int re = 0;
        for (int i = 1; i < array.size(); i++) {
            if (array.get(i) > max) {
                max = array.get(i);
                re = i;
            }
        }
        return re;
    }

    /**
     * Finds the maximum value of a specified field in a list of maps.
     * @param array The list of maps.
     * @param valLabel The label of the field whose maximum value is to be found.
     * @return The maximum value of the specified field.
     */
    private static float maxval(ArrayList<Map<String, Object>> array, String valLabel) {
        float max = (float) array.get(0).get(valLabel);
        for (int i = 1; i < array.size(); i++) {
            if ((float) array.get(i).get(valLabel) > max) {
                max = (float) array.get(i).get(valLabel);
            }
        }
        return max;
    }

    /**
     * Applies non-maximum suppression (NMS) to a list of bounding boxes based on their confidence scores.
     * @param boxes List of bounding boxes.
     * @param confidences List of confidence scores corresponding to each bounding box.
     * @return Indices of the selected bounding boxes after NMS.
     */
    private static MatOfInt getBBoxIndicesFromNonMaximumSuppression(ArrayList<Frame> boxes, ArrayList<Float> confidences) {
        MatOfInt result = new MatOfInt();
        ArrayList<Rect2d> rectBoxes = new ArrayList<>();
        for (Frame frame : boxes) {
            rectBoxes.add(frame.toRect2d());
        }
        MatOfRect2d mOfRect = new MatOfRect2d();
        mOfRect.fromList(rectBoxes);
        MatOfFloat mfConfs = new MatOfFloat(Converters.vector_float_to_Mat(confidences));
        
        Dnn.NMSBoxes(mOfRect, mfConfs, (float)(0.6), (float)(0.5), result);
        return result;
    }
}