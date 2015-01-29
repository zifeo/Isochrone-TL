package ch.epfl.isochrone.geo;

/**
 * Un point dans le systeme de coordonnees OSM (OpenStreetMap).
 * Classe immuable.
 */
public final class PointOSM {

    private final int zoom;
    private final double x, y;

    /**
     * Retourne le maximum des coordonnees X et Y, c'est-a-dire la taille de l'image de la carte (px) au niveau du zoom donne.
     *
     * @param   zoom
     *          Le niveau de zoom du point. Il ne doit pas etre negatif.
     * @return  La taille de l'image de la carte (px).
     * @throws  IllegalArgumentException
     *          En cas de zoom negatif.
     */
    public static int maxXY(int zoom) {

        if (zoom < 0)
            throw new IllegalArgumentException("le zoom ne doit pas etre negatif : "+zoom);

        return 256 << zoom;
    }

    /**
     * Constructeur public du point OSM.
     *
     * @param   zoom
     *          Le niveau de zoom du point. Il ne doit pas etre negatif.
     * @param   x
     *          La coordonnee X horizontale OSM (px).
     * @param   y
     *          La coordonnee Y verticale OSM (px).
     * @throws  IllegalArgumentException
     *          En cas de zoom negatif.
     * @throws  IllegalArgumentException
     *          En cas de coordonnees X ou Y en dehors de l'intervalle [0, maxXY].
     */
    public PointOSM(int zoom, double x, double y) {

        if (zoom < 0)
            throw new IllegalArgumentException("le zoom ne doit pas etre negatif : "+zoom);
        int max = maxXY(zoom);

        if (x < 0 || x > max)
            throw new IllegalArgumentException("la coordonnee X doit etre comprise dans l'intervalle [0, "+max+"] : "+x);
        if (y < 0 || y > max)
            throw new IllegalArgumentException("la coordonnee Y doit etre comprise dans l'intervalle [0, "+max+"] : "+y);

        this.x = x;
        this.y = y;
        this.zoom = zoom;
    }

    /**
     * Accesseur en lecture de la cordonnee X.
     *
     * @return  La cordonnee X du point OSM (px).
     */
    public double x() {
        return x;
    }

    /**
     * Accesseur en lecture de la cordonnee Y.
     *
     * @return  La cordonnee Y du point OSM (px).
     */
    public double y() {
        return y;
    }

    /**
     * Accesseur en lecture de la cordonnee X arrondie.
     *
     * @return  La cordonnee arrondie X du point OSM (px).
     */
    public int roundedX() {
        return (int) Math.round(x);
    }

    /**
     * Accesseur en lecture de la cordonnee Y arrondie.
     *
     * @return  La cordonnee arrondie Y du point OSM (px).
     */
    public int roundedY() {
        return (int) Math.round(y);
    }

    /**
     * Accesseur en lecture du niveau de zoom.
     *
     * @return  Le niveau de zoom du point OSM.
     */
    public int zoom() {
        return zoom;
    }

    /**
     * Retourne le meme point OSM mais au niveau de zoom donne.
     *
     * @param   newZoom
     *          Le nouveau niveau de zoom du point.
     * @return  Le meme point OSM en fonction du nouveau zoom.
     * @throws  IllegalArgumentException
     *          En cas de zoom negatif.
     */
    public PointOSM atZoom(int newZoom) {

        if (newZoom < 0)
            throw new IllegalArgumentException("le zoom ne doit pas etre negatif : "+newZoom);

        double differenceZoom = Math.pow(2, newZoom - zoom);
        return new PointOSM(newZoom, x*differenceZoom, y*differenceZoom);
    }

    /**
     * Retourne le meme point dans le systeme de coordonnees WGS84.
     *
     * @return  Le meme point en coordonnes WGS84.
     */
    public PointWGS84 toWGS84() {

        double s = maxXY(zoom);
        double lon = 2*Math.PI*x/s - Math.PI;
        double lat = Math.atan(Math.sinh(Math.PI - 2*Math.PI*y/s));

        return new PointWGS84(lon, lat);
    }

    /**
     * Retourne une representation textuelle du point.
     *
     * @return  (zoom,x,y)
     */
    @Override
    public String toString() {
        return "("+zoom+","+x+","+y+")";
    }
}