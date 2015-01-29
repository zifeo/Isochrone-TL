package ch.epfl.isochrone.tiledmap;

/**
 * Interface pour les fournisseurs de tuiles.
 */
public interface TileProvider {

    public static final int TILE_SIZE = 256, TILE_BIT_SIZE = 8;

    /**
     * Retrouve la tuile au niveau de zoom et aux coordonnees donnees.
     * 
     * @param   zoom
     *          Le niveau de zoom de la tuile.
     * @param   x
     *          La coordonnee x de la tuile.
     * @param   y
     *          La coordonnee y de la tuile.
     * @return  La tuile demandee.       
     */
    public Tile tileAt(int zoom, int x, int y);

}
