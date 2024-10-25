<script setup lang="ts">

import { selected } from '../http-api';
</script>

<template>
<div class="sidebar">
    <h1 class="title">Metadata</h1>
    <p> <b>Name</b>: {{ selected.name }} </p>
    <p> <b>Id</b>: {{ selected.id }} </p>
    <p> <b>Size</b>: {{ selected.size }} </p>
    <p> <b>Type</b>: {{ selected.type }} </p>
    <p> <b>Tags</b>: {{ selected.tags.join(", ") }} </p>
</div>

</template>