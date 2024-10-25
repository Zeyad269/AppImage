package pdl.app_image_back;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class ImageControllerTests {

    @Autowired
	private MockMvc mockMvc;

	// Helper method to copy a directory
	private static void copyDirectory(String srcDirPath, String destDirPath) throws IOException {
		File srcDir = new File(srcDirPath);
		File dstDir = new File(destDirPath);
		FileUtils.copyDirectory(srcDir, dstDir);
	}

	// Helper method to delete a directory
	private static void deleteDirectory(String dirPath) throws IOException {
		File file = new File(dirPath);

		FileUtils.deleteDirectory(file);

		file.delete();
	}

	// Initialize favorite file if it doesn't exist before running tests
	@BeforeAll
	static void initFavFile() throws IOException{
		
		File favFile = new File("Favorite.txt");	
		favFile.createNewFile();	
	}

	// Initialize test folders before running tests
	@BeforeAll
	static void initTestFolders() throws Exception{
		deleteDirectory("images_temp");
		copyDirectory("images", "images_temp");
		deleteDirectory("images");
		copyDirectory("test_images", "images");
	}
	
	// Clear test folders after all tests have been executed
	@AfterAll
	static void clearTestFolders() throws Exception{
		deleteDirectory("images");
		copyDirectory("images_temp", "images");
		deleteDirectory("images_temp");
	}

	// Test to make sure the list of images is retrieved correctly
	@Test
	@Order(1)
	public void getImageListShouldReturnSuccess() throws Exception {
		this.mockMvc.perform(get("/images")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

	// Test to make sure an error is raised if we try to retrieve an image with an invalid id
	@Test
	@Order(2)
	public void getImageShouldReturnNotFound() throws Exception {
		this.mockMvc.perform(get("/images/-1")).andDo(print()).andExpect(status().isNotFound());
	}

	// Test to make sure an image is retrieved correctly with at least one image when the server is initialized
	@Test
	@Order(3)
	public void getImageShouldReturnSuccess() throws Exception {
		this.mockMvc.perform(get("/images/0")).andDo(print()).andExpect(status().isOk());
	}

	// User/Client should not be allowed to delete the entire directory of images
	@Test
	@Order(4)
	public void deleteImagesShouldReturnMethodNotAllowed() throws Exception {
	    this.mockMvc.perform(delete("/images")).andDo(print()).andExpect(status().isMethodNotAllowed());
	}

	// Test to make sure an error is raised if the client tries to delete an image that doesn't exist (through invalid id)
	@Test
	@Order(5)
	public void deleteImageShouldReturnNotFound() throws Exception {
		this.mockMvc.perform(delete("/images/-1")).andDo(print()).andExpect(status().isNotFound());
	}

	// Test to see if a valid deletion is accepted and success is returned
	@Test
	@Order(6)
	public void deleteImageShouldReturnSuccess() throws Exception {
		this.mockMvc.perform(delete("/images/0")).andDo(print()).andExpect(status().isOk());
	}

	// Test to make sure success is returned after creating a new image using multipartFile
	@Test
	@Order(7)
	public void createImageShouldReturnSuccess() throws Exception {
		final ClassPathResource imgFile = new ClassPathResource("not_test.jpg");
		MockMultipartFile multipartFile = new MockMultipartFile("file", "not_test.jpg", "image/jpeg", imgFile.getInputStream());
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(multipartFile)).andDo(print())
				.andExpect(status().isCreated());
	}

	// Test to make sure an error is raised with respect to the mediaType of an image we want to create 
	@Test
	@Order(8)
	public void createImageShouldReturnUnsupportedMediaType() throws Exception {
		MockMultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "text/plain", "Test".getBytes());
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(multipartFile)).andDo(print())
				.andExpect(status().isUnsupportedMediaType());
	}

	// Test to make sure the same value is returned after two get requests of the same thing
	@Test
	@Order(9)
	public void getImageListShouldReturnEqualVal() throws Exception {
		String expected = "[{\"id\":1,\"name\":\"test2.png\",\"type\":\"image/png\",\"size\":\"600x600\",\"objects\":[],\"tags\":[\"test2\"],\"favorite\":false},{\"id\":2,\"name\":\"not_test.jpg\",\"type\":\"image/jpeg\",\"size\":\"320x320\",\"objects\":[],\"tags\":[\"not_test\"],\"favorite\":false}]";

        String outJson = mockMvc.perform(get("/images"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        assertEquals(expected, outJson);
	}

	// Test to make sure the list of similar images is well returned
	@Test
	@Order(10)
	public void getClosestListShouldReturnSuccess() throws Exception {
		this.mockMvc.perform(get("/images/1/similar?number=10&descriptor=hshist").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

	// Test to make sure the client cannot access the list of similar images with an invalid request
	@Test
	@Order(11)
	public void getClosestListShouldReturnBadRequest() throws Exception {
		this.mockMvc.perform(get("/images/1/similar?number=10&descriptor=blabla").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

	// Test to make sure the client has no result if he tries to get the list of similar images to an invalid image (through id)
	@Test
	@Order(12)
	public void getClosestListShouldReturnNotFound() throws Exception {
		this.mockMvc.perform(get("/images/-1/similar?number=10&descriptor=hshist").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

	@Test
	@Order(14)
	public void getObjectsShouldReturnNotFound() throws Exception {
		this.mockMvc.perform(get("/images/-1/objects")).andDo(print()).andExpect(status().isNotFound());
	}

	@Test
	@Order(15)
	public void searchShouldReturnSuccess() throws Exception {
		this.mockMvc.perform(get("/images/search?tag=bird")).andDo(print()).andExpect(status().isOk());
	}

	@Test
	@Order(16)
	public void searchShouldReturnBadRequest() throws Exception {
		this.mockMvc.perform(get("/images/search")).andDo(print()).andExpect(status().isBadRequest());
	}

	@Test
	@Order(18)
	public void addFavoriteShouldReturnNotFound() throws Exception {
		this.mockMvc.perform(post("/images/favorites?id=-1")).andDo(print()).andExpect(status().isNotFound());
	}

	@Test
	@Order(19)
	public void addFavoriteShouldReturnBadRequest() throws Exception {
		this.mockMvc.perform(post("/images/favorites")).andDo(print()).andExpect(status().isBadRequest());
	}

	@Test
	@Order(20)
	public void getFavoritesShouldReturnSuccess() throws Exception {
		this.mockMvc.perform(get("/images/favorites").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

	@Test
	@Order(22)
	public void getFilteredShouldReturnNotFound() throws Exception {
		this.mockMvc.perform(get("/images/-1/filter?name=Gray")).andDo(print()).andExpect(status().isNotFound());
	}

	@Test
	@Order(23)
	public void getFilteredShouldReturnBadRequest() throws Exception {
		this.mockMvc.perform(get("/images/0/filter")).andDo(print()).andExpect(status().isBadRequest());
	}

	@Test
	@Order(25)
	public void getFilteredParamShouldReturnNotFound() throws Exception {
		this.mockMvc.perform(get("/images/-1/filter_param?name=Mean&param=3")).andDo(print()).andExpect(status().isNotFound());
	}

	@Test
	@Order(26)
	public void getFilteredParamShouldReturnBadRequest() throws Exception {
		this.mockMvc.perform(get("/images/0/filter_param?name=blabla")).andDo(print()).andExpect(status().isBadRequest());
	}

	// Test to make sure the images directory is created and initialized correctly
	@Test
	@Order(27)
	public void startWithoutFolderRaisesError() throws IOException {
		deleteDirectory("images");
		assertThrows(FileNotFoundException.class, () -> (new ImageDao()).initImages("images", "Favorite.txt"));
	}
}
