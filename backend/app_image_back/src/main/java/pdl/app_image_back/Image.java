package pdl.app_image_back;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import java.lang.NullPointerException;
import boofcv.alg.color.ColorHsv;
import boofcv.alg.descriptor.UtilFeature;
import boofcv.alg.feature.color.Histogram_F64;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.Planar;
import pdl.object_recognition.Frame;
import pdl.object_recognition.ObjectsRecognition;

/**
 * Represents an image object with various properties and methods for image processing.
 */
public class Image {

    private static Long count = Long.valueOf(0);
    
    static String labelRes = getResPath("models/yolov3-608/coco.names");
    static String weightsRes = getResPath("models/yolov3-608/yolov3.weights");
    static String configRes = getResPath("models/yolov3-608/yolov3.cfg");

    private static ObjectsRecognition or = new ObjectsRecognition(
      labelRes, 
      weightsRes,
      configRes);
    private Long id;
    private String name;
    private int[] size;
    private byte[] data;
    private MediaType type;
    private double[] hueSatHist;
    private double[] rgbHist;
    private ArrayList<Map<String, Object>> objects;
    private ArrayList<String> tags;
    private boolean isFav;

    /**
     * Retrieves the file path of a resource.
     * 
     * @param path - The path to the resource.
     * @return The file path of the resource.
     */
    private static String getResPath(String path){
      try {
        File f = new ClassPathResource(path).getFile();
        return f.getPath();
      } catch (IOException e) {
        return "";
      }
    }

    /**
     * Retrieves the size of the image from its byte data.
     * 
     * @param data - The byte data of the image.
     * @return An array containing the width and height of the image.
     */
    private int[] getSize(final byte[] data){
        InputStream in = new ByteArrayInputStream(data);
        try {
            BufferedImage buf = ImageIO.read(in);
            int[] picSize = {buf.getWidth(), buf.getHeight()};
            return picSize;
        } catch (IOException|NullPointerException e) {
            int[] picSize = {0, 0};
            return picSize;
        }

    }

    /**
     * Retrieves the MediaType of the image from its byte data.
     * 
     * @param data - The byte data of the image.
     * @return The MediaType of the image.
     */
    private MediaType getMediaType(final byte[] data){
        InputStream in = new ByteArrayInputStream(data);
        try {
            String mimeType = URLConnection.guessContentTypeFromStream(in);
            return MediaType.valueOf(mimeType);
        } catch (IOException|InvalidMediaTypeException e) {
            return MediaType.ALL;
        }
    }

    /**
     * Computes the RGB histogram of an image.
     * 
     * @param data - The byte data of the image.
     * @return An array of doubles representing the RGB histogram.
     */
    private double[] computeRgbHistogram(byte[] data){
      if (data.length == 0){
        return new double[0];
      }

      // Creating a new image to read data from 
      Planar<GrayF32> rgb = new Planar<>(GrayF32.class, 1, 1, 4);    // Use Planar for colors and GrayF32 for more accuracy

      InputStream in = new ByteArrayInputStream(data);
      BufferedImage buffered;
      try {
        buffered = ImageIO.read(in);
      } catch (IOException e) {
        return new double[0];
      }

      // Convert the image to a format without an alpha channel
      BufferedImage rgbImage = new BufferedImage(buffered.getWidth(), buffered.getHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = rgbImage.createGraphics();
      g2d.drawImage(buffered, 0, 0, null);
      g2d.dispose();

      rgb.reshape(rgbImage.getWidth(), rgbImage.getHeight());
      ConvertBufferedImage.convertFrom(rgbImage, rgb, true);

      Histogram_F64 histogram = new Histogram_F64(10, 10, 10);
      histogram.setRange(0, 0, 255);
      histogram.setRange(1, 0, 255);
      histogram.setRange(2, 0, 255);


      final int D = histogram.getDimensions();
      int[] coordinate = new int[D];

      histogram.fill(0);

      for (int y = 0; y < rgb.getHeight(); y++) {
        int imageIndex = rgb.getStartIndex() + y*rgb.getStride();
        for (int x = 0; x < rgb.getWidth(); x++, imageIndex++) {
          float alpha = rgb.getBand(3).get(x, y);
          for (int i = 0; i < D; i++) {
            coordinate[i] = histogram.getDimensionIndex(i, rgb.getBand(i).data[imageIndex]);
          }
          int index = histogram.getIndex(coordinate);
          histogram.data[index] += 1*(alpha/255f);
        }
      }

      UtilFeature.normalizeL2(histogram);

      return histogram.data;
    }

    /**
     * Computes the Hue-Saturation histogram of an image.
     * 
     * @param data - The byte data of the image.
     * @return An array of doubles representing the Hue-Saturation histogram.
     */
    private double[] computeHsvHistogram(byte[] data){
      if (data.length == 0){
        return new double[0];
      }

      Planar<GrayF32> rgb = new Planar<>(GrayF32.class, 1, 1, 4);
      Planar<GrayF32> hsv = new Planar<>(GrayF32.class, 1, 1, 4);

      InputStream in = new ByteArrayInputStream(data);
      BufferedImage buffered;
      try {
        buffered = ImageIO.read(in);
      } catch (IOException e) {
        return new double[0];
      }

      BufferedImage rgbImage = new BufferedImage(buffered.getWidth(), buffered.getHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = rgbImage.createGraphics();
      g2d.drawImage(buffered, 0, 0, null);
      g2d.dispose();

      rgb.reshape(rgbImage.getWidth(), rgbImage.getHeight());
      hsv.reshape(rgbImage.getWidth(), rgbImage.getHeight());

      ConvertBufferedImage.convertFrom(rgbImage, rgb, true);
      ColorHsv.rgbToHsv(rgb, hsv);      // Convert from rgb to hsv 

      Planar<GrayF32> hs = hsv.partialSpectrum(0, 1);

      Histogram_F64 histogram = new Histogram_F64(12, 12);
      histogram.setRange(0, 0, 2.0*Math.PI); // range of hue is from 0 to 2PI
      histogram.setRange(1, 0, 1.0);         // range of saturation is from 0 to 1

      final int D = histogram.getDimensions();
      int[] coordinate = new int[D];

      histogram.fill(0);

      for (int y = 0; y < hs.getHeight(); y++) {
        int imageIndex = hs.getStartIndex() + y*rgb.getStride();
        for (int x = 0; x < hs.getWidth(); x++, imageIndex++) {
          float alpha = rgb.getBand(3).get(x, y);
          for (int i = 0; i < D; i++) {
            coordinate[i] = histogram.getDimensionIndex(i, hs.getBand(i).data[imageIndex]);
          }
          int index = histogram.getIndex(coordinate);
          histogram.data[index] += 1*(alpha/255f);
        }
      }

      UtilFeature.normalizeL2(histogram);

      return histogram.data;
    }

  /**
     * Identifies objects in the image.
     * 
     * @param data - The byte data of the image.
     * @return A list of maps containing object properties.
     */
  private ArrayList<Map<String, Object>> idObjects(byte[] data){
    ArrayList<Map<String, Object>> resArray = new ArrayList<>();

    if (data.length == 0) {
      return resArray;
    }

    InputStream is = new ByteArrayInputStream(data);
    BufferedImage bi;
    try {
      bi = ImageIO.read(is);
    } catch (IOException e) {
      return resArray;
    }

    BufferedImage rgbImage = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g2d = rgbImage.createGraphics();
    g2d.drawImage(bi, 0, 0, null);
    g2d.dispose();

    HashMap<Frame, ArrayList<Map<String, Object>>> objects = or.detectObjects(rgbImage, 1);

    for (Frame frm : objects.keySet()) {
      HashMap<String, Object> resMap = new HashMap<>();
      resMap.put("label", objects.get(frm).get(0).get("label"));
      resMap.put("confidence", objects.get(frm).get(0).get("confidence"));
      resMap.put("width", frm.getWidth());
      resMap.put("height", frm.getHeight());
      resMap.put("pos_x", frm.getI().getX());
      resMap.put("pos_y", frm.getI().getY());
  
      resArray.add(resMap);
    }

    return resArray;
  }

  /**
     * Generates tags for the image based on detected objects and its name.
     * 
     * @param objects - Detected objects in the image.
     * @param name - The name of the image.
     * @return A list of tags associated with the image.
     */
  private ArrayList<String> makeTags(ArrayList<Map<String, Object>> objects, String name){
    ArrayList<String> tags = new ArrayList<>();

    for (Map<String, Object> map : objects) {
      if (!tags.contains((String) map.get("label"))) {
        tags.add((String) map.get("label"));
      }
    }

    String nameTag;
    if (name.contains(".")) {
      nameTag = name.substring(0, name.lastIndexOf('.'));
    }
    else {
      nameTag = name;
    }

    if (!tags.contains(nameTag)) {
      tags.add(nameTag);
    }
    
    return tags;

  }
  
    /**
     * Constructs an Image object with provided parameters.
     * 
     * @param name - The name of the image.
     * @param data - The byte data of the image.
     * @param isFav - The favorite status of the image.
     */
    public Image(final String name, final byte[] data, boolean isFav) {
      id = count++;
      this.name = name;
      this.data = data;
      this.size = getSize(this.data);
      this.type = getMediaType(this.data);
      this.hueSatHist = computeHsvHistogram(data);
      this.rgbHist = computeRgbHistogram(data);
      this.objects = idObjects(data);
      this.tags = makeTags(this.objects, this.name);
      this.isFav = isFav;
    }

    /**
     * Constructs an Image object with provided parameters.
     * 
     * @param name - The name of the image.
     * @param data - The byte data of the image.
     * @param id - The ID of the image.
     * @param isFav - The favorite status of the image.
     */
    public Image(final String name, final byte[] data, long id, boolean isFav) {
      this.id = id;
      this.name = name;
      this.data = data;
      this.size = getSize(this.data);
      this.type = getMediaType(this.data);
      this.hueSatHist = computeHsvHistogram(data);
      this.rgbHist = computeRgbHistogram(data);
      this.objects = idObjects(data);
      this.tags = makeTags(this.objects, this.name);
      this.isFav = isFav;
    }

    /**
     * Constructs an Image object with provided parameters.
     * 
     * @param name - The name of the image.
     * @param data - The byte data of the image.
     * @param id - The ID of the image.
     * @param size - The size of the image.
     * @param type - The MediaType of the image.
     * @param hshist - The Hue-Saturation histogram of the image.
     * @param rgbhist - The RGB histogram of the image.
     * @param isFav - The favorite status of the image.
     */
    public Image(final String name, final byte[] data, long id, int[] size, MediaType type, double[] hshist, double[] rgbhist, boolean isFav) {
      this.id = id;
      this.name = name;
      this.data = data;
      this.size = size;
      this.type = type;
      this.hueSatHist = hshist;
      this.rgbHist = rgbhist;
      this.objects = idObjects(data);
      this.tags = makeTags(this.objects, this.name);
      this.isFav = isFav;
    }

    /**
     * Constructs an Image object with provided parameters.
     * 
     * @param name - The name of the image.
     * @param data - The byte data of the image.
     * @param id - The ID of the image.
     * @param size - The size of the image.
     * @param type - The MediaType of the image.
     * @param hshist - The Hue-Saturation histogram of the image.
     * @param rgbhist - The RGB histogram of the image.
     * @param objects - The detected objects in the image.
     * @param tags - The tags associated with the image.
     * @param isFav - The favorite status of the image.
     */
    public Image(final String name, final byte[] data, long id, int[] size, MediaType type, 
    double[] hshist, double[] rgbhist, ArrayList<Map<String, Object>> objects, ArrayList<String> tags, boolean isFav) {
      this.id = id;
      this.name = name;
      this.data = data;
      this.size = size;
      this.type = type;
      this.hueSatHist = hshist;
      this.rgbHist = rgbhist;
      this.objects = objects;
      this.tags = tags;
      this.isFav = isFav;
    }

    /**
     * Checks if the image is empty.
     * 
     * @return true if the image is empty, false otherwise.
     */
    public boolean isEmpty() {
      if (this.getData().length == 0 && this.getHeight()==0 && this.getWidth()==0) {
        return true;
      }
      return false;
    }
    
  
    /* Getters and Setters */

    /**
     * Gets the ID of the image.
     * 
     * @return The ID of the image.
     */
    public long getId() {
      return id;
    }
  
    /**
     * Gets the name of the image.
     * 
     * @return The name of the image.
     */
    public String getName() {
      return name;
    }
  
    /**
     * Sets the name of the image.
     * 
     * @param name - The new name of the image.
     */
    public void setName(final String name) {
      this.name = name;
    }
  
    /**
     * Gets the byte data of the image.
     * 
     * @return The byte data of the image.
     */
    public byte[] getData() {
      return data;
    }

    /**
     * Gets the width of the image.
     * 
     * @return The width of the image.
     */
    public int getWidth() {
      return this.size[0];
    }

    /**
     * Gets the height of the image.
     * 
     * @return The height of the image.
     */
    public int getHeight() {
    return this.size[1];
    }

    /**
     * Gets the size string of the image.
     * 
     * @return The size string of the image.
     */
    public String getSizeString() {
        return String.format("%dx%d", this.getWidth(), this.getHeight());
    }

    /**
     * Gets the MediaType of the image.
     * 
     * @return The MediaType of the image.
     */
    public MediaType getType(){
      return this.type;
    }

    /**
     * Gets the Hue-Saturation histogram of the image.
     * 
     * @return The Hue-Saturation histogram of the image.
     */
    public double[] getHueTintHist(){
      return this.hueSatHist;
    }

    /**
     * Gets the RGB histogram of the image.
     * 
     * @return The RGB histogram of the image.
     */
    public double[] getRGBHist(){
      return this.rgbHist;
    }

    /**
     * Gets the count of image instances.
     * 
     * @return The count of image instances.
     */
    public Long getCount(){
      return count;
    }

    /**
     * Gets the detected objects in the image.
     * 
     * @return The detected objects in the image.
     */
    public ArrayList<Map<String, Object>> getObjects() {
        return objects;
    }

    /**
     * Gets the tags associated with the image.
     * 
     * @return The tags associated with the image.
     */
    public ArrayList<String> getTags(){
      return this.tags;
    }

    /**
     * Gets the favorite status of the image.
     * 
     * @return The favorite status of the image.
     */
    public boolean getFavStatus() {
      return this.isFav;
    }

    /**
     * Sets the count of image instances.
     * 
     * @param val - The new value for the count.
     */
    public void setCount(long val){
      count = val;
    }

    /**
     * Sets the ID of the image.
     * 
     * @param val - The new ID for the image.
     */
    public void setId(long val){
      this.id = val;
    }

    /**
     * Sets the favorite status of the image.
     * 
     * @param val - The new favorite status for the image.
     */
    public void setFavStatus(boolean val) {
      this.isFav = val;
    }
  }
