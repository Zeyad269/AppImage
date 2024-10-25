<script setup lang="ts">

import { imageUrl,imgSimilarList} from '../http-api';

</script>

<template>
<div class="similar-imgs sidebar">    
  <div class="gallery-container"  v-for="(image, index) in imgSimilarList">
    <figure>
      <img :key="index" :src="imageUrl+image.id" alt="image" id="selected">
      <figcaption>d = {{image.distance}}</figcaption>
    </figure>      
          
  </div>
</div>

  
</template>