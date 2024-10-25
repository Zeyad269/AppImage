import {filterSelected, filterIsSelected, urlMeanFilter, urlColorFilter, processedImage, processedImageURL, urlFilterSingleParam, colorCode, colorList, processing, processed} from './http-api'; 
import { ref } from 'vue'
import { ImageType } from './image';
import axios from 'axios';

// Boolean to know when to display the formular for histograms
export const showForm = ref<boolean>(false);
// Boolean used to know when the select button (filters) is demanded
export const showSelect = ref<boolean>(false);

// Method used to clear the blob that will contain the processed image
export function resetProcessed(){
  processedImageURL.value = "";
  processedImage.value = new Blob();
}

// Axios get request -> timeout is 0 because we do not want the request
// to expire (especially when the size of the mean filter is high)
const instance = axios.create({
  baseURL: "/",
  timeout: 0,
});

// Apply the different filters or feature by calling the methods that get 
// the different processed images but for images in HTML not HTML elements
export function setFilterBis(selected:ImageType){
  processedImage.value = new Blob();
  processedImageURL.value = '';
  const filter = ref('');
  filter.value = filterSelected.value ;
  filterIsSelected.value = true;
  if (filter.value == "Mean") {
    changeMeanParameter(selected.id, 11);
  } else if (filter.value == "Gray"){
    applyGrayFilter(selected.id);
  } else if (filter.value == "Sobel"){
    applySobelFilter(selected.id);
  } else if (filter.value == "Color"){
    applyColorFilter(selected.id, 0);
  } else if (filter.value == "Histogram"){
    showHistogram(selected.id);
  } else {
    showHistogram2D(selected.id);
  }
}

// Apply the different filters or feature by calling the methods that get 
// the different processed images
export function setFilter(event:Event, selected:ImageType){
  processedImage.value = new Blob();
  processedImageURL.value = '';
  let filter = (event.target as HTMLSelectElement).value;
  filterSelected.value=filter;
  filterIsSelected.value = true;
  if (filter == "Mean") {
    changeMeanParameter(selected.id, 11);
  } else if (filter == "Gray"){
    applyGrayFilter(selected.id);
  } else if (filter == "Sobel"){
    applySobelFilter(selected.id);
  } else if (filter == "Color"){
    applyColorFilter(selected.id, 0);
  } else if (filter == "Histogram"){
    showHistogram(selected.id);
  } else {
    showHistogram2D(selected.id);
  }
}

// Updates the filter or feature selected by the user
export function resetFilter(){
  filterSelected.value='';
  showSelect.value=false;
  filterIsSelected.value = false;
}

// Set the color code parameter and call the corresponding get method with it
export function setColor(code:number, id:number){
  processedImage.value = new Blob();
  processedImageURL.value = '';
  colorCode.value=colorList.value[code];
  applyColorFilter(id, colorCode.value)
}

// Get method for the processed image (gray filter)
export function applyGrayFilter(id:number){
  processing();
  instance.get('images/' + id + urlFilterSingleParam + 'Gray', { responseType: 'blob' })
  .then(response => {
    if(response.data){
      processedImage.value = new Blob([response.data], { type: response.headers['content-type'] });
      processedImageURL.value = window.URL.createObjectURL(processedImage.value);
      processed();
    } else {
      console.error('Error: Response data is undefined');
    }
  })
  .catch(error => {
    console.log(error)
  })
}

// Get method for the processed image (color filter)
export function applyColorFilter(id:number, colorCode:number){
  processing();
  instance.get('images/' + id + urlColorFilter + colorCode, { responseType: 'blob' })
  .then(response => {
    if(response.data){
      processedImage.value = new Blob([response.data], { type: response.headers['content-type'] });
      processedImageURL.value = window.URL.createObjectURL(processedImage.value);
      processed();
    } else {
      console.error('Error: Response data is undefined');
    }
  })
  .catch(error => {
    console.log(error)
  })
}

// Get method for the processed image (sobel filter)
export function applySobelFilter(id:number){
  processing();
  instance.get('images/' + id + urlFilterSingleParam + 'Sobel', { responseType: 'blob' })
  .then(response => {
    if(response.data){
      processedImage.value = new Blob([response.data], { type: response.headers['content-type'] });
      processedImageURL.value = window.URL.createObjectURL(processedImage.value);
      processed();
    } else {
      console.error('Error: Response data is undefined');
    }
  })
  .catch(error => {
    console.log(error)
  })
}

// Get method for the processed image (mean filter)
export function changeMeanParameter(id:number, filterSize:number){
  processing();
  instance.get('images/' + id + urlMeanFilter + filterSize, { responseType: 'blob' })
  .then(response => {
    if (response.data) {
      processedImage.value = new Blob([response.data], { type: response.headers['content-type'] });
      processedImageURL.value = window.URL.createObjectURL(processedImage.value);
      processed();
    } else {
      console.error('Error: Response data is undefined');
    }
  })
  .catch(error => {
      console.log(error)
  })
}

// Get method for the processed image (display RGB histogram)
export function showHistogram(id:number){
  processing();
  instance.get('images/' + id + urlFilterSingleParam + 'Histogram', { responseType: 'blob' })
  .then(response => {
    if(response.data){
      processedImage.value = new Blob([response.data], { type: response.headers['content-type'] });
      processedImageURL.value = window.URL.createObjectURL(processedImage.value);
      processed();
    } else {
      console.error('Error: Response data is undefined');
    }
  })
  .catch(error => {
    console.log(error)
  })
}

// Get method for the processed image (display 2Dhistogram)
export function showHistogram2D(id:number){
  processing();
  instance.get('images/' + id + urlFilterSingleParam + 'Histogram2D', { responseType: 'blob' })
  .then(response => {
    if(response.data){
      processedImage.value = new Blob([response.data], { type: response.headers['content-type'] });
      processedImageURL.value = window.URL.createObjectURL(processedImage.value);
      processed();
    } else {
      console.error('Error: Response data is undefined');
    }
  })
  .catch(error => {
    console.log(error)
  })
}