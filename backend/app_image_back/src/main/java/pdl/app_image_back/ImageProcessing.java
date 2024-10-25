package pdl.app_image_back;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import boofcv.alg.color.ColorHsv;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.Planar;

/**
 * Provides methods for image processing operations.
 */
public class ImageProcessing {

    /**
     * Applies Gray Filter to an image
     * 
     * @param image - The image that we want to be processed.
     * @return A new byte[], namely the processed input image
     * 
     * The content of the input image is converted to Planar to modify
     * the bands of the image in order to apply the filter and then converted
     * again to byte[] to chime with Image.java class.
     */
    public static byte[] grayFilter(Image image) {
        // Gray 
        float redLevel = 0.30f;
        float greenLevel = 0.59f;
        float blueLevel = 0.11f;

        Planar<GrayF32> inputPlanar = new Planar<>(GrayF32.class,1,1,3);    // Use Planar for colors and GrayF32 for more accuracy

        GrayF32 gray = new GrayF32();

        InputStream input = new ByteArrayInputStream(image.getData());
        BufferedImage buffered;
        try {
            buffered = ImageIO.read(input);
        } catch (IOException e) {
            return new byte[0];
        }
        BufferedImage inputbuffer = new BufferedImage(buffered.getWidth(), buffered.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = inputbuffer.createGraphics();
        g2d.drawImage(buffered, 0, 0, null);
        g2d.dispose();

        ConvertBufferedImage.convertFrom(inputbuffer, inputPlanar, true);

        gray.reshape(inputbuffer.getWidth(), inputbuffer.getHeight());

        for (int x = 0; x < inputbuffer.getWidth(); x++) {
            for (int y = 0; y < inputbuffer.getHeight(); y++) {
                gray.set(x, y, inputPlanar.getBand(0).get(x, y)*redLevel + inputPlanar.getBand(1).get(x, y)*greenLevel + inputPlanar.getBand(2).get(x, y)*blueLevel);
            }
        }

        ConvertBufferedImage.convertTo(gray, inputbuffer);
        ByteArrayOutputStream dataImg = new ByteArrayOutputStream();
        try {
            ImageIO.write(inputbuffer,"jpg",dataImg);
        } catch (IOException e) {
            return new byte[0];
        } 
        byte[] bytes = dataImg.toByteArray();

        return bytes;
    }

    /**
     * Applies Mean Filter to an image
     * 
     * @param image - The image that we want to be processed.
     * @param size - The size of the mean filter  that will be applied
     * @return A new byte[], namely the processed input image after a copy
     * 
     * The copy of the input image (output) is now a Planar. We then modify
     * the bands of the image in order to apply the filter and convert it
     * to byte[] to chime with Image.java class.
     */
    public static byte[] meanFilter(Image image, int size){
        
        Planar<GrayF32> inputPlanar = new Planar<>(GrayF32.class,1,1,3);    // Use Planar for colors and GrayF32 for more accuracy

        Planar<GrayF32> outputPlanar = inputPlanar.createSameShape();

        InputStream input = new ByteArrayInputStream(image.getData());
        BufferedImage buffered;
        try {
            buffered = ImageIO.read(input);
        } catch (IOException e) {
            return new byte[0];
        }
        BufferedImage inputbuffer = new BufferedImage(buffered.getWidth(), buffered.getHeight(), BufferedImage.TYPE_INT_RGB);
        BufferedImage outputbuffer = new BufferedImage(buffered.getWidth(), buffered.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = inputbuffer.createGraphics();
        g2d.drawImage(buffered, 0, 0, null);
        g2d.dispose();

        ConvertBufferedImage.convertFrom(inputbuffer, inputPlanar, true);

        outputPlanar.reshape(inputbuffer.getWidth(), inputbuffer.getHeight());
        
        // Processing here
        applyMeanFilter(inputPlanar, outputPlanar, size);

        
        ConvertBufferedImage.convertTo_F32(outputPlanar, outputbuffer, true);

        ByteArrayOutputStream dataImg = new ByteArrayOutputStream();
        try {
            ImageIO.write(outputbuffer,"jpg",dataImg);
        } catch (IOException e) {
            return new byte[0];
        } 
        byte[] bytes = dataImg.toByteArray();
        
        return bytes;
        
    }

    /**
     * Computes the mean value according to the neighborhood of a pixel of coord (x,y)
     * @param input The input image
     * @param x,y The coordinatess of a pixel (x,y)
     * @param size The size of the filter
     */
    private static float getAverage(GrayF32 input, int x, int y, int size){
        float total = 0f;
        int radius = size/2;
        float weight = 0f;
        for (int i = y-radius; i <= y+radius; ++i) {
                for (int j = x-radius; j <= x+radius; ++j) {
                    if (input.width > j && j >= 0 && input.height > i && i >= 0) {
                    weight += 1;
                    total += input.get(j, i);
                    }
                }
            }
        return total/weight;
      }
    
    /**
     * Applies a mean filter to an image.
     * @param input The input image
     * @param output The output image
     * @param size The size of the filter kernel
     */
    public static void applyMeanFilter(Planar<GrayF32> input, Planar<GrayF32> output, int size) {
        for (int y = 0; y < input.height; ++y) {
            for (int x = 0; x < input.width; ++x) {
                for (int c = 0; c < input.getNumBands(); c++) {
                    output.getBand(c).set(x, y, getAverage(input.getBand(c), x, y, size));
                }
            }
        }
    }


    /**
     * Display the hue histogram of an image
     * 
     * @param image - The image that we want to display its histogram
     * @return A byte[], namely the image's histogram 
     * 
     *
     */
    public static byte[] Histogram(Image image){


        /**  Converting the image to process the display of the histogram  **/

        Planar<GrayF32> inputPlanar = new Planar<>(GrayF32.class,1,1,3);

        InputStream input = new ByteArrayInputStream(image.getData());
        BufferedImage buffered;

        try {
            buffered = ImageIO.read(input);
        } catch (IOException e) {
            return new byte[0];
        }
        BufferedImage inputbuffer = new BufferedImage(buffered.getWidth(), buffered.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = inputbuffer.createGraphics();
        g2d.drawImage(buffered, 0, 0, null);
        g2d.dispose();

        ConvertBufferedImage.convertFrom(inputbuffer, inputPlanar, true);

        /**  Proccessing the display of the histogram  **/

        float[] histArray = new float[360];
        float[] Hsv = new float[3];
        float[] color = new float[3];

        for (int y = 0; y < inputPlanar.getHeight(); ++y) {
			for (int x = 0; x < inputPlanar.getWidth(); ++x) {

                
                for (int canal=0; canal<3; canal++){
                    color[canal] = (float) inputPlanar.getBand(canal).get(x,y);    
                }
                ColorHsv.rgbToHsv(color[0], color[1], color[2], Hsv);

                // Incrementing the histogram array
                histArray[(int) Math.toDegrees(Hsv[0]) ]++;                 

            }
        }

        float hist_max = 0;
        for (int i=0; i < histArray.length; i++){
            if (histArray[i] > hist_max){
                hist_max=histArray[i];
            }
        }

        // Creating the histogram image        
        int width = histArray.length;
        int height = 200;
        GrayF32 hist = new GrayF32(width, height);

        for (int y = 0; y < height-1; y++) {
            for (int x = 0; x < width; x++) {
                hist.set(x, y, 0);
            }
        }
        for (int x = 0; x < width; x++){
            hist.set(x,height-1,255);
        }

        // Normalizing histogram's values 
        float factor = ((float) height / hist_max);
        for (int i=0; i < histArray.length; i++){
            histArray[i] = histArray[i] * factor;            
        }

        // Setting the histogram        
        for( int x = 0; x < histArray.length; x++){
            for (int y = height-1; y > (height-1) - ((int)histArray[x]); y--){
                hist.set(x,y,255);
            }
        }


        /**  Converting the histogram to display it **/

        BufferedImage rgbhist = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        ConvertBufferedImage.convertTo(hist, rgbhist);
        ByteArrayOutputStream dataImg = new ByteArrayOutputStream();

        try {
            ImageIO.write(rgbhist,"jpg",dataImg);
        } catch (IOException e) {
            return new byte[0];
        } 
        byte[] bytes = dataImg.toByteArray();

       return bytes;
    }

    
    /**
     * Display the hue/sat histogram of an image
     * 
     * @param image - The image that we want to display its histogram
     * @return A byte[], namely the image's histogram 
     * 
     *
     */
    public static byte[] Histogram2D(Image image){


        /**  Converting the image to process the display of the histogram  **/

        Planar<GrayF32> inputPlanar = new Planar<>(GrayF32.class,1,1,3);

        InputStream input = new ByteArrayInputStream(image.getData());
        BufferedImage buffered;

        try {
            buffered = ImageIO.read(input);
        } catch (IOException e) {
            return new byte[0];
        }
        BufferedImage inputbuffer = new BufferedImage(buffered.getWidth(), buffered.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = inputbuffer.createGraphics();
        g2d.drawImage(buffered, 0, 0, null);
        g2d.dispose();

        ConvertBufferedImage.convertFrom(inputbuffer, inputPlanar, true);

        /**  Proccessing the display of the histogram  **/

        int max_hue = 360;
        int max_saturation = 101;

        float[][] hist2DArray = new float[max_hue][max_saturation];
        float[] Hsv = new float[3];
        float[] color = new float[3];

        for (int y = 0; y < inputPlanar.getHeight(); ++y) {
			for (int x = 0; x < inputPlanar.getWidth(); ++x) {

                
                for (int canal=0; canal<3; canal++){
                    color[canal] = (float) inputPlanar.getBand(canal).get(x,y);    
                }
                ColorHsv.rgbToHsv(color[0], color[1], color[2], Hsv);

                int hue_deg=(int) Math.toDegrees(Hsv[0]);
                int sat_val=(int) (Hsv[1]*100);

                // Incrementing the histogram 2D array
                hist2DArray[ hue_deg ][ sat_val ]++;                 

            }
        }


        // Creating the histogram image        
        int width = max_hue;
        int height = max_saturation;
        GrayF32 hist = new GrayF32(width, height);

        for (int y = 0; y < height-1; y++) {
            for (int x = 0; x < width; x++) {
                hist.set(x, y, 0);
            }
        }


        // Normalizing histogram's values 
        for (int i=0; i < max_hue; i++){
            for (int j=0; j < max_saturation; j++){
                if (hist2DArray[i][j] > 255){
                    hist2DArray[i][j] = 255; 
                }                
            }           
        }

        // Setting the histogram        
        for (int i=0; i < max_hue; i++){
            for (int j=0; j < max_saturation; j++){
                hist.set(i,j,(int)hist2DArray[i][j]);
            }
        }


        /**  Converting the histogram to display it **/

        BufferedImage rgbhist = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        ConvertBufferedImage.convertTo(hist, rgbhist);
        ByteArrayOutputStream dataImg = new ByteArrayOutputStream();

        try {
            ImageIO.write(rgbhist,"jpg",dataImg);
        } catch (IOException e) {
            return new byte[0];
        } 
        byte[] bytes = dataImg.toByteArray();

       return bytes;
    }


    /**
     * Applies a color filter to an image.
     * @param image The input image
     * @param Hue The color code to apply
     * @return A new byte[], namely the processed input image
     */
    public static byte[] hueFilter(Image image, int Hue) {
        

        /**  Converting the image to process the color filter **/

        Planar<GrayF32> inputPlanar = new Planar<>(GrayF32.class,1,1,3);
        Planar<GrayF32> outputPlanar = inputPlanar.createSameShape();

        InputStream input = new ByteArrayInputStream(image.getData());
        BufferedImage buffered;

        try {
            buffered = ImageIO.read(input);
        } catch (IOException e) {
            return new byte[0];
        }
        BufferedImage inputbuffer = new BufferedImage(buffered.getWidth(), buffered.getHeight(), BufferedImage.TYPE_INT_RGB);
      
        Graphics2D g2d = inputbuffer.createGraphics();
        g2d.drawImage(buffered, 0, 0, null);
        g2d.dispose();

        ConvertBufferedImage.convertFrom(inputbuffer, inputPlanar, true);

        outputPlanar.reshape(inputbuffer.getWidth(), inputbuffer.getHeight());

        /**  Processing **/

        applyFilter(inputPlanar, outputPlanar, Hue);

        /**  Converting the colored image to display it **/

        ConvertBufferedImage.convertTo_F32(outputPlanar, inputbuffer, true);

        ByteArrayOutputStream dataImg = new ByteArrayOutputStream();
        try {
            ImageIO.write(inputbuffer,"jpg",dataImg);
        } catch (IOException e) {
            return new byte[0];
        } 
        byte[] bytes = dataImg.toByteArray();

       return bytes;

    }

    /**
     * Applies a filter to an image.
     * 
     * @param input The input image.
     * @param output The output image.
     * @param Hue The color code to apply.
     */
    public static void applyFilter(Planar<GrayF32> input, Planar<GrayF32> output, int Hue){

        float[] Hsv = new float[3];
        float[] color = new float[3];

        for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {

				// On parcourt chaque canal
                for (int canal=0; canal<3; canal++){
                    color[canal] = (float) input.getBand(canal).get(x,y);    
                }

                // On effectue la conversion rgb => hsv sur chaque pixel
                ColorHsv.rgbToHsv(color[0], color[1], color[2], Hsv);                
                float deg = (float) ( ( Math.PI / 180 ) * Hue );                
                Hsv[0] = deg;

                // On effectue la conversion hsv => rgb sur chaque pixel
                ColorHsv.hsvToRgb(Hsv[0],Hsv[1],Hsv[2],color);

                // On ajoute a l'image
                for (int canal=0; canal<3; canal++){
                    output.getBand(canal).set(x,y,(int)color[canal]);    
                }
                                
            }
        }

        
    }

    /**
     * Applies convolution to an image.
     * 
     * @param input The input image.
     * @param output The output image.
     * @param kernel The convolution kernel.
     */
    public static void convolution(GrayF32 input, GrayF32 output, int[][] kernel) {

        int width = kernel[0].length; 
        int rayon = (width-1)/2;
    
        for (int y = rayon; y < (input.height - rayon); ++y) {
          for (int x = rayon; x < (input.width - rayon); ++x)  {
    
            int r=0;
    
            for (int i = -rayon; i <= +rayon; i++) { 
              for (int j = -rayon; j <= +rayon; j++) { 
    
                r +=  input.get(x+i,y+j) * kernel[j+rayon][i+rayon];              
              }
            }
            output.set(x, y, r);
    
          }
        }
    
      }
    
    /**
     * Computes the gradient image using convolution with Sobel operators.
     * 
     * @param input The input image.
     * @param output The output image.
     * @param kernelX The convolution kernel for X-direction.
     * @param kernelY The convolution kernel for Y-direction.
     */
    public static void gradientImage(GrayF32 input, GrayF32 output, int[][] kernelX, int[][] kernelY){

        GrayF32 Gx = new GrayF32(input.width, input.height);
        GrayF32 Gy = new GrayF32(input.width, input.height);

       
        convolution(input, Gx, kernelX);
        convolution(input, Gy, kernelY);

        for (int y = 0; y < input.height; y++) {
            for (int x = 0; x < input.width; x++) {

                int m = (int) Math.sqrt( Math.pow(Gx.get(x, y), 2) + Math.pow(Gy.get(x, y), 2) );
                
                if (m>255){
                    m=255;
                }
                if (m<0){
                    m=0;
                }
                output.set(x, y, m);
            }
        }
    }

    /**
     * Applies a Sobel filter to an image.
     * 
     * @param image - The image that we want to be processed.
     * @return A new byte[], namely the processed input image
     */
    public static byte[] gradientImageSobel(Image image){

        int[][] kernelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] kernelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};


        /***  Applying Gray filter  ***/ 

        float redLevel = 0.30f;
        float greenLevel = 0.59f;
        float blueLevel = 0.11f;

        Planar<GrayF32> inputPlanar = new Planar<>(GrayF32.class,1,1,3);

        GrayF32 output = new GrayF32();
        GrayF32 gray = new GrayF32();
        
        InputStream input = new ByteArrayInputStream(image.getData());
        BufferedImage buffered;
        try {
            buffered = ImageIO.read(input);
        } catch (IOException e) {
            return new byte[0];
        }
        BufferedImage inputbuffer = new BufferedImage(buffered.getWidth(), buffered.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = inputbuffer.createGraphics();
        g2d.drawImage(buffered, 0, 0, null);
        g2d.dispose();

        ConvertBufferedImage.convertFrom(inputbuffer, inputPlanar, true);

        gray.reshape(inputbuffer.getWidth(), inputbuffer.getHeight());
        output.reshape(inputbuffer.getWidth(), inputbuffer.getHeight());

        for (int x = 0; x < inputbuffer.getWidth(); x++) {
            for (int y = 0; y < inputbuffer.getHeight(); y++) {
                gray.set(x, y, inputPlanar.getBand(0).get(x, y)*redLevel + inputPlanar.getBand(1).get(x, y)*greenLevel + inputPlanar.getBand(2).get(x, y)*blueLevel);
            }
        }
        

        /***  Processing  ***/

        gradientImage(gray, output, kernelX, kernelY);


        /***  Converting  ***/

        ConvertBufferedImage.convertTo(output, inputbuffer);
        ByteArrayOutputStream dataImg = new ByteArrayOutputStream();
        try {
            ImageIO.write(inputbuffer,"jpg",dataImg);
        } catch (IOException e) {
            return new byte[0];
        } 
        byte[] bytes = dataImg.toByteArray();

        return bytes;


    }
    


    
}