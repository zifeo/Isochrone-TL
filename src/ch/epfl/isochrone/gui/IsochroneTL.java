package ch.epfl.isochrone.gui;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JViewport;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.epfl.isochrone.geo.PointOSM;
import ch.epfl.isochrone.geo.PointWGS84;
import ch.epfl.isochrone.tiledmap.AsynchroneCachedTileProvider;
import ch.epfl.isochrone.tiledmap.CachedTileProvider;
import ch.epfl.isochrone.tiledmap.ColorTable;
import ch.epfl.isochrone.tiledmap.IsochroneTileProvider;
import ch.epfl.isochrone.tiledmap.OSMTileProvider;
import ch.epfl.isochrone.tiledmap.TileProvider;
import ch.epfl.isochrone.tiledmap.TransparentTileProvider;
import ch.epfl.isochrone.timetable.Date;
import ch.epfl.isochrone.timetable.FastestPathTree;
import ch.epfl.isochrone.timetable.Graph;
import ch.epfl.isochrone.timetable.Service;
import ch.epfl.isochrone.timetable.Stop;
import ch.epfl.isochrone.timetable.TimeTable;
import ch.epfl.isochrone.timetable.TimeTableReader;
import ch.epfl.isochrone.timetable.Date.Month;
import ch.epfl.isochrone.timetable.SecondsPastMidnight;

/**
 * Classe principale du programme Isochrone TL.
 */
public final class IsochroneTL {
    private static final String OSM_TILE_URL = "http://b.tile.openstreetmap.org/";
    private static final int INITIAL_ZOOM = 11;
    private static final PointWGS84 INITIAL_POSITION = new PointWGS84(Math.toRadians(6.38), Math.toRadians(46.64));
    private static final String INITIAL_STARTING_STOP_NAME = "Lausanne-Flon";
    private static final int INITIAL_DEPARTURE_TIME = SecondsPastMidnight.fromHMS(6, 8, 0);
    private static final Date INITIAL_DATE = new Date(1, Month.OCTOBER, 2013);
    private static final int WALKING_TIME = 5 * 60;
    private static final double WALKING_SPEED = 1.25;
    private static final double ISOCHRONE_OPACITY = 0.5;
    private static final int DEFAULT_ANIMATION_DELAY = 500;
    private static final int SLOWEST_ANIMATION_DELAY = 1000;
    private static final int FASTEST_ANIMATION_DELAY = 100;

    private final TiledMapComponent tiledMapComponent;
    private Date currentDate;
    private Stop currentStop;
    private int currentSpm;
    private Set<Service> currentServices;
    private TimeTableReader reader;
    private FastestPathTree pathTree;
    private Graph graph;
    private TimeTable timetable;
    private ColorTable colorTable;
    private TileProvider mainProvider;
    private Robot robot;
    private JComboBox<Stop> selectStop;

    /**
     * Lance le programme.
     * 
     * @param   args
     *          Les arguments de programme.
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new IsochroneTL().start();
                } catch (IOException | HeadlessException | AWTException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // defini les options du gestionnaire de fenetre
    private void start() {
        JFrame frame = new JFrame("Isochrone Map");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(createCenterPanel(), BorderLayout.CENTER);
        frame.getContentPane().add(createTopPanel(), BorderLayout.PAGE_START);

        frame.pack();
        frame.setVisible(true);
    }

    // construit la barre de recherche
    @SuppressWarnings("deprecation")
    private JComponent createTopPanel() {

        Vector<Stop> allStops = new Vector<>(timetable.stops());
        Collections.sort(allStops, new Comparator<Stop>(){
            @Override
            public int compare(Stop s1, Stop s2) {
                return s1.name().compareTo(s2.name());
            }
        });

        // selection des arrets
        selectStop = new JComboBox<>(allStops);
        selectStop.setSelectedItem(currentStop);
        selectStop.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                setStartingStop((Stop) selectStop.getSelectedItem());
            }
        });

        // selection des dates
        final SpinnerDateModel dateModel = new SpinnerDateModel();
        java.util.Date date = currentDate.toJavaDate();
        date.setSeconds(currentSpm);
        dateModel.setValue(date);
        dateModel.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e) {
                java.util.Date dateChange = dateModel.getDate();               
                if (dateChange.getHours() <= 4) {
                    setDate(new Date(dateChange).relative(-1));
                    setStartingTime(SecondsPastMidnight.fromJavaDate(dateChange) + SecondsPastMidnight.fromHMS(24, 0, 0));                    
                } else {
                    setDate(new Date(dateChange));
                    setStartingTime(SecondsPastMidnight.fromJavaDate(dateChange));
                }
            }
        });
        JSpinner selectDateTime = new JSpinner(dateModel);

        // gestion des animations / selecteur d'iteration
        Icon playIcon = new ImageIcon(getClass().getResource("/images/play.png"));
        Icon plusIcon = new ImageIcon(getClass().getResource("/images/plus.png"));
        Icon minusIcon = new ImageIcon(getClass().getResource("/images/minus.png"));
        Vector<String> timeSpanToIterate = new Vector<>(Arrays.asList("Années", "Mois", "Jours", "Heures", "Minutes", "Seconds"));
        final JComboBox<String> timeToIterate = new JComboBox<>(timeSpanToIterate);
        timeToIterate.setSelectedItem("Minutes");
        final Timer timer = new Timer(DEFAULT_ANIMATION_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateModel.getDate());
                switch ((String) timeToIterate.getSelectedItem()) {
                case "Années" : 
                    cal.add(Calendar.YEAR, 1);
                    break;
                case "Mois":
                    cal.add(Calendar.MONTH, 1);
                    break;
                case "Jours":
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                    break;
                case "Heures":
                    cal.add(Calendar.HOUR_OF_DAY, 1);
                    break;
                case "Minutes":
                    cal.add(Calendar.MINUTE, 1);
                    break;
                case "Secondes":
                    cal.add(Calendar.SECOND, 1);
                    break;
                }
                dateModel.setValue(cal.getTime());
            }
        });
        
        // bouton play/pause
        final JButton startAnimation = new JButton();
        startAnimation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(timer.isRunning())
                    timer.stop();
                else
                    timer.start();
            }
        });
        startAnimation.setIcon(playIcon);
        startAnimation.setPreferredSize(new Dimension(20,20));

        // bouton accelerer
        JButton increaseAnimationSpeed = new JButton();
        increaseAnimationSpeed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.setDelay(Math.max(timer.getDelay() - 100, FASTEST_ANIMATION_DELAY));          
            }
        });
        increaseAnimationSpeed.setIcon(plusIcon);
        increaseAnimationSpeed.setPreferredSize(new Dimension(20,20));

        // bouton ralentir
        JButton decreaseAnimationSpeed = new JButton();
        decreaseAnimationSpeed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.setDelay(Math.min(timer.getDelay() + 100, SLOWEST_ANIMATION_DELAY));          
            }
        });
        decreaseAnimationSpeed.setIcon(minusIcon);
        decreaseAnimationSpeed.setPreferredSize(new Dimension(20,20));   

        // creation du panneau superieur
        JPanel topPanel = new JPanel(new FlowLayout());      
        topPanel.add(new JLabel("Départ"));
        topPanel.add(selectStop);
        topPanel.add(new JSeparator());
        topPanel.add(new JLabel("Date et heure"));
        topPanel.add(selectDateTime);
        topPanel.add(new JSeparator());
        topPanel.add(new JLabel("Animation"));
        topPanel.add(startAnimation);
        topPanel.add(increaseAnimationSpeed);
        topPanel.add(decreaseAnimationSpeed);
        topPanel.add(timeToIterate);
        return topPanel;
    }

    // construit la carte
    private JComponent createCenterPanel() {
        final JViewport viewPort = new JViewport();
        viewPort.setView(tiledMapComponent);
        PointOSM startingPosOSM = INITIAL_POSITION.toOSM(tiledMapComponent.zoom());
        viewPort.setViewPosition(new Point(startingPosOSM.roundedX(), startingPosOSM.roundedY()));

        final JPanel copyrightPanel = createCopyrightPanel();
        final JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(500, 400));
        layeredPane.add(viewPort, new Integer(0));
        layeredPane.add(copyrightPanel, new Integer(1));

        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                final Rectangle newBounds = layeredPane.getBounds();
                viewPort.setBounds(newBounds);
                copyrightPanel.setBounds(newBounds);

                viewPort.revalidate();
                copyrightPanel.revalidate();
            }
        }); 

        // transformation de la vue quand il y a un glissement de souris
        MouseAdapter mouseClickEvent = new MouseAdapter() {
            private Point startLocationOnScreen, startViewPosition; 
            private Stop closestStopFromMouse = currentStop;
            boolean isNotTooFar = true;
            @Override
            public void mousePressed(MouseEvent e) {

                if (!SwingUtilities.isLeftMouseButton(e))
                    return;

                startLocationOnScreen = e.getLocationOnScreen();
                startViewPosition = viewPort.getViewPosition();

            }       
            @Override
            public void mouseDragged(MouseEvent e) {

                if (!SwingUtilities.isLeftMouseButton(e))
                    return;

                Point currentDrag = e.getLocationOnScreen();
                int dx = (int) (startViewPosition.getX() - currentDrag.getX() + startLocationOnScreen.getX());
                int dy = (int) (startViewPosition.getY() - currentDrag.getY() + startLocationOnScreen.getY());

                viewPort.setViewPosition(new Point(dx, dy));
                layeredPane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            }
            @Override
            public void mouseClicked(MouseEvent e) {

                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.getClickCount() >= 2) {
                        robot.mouseWheel(1);
                        e.consume();
                    } else if (isNotTooFar) {
                        selectStop.setSelectedItem(closestStopFromMouse);
                    }
                    return;
                }

                // redessin
                Rectangle bounds = layeredPane.getBounds();
                layeredPane.paintImmediately(0, 0, (int) bounds.getMaxX(), (int) bounds.getMaxY());

                // recherche du chemin d'acces
                int zoom = tiledMapComponent.zoom();
                Point view = SwingUtilities.convertPoint(layeredPane, e.getPoint(), tiledMapComponent);
                Point lastCursorPos = layeredPane.getMousePosition();
                PointOSM lastCursorPosOSM = new PointOSM(zoom, view.getX(), view.getY());
                PointWGS84 lastCursorPosWGS84 = lastCursorPosOSM.toWGS84();
                Stop closestStop = null;
                int minArrivalTime = Integer.MAX_VALUE;
                for(Stop s : pathTree.stops()) {
                    double distance = lastCursorPosWGS84.distanceTo(s.position());
                    int arrivalTime = (int) (pathTree.arrivalTime(s) + distance * WALKING_SPEED);
                    if (arrivalTime < minArrivalTime){
                        minArrivalTime = arrivalTime;
                        closestStop = s;
                    }
                }

                // dessin du chemin
                Graphics2D context = (Graphics2D) layeredPane.getGraphics();
                context.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                context.setColor(Color.RED);
                context.setStroke(new BasicStroke(1));
                
                List<Stop> path = pathTree.pathTo(closestStop);
                Stop previousStop = null;
                for (Stop curStop : path) {
                    if (previousStop != null) {
                        PointOSM curStopOSM = curStop.position().toOSM(zoom);
                        PointOSM previousStopOSM = previousStop.position().toOSM(zoom);

                        Point curStopPoint = new Point(curStopOSM.roundedX(), curStopOSM.roundedY());
                        Point previousStopPoint = new Point(previousStopOSM.roundedX(), previousStopOSM.roundedY());

                        Point curStopConverted = SwingUtilities.convertPoint(tiledMapComponent, curStopPoint, layeredPane);
                        Point previousStopConverted = SwingUtilities.convertPoint(tiledMapComponent, previousStopPoint, layeredPane);

                        context.drawLine((int) previousStopConverted.getX(), (int) previousStopConverted.getY(), (int) curStopConverted.getX(), (int) curStopConverted.getY());
                    }
                    previousStop = curStop;
                }
                PointOSM previousStopOSM = previousStop.position().toOSM(zoom);
                Point previousStopPoint = new Point(previousStopOSM.roundedX(), previousStopOSM.roundedY());
                Point previousStopConverted = SwingUtilities.convertPoint(tiledMapComponent, previousStopPoint, layeredPane);
                context.drawLine(previousStopConverted.x, previousStopConverted.y, lastCursorPos.x, lastCursorPos.y);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                layeredPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                layeredPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                layeredPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
            @Override
            public void mouseMoved(MouseEvent e){                
                
                // calcul du stop le plus proche
                double minDistance = Double.MAX_VALUE;
                int zoom = tiledMapComponent.zoom();
                Point view = SwingUtilities.convertPoint(layeredPane, e.getPoint(), tiledMapComponent);
                PointOSM lastCursorPosOSM = new PointOSM(zoom, view.getX(), view.getY());
                PointWGS84 lastCursorPosWGS84 = lastCursorPosOSM.toWGS84();
                for (Stop s : pathTree.stops()) {
                    double distance = s.position().distanceTo(lastCursorPosWGS84);
                    if (distance < minDistance){
                        minDistance = distance;
                        closestStopFromMouse = s;
                    }
                }
                
                PointOSM stopPosOSM = closestStopFromMouse.position().toOSM(zoom);
                Point stopPos = new Point(stopPosOSM.roundedX(), stopPosOSM.roundedY());

                // si un stop est disponible
                double distanceToMouse = view.distance(stopPos);
                if (isNotTooFar = distanceToMouse < 20) {
                    // redessin
                    Rectangle bounds = layeredPane.getBounds();
                    layeredPane.paintImmediately(0, 0, (int) bounds.getMaxX(), (int) bounds.getMaxY());
                    
                    Point stopPosConverted = SwingUtilities.convertPoint(tiledMapComponent, stopPos, layeredPane);
                    int px = (int) stopPosConverted.getX() - 4;
                    int py = (int) stopPosConverted.getY() - 4;
                    Graphics2D context = (Graphics2D) layeredPane.getGraphics();
                    context.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    context.setColor(Color.DARK_GRAY);
                    context.fill(new Rectangle2D.Double(px, py, 8, 8));
                }
            }
        };
        layeredPane.addMouseListener(mouseClickEvent);
        layeredPane.addMouseMotionListener(mouseClickEvent);

        // transformation de la vue quand il y a un zoom
        layeredPane.addMouseWheelListener(new MouseWheelListener(){
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {

                int currentZoom = tiledMapComponent.zoom();
                int newZoom = currentZoom - e.getWheelRotation();
                newZoom = (newZoom < 10) ? 10: Math.min(newZoom, 19);

                if (currentZoom == newZoom)
                    return;

                Point view = SwingUtilities.convertPoint(layeredPane, e.getPoint(), tiledMapComponent);
                PointOSM current = new PointOSM(currentZoom, view.getX(), view.getY());

                PointOSM converted = current.atZoom(newZoom);
                viewPort.setViewPosition(new Point(converted.roundedX() - e.getX(), converted.roundedY() - e.getY()));
                tiledMapComponent.setZoom(newZoom);
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(layeredPane, BorderLayout.CENTER);
        return centerPanel;
    }

    // construit le panneau de copyright
    private JPanel createCopyrightPanel() {
        Icon tlIcon = new ImageIcon(getClass().getResource("/images/tl-logo.png"));
        String copyrightText = "Licence GNU / Carte : © contributeurs d'OpenStreetMap / Icônes (CC) Andre Allen et Björn Andersson @ thenounproject.com";
        JLabel copyrightLabel = new JLabel(copyrightText, tlIcon, SwingConstants.CENTER);
        copyrightLabel.setOpaque(true);
        copyrightLabel.setForeground(new Color(1f, 1f, 1f, 0.6f));
        copyrightLabel.setBackground(new Color(0f, 0f, 0f, 0.4f));
        copyrightLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 5, 0));

        JPanel copyrightPanel = new JPanel(new BorderLayout());
        copyrightPanel.add(copyrightLabel, BorderLayout.PAGE_END);
        copyrightPanel.setOpaque(false);
        return copyrightPanel;
    }

    /**
     * Constructeur du programme. Effectue la recherche de trajet initial, defini les options et affiche le tout.
     * 
     * @throws  IOException
     *          En cas d'erreurs de lecture dans un fichier.
     * @throws AWTException 
     * @throws HeadlessException 
     */
    public IsochroneTL() throws IOException, HeadlessException, AWTException {

        reader = new TimeTableReader("/time-table-test/");
        timetable = reader.readTimeTable();
        mainProvider = new AsynchroneCachedTileProvider(new OSMTileProvider(new URL(OSM_TILE_URL)));
        tiledMapComponent = new TiledMapComponent(INITIAL_ZOOM);

        currentSpm = INITIAL_DEPARTURE_TIME;
        currentDate = INITIAL_DATE;
        currentStop = getStopFromString(INITIAL_STARTING_STOP_NAME);
        assert currentStop != null;

        colorTable = new ColorTable.Builder(WALKING_TIME)
        .addColor(0, 0, 0)
        .addColor(0, 0, 0.5)
        .addColor(0, 0, 1)
        .addColor(0, 0.5, 0.5)
        .addColor(0, 1, 0)
        .addColor(0.5, 1, 0)
        .addColor(1, 1, 0)
        .addColor(1, 0.5, 0)
        .addColor(1, 0, 0)
        .build();

        updateServices();
        robot = new Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
    }

    /**
     * Accesseur en ecriture de la date.
     * 
     * @param   d
     *          La nouvelle date.
     */
    public void setDate(Date d) {
        if (!currentDate.equals(d)) {
            currentDate = d;
            updateServices();
        }
    }

    // met a jour la table des horaires et le graphe
    private void updateServices() {
        Set<Service> temp = timetable.servicesForDate(currentDate);
        if (!temp.equals(currentServices)) {
            currentServices = temp;
            try {
                graph = reader.readGraphForServices(timetable.stops(), currentServices, WALKING_TIME, WALKING_SPEED);    
            } catch (IOException e) {
                e.printStackTrace();
            }
            updateFastestPathTree();
        }
    }

    /**
     * Accesseur en ecriture de l'arret
     * 
     * @param   s
     *          Le nouvelle arret
     */
    public void setStartingStop(Stop s) {
        if (!currentStop.equals(s)) {
            currentStop = s;
            updateFastestPathTree();
        }
    }

    /**
     * Accesseur en ecriture du temps de depart.
     * 
     * @param   spm
     *          Le nouveau temps de depart en seconde.
     */
    public void setStartingTime(int spm) {
        if (currentSpm != spm) {
            currentSpm = spm;
            updateFastestPathTree();
        }
    }

    // met a jour l'arbre du chemin le plus rapide
    private void updateFastestPathTree() {
        pathTree = graph.fastestPaths(currentStop, currentSpm);
        TileProvider isochroneProvider = new CachedTileProvider(new TransparentTileProvider(new IsochroneTileProvider(pathTree, colorTable, WALKING_SPEED), ISOCHRONE_OPACITY));
        List<TileProvider> providers = Arrays.asList(mainProvider, isochroneProvider);
        tiledMapComponent.setTileProviders(providers);
        tiledMapComponent.repaint();
    }

    // retourne le stop correspondant au nom
    private Stop getStopFromString(String stopName) {
        Stop temp = null;
        for (Stop s : timetable.stops()) {
            if (s.name().equals(stopName)) {
                temp = s;
                break;
            }
        }
        return temp;
    }
}
