package pdl.app_image_back;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import java.io.FileOutputStream;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import org.postgresql.jdbc.PgArray;

/**
 * Data Access Object for managing Image entities.
 */
@Repository
public class ImageDao implements Dao<Image>, InitializingBean {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * Returns an image wrapped in an Optional, given a path, a name, and an id.
   * 
   * @param path  - The path to the image file.
   * @param name  - The name of the image file.
   * @param id    - The ID of the image.
   * @param isFav - The favorite state of the image.
   * @return An Optional containing the Image if it is successfully opened, otherwise an empty Optional.
   */
  private Optional<Image> openImage(String path, String name, long id, boolean isFav){
    byte[] fileContent;
    try {
      fileContent = Files.readAllBytes(Path.of(path));
    } catch (IOException e) {
      return Optional.empty();
    }
    Optional<Image> im = Optional.of(new Image(name, fileContent, id, isFav));
    return im;
  }

  /**
   * Returns an image wrapped in an Optional, given a path and a name, but no id.
   * 
   * @param path  - The path to the image file.
   * @param name  - The name of the image file.
   * @param isFav - The favorite state of the image.
   * @return An Optional containing the Image if it is successfully opened, otherwise an empty Optional.
   */
  private Optional<Image> openImage(String path, String name, boolean isFav){
    byte[] fileContent;
    try {
      fileContent = Files.readAllBytes(Path.of(path));
    } catch (IOException e) {
      return Optional.empty();
    }
    Optional<Image> im = Optional.of(new Image(name, fileContent, isFav));
    return im;
  }

  /**
   * Returns an image wrapped in an Optional, given all its components.
   * 
   * @param path     - The path to the image file.
   * @param name     - The name of the image file.
   * @param id       - The ID of the image.
   * @param size     - The size of the image.
   * @param type     - The MediaType of the image.
   * @param hshist   - The hue-saturation histogram of the image.
   * @param rgbhist  - The RGB histogram of the image.
   * @param objects  - The ArrayList of objects detected on the image.
   * @param tags     - The tags linked to the image.
   * @param isFav    - The favorite state of the image.
   * @return An Optional containing the Image if it is successfully opened, otherwise an empty Optional.
   */
  private Optional<Image> openImage(String path, String name, long id, int[] size, MediaType type, double[] hshist, double[] rgbhist, ArrayList<Map<String, Object>> objects, ArrayList<String> tags, boolean isFav){
    byte[] fileContent;
    try {
      fileContent = Files.readAllBytes(Path.of(path));
    } catch (IOException e) {
      return Optional.empty();
    }
    Optional<Image> im = Optional.of(new Image(name, fileContent, id, size, type, hshist, rgbhist, objects, tags, isFav));
    return im;
  }

  /**
   * Creates the SQL table for storing images and initializes image data.
   * @throws IOException 
   */
  private void createTable() throws IOException{
    jdbcTemplate.execute("DROP TABLE IF EXISTS images");
    this.jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS images (id bigint PRIMARY KEY, name text, type text, size text, hshist double precision[], rgbhist double precision[], objects jsonb, tags text[], fav boolean)");
    try{
      Field field = Image.class.getDeclaredField("count");
      field.setAccessible(true);
      field.set(null, Long.valueOf(0));
    }
    catch (Exception e){

    }

    initImages("images", "Favorite.txt");

  }

  /**
   * Adds all images from a folder to the database.
   * 
   * @param path    - The path to the folder containing images.
   * @param favPath - The path to the file where favorites are stored.
   * @throws IOException if the folder path doesn't exist
   */
  public void initImages(String path, String favPath) throws IOException{
    File folder = new File(path);

    if (!folder.exists()){
      throw new FileNotFoundException();
    }

    ArrayList<String> favs = new ArrayList<>();
    File fav = new File(favPath);
    if (!fav.exists()) {
      fav.createNewFile();
    }
    Scanner reader = new Scanner(fav);
    while (reader.hasNextLine()) {
      String imgName = reader.nextLine();
      favs.add(imgName);
    }
    reader.close();

    File[] paths = folder.listFiles();
    for (File file : paths) {
      if (file.isFile()){
        MediaType type = MediaType.valueOf(Files.probeContentType(file.toPath()));
        boolean isFav = favs.contains(file.getName());
        if (type.equals(MediaType.IMAGE_JPEG) || type.equals(MediaType.IMAGE_PNG)){
          Optional<Image> image = openImage(String.format("%s/%s", path, file.getName()), file.getName(), isFav);
          if (image.isPresent()){
            
            create(image.get());
            System.out.println(String.format("%s loaded", file.getName()));
          }
          else {
            System.err.println(String.format("%s couldn't be loaded", file.getName()));
          }
        }
      }
    }
  }

  /**
   * Performs actions after the properties of the bean have been set.
   * 
   * @throws Exception if an error occurs during initialization.
   */
  public void afterPropertiesSet() throws Exception {
    createTable();
  }

  /**
   * Checks if an image with the given ID exists in the database.
   * 
   * @param id - The ID of the image.
   * @return true if the image exists, otherwise false.
   */
  public boolean exists(long id){
    return this.jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM images WHERE id=?)", boolean.class, id);
  }


  /**
   * Retrieves an image from the database given its ID.
   * 
   * @param id - The ID of the image.
   * @return An Optional containing the retrieved Image if it exists, otherwise an empty Optional.
   */
  @Override
  public Optional<Image> retrieve(final long id) {
    try {
      String name = jdbcTemplate.queryForObject("SELECT name FROM images WHERE id = ?", String.class, id);

      Map<String, Object> res = jdbcTemplate.queryForMap("SELECT name, fav FROM images WHERE id = ?", id);

      return openImage(String.format("images/%s", name), name, id, (boolean) res.get("fav"));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * Retrieves the content of an image from the database given its ID.
   * 
   * @param id - The ID of the image.
   * @return An Optional containing the retrieved Image content if it exists, otherwise an empty Optional.
   */
  public Optional<Image> retrieveContent(final long id) {
    try {
      Map<String, Object> res = jdbcTemplate.queryForMap("SELECT name, type, fav FROM images WHERE id = ?", id);
      String name = (String) res.get("name");
      MediaType type = MediaType.valueOf((String) res.get("type"));
      return openImage(String.format("images/%s", name), name, id, new int[2], type, new double[0], new double[0], new ArrayList<>(), new ArrayList<>(), (boolean) res.get("fav"));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * Retrieves the metadata of an image from the database given its ID.
   * 
   * @param id - The ID of the image.
   * @return An Optional containing the retrieved Image metadata if it exists, otherwise an empty Optional.
   */
  public Optional<Image> retrieveMeta(final long id) {
    try {

      Map<String, Object> res = jdbcTemplate.queryForMap("SELECT id, name, size, type, hshist, rgbhist, objects, tags, fav FROM images");

      ObjectMapper objectMapper = new ObjectMapper();

      int w = Integer.valueOf(((String) res.get("size")).split("x")[0]);
      int h = Integer.valueOf(((String) res.get("size")).split("x")[1]);
      int[] size = {w, h};

      double[] hshist = convertToPrimitive((Double[]) ((PgArray) res.get("hshist")).getArray());
      double[] rgbhist = convertToPrimitive((Double[]) ((PgArray) res.get("rgbhist")).getArray());

      ArrayList<String> tags = new ArrayList<>(Arrays.asList((String[]) ((PgArray) res.get("tags")).getArray()));

      Object objectsJson = res.get("objects");
      ArrayList<Map<String, Object>> objectsList;
      if (objectsJson != null) {
        objectsList = objectMapper.readValue(objectsJson.toString(), new TypeReference<ArrayList<Map<String, Object>>>() {});
      }
      else {
        throw new Exception();
      }

      Optional<Image> im = openImage(String.format("images/%s", res.get("name")), (String) res.get("name"), (long) res.get("id"), size,
      MediaType.valueOf((String) res.get("type")), hshist, rgbhist, objectsList, tags, (boolean) res.get("fav"));
      return im;    
    }
    catch (Exception e) {
      return Optional.empty();
    }
  }


  /**
   * Retrieves all images from the database.
   * 
   * @return An ArrayList containing all retrieved images.
   */
  @Override
  public ArrayList<Image> retrieveAll() {
    ArrayList<Image> images = new ArrayList<>();

    List<Map<String, Object>> results = jdbcTemplate.queryForList("SELECT id, name, size, type, hshist, rgbhist, objects, tags, fav FROM images");

    ObjectMapper objectMapper = new ObjectMapper();

    for (Map<String, Object> res : results) {
      int w = Integer.valueOf(((String) res.get("size")).split("x")[0]);
      int h = Integer.valueOf(((String) res.get("size")).split("x")[1]);
      int[] size = {w, h};
      try {
        double[] hshist = convertToPrimitive((Double[]) ((PgArray) res.get("hshist")).getArray());
        double[] rgbhist = convertToPrimitive((Double[]) ((PgArray) res.get("rgbhist")).getArray());

        ArrayList<String> tags = new ArrayList<>(Arrays.asList((String[]) ((PgArray) res.get("tags")).getArray()));

        Object objectsJson = res.get("objects");
        ArrayList<Map<String, Object>> objectsList;
        if (objectsJson != null) {
          objectsList = objectMapper.readValue(objectsJson.toString(), new TypeReference<ArrayList<Map<String, Object>>>() {});
        }
        else {
          throw new Exception();
        }

        Optional<Image> im = openImage(String.format("images/%s", res.get("name")), (String) res.get("name"), (long) res.get("id"), size,
        MediaType.valueOf((String) res.get("type")), hshist, rgbhist, objectsList, tags, (boolean) res.get("fav"));
        if (im.isPresent()) {
          images.add(im.get());
        }
      } catch (Exception e) {
        e.printStackTrace();
        System.err.println(String.format("Image %s couldn't be loaded", (String) res.get("name")));
      }     
    }

    return images;
  }

  /**
   * Converts an array of Double objects to an array of primitive doubles.
   * 
   * @param doubleObjects - The array of Double objects to be converted.
   * @return An array of primitive doubles.
   */
  private static double[] convertToPrimitive(Double[] doubleObjects) {
    double[] doubles = new double[doubleObjects.length];
    for (int i = 0; i < doubleObjects.length; i++) {
        doubles[i] = doubleObjects[i].doubleValue();
    }
    return doubles;
  }

  /**
   * Retrieves all images with metadata only (without loading image data).
   * 
   * @return An ArrayList containing images with metadata only.
   */
  public ArrayList<Image> retrieveAllMeta() {
    ArrayList<Image> images = new ArrayList<>();

    List<Map<String, Object>> results = jdbcTemplate.queryForList("SELECT id, name, size, type, hshist, rgbhist, objects, tags, fav FROM images");

    ObjectMapper objectMapper = new ObjectMapper();

    for (Map<String, Object> res : results) {
      int w = Integer.valueOf(((String) res.get("size")).split("x")[0]);
      int h = Integer.valueOf(((String) res.get("size")).split("x")[1]);
      int[] size = {w, h};
      try {
        double[] hshist = convertToPrimitive((Double[]) ((PgArray) res.get("hshist")).getArray());
        double[] rgbhist = convertToPrimitive((Double[]) ((PgArray) res.get("rgbhist")).getArray());

        ArrayList<String> tags = new ArrayList<>(Arrays.asList((String[]) ((PgArray) res.get("tags")).getArray()));

        Object objectsJson = res.get("objects");
        ArrayList<Map<String, Object>> objectsList;
        if (objectsJson != null) {
          objectsList = objectMapper.readValue(objectsJson.toString(), new TypeReference<ArrayList<Map<String, Object>>>() {});
        }
        else {
          throw new Exception();
        }

        images.add(new Image((String) res.get("name"), new byte[0], (long) res.get("id"), size, 
        MediaType.valueOf((String) res.get("type")), hshist, rgbhist, objectsList, tags, (boolean) res.get("fav")));
      } catch (Exception e) {
        e.printStackTrace();
        System.err.println(String.format("Image %s couldn't be loaded", (String) res.get("name")));
      }
      
    }
    return images;
  }

  /**
   * Retrieves all images with a specific tag from the database along with their metadata.
   * 
   * @param tag - The tag to filter images by.
   * @return An ArrayList containing images with the specified tag and their metadata.
   */
  public ArrayList<Image> retriveAllWithTagMeta(String tag) {
    ArrayList<Image> images = new ArrayList<>();

    List<Map<String, Object>> results = jdbcTemplate.queryForList("SELECT id, name, size, type, hshist, rgbhist, objects, tags, fav FROM images WHERE ? = ANY(tags)", tag);

    ObjectMapper objectMapper = new ObjectMapper();

    for (Map<String, Object> res : results) {
      int w = Integer.valueOf(((String) res.get("size")).split("x")[0]);
      int h = Integer.valueOf(((String) res.get("size")).split("x")[1]);
      int[] size = {w, h};
      try {
        double[] hshist = convertToPrimitive((Double[]) ((PgArray) res.get("hshist")).getArray());
        double[] rgbhist = convertToPrimitive((Double[]) ((PgArray) res.get("rgbhist")).getArray());

        ArrayList<String> tags = new ArrayList<>(Arrays.asList((String[]) ((PgArray) res.get("tags")).getArray()));

        Object objectsJson = res.get("objects");
        ArrayList<Map<String, Object>> objectsList;
        if (objectsJson != null) {
          objectsList = objectMapper.readValue(objectsJson.toString(), new TypeReference<ArrayList<Map<String, Object>>>() {});
        }
        else {
          throw new Exception();
        }

        images.add(new Image((String) res.get("name"), new byte[0], (long) res.get("id"), size, 
        MediaType.valueOf((String) res.get("type")), hshist, rgbhist, objectsList, tags, (boolean) res.get("fav")));
      } catch (Exception e) {
        e.printStackTrace();
        System.err.println(String.format("Image %s couldn't be loaded", (String) res.get("name")));
      }
      
    }
    return images;
  }

  /**
   * Retrieves all favorite images from the database along with their metadata.
   * 
   * @return An ArrayList containing all favorite images and their metadata.
   */
  public ArrayList<Image> retrieveAllFavMeta() {
    ArrayList<Image> images = new ArrayList<>();

    List<Map<String, Object>> results = jdbcTemplate.queryForList("SELECT id, name, size, type, hshist, rgbhist, objects, tags, fav FROM images WHERE fav");

    ObjectMapper objectMapper = new ObjectMapper();

    for (Map<String, Object> res : results) {
      int w = Integer.valueOf(((String) res.get("size")).split("x")[0]);
      int h = Integer.valueOf(((String) res.get("size")).split("x")[1]);
      int[] size = {w, h};
      try {
        double[] hshist = convertToPrimitive((Double[]) ((PgArray) res.get("hshist")).getArray());
        double[] rgbhist = convertToPrimitive((Double[]) ((PgArray) res.get("rgbhist")).getArray());

        ArrayList<String> tags = new ArrayList<>(Arrays.asList((String[]) ((PgArray) res.get("tags")).getArray()));

        Object objectsJson = res.get("objects");
        ArrayList<Map<String, Object>> objectsList;
        if (objectsJson != null) {
          objectsList = objectMapper.readValue(objectsJson.toString(), new TypeReference<ArrayList<Map<String, Object>>>() {});
        }
        else {
          throw new Exception();
        }

        images.add(new Image((String) res.get("name"), new byte[0], (long) res.get("id"), size, 
        MediaType.valueOf((String) res.get("type")), hshist, rgbhist, objectsList, tags, (boolean) res.get("fav")));
      } catch (Exception e) {
        e.printStackTrace();
        System.err.println(String.format("Image %s couldn't be loaded", (String) res.get("name")));
      }
      
    }
    return images;
  }

  /**
   * Creates a new image entry in the database.
   * 
   * @param img - The Image object to be created in the database.
   */
  @Override
  public void create(final Image img) {
    if (MediaType.ALL.equals(img.getType())) {
      return;
    }

    while (exists(img.getId())) {
      img.setCount(img.getCount()+1);
      img.setId(img.getCount());
    }
    
    double[] hueTintHistArray = img.getHueTintHist();

    double[] rgbHistArray = img.getRGBHist();

    String jsonString = "";
    ObjectMapper objectMapper = new ObjectMapper(); 
    try { 
        jsonString = objectMapper.writeValueAsString(img.getObjects()); 
    } 
    catch (JsonProcessingException e) { 
        e.printStackTrace(); 
    } 
    

    jdbcTemplate.update("INSERT INTO images (id, name, type, size, hshist, rgbhist, objects, tags, fav) VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, ?, false)", img.getId(), img.getName(), img.getType().toString(), img.getSizeString(), hueTintHistArray, rgbHistArray, jsonString, (String[]) (img.getTags().toArray(new String[0])));
  }

  /**
   * Creates a new image file and its corresponding entry in the database.
   * 
   * @param img - The Image object to be created with a file.
   * @throws IOException if the file can't be created
   * @throws FileAlreadyExistsException if a file with same name exists
   */
  public void createWithFile(final Image img) throws FileAlreadyExistsException, IOException {

    if (MediaType.ALL.equals(img.getType())) {
      return;
    }

    final File newFile = new File(String.format("images/%s", img.getName()));
    
    if (newFile.createNewFile()) {
      FileOutputStream outputStream;
      outputStream = new FileOutputStream(newFile);
      outputStream.write(img.getData());
      outputStream.close();
      this.create(img);
      System.out.println("File created: " + newFile.getName());
    }

    else {
      throw new FileAlreadyExistsException(img.getName());
    }
    
  }

  /**
   * Updates the metadata of an image in the database.
   * 
   * @param img    - The Image object whose metadata is to be updated.
   * @param params - An array of strings containing the parameters to be updated (not used in the current implementation).
   */
  @Override
  public void update(final Image img, final String[] params) {
    // Not used
  }

  /**
   * Sets the favorite status of an image with the given ID in the database.
   * 
   * @param id    - The ID of the image.
   * @param value - The boolean value indicating whether the image should be marked as favorite or not.
   */
  public void setFav(long id, boolean value) {
    jdbcTemplate.update("UPDATE images SET fav = ? WHERE id = ?", value, id);
  }

  /**
   * Deletes an image entry from the database.
   * 
   * @param img - The Image object to be deleted.
   */
  @Override
  public void delete(final Image img) {
    jdbcTemplate.update("DELETE FROM images WHERE id = ?", img.getId());
    final File imgFile = new File(String.format("images/%s", img.getName()));

    imgFile.delete();
  }
}