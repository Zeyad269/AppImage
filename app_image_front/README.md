# Front end

## Site display and features

The first page of the site, considered the home page, includes:
   - A toolbar at the top of the page with our group logo as well as five features:
      - search bar => allows the client to search an object and display all the images containing that object in the gallery
      - refresh => allows the client to refresh the gallery
      - favorites => allows the client to display his favorites images
      - import => allows the client to add an image to the server
      - download favorites => when the favorites are displayed , allows the client to download the list of his favorites images to disk
   - An image gallery showing the current state of the server, with options for each image that are displayed when browsing each one:
      - download => allows the client to download the image
      - favorite => allows the client to add the image to favorites
      - delete => allows the client to delete the image
   - A button at the bottom right of the page to return to the top of the gallery

The second page of the site, displayed by clicking on one of the images in the gallery, includes:
   - The selected image in large format
   - Right and left arrows to navigate between gallery images
   - A toolbar at the top of the page with several features:
      - go back => a back button which returns to the gallery
      - the logo of our group which, when clicked, also allows you to return to the gallery
      - imageprocessing => allows the client to apply filter or features to the selected image
      - object recognition => allows the client to detect the differents objects present in the selected image
      - metadata => an information button that displays the metadata of the currently selected image
      - download => a button that allows the client to download the selected image to disk
      - rgb & hue/sat descriptors => a first button which allows the client to display images similar to the selected image according to an RGB descriptor and a second button which performs the same operation but according to an HT (Hue Sat) descriptor
      - delete => a button that allows the client to delete the selected image from the gallery

## ImageProcessing features

The imageprocessing tool is used to apply different filters to the selected image :
   - gray filter 
   - colored filter : the client can choose between 12 differents colors
   - mean filter : the client can use a scale to select the mean level to apply 
   - sobel filter
It can also display the histogram and the 2D (Hue/Sat) histogram of the selected image

## Implementation

The application is displayed through different `view` components organized as follows:
      - A first main component `App.vue` allowing the display of a page in its entirety through a “router-view”
      - The `Home.vue` component allowing you to display the first page of the site
      - The `Tools.vue` component which contains the visual rendering of the second page of the site
      - Three components imported into `Tools.vue`:
         - `Metadata.vue` which displays the metadata of an image
         - `Similarity.vue` which displays images similar to a selected image
         - `ImageProcessing.vue` which displays the visual rendering of the processed image 
      - The display of these three components, managed in `Tools.vue`, is done using "v-if" according to the functionality chosen by the customer on the toolbar

Navigation between pages is done using a `router` with:
      - A `routeur.ts` file which includes the inventory of the urls of the different pages of the site associated with their respective component
      - "router-link" in components, which allow you to change the content of the "router-view" located in `App.vue`, in order to load the content of the desired page

Requests sent to the server are handled in `http-api.ts`
