package pdl.object_recognition;

/**
 * Represents coordinates with x and y values.
 */
public class Coordinates {
    Integer x;
    Integer y;

    /**
     * Constructs a new Coordinates object with the specified x and y values.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public Coordinates(Integer x, Integer y){
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x-coordinate.
     * @return The x-coordinate.
     */
    public Integer getX(){
        return this.x;
    }

    /**
     * Gets the y-coordinate.
     * @return The y-coordinate.
     */
    public Integer getY(){
        return this.y;
    }
}