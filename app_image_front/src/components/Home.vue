<script setup lang="ts">

import { imgList, imageUrl, get, fileUpload, remove, downloadImage, setFav, getFav, displayFav, objectSearched, getSearched, refreshGallery, erase, downloadFavorites } from '../http-api'

// Code to setup the display of a horizontal bar (with download and delete button) whenever the mouse hovers over an image
function displayIcon(index:number){
  const icons = document.querySelectorAll(".overlay");  
  (icons[3*index] as HTMLElement).style.opacity = "1"; 
  (icons[3*index+1] as HTMLElement).style.opacity = "1";
  (icons[3*index+2] as HTMLElement).style.opacity = "1";
  const borders = document.querySelectorAll(".border"); 
  (borders[index] as HTMLElement).style.opacity = "1"; 
}

// Code to disable the horizontal bar whenever the mouse leaves the image 
function hideIcon(){
  const icons = document.querySelectorAll(".overlay");
  for (const icon of icons) {
    (icon as HTMLElement).style.opacity = "0";
  }
  const borders = document.querySelectorAll(".border");
  for (const border of borders) {
    (border as HTMLElement).style.opacity = "0";
  }
}

// Code to get to the top of the page when the "TOP button" is clicked 
function scrollToTop() {
  window.scrollTo({
  top: 0,
  left: 0,
  behavior: "smooth",
  });
}

</script>

<template>  


  <nav id="tools">

    <ul class="left-tools">

      <div class="search-bar">
          <div class="mainbox">
              <div class="iconContainer">
                  <svg v-if="objectSearched==''" title="Search an object" viewBox="0 0 512 512" height="1em" xmlns="http://www.w3.org/2000/svg" class="search_icon"><path d="M416 208c0 45.9-14.9 88.3-40 122.7L502.6 457.4c12.5 12.5 12.5 32.8 0 45.3s-32.8 12.5-45.3 0L330.7 376c-34.4 25.2-76.8 40-122.7 40C93.1 416 0 322.9 0 208S93.1 0 208 0S416 93.1 416 208zM208 352a144 144 0 1 0 0-288 144 144 0 1 0 0 288z"></path></svg>
                  <img v-if="objectSearched!=''" @click="erase()" src="../assets/erase.png" class="tool erase" >
                </div>
          <input v-model="objectSearched" @input ="getSearched()" class="search_input" placeholder="Search an object" type="text">
          
          </div>
      </div>
    </ul>

    <img src="../assets/logoV2B.svg" class="logo">
    
    <ul class="right-tools">        
      <li>
        <img @click="fileUpload()"  src="../assets/importer.png" id="uploaded" class="tool" title="Import Image">         
      </li>
      <li>
        <img v-if="displayFav" @click="getFav()" src="../assets/star-filled.png" class="tool" id="fav" title="Show all">
        <img v-else @click="getFav()" src="../assets/star.png" class="tool" id="fav" title="Show favorites">
      </li>
      <li>
        <img @click="refreshGallery()" src="../assets/rafraichir.png" class="tool" id="refresh" title="Refresh Gallery">
      </li>
      <li>
        <img v-if="displayFav" @click="downloadFavorites(imgList)" src="../assets/telecharger-favs.png" class="tool" id="dlFavs" title="Download list of images">
      </li>
    </ul>
  </nav>

  <div id="backToTop"></div>
  <div class="imageList">    
    <div class="gallery-container"  v-for="(image, index) in imgList">
      <router-link to="/imagetools">
        <img :key="image.id" :src="imageUrl+image.id" @mouseover="displayIcon(index)" @mouseleave="hideIcon()" @click="get(image)" alt="image" class="imgs" id="selected">
      </router-link>    
      <div class="border" @mouseleave="hideIcon()" @mouseover="displayIcon(index)">
        <ul class="overlay-icons">
        <li>
          <a :href=imageUrl+image.id download="img">
            <img @click="downloadImage(image)" src="../assets/telecharger.png" @mouseover="displayIcon(index)" download="img" alt="download" class="overlay" id="left" title="Download Image">
          </a>
        </li>
        <li>
          <img v-if="image.favorite" @click="setFav(image)" src="../assets/star-filled.png" class="overlay fav" id="middle" title="Remove Favorite">
          <img v-else @click="setFav(image)" src="../assets/star.png" class="overlay fav" id="middle" title="Add Favorite">
        </li>
        <li>
          <img @click="remove(image)" src="../assets/supprimer.png" class="overlay" id="right" title="Delete Image">
        </li>
      </ul>
      
      </div>
    </div>
  </div>
 
  <img @click="scrollToTop()" src="../assets/fleches-haut.png" alt="top" class="top" title="Go to top">

</template>

