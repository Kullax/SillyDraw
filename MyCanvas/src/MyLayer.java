import Interfaces.Layer;

import java.util.ArrayList;

/**
 * Created by Martin on 03-Feb-17.
 */
public class MyLayer extends Layer implements java.io.Serializable {
    public ArrayList<Stroke> strokes = new ArrayList<Stroke>();
    public String name = "";

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    private double opacity = 1d;


    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    private boolean visible;


}
