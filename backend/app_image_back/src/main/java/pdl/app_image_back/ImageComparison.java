package pdl.app_image_back;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.lang.IndexOutOfBoundsException;

/**
 * A utility class for comparing images based on histograms.
 */
public class ImageComparison {

    /**
     * Computes the Euclidean distance between two arrays.
     * 
     * @param i - The first array.
     * @param j - The second array.
     * @return The Euclidean distance between the two arrays.
     */
    private static Double getDistance(double[] i, double[] j){

        double total = 0.0;
        for (int k = 0; k < i.length; k++) {
            total += Math.pow((i[k] - j[k]), 2);
        }

        return Double.valueOf(Math.sqrt(total));
    }

    /**
     * Compares a given histogram with a list of histograms and returns a map of distances.
     * 
     * @param in     - The histogram to compare.
     * @param imList - The list of histograms to compare against as a Map.
     * @return A map containing distances between the given histogram and each histogram in the list.
     */
    private static Map<Long, Double> compareHist(double[] in, Map<Long, double[]> imList){

        Map<Long, Double> out = new HashMap<>();

        for (Map.Entry<Long, double[]> pair : imList.entrySet()) {
            out.put(pair.getKey(), getDistance(in, pair.getValue()));
        }

        return out;
    }

    /**
     * Compares the hue-saturation histogram of an image with the histograms of other images in the list.
     * 
     * @param inId   - The ID of the input image.
     * @param imList - The list of images.
     * @param maxNum - The maximum number of results to return.
     * @return An ArrayList containing arrays of Image objects and their corresponding distances, sorted by distance.
     * @throws IndexOutOfBoundsException if the input image ID is invalid.
     */
    public static ArrayList<Object[]> compareHueSatHist(long inId, ArrayList<Image> imList, int maxNum){
        Map<Long, double[]> arrayMap = new HashMap<>();
        Map<Long, Image> imgMap = new HashMap<>();

        Image inImg = new Image("", new byte[0], -1, false);

        for (Image img : imList) {
            if (img.getId() != inId){
                arrayMap.put(img.getId(), img.getHueTintHist());
            }
            else {
                inImg = img;
            }
            imgMap.put(img.getId(), img);
        }

        if (inImg.isEmpty()){
            throw new IndexOutOfBoundsException(inId);
        }

        Map<Long, Double> distances = compareHist(inImg.getHueTintHist(), arrayMap);

        ArrayList<Object[]> out = new ArrayList<>();

        for (Map.Entry<Long, Double> pair : distances.entrySet()) {
            Object[] field = {imgMap.get(pair.getKey()), pair.getValue()};
            out.add(field);
        }

        out.sort(Comparator.comparingDouble(o -> (Double) o[1]));

        int size = Math.min(maxNum, out.size());
        ArrayList<Object[]> result = new ArrayList<>(out.subList(0, size));

        return result;
    }

    /**
     * Compares the RGB histogram of an image with the histograms of other images in the list.
     * 
     * @param inId   - The ID of the input image.
     * @param imList - The list of images.
     * @param maxNum - The maximum number of results to return.
     * @return An ArrayList containing arrays of Image objects and their corresponding distances, sorted by distance.
     * @throws IndexOutOfBoundsException if the input image ID is invalid.
     */
    public static ArrayList<Object[]> compareRGBHist(long inId, ArrayList<Image> imList, int maxNum){
        Map<Long, double[]> arrayMap = new HashMap<>();
        Map<Long, Image> imgMap = new HashMap<>();

        Image inImg = new Image("", new byte[0], -1, false);

        for (Image img : imList) {
            if (img.getId() != inId){
                arrayMap.put(img.getId(), img.getRGBHist());
            }
            else {
                inImg = img;
            }
            imgMap.put(img.getId(), img);
        }

        if (inImg.isEmpty()){
            throw new IndexOutOfBoundsException(inId);
        }

        Map<Long, Double> distances = compareHist(inImg.getRGBHist(), arrayMap);

        ArrayList<Object[]> out = new ArrayList<>();

        for (Map.Entry<Long, Double> pair : distances.entrySet()) {
            Object[] field = {imgMap.get(pair.getKey()), pair.getValue()};
            out.add(field);
        }

        out.sort(Comparator.comparingDouble(o -> (Double) o[1]));

        int size = Math.min(maxNum, out.size());
        ArrayList<Object[]> result = new ArrayList<>(out.subList(0, size));

        return result;
    }
}