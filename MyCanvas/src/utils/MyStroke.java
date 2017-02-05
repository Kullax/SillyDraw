package utils;

import java.awt.*;
import java.util.Set;

/**
 * Created by Martin on 05-02-2017.
 */
public class MyStroke implements java.io.Serializable {
    public Set<MyLine> lines;
    public Color color;

    public MyStroke(Set<MyLine> lines, Color black) {
        this.lines = lines;
        this.color = black;
    }
}
