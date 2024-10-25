<script setup lang="ts">

import {selected,filterSelected, downloadBlob, filterIsSelected, processedImageURL, processedImage, isProcessing} from '../http-api'; 
import {changeMeanParameter, setColor} from '../utils'; 
import {ref} from 'vue'

const slider = document.getElementById('slider') as HTMLInputElement;
const output = document.getElementById('value');
// Value of the mean parameter (size) -> by default 11
const val = ref<String>('11');

// Update the value of the slider for the output
if(slider != null && output != null){
  slider.addEventListener('input', function() {
    const value = slider.value;
    output!.textContent = value;
  });
}

// Updates live the value of the slider (value of mean filter)
function updateValue(){
  const slider = document.getElementById('slider') as HTMLInputElement;
  if (slider) {
    slider.addEventListener('input', () => {
    val.value = (slider.value);
    });
  }
}

</script>


<template>

<div class="sidebar">

<h1 class="colorTitle">  {{ filterSelected }}   </h1>

<div v-if="isProcessing">
  <img src="../assets/spinner.gif" class="filter-imgs processing-spinner"/>
</div>

<div v-show="!isProcessing">
  
  <img v-if="filterSelected=='Gray'" :src="processedImageURL?.toString()" id="selected" class="filter-imgs" >
  <img v-if="filterSelected=='Mean' || filterSelected=='Sobel' || filterSelected=='Histogram' || filterSelected=='Histogram2D'" :src="processedImageURL?.toString()" id="selected" class="filter-imgs">
</div>
<input v-if="filterSelected=='Mean'" @input="updateValue()" type="range" min="1" max="99" :value=val step="2" :v-model=val id="slider" class="sliderRange">

<p v-if="filterSelected=='Mean'" class="submit_button">Value: <span id="value">{{val}}</span></p>
<button @click="changeMeanParameter(selected.id,Number(val))" v-if="filterSelected=='Mean'" type="submit" class="submit_button">Apply Processing</button>
<div v-if="filterSelected=='Color'">

  <img :src="processedImageURL?.toString()" id="selected" class="filter-imgs">

  <p class="colorTitle">Choose which color to apply</p>

  <div class="colorList">

      <button><img src="../assets/red-filter.png" @click="setColor(0, selected.id)" class="color"></button>
      <button><img src="../assets/orange-filter.png" @click="setColor(1, selected.id)" class="color"></button>
      <button><img src="../assets/yellow-filter.png" @click="setColor(2, selected.id)" class="color"></button>
      <button><img src="../assets/jade-filter.png" @click="setColor(3, selected.id)" class="color"></button>
      <button><img src="../assets/light-green-filter.png" @click="setColor(4, selected.id)" class="color"></button>
      <button><img src="../assets/green-filter.png" @click="setColor(5, selected.id)" class="color"></button>
      <button><img src="../assets/sky-blue-filter.png" @click="setColor(6, selected.id)" class="color"></button>
      <button><img src="../assets/light-blue-filter.png" @click="setColor(7, selected.id)" class="color"></button>
      <button><img src="../assets/blue-filter.png" @click="setColor(8, selected.id)" class="color"></button>
      <button><img src="../assets/purple-filter.png" @click="setColor(9, selected.id)" class="color"></button>
      <button><img src="../assets/magenta-filter.png" @click="setColor(10, selected.id)" class="color"></button>
      <button><img src="../assets/pink-filter.png" @click="setColor(11, selected.id)" class="color"></button>

  </div>

</div>

<br>
<img v-if="filterIsSelected && filterSelected!=''" @click="downloadBlob(processedImage!, selected.name);" src="../assets/telecharger.png" alt="download" class="dlButton" id="DlMeanFilterImg">

</div>

       
</template>