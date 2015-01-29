package ch.epfl.isochrone.tiledmap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Un cache de tuiles.
 */
public class TileCache {

    private final int MAX_SIZE;
    private final LinkedHashMap<Long, Tile> cache;

    /**
     * Constructeur public du cache de tuiles.
     * 
     * @param   maxSize
     *          Le nombre maximal de tuiles en cache.
     */
    public TileCache(int maxSize) {
        MAX_SIZE = maxSize;
        cache = new LinkedHashMap<Long, Tile>() {
            private static final long serialVersionUID = -5718163927191766183L;

            /**
             * Modifie la condition d'elimination des tuiles depassant la capacite de liste.
             * 
             * @param   e
             *          La tuile a tester.
             */
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long,Tile> e){
                return size() > MAX_SIZE; // limite le nombre de tuiles dans la liste
            }
        };
    }

    /**
     * Retrouve la tuile dans le cache.
     * 
     * @param   zoom
     *          Le niveau de zoom de la tuile.
     * @param   x
     *          La coordonnee X de la tuile.
     * @param   y
     *          La coordonnee Y de la tuile.
     * @throws  IllegalArgumentException
     *          En cas de zoom negatif.          
     * @return  La tuile en cache demandee ou null si elle n'est pas en cache.
     */
    public Tile get(int zoom, int x, int y) {
    
        if (zoom < 0)
            throw new IllegalArgumentException("le zoom ne doit pas etre negatif : "+zoom);
    
        return cache.get(encodeTileCoordinates(zoom, x, y));
    }

    /**
     * Met en cache la tuile specifique au niveau de zoom et aux coordonnees donnees.
     * 
     * @param   tile
     *          La tuile.
     */
    public void put(Tile tile) {
        cache.put(encodeTileCoordinates(tile.zoom(), tile.x(), tile.y()), tile); // coordonnees valides
    }

    /**
     * Encode la tuile au niveau de zoom et coordonnee donnee dans un entier de type Long
     * 
     * @param   zoom
     *          Le niveau de zoom de la tuile.
     * @param   x
     *          La coordonnee X de la tuile.
     * @param   y
     *          La coordonnee Y de la tuile.
     * @throws  IllegalArgumentException
     *          En cas de zoom negatif.     
     * @return  Un identifiant unique pour la tuile.
     */
    protected static long encodeTileCoordinates(int zoom, int x, int y) {
        if (zoom < 0)
            throw new IllegalArgumentException("le zoom ne doit pas etre negatif : "+zoom);
    
        return zoom << 40 | x << 20 | y;
    }
}
