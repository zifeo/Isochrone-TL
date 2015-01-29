package ch.epfl.isochrone.geo;

import static ch.epfl.isochrone.math.Math.asinh;
import static ch.epfl.isochrone.math.Math.haversin;

/**
 * Un point dans le systeme de coordonnees WGS 84.
 * Classe immuable.
 */
public final class PointWGS84 {

    private static final int RAYON_TERRE = 6378137;
    private final double longitude, latitude; // en radians
    /**
     * Constructeur public du point WGS 84.
     * 
     * @param   longitude 
     *          La longitude (en radians) comprise dans l'intervalle [-pi, +pi].
     * @param   latitude
     *          La latitude (en radians) comprise dans l'intervalle [-pi/2, +pi/2].
     * @throws  IllegalArgumentException
     *          En cas de longitude non comprise dans [-pi, +pi] ou latitude non comprise dans [-pi/2, +pi/2].
     */
    public PointWGS84 (double longitude, double latitude) {

        if (Math.abs(longitude) > Math.PI)
            throw new IllegalArgumentException("la longitude doit etre comprise dans [-pi, +pi] : "+longitude);

        if (Math.abs(latitude) > Math.PI/2)
            throw new IllegalArgumentException("la latitude doit etre comprise entre [-pi/2, +pi/2] : "+latitude);

        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * Accesseur en lecture de la longitude.
     *
     * @return  La longitude du point (en radians).
     */
    public double longitude() {
        return longitude;
    }

    /**
     * Accesseur en lecture de la latitude.
     *
     * @return  La latitude du point (en radians).
     */
    public double latitude() {
        return latitude;
    }

    /**
     * Calcule la distance entre ce point et un autre point WGS84 donne.
     *
     * @param   that
     *          Le point WGS84 de comparaison.
     * @return  La distance entre les deux points (m).
     */
    public double distanceTo(PointWGS84 that) {
        return 2*RAYON_TERRE*
                Math.asin(
                        Math.sqrt(
                                haversin(this.latitude-that.latitude) + Math.cos(this.latitude)*
                                Math.cos(that.latitude)*
                                haversin(this.longitude-that.longitude)
                                )
                        );
    }

    /**
     * Retourne le meme point dans le systeme de coordonnees OSM au niveau de zoom donne.
     *
     * @param   zoom
     *          Le niveau de zoom du futur point OSM.
     * @return  Le point en coordonnees OSM.
     * @throws  IllegalArgumentException
     *          En cas de zoom negatif.
     */
    public PointOSM toOSM(int zoom) {

        if (zoom < 0)
            throw new IllegalArgumentException("le zoom ne doit pas etre negatif : "+zoom);

        double s = PointOSM.maxXY(zoom);
        double x = s*(longitude+Math.PI)/(2*Math.PI);
        double y = s*(Math.PI-asinh(Math.tan(latitude)))/(2*Math.PI);

        return new PointOSM(zoom, x, y);
    }

    /**
     * Retourne une representation textuelle du point en degrés.
     *
     * @return  (longitude,latitude)
     */
    @Override
    public String toString() {
        return "("+Math.toDegrees(longitude)+","+Math.toDegrees(latitude)+")";
    }
}