package ch.epfl.isochrone.tiledmap;

import java.awt.image.BufferedImage;

/**
 * Un filtre de fournisseur de tuile abstrait. Applique sur chaque pixel la fonction transformARGB.
 */
public abstract class FilteringTileProvider implements TileProvider {

    protected final TileProvider originalProvider;

    /**
     * Constructeur du filture de fournisseur de tuile.
     * 
     * @param   originalProvider
     *          Le fournisseur de tuile original.
     */
    public FilteringTileProvider(TileProvider originalProvider) {
        this.originalProvider = originalProvider;
    }

    /**
     * Applique une transformation au niveau de la couleur.
     * 
     * @param   argb
     *          La couleur d'origine.
     * @return  La couleur transformee.
     */
    abstract public int transformARGB(int argb);

    /**
     * Retourne la tuile transformee au niveau de zoom et aux coordonnees donnees.
     * 
     * @param   zoom
     *          Le niveau de zoom de la tuile.
     * @param   x
     *          La coordonnee x de la tuile.
     * @param   y
     *          La coordonnee y de la tuile.
     * @return  La tuile transformee demandee.
     * @throws  IllegalArgumentException
     *          En cas de zoom negatif.  
     */
    @Override
    public Tile tileAt(int zoom, int x, int y) {

        if (zoom < 0)
            throw new IllegalArgumentException("le zoom doit etre positif : "+zoom);

        BufferedImage currentImage = originalProvider.tileAt(zoom, x, y).image();
        int width = currentImage.getWidth();
        int height = currentImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);        

        for (int dx = 0; dx < width; ++dx) {  
            for (int dy = 0; dy < height; ++dy) {
                newImage.setRGB(dx, dy, transformARGB(currentImage.getRGB(dx, dy)));            
            }
        }
        return new Tile(zoom, x, y, newImage);
    }
}
