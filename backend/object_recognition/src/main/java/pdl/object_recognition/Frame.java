package pdl.object_recognition;

import java.util.Arrays;

import org.opencv.core.Rect2d;

/**
 * Represents a frame defined by two sets of coordinates.
 */
public class Frame {
    Coordinates i;
    Coordinates j;

    /**
     * Constructs a new Frame object with the specified coordinates.
     * @param i The first coordinate.
     * @param j The second coordinate.
     */
    public Frame(Coordinates i, Coordinates j){
        Integer xs[] = {i.getX(), j.getX()};
        Integer ys[] = {i.getY(), j.getY()};

        Arrays.sort(xs);
        Arrays.sort(ys);
        this.i = new Coordinates(xs[0], ys[0]);
        this.j = new Coordinates(xs[1], ys[1]);
    }

    /**
     * Constructs a new Frame object with the specified coordinates.
     * @param x1 The x-coordinate of the first point.
     * @param y1 The y-coordinate of the first point.
     * @param x2 The x-coordinate of the second point.
     * @param y2 The y-coordinate of the second point.
     */
    public Frame(int x1, int y1, int x2, int y2){
        Integer xs[] = {x1, x2};
        Integer ys[] = {y1, y2};

        Arrays.sort(xs);
        Arrays.sort(ys);
        this.i = new Coordinates(xs[0], ys[0]);
        this.j = new Coordinates(xs[1], ys[1]);
    }

    /**
     * Gets the height of the frame.
     * @return The height of the frame.
     */
    public Integer getHeight(){
        return (this.j.getY() - this.i.getY());
    }

    /**
     * Gets the width of the frame.
     * @return The width of the frame.
     */
    public Integer getWidth(){
        return (this.j.getX() - this.i.getX());
    }

    /**
     * Gets the first coordinate.
     * @return The first coordinate.
     */
    public Coordinates getI(){
        return this.i;
    }

    /**
     * Gets the second coordinate.
     * @return The second coordinate.
     */
    public Coordinates getJ(){
        return this.j;
    }

    /**
     * Converts the frame to a Rect2d object.
     * @return The Rect2d object representing the frame.
     */
    public Rect2d toRect2d(){
        return new Rect2d(this.getI().getX(), this.getI().getY(), this.getWidth(), this.getHeight());
    }
}