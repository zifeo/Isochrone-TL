package ch.epfl.isochrone.tiledmap;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Set;

import ch.epfl.isochrone.geo.PointOSM;
import ch.epfl.isochrone.timetable.FastestPathTree;
import ch.epfl.isochrone.timetable.Stop;

/**
 * Un fournisseur de tuile isochrone.
 */
public final class IsochroneTileProvider implements TileProvider {

    private final FastestPathTree pathTree;
    private final ColorTable colors;
    private final double walkingSpeed;
    private final Set<Stop> stops;

    /**
     * Constructueur du fournisseur de tuile isochrone.
     * 
     * @param   pathTree
     *          L'arbre du chemin le plus rapide.
     * @param   colors
     *          La table de couleurs.
     * @param   walkingSpeed
     *          La vitesse de marche.
     * @throws  IllegalArgumentException
     *          En cas de vitesse de marche negative.
     */
    public IsochroneTileProvider(FastestPathTree pathTree, ColorTable colors, double walkingSpeed) {

        if (walkingSpeed < 0)
            throw new IllegalArgumentException("La vitesse de marche doit etre positive : "+walkingSpeed);

        this.pathTree = pathTree;
        this.colors = colors;
        this.walkingSpeed = walkingSpeed;
        this.stops = pathTree.stops();

    }

    /**
     * Retourne la tuile isochrone au niveau de zoom et aux coordonnees donnees.
     * 
     * @param   zoom
     *          Le niveau de zoom de la tuile.
     * @param   x
     *          La coordonnee x de la tuile.
     * @param   y
     *          La coordonnee y de la tuile.
     * @return  La tuile isochrone demandee.
     * @throws  IllegalArgumentException
     *          En cas de zoom negatif.  
     */
    @Override
    public Tile tileAt(int zoom, int x, int y) {

        if (zoom < 0)
            throw new IllegalArgumentException("le zoom doit etre positif : "+zoom);

        BufferedImage tile = new BufferedImage(TileProvider.TILE_SIZE, TileProvider.TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D context = tile.createGraphics();
        
        context.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        context.setColor(colors.getSliceColor(0));
        context.fillRect(0, 0, tile.getWidth(), tile.getHeight());

        int x256 = x << TileProvider.TILE_BIT_SIZE;
        int y256 = y << TileProvider.TILE_BIT_SIZE;

        // calcul de l'echelle
        PointOSM p1 = new PointOSM(zoom, x256, y256);
        PointOSM p2 = new PointOSM(zoom, x256, y256+1);
        double rapport = 1/p1.toWGS84().distanceTo(p2.toWGS84());
        int layers = colors.getSliceCount();

        for (int i = 1; i < layers; ++i) {
            
            int layerTime = (layers-i)*colors.getTimeFrame();
            for (Stop s : stops) {

                int timeLeft = layerTime - (pathTree.arrivalTime(s) - pathTree.startingTime());
                if (timeLeft > 0) { // s'il reste du temps de marche apres l'arrivee a un arret

                    PointOSM position = s.position().toOSM(zoom);

                    double px = position.x() - x256;
                    double py = position.y() - y256;
                    double radius = timeLeft*walkingSpeed*rapport;

                    // si l'arret est sur la tuile
                    if (px + radius > 0 && px - radius < TileProvider.TILE_SIZE &&
                            py + radius > 0 && py - radius < TileProvider.TILE_SIZE) {  

                        context.setColor(colors.getSliceColor(i));
                        context.fill(new Ellipse2D.Double(px - radius, py - radius, 2*radius, 2*radius));
                    }
                }  
            }
        }      
        return new Tile(zoom, x, y, tile);
    }
}
