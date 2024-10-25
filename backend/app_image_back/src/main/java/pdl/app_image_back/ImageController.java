package pdl.app_image_back;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pdl.object_recognition.Labeling;

/**
 * REST controller for handling connection between the front and backend.
 */
@RestController
public class ImageController {

  @Autowired
  private ObjectMapper mapper;

  private final ImageDao imageDao;

  /**
   * Constructs an ImageController with the specified ImageDao.
   * 
   * @param imageDao The ImageDao to be used by the controller.
   */
  public ImageController(ImageDao imageDao) {
    this.imageDao = imageDao;
  }

  /**
   * Retrieves an image by its ID.
   * 
   * @param id - The ID of the image to retrieve.
   * @return ResponseEntity with the image data if found, otherwise returns NOT_FOUND.
   */
  @RequestMapping(value = "/images/{id}", method = RequestMethod.GET, produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE} )
  public ResponseEntity<?> getImage(@PathVariable("id") long id) {

    Optional<Image> image = imageDao.retrieveContent(id);

    if (image.isPresent()) {
      return ResponseEntity.ok().contentType(image.get().getType()).body(image.get().getData());
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }
  
  /**
   * Deletes an image by its ID.
   * 
   * @param id - The ID of the image to delete.
   * @return ResponseEntity indicating success or failure of the deletion operation.
   */
  @RequestMapping(value = "/images/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteImage(@PathVariable("id") long id) {

    Optional<Image> image = imageDao.retrieve(id);

    if (image.isPresent()) {
      imageDao.delete(image.get());
      return new ResponseEntity<>(HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }


  /**
   * Adds a new image to the server.
   * 
   * @param file                - The image file to add.
   * @param redirectAttributes  - The RedirectAttributes set for the request
   * @return ResponseEntity indicating success or failure of the operation.
   */
  @RequestMapping(value = "/images", method = RequestMethod.POST)
  public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

    MediaType contentType = MediaType.valueOf(file.getContentType());
    if (!contentType.equals(MediaType.IMAGE_JPEG) && !contentType.equals(MediaType.IMAGE_PNG)) {
      return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // Try creating the new image we want to add to the server
    try {
      imageDao.createWithFile(new Image(file.getOriginalFilename(), file.getBytes(), false));
    } catch (FileAlreadyExistsException e) {
      return new ResponseEntity<>(HttpStatus.CONFLICT); // Return a conflict status code
    } catch (IOException e) {
      return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
    }
    return new ResponseEntity<>(HttpStatus.CREATED);
}


   /**
   * Retrieves the list of images on the server.
   * 
   * @return ResponseEntity containing a JSON array of image metadata.
   */
   @RequestMapping(value = "/images", method = RequestMethod.GET, produces = "application/json")
   @ResponseBody
   public ResponseEntity<?> getImageList() {
      ArrayList<Image> images = imageDao.retrieveAllMeta();
      ArrayNode nodes = mapper.createArrayNode();
      for (Image image : images) {
        // Use objectNode class with Jackson before adding image descriptors to the HMAP
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("id", image.getId());
        objectNode.put("name", image.getName());
        objectNode.put("type", image.getType().toString());
        objectNode.put("size", image.getSizeString());
        ArrayList<Map<String, Object>> imObjs = image.getObjects();
        ArrayNode objects = mapper.createArrayNode();
        for (Map<String, Object> obj : imObjs) {
          ObjectNode vals = mapper.createObjectNode();
          vals.put("label", (String) obj.get("label"));
          vals.put("confidence", (float) ((double)obj.get("confidence")));
          vals.put("width", (Integer) obj.get("width"));
          vals.put("height", (Integer) obj.get("height"));
          vals.put("pos_x", (Integer) obj.get("pos_x"));
          vals.put("pos_y", (Integer) obj.get("pos_y"));
          objects.add(vals);
        }
        objectNode.set("objects", objects);
        ArrayNode tagsArray = mapper.createArrayNode();
        for (String tag : image.getTags()) {
          tagsArray.add(tag);
        }
        objectNode.set("tags", tagsArray);
        objectNode.put("favorite", image.getFavStatus());
        nodes.add(objectNode);
      }
      return new ResponseEntity<>(nodes, HttpStatus.OK);
 }

 /**
  * Retrieves the list of images by a specified tag.
  * 
  * @param searchTag - The tag to search for.
  * @return ResponseEntity containing a JSON array of image metadata.
  */
 @RequestMapping(value = "/images/search", method = RequestMethod.GET, produces = "application/json")
 @ResponseBody
 public ResponseEntity<?> getImagesByTag(@RequestParam("tag") String searchTag) {
    ArrayList<Image> images = imageDao.retriveAllWithTagMeta(searchTag);
    ArrayNode nodes = mapper.createArrayNode();
    for (Image image : images) {
      // Use objectNode class with Jackson before adding image descriptors to the HMAP
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("id", image.getId());
      objectNode.put("name", image.getName());
      objectNode.put("type", image.getType().toString());
      objectNode.put("size", image.getSizeString());
      ArrayList<Map<String, Object>> imObjs = image.getObjects();
      ArrayNode objects = mapper.createArrayNode();
      for (Map<String, Object> obj : imObjs) {
        ObjectNode vals = mapper.createObjectNode();
        vals.put("label", (String) obj.get("label"));
        vals.put("confidence", (float) ((double)obj.get("confidence")));
        vals.put("width", (Integer) obj.get("width"));
        vals.put("height", (Integer) obj.get("height"));
        vals.put("pos_x", (Integer) obj.get("pos_x"));
        vals.put("pos_y", (Integer) obj.get("pos_y"));
        objects.add(vals);
      }
      objectNode.set("objects", objects);
      ArrayNode tagsArray = mapper.createArrayNode();
      for (String tag : image.getTags()) {
        tagsArray.add(tag);
      }
      objectNode.set("tags", tagsArray);
      objectNode.put("favorite", image.getFavStatus());
      nodes.add(objectNode);
    }
    return new ResponseEntity<>(nodes, HttpStatus.OK);
}

  /**
   * Retrieves the most similar images to a given image.
   * 
   * @param id - The ID of the reference image.
   * @param maxNb - The maximum number of similar images to retrieve.
   * @param method - The descriptor comparison method ("hshist" or "rgbhist").
   * @return ResponseEntity containing a JSON array of similar images metadata.
   */
  @RequestMapping(value = "/images/{id}/similar", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ResponseEntity<?> getClosestImages(@PathVariable("id") long id, @RequestParam("number") int maxNb, @RequestParam("descriptor") String method) {

  ArrayNode nodes = mapper.createArrayNode();

  if (!imageDao.exists(id)) {
      return new ResponseEntity<>(nodes, HttpStatus.NOT_FOUND);
  }

  ArrayList<Image> images = imageDao.retrieveAllMeta();
  ArrayList<Object[]> values;

  // Determine if RGB or HueSat to know which comparison is needed
  switch (method) {
    case "hshist":
      values = ImageComparison.compareHueSatHist(id, images, maxNb);
      break;

    case "rgbhist":
      values = ImageComparison.compareRGBHist(id, images, maxNb);
      break;

    default:
      return new ResponseEntity<>(nodes, HttpStatus.BAD_REQUEST);
  }

  // Add every image in the list to the HMAP
  for (Object[] val : values) {
    ObjectNode objectNode = mapper.createObjectNode();
    Image img = (Image) val[0];
    objectNode.put("id", img.getId());
    objectNode.put("name", img.getName());
    objectNode.put("type", img.getType().toString());
    objectNode.put("size", img.getSizeString());
    objectNode.put("distance", (Double) val[1]);
    nodes.add(objectNode);
  }

  return new ResponseEntity<>(nodes, HttpStatus.OK);
  }

  /**
  * Retrieves the objects detected in an image.
  * 
  * @param id - The ID of the image.
  * @return ResponseEntity containing the image with objects labeled.
  */
  @RequestMapping(value = "/images/{id}/objects", method = RequestMethod.GET, produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE} )
  public ResponseEntity<?> getImageObjects(@PathVariable("id") long id) {

    Optional<Image> image = imageDao.retrieve(id);

    if (image.isPresent()) {

      InputStream is = new ByteArrayInputStream(image.get().getData());
      BufferedImage bi;
      try {
        bi = ImageIO.read(is);
      } catch (IOException e) {
        return ResponseEntity.ok().contentType(image.get().getType()).body(image.get().getData());
      }

      BufferedImage rgbImage = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
      Graphics2D g2d = rgbImage.createGraphics();
      g2d.drawImage(bi, 0, 0, null);
      g2d.dispose();

      BufferedImage newBi = Labeling.labelImage(rgbImage, image.get().getObjects());

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try {
        ImageIO.write(newBi, "jpg", baos);
      } catch (IOException e) {
        return ResponseEntity.ok().contentType(image.get().getType()).body(image.get().getData());
      }
      byte[] bytes = baos.toByteArray();

      return ResponseEntity.ok().contentType(image.get().getType()).body(bytes);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  
  /**
   * Adds the name of a favorite image to the Favorite list.
   * 
   * @param id - The if of the image to set.
   * @return ResponseEntity indicating success or failure of the operation.
   * @throws IOException if there's an error reading the file.
   */
  @RequestMapping(value = "/images/favorites", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> setImageFavList(@RequestParam("id") long id) throws IOException {

    Optional<Image> image = imageDao.retrieve(id);
    if (image.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    // Checks if the image is not already in list of the favorite
    File fav = new File("Favorite.txt");
    Scanner reader = new Scanner(fav);
    while (reader.hasNextLine()) {
      if ( reader.nextLine().equals(image.get().getName()) ){
        reader.close();
        return removeImageFavList(image.get().getName(), id);
      }
    }
    reader.close();   
    
    // Add the image to the Favorite list 
    RandomAccessFile writer = new RandomAccessFile("Favorite.txt", "rw");
    writer.seek(writer.length());
    writer.write(image.get().getName().getBytes());
    writer.write(("\n").getBytes());
    writer.close();

    imageDao.setFav(id, true);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Reads the content of a file and converts it to a string.
   * 
   * @param file - The file to read.
   * @return The content of the file as a string.
   * @throws IOException if there's an error reading the file.
   */
  public static String fileToString(String file)throws IOException{

    String input = null;
    File fav = new File(file);
    Scanner reader = new Scanner(fav);
    StringBuffer buf = new StringBuffer();
    while (reader.hasNextLine()){
      input = reader.nextLine();
      buf.append(input);
    }
    reader.close();
    return buf.toString();
  }

  /**
   * Removes the name of a favorite image from the Favorite list.
   * 
   * @param name - The name of the image to remove from the favorite list.
   * @param id - The ID of the image to remove from the favorite list.
   * @return ResponseEntity indicating success or failure of the operation.
   * @throws IOException if there's an error writing in the file.
   */
  public ResponseEntity<?> removeImageFavList(String name, long id) throws IOException {

    // Searchs for the image to remove in the Favorite list
    String str = fileToString("Favorite.txt");  
    str=str.replaceAll(name, "");
    str=str.replaceAll("\n\n", "\n");
    File fav = new File("Favorite.txt");
    PrintWriter writer = new PrintWriter(fav);
    writer.append(str);
    writer.flush();
    writer.close();
    imageDao.setFav(id, false);    
    return new ResponseEntity<>(HttpStatus.OK);
}

  /**
   * Retrieves the list of favorite images on the server.
   * 
   * @return ResponseEntity containing a JSON array of image metadata.
   */
  @RequestMapping(value = "/images/favorites", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ResponseEntity<?> getImageFavList() {
    ArrayList<Image> images = imageDao.retrieveAllFavMeta();
      ArrayNode nodes = mapper.createArrayNode();
      for (Image image : images) {
        // Use objectNode class with Jackson before adding image descriptors to the HMAP
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("id", image.getId());
        objectNode.put("name", image.getName());
        objectNode.put("type", image.getType().toString());
        objectNode.put("size", image.getSizeString());
        ArrayList<Map<String, Object>> imObjs = image.getObjects();
        ArrayNode objects = mapper.createArrayNode();
        for (Map<String, Object> obj : imObjs) {
          ObjectNode vals = mapper.createObjectNode();
          vals.put("label", (String) obj.get("label"));
          vals.put("confidence", (float) ((double)obj.get("confidence")));
          vals.put("width", (Integer) obj.get("width"));
          vals.put("height", (Integer) obj.get("height"));
          vals.put("pos_x", (Integer) obj.get("pos_x"));
          vals.put("pos_y", (Integer) obj.get("pos_y"));
          objects.add(vals);
        }
        objectNode.set("objects", objects);
        ArrayNode tagsArray = mapper.createArrayNode();
        for (String tag : image.getTags()) {
          tagsArray.add(tag);
        }
        objectNode.set("tags", tagsArray);
        objectNode.put("favorite", image.getFavStatus());
        nodes.add(objectNode);
      }
      return new ResponseEntity<>(nodes, HttpStatus.OK);
  }

  /**
  * Retrieves the image data with a specified filter applied.
  * 
  * @param id - The ID of the image.
  * @param nameFilter - The name of the filter to apply.
  * @return ResponseEntity containing the filtered image.
  */
  @RequestMapping(value = "/images/{id}/filter", method = RequestMethod.GET, produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE} )
  public ResponseEntity<?> getFilter(@PathVariable("id") long id, @RequestParam("name") String nameFilter) {
    Optional<Image> image = imageDao.retrieve(id);
    if(image.isPresent()){
      switch(nameFilter){
        case "Gray":
          byte[] grayImageData = ImageProcessing.grayFilter(image.get());
          return ResponseEntity.ok().contentType(image.get().getType()).body(grayImageData);
        case "Histogram":
          byte[] histogramImageData = ImageProcessing.Histogram(image.get());
          return ResponseEntity.ok().contentType(image.get().getType()).body(histogramImageData);
        case "Histogram2D":
          byte[] histogram2DImageData = ImageProcessing.Histogram2D(image.get());
          return ResponseEntity.ok().contentType(image.get().getType()).body(histogram2DImageData);
        case "Sobel":
          byte[] SobelImageData = ImageProcessing.gradientImageSobel(image.get());
          return ResponseEntity.ok().contentType(image.get().getType()).body(SobelImageData);
        default :
          return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  /**
  * Retrieves the image data with a specified filter applied with parameter.
  * 
  * @param id - The ID of the image.
  * @param nameFilter - The name of the filter to apply.
  * @param n - The parameter for the filter.
  * @return ResponseEntity containing the filtered image.
  */
  @RequestMapping(value = "/images/{id}/filter_param", method = RequestMethod.GET, produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE} )
  public ResponseEntity<?> getFilterWithParameter(@PathVariable("id") long id, @RequestParam("name") String nameFilter, @RequestParam("param") int n) {
    Optional<Image> image = imageDao.retrieve(id);
    if(image.isPresent()){
      switch(nameFilter){
        case "Mean":
          byte[] meanImageData = ImageProcessing.meanFilter(image.get(),n);
          return ResponseEntity.ok().contentType(image.get().getType()).body(meanImageData);
        case "Color":
          byte[] colorImageData = ImageProcessing.hueFilter(image.get(), n);
          return ResponseEntity.ok().contentType(image.get().getType()).body(colorImageData);
        default :
          return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
  
}
