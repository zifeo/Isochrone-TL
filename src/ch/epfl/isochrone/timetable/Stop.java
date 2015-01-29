package ch.epfl.isochrone.timetable;

import ch.epfl.isochrone.geo.PointWGS84;

/**
 * Un arret de transports en commun.
 * Classe immuable.
 */
public final class Stop {

    private final String name;
    private final PointWGS84 position;

    /**
     * Constructeur public d'un arret avec le nom et la position donnee.
     * 
     * @param   name
     *          Le nom de l'arret.
     * @param   position
     *          La position de l'arret en coordonnees WGS 84.
     */
    public Stop(String name, PointWGS84 position) {
        this.name = name;
        this.position = position;
    }

    /**
     * Accesseur en lecture du nom de l'arret.
     * 
     * @return  Le nom de l'arret
     */
    public String name() {
        return name;
    }

    /**
     * Accesseur en lecture de la position de l'arret.
     * 
     * @return  La position de l'arret en coordonnees WGS 84.
     */
    public PointWGS84 position() {
        return position;
    }

    /**
     * Retourne une representation textuelle de l'arret.
     * 
     * @return  Le nom de l'arret
     */
    @Override
    public String toString() {
        return name;
    }
}
