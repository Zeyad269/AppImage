import { ref } from 'vue'
import axios from 'axios'
import { ImageType } from './image'
import { useToast } from 'vue-toastification'

// Contains the list of the images to display in the gallery
export const imgList = ref<ImageType[]>([]);

// Will contain the content of a processed image
export const processedImage = ref<Blob>();
// URL of the processed image to retreive the Blob
export const processedImageURL = ref<String>('');

// Contains the list of the similar images to display in the gallery
export const imgSimilarList = ref<ImageType[]>([]);

// List of features and filters (Image processing)
export const imgProcessingFilters = ref<String[]>(
  ['Gray', 'Color', 'Mean', 'Sobel', 'Histogram', 'Histogram2D']
);

// Contains the path of the images
export const imageUrl = '/images/'

// URL Mean Filter (without size parameter)
export const urlMeanFilter = '/filter_param?name=Mean&param='

// URL Color Filter (without color code parameter)
export const urlColorFilter = '/filter_param?name=Color&param='

// URL Features that do not need extra parameters
export const urlFilterSingleParam = '/filter?name='

// Contains the tool to display
export const showTool = ref('');

// Contains the filter to display
export const filterSelected = ref('');

// Contains the object to search
export const objectSearched = ref('');

// Boolean to know if a feature or filter was selected (from select button)
export const filterIsSelected = ref<boolean>(false);

// Boolean to know if the loading GIF is running
export const isLoading = ref<boolean>(true);

export const isProcessing = ref<boolean>(false);

// Booleans to know whether the objects recognized within an image should be displayed or not
export const showObjectsImg = ref<boolean>(false);
export const displayObjects = ref<boolean>(false);

// Boolean to know if the user wants the list of favorite images
export const displayFav = ref<boolean>(false);

// Pop-up-like feature that uses Toast from Vue
export const toast = useToast();

// Declaring the color code for the color filters feature
export const colorCode = ref<number>(-1);

// Contains the color codes used for the filters
export const colorList = ref<Array<number>>([
  0,  /** red **/
  30, /** orange **/
  60, /** yellow **/
  90, /** jade **/
  120, /** light-green **/
  150, /** green **/
  180, /** sky-blue **/
  210, /** light-blue **/
  240, /** blue **/
  270, /** purple **/
  300, /** magenta **/
  330, /** pink **/
])

// Contains the informations of an image selected from the gallery
export const selected = ref<ImageType>({
  id: -1,
  name: "Empty",
  type: "Empty",
  size: "Empty",
  distance: -1.0,
  tags: [],
  favorite: false
})

// Clear the list of similar images
function resetSimilarList(){
  imgSimilarList.value = [];
}

// Create axios instance for /images
export const instance = axios.create({
  baseURL: "/",
  timeout: 15000,
});

export function loaded() {
  isLoading.value = false;
}

export function loading() {
  isLoading.value = true;
}

// Flushes the text that was typed in the search bar and returns the gallery (from the home page)
export function erase() {
  objectSearched.value = '';
  getGallery();
}

export function processed() {
  isProcessing.value = false;
}

export function processing() {
  isProcessing.value = true;
}

// Method used to refresh the gallery
export function refreshGallery(){

  displayFav.value=false;
  displayObjects.value=false;
  getGallery();

}

// Request to get the list of images on the server
export function getGallery(){
  
  let req_url = ""
  if (displayFav.value) {
    req_url = "images/favorites";
  } 
  else if (objectSearched.value!="") {
    req_url = "images/search?tag="+ objectSearched.value;
  } else {
    req_url = "images";
  }
  instance.get(req_url)
  .then(response => {
    imgList.value = response.data;
  })
  .catch(error => {
    toast.error("An error occured while loading the gallery", {
      timeout: 3000
    });
    console.log(error);
  })
}

// Display the list of images in the form of a gallery
getGallery();

// Code to get the previous image on the server when horizontal-scrolling
export function previous(img:ImageType){
  loading();
  showObjectsImg.value = false;
  const index = imgList.value.indexOf(img);
  if (index-1 >= 0){
    selected.value = imgList.value[index-1];
  }
  else{
    selected.value = imgList.value[imgList.value.length-1];
  }
  resetSimilarList();
}

// Code to get the next image on the server when horizontal-scrolling
export function next(img:ImageType){
  loading();
  showObjectsImg.value = false;
  const index = imgList.value.indexOf(img);
  if (index+1 < imgList.value.length){
    selected.value = imgList.value[index+1];
  }
  else{
    selected.value = imgList.value[0];
  }
  resetSimilarList();
}

export function get(img:ImageType){
  selected.value=img;
}

export function setTool(tool:string){
  imgSimilarList.value = [];
  showTool.value=tool;
}

// Request to delete an image from the server
export function remove(img:ImageType){
  instance.delete(`images/${img.id}`)
    .then(_ =>{
    getGallery();
    toast.success("Image " + img.name + " deleted successfully !", {
      timeout: 3000
    })
    console.log('SUCCESS!!');
  }).catch(error => {
    toast.error("An error occured while removing the image", {
      timeout: 3000
    });
    console.log(error)
  });
}

// Code to trigger the request to upload an image to the server
export function fileUpload(){
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = 'image/*';
  input.addEventListener('change', handleFileUpload);
  input.click();
}

// Updates the value of file -> new value : image to upload
function handleFileUpload(event:any){  
  const file = ref('');
  file.value=event.target.files[0];
  console.log(file.value);

  // Updates the image on the server 
  let formData = new FormData();
  formData.append('file', file.value);
  
  // Request to add the image to the gallery
  instance.post( 'images',
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
  }).then(_ => {
    getGallery();   // Refresh the gallery after the upload of an image
    toast.success("Image uploaded successfully", {
      timeout: 3000
    })
    console.log('SUCCESS !!');
  }).catch(error => {
    if (error.response.status == 409) {
      toast.error("A file with the same name already exists", {
        timeout: 3000
      })
    }
    else {
      toast.error("An error occured while uploading the image", {
        timeout: 3000
      })
    }
  });
}

// Request to download an image from the server to disk
export function downloadImage(img:ImageType){
  // Default name of the image downloaded
  const imgName = img.name;

  // Axios get request
  instance({
    method: "GET",
    url: imageUrl+img.id,
    responseType: "blob"
    }).then((res:any) => {
      const blob = new Blob([res.data], { type: res.headers['content-type'] });
      const url = window.URL.createObjectURL(blob);
      const link= document.createElement('a');
      link.href = url;
      link.download = imgName;
      document.body.appendChild(link);
      link.click();
      window.URL.revokeObjectURL(url);
      toast.success("Download complete", {
        timeout: 3000
      })
      console.log("Download complete");
    })
    .catch(error => {
      toast.error("An error occured while downloading the image", {
        timeout: 3000
      })
      console.log('Error occurred:', error);});
}

// Method used to dowload the entire list of favorite images
export function downloadFavorites(listFavs:ImageType[]){
  listFavs.forEach(function (img){
    downloadImage(img)
  });
}

// Method used to download the correct processed Image
export function downloadBlob(blob:Blob, name:string){
  const url = window.URL.createObjectURL(blob);
  // Create a link element
  const link = document.createElement('a');
  link.href = url;
  link.download = name;

  // Append the link to the body and trigger the download
  document.body.appendChild(link);
  link.click();

  // Cleanup
  window.URL.revokeObjectURL(url);
  document.body.removeChild(link);

  console.log("Download complete");
}

// GET request to retrieve the list of similar images 
export function getSimilarity(id:Number,N:number,desc:string){
  instance.get('images/'+id+'/similar?number='+N+'&descriptor='+desc)
  .then(response => {
    imgSimilarList.value = response.data;
  })
  .catch(error => {
    toast.error("An error occured while getting similar images", {
      timeout: 3000
    })
    console.log(error)
  })
}

// POST request to add an image to the favorites 
export function setFav(img:ImageType){
  img.favorite = !img.favorite;
  instance.post('/images/favorites?id='+img.id)
  .then(_ => {
    let new_state = img.favorite;
    if (displayFav.value) {
      getGallery();
    }
    if (new_state == true) {
      toast.success("Added successfully", {
        timeout: 3000
      })
    } else {
      toast.success("Removed successfully", {
        timeout: 3000
      })
    }
    
    console.log("SUCCESS !!")
  })
  .catch(error => {
    console.log(error)
    toast.error("An error occured while adding the image to favorites", {
      timeout: 3000
    })
  })
}

// GET request to retrieve the list of the favorites images 
export function getFav(){
  displayFav.value = !(displayFav.value);
  getGallery();
}

// Returns the list of images recgonized (AI feature) that correspond to the typed text.
export function getSearched(){
  getGallery();
}