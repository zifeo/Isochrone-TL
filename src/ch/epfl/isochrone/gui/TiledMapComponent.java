package ch.epfl.isochrone.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.Timer;

import ch.epfl.isochrone.geo.PointOSM;
import ch.epfl.isochrone.tiledmap.Tile;
import ch.epfl.isochrone.tiledmap.TileProvider;

/**
 * Un panneau de tuile.
 */
public final class TiledMapComponent extends JComponent {

    private static final long serialVersionUID = 1313122480076801113L;
    private int zoom;
    private List<TileProvider> providers;
    private Timer repainting;

    /**
     * Constructeur du panneau de tuile.
     * 
     * @param   zoom
     *          Le zoom intial.
     * @throws  IllegalArgumentException
     *          En cas de zoom non compris dans l'intervalle [10,19].
     */
    public TiledMapComponent(int zoom) {

        if (zoom < 10 || zoom > 19)
            throw new IllegalArgumentException("le zoom doit etre compris entre [10,19] : "+zoom);

        this.zoom = zoom;
        this.providers = new ArrayList<>();
        this.repainting = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                repaint();
            }
        });
        this.repainting.setRepeats(false);
    }

    /**
     * Retourne la taille totale de toutes les tuiles en fonction du zoom.
     * 
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        int edge = PointOSM.maxXY(zoom);
        return new Dimension(edge, edge);
    }

    /**
     * Dessine la partie visible des tuiles selon le zoom et l'ordre des couches.
     * Et la repaint tant que toutes les tuiles ne sont pas chargees.
     * 
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g0) {

        Graphics2D context = (Graphics2D) g0;
        Rectangle r = getVisibleRect();
        boolean shouldRepaint = false;
        
        int minX = (int) r.getMinX() >> TileProvider.TILE_BIT_SIZE;
        int maxX = (int) r.getMaxX() >> TileProvider.TILE_BIT_SIZE;
        int minY = (int) r.getMinY() >> TileProvider.TILE_BIT_SIZE;
        int maxY = (int) r.getMaxY() >> TileProvider.TILE_BIT_SIZE;
        
        for (int x = minX;  x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (TileProvider p : providers) {
                    Tile t = p.tileAt(zoom, x, y);
                    shouldRepaint |= t.equals(Tile.LOADING);
                    context.drawImage(t.image(), null, x << TileProvider.TILE_BIT_SIZE, y << TileProvider.TILE_BIT_SIZE);
                }
            }
        }

        if (shouldRepaint && !repainting.isRunning())
            repainting.start();
    }

    /**
     * Accesseur en lecture du zoom.
     * 
     * @return  Le zoom.
     */
    public int zoom() {
        return zoom;
    }

    /**
     * Accesseur en ecriture du zoom.
     * 
     * @param   newZoom
     *          Le nouveau zoom.
     * @throws  IllegalArgumentException
     *          En cas de zoom non compris dans l'intervalle [0,19].
     */
    public void setZoom(int newZoom) {

        if (zoom < 0 || zoom > 19)
            throw new IllegalArgumentException("le zoom doit etre compris entre [0,19] : "+zoom);

        this.zoom = newZoom;
        repaint();
    }

    /**
     * Ajoute les fournisseurs de tuile.
     * 
     * @param   p
     *          Une liste de fournisseurs de tuile.
     */
    public void setTileProviders(List<TileProvider> p) {
        providers = new ArrayList<>(p);
        repaint();
    }
}
