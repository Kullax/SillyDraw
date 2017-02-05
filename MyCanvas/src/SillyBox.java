import Interfaces.Layer;
import com.sun.javafx.geom.Vec2d;
import jpen.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import jpen.event.PenAdapter;
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * Created by Martin on 03-Feb-17.
 */
class MyLayer extends Layer implements java.io.Serializable {
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

/**
 * Created by Martin on 03-Feb-17.
 */
class MyLine implements java.io.Serializable{
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

/**
 * Created by Martin on 03-Feb-17.
 */
class Stroke implements java.io.Serializable {
    Set<MyLine> lines;
    Color color;

    public Stroke(Set<MyLine> lines, Color black) {
        this.lines = lines;
        this.color = black;
    }
}

/**
 * Created by Martin on 21-01-2017.
 */
public class SillyBox extends JLayeredPane implements KeyListener {

    Socket socketToServer = null;
    static ObjectInputStream inputStream;
    static ObjectOutputStream outStream;

    boolean ERASE = false;
    boolean STROKE_START = false;
    ArrayList<MyLayer> layers = new ArrayList<MyLayer>();

    int selectedLayer = 0;

    public void SelectLayer(int i){
        selectedLayer = i;
    }

    public void SwapLayers(int i, int j){
        Collections.swap(layers, i, j);
    }

    Set<MyLine> lines = new HashSet<MyLine>();
    private float pressure;
    Color color;

    public void setErase(boolean value){
        ERASE = value;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        for (MyLayer layer : layers){
            if (!layer.isVisible())
                continue;
            for(Stroke s : layer.strokes){
                for(MyLine l : s.lines){
                    g2.setColor(new Color(s.color.getRed(), s.color.getGreen(), s.color.getBlue(),
                            (int)(255* (s.color.getTransparency() * layers.get(layers.indexOf(layer)).getOpacity()))));
                    g2.setStroke(new BasicStroke((float) l.getPressure() * 10, BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_MITER));
                    g2.draw(new Line2D.Double(l.getX1(), l.getY1(), l.getX2(), l.getY2()));
                }
            }
        }
    }

    public void addNotify() {
        super.addNotify();
        requestFocus();
    }

    private Point lastPoint;

    private double getDistance(double x1, double y1, double x2, double y2){
        return Math.hypot(x1-x2, y1-y2);
    }

    private Vec2d moveTowards(double x1, double y1, double x2, double y2){
        double dX = x2 - x1;
        double dY = y2 - y1;

        if(dX > dY){
            dY = dY / dX;
            dX = 1;
        }else{
            dX = dX / dY;
            dY = 1;
        }
        return new Vec2d(dX, dY);
    }

    public void HandleIncomingObject(Object o){
        if(o instanceof Stroke){
            layers.get(selectedLayer).strokes.add((Stroke) o);
            repaint();
        }else{
            System.out.println("Unknown Input");
        }
    }


    class ClientHandler extends Thread {
        private Socket socket;

        ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            outStream = new ObjectOutputStream(socket.getOutputStream());
            try{
                inputStream = new ObjectInputStream(socket.getInputStream());
            }catch (EOFException e){
                inputStream.close();
            }
        }

        @Override
        public void run() {
            System.out.println("Starting a Run");
            boolean running = true;
            while (running) {
                try {
                    Object o = inputStream.readObject();
                    HandleIncomingObject( o);
                } catch (SocketException e){
                    running = false;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Ending a Run");

            try {
                inputStream.close();
                outStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public SillyBox() {
        try {
            socketToServer = new Socket("s1.feral.dk", 15001);
            ClientHandler client = new ClientHandler(socketToServer);
            client.start();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        MyLayer layer_0 = new MyLayer();
        layer_0.setVisible(true);
        layers.add(layer_0);

        MyLayer layer_1 = new MyLayer();
        layer_1.setVisible(true);
        layer_1.setOpacity(0.5);
        layers.add(layer_1);

        color = new Color (17, 255, 21, 255);

        addKeyListener(this);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                STROKE_START = true;
                lastPoint = new Point(e.getX(), e.getY());
            }

            public void mouseReleased(MouseEvent e) {
                if(ERASE) {
                    repaint();
                    return;
                }
                STROKE_START = false;
                Stroke new_stroke = new Stroke(lines, color);
                layers.get(selectedLayer).strokes.add(new_stroke);
                lines = new HashSet<MyLine>();
                repaint();

                try {
                    outStream.writeObject(new_stroke);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {

            public void mouseDragged(MouseEvent e) {
                if(ERASE) {
                    for (Stroke s : layers.get(selectedLayer).strokes) {
                        Iterator<MyLine> iter = s.lines.iterator();

                        while (iter.hasNext()) {
                            MyLine l = iter.next();
                            if (getDistance(e.getX(), e.getY(), l.getX2(), l.getY2()) <= l.getPressure()*10) {
                                Vec2d v = moveTowards(l.getX1(), l.getY1(), l.getX2(), l.getY2());
                                if(l.getX2() < l.getX1())
                                    l.setX2(l.getX2() + Math.abs(v.x));
                                else
                                    l.setX2(l.getX2() - Math.abs(v.x));
                                if(l.getY2() < l.getY1())
                                    l.setY2(l.getY2() + Math.abs(v.y));
                                else
                                    l.setY2(l.getY2() - Math.abs(v.y));
                            } else if (getDistance(e.getX(), e.getY(), l.getX1(), l.getY1()) <= l.getPressure()*10) {
                                    Vec2d v = moveTowards(l.getX1(), l.getY1(), l.getX2(), l.getY2());
                                if(l.getX1() < l.getX2())
                                    l.setX1(l.getX1() + Math.abs(v.x));
                                else
                                    l.setX1(l.getX1() - Math.abs(v.x));
                                if(l.getY1() < l.getY2())
                                    l.setY1(l.getY1() + Math.abs(v.x));
                                else
                                    l.setY1(l.getY1() - Math.abs(v.x));
                            } else if(getDistance(e.getX(), e.getY(), l.getX1(), l.getY1()) <= 5) {
                                System.out.println("m d: " + getDistance(e.getX(), e.getY(), l.getX1(), l.getY1()));
                                System.out.println("d d: " + getDistance(l.getX1(), l.getY1(), l.getX2(), l.getY2()));
                            } else {
                                continue;
                            }
                            if (getDistance(l.getX1(), l.getY1(), l.getX2(), l.getY2()) <= 5 || Double.isFinite(getDistance(e.getX(), e.getY(), l.getX1(), l.getY1())))
                                if(getDistance(e.getX(), e.getY(), l.getX1(), l.getY1()) <= 5)
                                    iter.remove();
                        }
                    }
                    repaint();
                    return;
                }
                Graphics2D g2 = (Graphics2D) getGraphics();
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(),
                        (int)(255* (color.getTransparency() * layers.get(selectedLayer).getOpacity()))));


                g2.setStroke(new BasicStroke((float)pressure*10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));

                MyLine l = new MyLine();
                l.setLine(lastPoint.x, lastPoint.y, e.getX(), e.getY(), pressure);
                lines.add(l);
                if(layers.get(selectedLayer).isVisible())
                    g2.drawLine((int)l.getX1(), (int)l.getY1(), (int)l.getX2(), (int)l.getY2());
                lastPoint = new Point(e.getX(), e.getY());
                g2.dispose();
            }
        });
    }

    public void setPressure(double pressure){
       this.pressure = (float)pressure;
    }

    private void SwitchLayers(){
        if(selectedLayer == 1)
            SelectLayer(0);
        else
            SelectLayer(1);
        System.out.println(selectedLayer);
        System.out.println(layers.get(selectedLayer).getOpacity());
        repaint();
        requestFocus();
    }


    private void Clear(){
        STROKE_START = false;
        layers.get(selectedLayer).strokes.clear();
        repaint();
        requestFocus();
    }

    private void SetLayerOpacity(double i){
        layers.get(selectedLayer).setOpacity(i);
        repaint();
        requestFocus();
    }

    private void ToggleVisibleLayer(){
        layers.get(selectedLayer).setVisible(!layers.get(selectedLayer).isVisible());
        repaint();
        requestFocus();
    }


    private void Undo(){
        if(!layers.get(selectedLayer).strokes.isEmpty()){
            layers.get(selectedLayer).strokes.remove(layers.get(selectedLayer).strokes.remove(layers.get(selectedLayer).strokes.size()-1));
            repaint();
        }else
            System.out.println("EMPTY");
    }


    public static synchronized Cursor createTransparentCursor( int size, int frameThickness, Color frameColor ) {
        final int cursourSize = size + (2 * frameThickness);
        final BufferedImage bufferedImage = new BufferedImage( 32 + 2, 32 + 2, BufferedImage.TYPE_INT_ARGB );
        final Graphics graphic = bufferedImage.getGraphics();
        final Color colTrans = new Color( 0, 0, 0, 0 );
        graphic.fillOval(size / 2, size / 2, size, size);
        graphic.setColor( frameColor );
        graphic.drawRoundRect(size / 2,  size/ 2, size, size, 1, 1);
        final Point hotSpot = new Point( cursourSize / 2, cursourSize / 2 );
        return Toolkit.getDefaultToolkit().createCustomCursor( bufferedImage, hotSpot, "Trans" );
    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame("");

        BufferedImage blankImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

        final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                blankImage, new Point(0, 0), "blank cursor");

        final SillyBox sillyBox = new SillyBox();

        PenManager pm = new PenManager(sillyBox);

        pm.pen.addListener(new PenAdapter() {
            @Override

            public void penLevelEvent(PLevelEvent ev) {
                PKind type = ev.pen.getKind();
                if (type == PKind.valueOf(PKind.Type.CURSOR)){
                    frame.getContentPane().setCursor(Cursor.getDefaultCursor());
                    return;
                }
                float pressure = ev.pen.getLevelValue(PLevel.Type.PRESSURE);
                if(type == PKind.valueOf(PKind.Type.STYLUS)){
                    if(pressure != 0.0f)
                        frame.getContentPane().setCursor(blankCursor);
                    else
                        frame.getContentPane().setCursor(createTransparentCursor(2, 2, Color.black));
                    sillyBox.setErase(false);
                }
                if(type == PKind.valueOf(PKind.Type.ERASER)) {
                    sillyBox.setErase(true);
                    frame.getContentPane().setCursor(createTransparentCursor(5, 5, Color.darkGray));
                }
                sillyBox.setPressure(pressure);
            }

            @Override
            public void penKindEvent(PKindEvent ev) {

            }
        });

        frame.getContentPane().add(sillyBox, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sillyBox.Clear();
            }
        }));

        buttonPanel.add(new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sillyBox.SwitchLayers();
            }
        }));

        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        final JColorChooser tcc = new JColorChooser();
        tcc.setPreviewPanel(new JPanel());

        //Remove the default chooser panels
        AbstractColorChooserPanel panels[] = tcc.getChooserPanels();
        for (int i = 1; i < panels.length; i ++) {
            tcc.removeChooserPanel(panels[i]);
        }


        tcc.getSelectionModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Color newColor = tcc.getColor();
                sillyBox.color = newColor;
            }
        });

        frame.getContentPane().add(tcc, BorderLayout.EAST);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 300);
        frame.setVisible(true);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_Z) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
            Undo();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
