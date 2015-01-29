package ch.epfl.isochrone.tiledmap;

/**
 * Un fournisseur de tuiles avec cache.
 */
public class CachedTileProvider implements TileProvider {

    private final TileProvider originalProvider;
    private final TileCache cache; 

    /**
     * Constructeur public du fournisseur qui s'occupe de mettre en cache les tuiles du fournisseur original.
     * 
     * @param   originalProvider
     *          Le fournisseur de tuiles original
     */
    public CachedTileProvider(TileProvider originalProvider) {
        this.originalProvider = originalProvider;
        this.cache = new TileCache(100); // 100 images en cache
    }

    /**
     * Retrouve la tuile aux coordonnees donnees dans le cache ou appelle le fournisseur d'origine.
     * 
     * @param   zoom
     *          Le niveau de zoom de la tuile.
     * @param   x
     *          La coordonnee x de la tuile.
     * @param   y
     *          La coordonnee y de la tuile.
     * @throws  IllegalArgumentException
     *          En cas de zoom negatif.
     * @return  La tuile demandee.       
     */
    @Override
    public Tile tileAt(int zoom, int x, int y) {

        if (zoom < 0)
            throw new IllegalArgumentException("le zoom ne doit pas etre negatif : "+zoom);

        Tile tile = cache.get(zoom, x, y);

        if (tile == null) {        
            tile = originalProvider.tileAt(zoom, x, y);
            cache.put(tile);
        }
        return tile;
    }
}
