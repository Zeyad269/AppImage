package pdl.object_recognition;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Map;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Provides methods for labeling objects in images.
 */
public class Labeling {

    /**
     * Converts a OpenCV Mat matrix to a BufferedImage.
     * @param matrix The OpenCV Mat matrix to convert.
     * @return The BufferedImage representation of the input matrix.
     * @throws Exception If an error occurs during the conversion.
     */
    private static BufferedImage Mat2BufferedImage(Mat matrix) throws Exception {        
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".png", matrix, mob);
        byte ba[] = mob.toArray();
    
        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(ba));
        return bi;
    }

    /**
     * Labels objects in an image and returns the labeled image.
     * @param bi The input image to label.
     * @param objects The list of objects to label.
     * @return The labeled BufferedImage.
     */
    public static BufferedImage labelImage(BufferedImage bi, ArrayList<Map<String, Object>> objects) {
        byte[] pixels = ((DataBufferByte) bi.getData().getDataBuffer()).getData();
        Mat img = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        img.put(0, 0, pixels);

        Scalar color = new Scalar(new double[] {0, 0, 255});

        int min_margin = (int) Math.round(0.01 * bi.getWidth());
        for (int i = 0; i < objects.size(); i++) {
            int base_x = (int) objects.get(i).get("pos_x");
            int new_x = (int) objects.get(i).get("pos_x");
            if (new_x < min_margin) {
                new_x = min_margin;
            }

            int base_y = (int) objects.get(i).get("pos_y");
            int new_y = (int) objects.get(i).get("pos_y");
            if (new_y < min_margin) {
                new_y = min_margin;
            }
            
            int bottom_x = base_x + (int) objects.get(i).get("width");
            int bottom_y = base_y + (int) objects.get(i).get("height");

            if (bottom_x > bi.getWidth() - min_margin) {
                bottom_x = bi.getWidth() - min_margin;
            }

            if (bottom_y > bi.getHeight() - min_margin) {
                bottom_y = bi.getHeight() - min_margin;
            }

            Point x_y = new Point(new_x, new_y);
            Point w_h = new Point(bottom_x, bottom_y);
            Point text_point = new Point(x_y.x, x_y.y - 5);

            Imgproc.rectangle(img, w_h, x_y, color, 4);
            String label = (String) objects.get(i).get("label");
            Imgproc.putText(img, label, text_point, Imgproc.FONT_HERSHEY_SIMPLEX, 2, color, 2);
        } 

        try {
            return Mat2BufferedImage(img);
        } catch (Exception e) {
            return bi;
        }
    } 
}