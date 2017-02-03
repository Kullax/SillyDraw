import java.awt.*;
import java.util.Set;

/**
 * Created by Martin on 03-Feb-17.
 */
public class Stroke implements java.io.Serializable{
    Set<MyLine> lines;
    Color color;
    public Stroke(Set<MyLine> lines, Color black){
        this.lines = lines;
        this.color = black;
    }
}