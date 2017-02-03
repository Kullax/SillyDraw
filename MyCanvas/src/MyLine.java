import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Created by Martin on 03-Feb-17.
 */
public class MyLine implements java.io.Serializable{
    private double x1, x2, y1, y2;
    private float pressure;
    public double getX1() {
        return x1;
    }

    public double getY1() {
        return y1;
    }

    public void setX2(double x){
        x2 = x;
    }

    public void setY2(double y){
        y2 = y;
    }

    public void setX1(double x){
        x1 = x;
    }

    public void setY1(double y){
        y1 = y;
    }

    public Point2D getP1() {
        return null;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }

    public Point2D getP2() {
        return null;
    }

    public void setLine(double x1, double y1, double x2, double y2, float pressure) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.pressure = pressure;
    }

    public Rectangle2D getBounds2D() {
        return null;
    }

    public float getPressure(){ return pressure; };
}