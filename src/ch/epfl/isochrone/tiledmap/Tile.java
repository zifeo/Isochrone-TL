package ch.epfl.isochrone.tiledmap;

import java.awt.image.BufferedImage;

/**
 * Une tuile.
 */
public final class Tile {

    public static final Tile LOADING = new Tile(0, 0, 0, null);
    private final int zoom, x, y;
    private final BufferedImage image;

    /**
     * Constructeur public de tuile.
     * 
     * @param   x
     *          La coodonnee X de la tuile.
     * @param   y
     *          La coordonnee Y de la tuile.
     * @param   image
     *          L'image representant la tuile.
     * @throws  IllegalArgumentException
     *          En cas de zoom negatif.
     */
    public Tile(int zoom, int x, int y, BufferedImage image) {

        if (zoom < 0)
            throw new IllegalArgumentException("le zoom ne doit pas etre negatif : "+zoom);

        this.zoom = zoom;
        this.x = x;
        this.y = y;
        this.image = image;
    }

    /**
     * Accesseur en lecture de l'image de la tuile.
     * 
     * @return  l'image
     */
    public BufferedImage image() {
        return image;
    }

    /**
     * Accesseur en lecture de la coordonnee X de la tuile.
     * 
     * @return  La coordonnee X de la tuile.
     */
    public int x() {
        return x;
    }

    /**
     * Accesseur en lecture de la coordonnee Y de la tuile.
     * 
     * @return  La coordonnee Y de la tuile.
     */
    public int y() {
        return y;
    }

    /**
     * Accesseur en lecture du zoom de la tuile.
     * 
     * @return Le zoom de la tuile.
     */
    public int zoom() {
        return zoom;
    }
}
