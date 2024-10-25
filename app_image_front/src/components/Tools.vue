<script setup lang="ts">

import { downloadImage,imageUrl,previous,next,remove,selected,getSimilarity,showTool,setTool,
imgProcessingFilters, loaded, loading, isLoading, showObjectsImg, filterSelected} from '../http-api';
import { showForm, showSelect, resetFilter, setFilter, setFilterBis } from '../utils';
import Metadata from './Metadata.vue';
import Similarity from './Similarity.vue';
import ImageProcessing from './ImageProcessing.vue';
import { ref } from 'vue';


// Number of similar images requested by the user (default is 5)
const N = ref(5);
// String that will contain the descriptor
const desc = ref('');

// Method used to display the formular (Histograms -> image comparisons)
function displayForm(d:string){
  showForm.value=!showForm.value;
  desc.value = d;
}

// Method to toggle the select button (Features and filters)
function toggleSelectButton(){
  showForm.value=false;
  filterSelected.value='';
  if (showTool.value=='processing'){
    setTool('');
    showSelect.value = false;
  }
  else{
    setTool('processing');
    showSelect.value = true;
  }  
} 

// Updating the boolean to know if the objects within an image should be displayed
function setObjectsShow(){
  showObjectsImg.value = (!showObjectsImg.value);
  isLoading.value = true;
  console.log("loading");
}

// Submit the process to get the list of similar images
function submitSimilarity(id:number) {
  setTool('');
  if (!isNaN(N.value) && N.value > 0) {
    setTool('');
    displayForm(desc.value);
    getSimilarity(id, N.value, desc.value);
    setTool('similarity');
  } else {
    alert('You must choose a positive number !')
    console.error('FAILED !!');
  }
}

// Code to display the metadata of an image when the user asks
const loadMetadata = () => {
  showForm.value=false;
  if (showTool.value=='metadata'){
    setTool('');
  }
  else{
    setTool('metadata');
  }  
};

// Top-left button (when app is running, to get back to the home page)
function goBack(){
  setTool('');
  filterSelected.value = '';
  loading();
  showObjectsImg.value = false;
  showSelect.value=false;
}

</script>

<template>

    <nav id="tools">
      <ul class="left-tools">    
        <li>
          <router-link to="/">
            <img @click="goBack()" src="../assets/retour.png" alt="back" class="back" title="Go back">
            
          </router-link>
        </li>
      </ul>
      <router-link to="/">
        <img @click="goBack()" src="../assets/logoV2B.svg" class="logo">
      </router-link>
      <ul class="right-tools">
        <li>
          <router-link to="/">
          <img @click="remove(selected)" src="../assets/supprimer.png" alt="remove" class="tool" title="Remove Image">
          </router-link>
        </li>
        <li>
          <img @click="displayForm('hshist'); resetFilter()" src="../assets/hthist.png" download="img" alt="hshist" class="tool" title="HS comparison">
        </li>
        <li>
          <img @click="displayForm('rgbhist'); resetFilter()" src="../assets/rgbhist.png" download="img" alt="rghist" class="tool" title="RGB comparison">
        </li>
        <li>
          <img @click="downloadImage(selected); resetFilter()" src="../assets/telecharger.png" download="img" alt="download" class="tool" title="Download Image">
        </li>
        <li>
          <img @click="loadMetadata(); resetFilter()" src="../assets/informations.png" download="img" alt="informations" class="tool" title="Show Metadata">
        </li>
        <li>
          <img @click="setObjectsShow(); resetFilter()" src="../assets/id-objects.png" download="img" alt="Show objects" class="tool" title="Show Objects">
        </li>
        <li>
          <img @click="toggleSelectButton()" src="../assets/filtre.png" download="img" alt="Image processing" class="tool" title="Filters">
        </li>

      </ul>
    </nav>

    <form class="input-form" v-if="showForm" @submit.prevent="submitSimilarity(selected.id)">
      <input type="number" v-model="N" placeholder="Entrez un nombre">
      <button type="submit">Search</button>
    </form>

    <select v-if="showSelect" id="selectButton" v-on:change="setFilter($event, selected)">
      <option disabled selected value> -- Select a filter or a feature -- </option>
      <option v-for="(option, index) in imgProcessingFilters":key="index">{{ option }}</option>
    </select>

  <div class="container">
    <div :class="showTool=='' ? 'img-view-cont-full' : 'img-view-cont'" >
      <ul class="view">
        <li>
          <img @click="previous(selected) ; setFilterBis(selected)" src="../assets/fleche-gauche.png" class="arrow"/>
        </li>
        <li>
          <img v-if="isLoading" src="../assets/spinner.gif" class="imgSelected spinner"/>
          <img v-show="!isLoading" :src="showObjectsImg ? imageUrl + selected.id + '/objects' : imageUrl + selected.id" class="imgSelected" @load="() => loaded()">
        </li>

        <li>
          <img @click="next(selected) ; setFilterBis(selected)" src="../assets/fleche-droite.png" class="arrow">
        </li>
      </ul>
    </div>
    <div :class="showTool=='' ? 'sidebar-cont invisible' : 'sidebar-cont'">
      <Metadata v-if="showTool=='metadata'"/>
      <Similarity v-if="showTool=='similarity'" />
      <ImageProcessing v-if="showTool=='processing'" />
    </div>
  </div>
  
</template>

