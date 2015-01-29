package ch.epfl.isochrone.tiledmap;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * Un fournisseur de tuiles OSM (OpenStreetMap). Le zoom maximal est 19.
 */
public class OSMTileProvider implements TileProvider {

    private final static String EXTENSION = "png",
            ERROR_TILE_PATH = "/images/error-tile.png";
    private final URL serverPath;

    /**
     * Constructeur public du fournisseur de tuiles OSM en fonction du serveur.
     * 
     * @param   serverPath
     *          Le serveur OSM (http://) sans "/" a la fin.
     */
    public OSMTileProvider(URL serverPath) {
        this.serverPath = serverPath;
    }

    /**
     * Charge la tuile au niveau de zoom et aux coordonnees donnees depuis le serveur specifie.
     * 
     * @param   zoom
     *          Le niveau de zoom de la tuile.
     * @param   x
     *          La coordonnee x de la tuile.
     * @param   y
     *          La coordonnee y de la tuile.
     * @throws  IllegalArgumentException
     *          En cas de zoom non compris dans l'intervalle [0, 19].
     * @throws  IllegalArgumentException
     *          En cas d'erreur dans le chargement de l'image d'erreur.
     * @return  La tuile demandee.  
     */
    @Override
    public Tile tileAt(int zoom, int x, int y) {

        if (zoom < 0  || zoom > 19)
            throw new IllegalArgumentException("le zoom doit etre dans [0, 19] : "+zoom);

        BufferedImage image = null;        
        try {
            image = getOSMImage(zoom, x, y);
        } catch (IOException e) {
            throw new IllegalStateException("erreur d'acces a l'image d'erreur ("+e.getMessage()+") : "+ERROR_TILE_PATH+"\n");
        }
        assert image != null;

        return new Tile(zoom, x, y, image);
    }

    // charge l'image depuis le serveur specifie ou charge l'image d'erreur en cas d'erreur
    private BufferedImage getOSMImage(int zoom, int x, int y) throws IOException {
        try {
            URL imageURL = new URL(serverPath, zoom+"/"+x+"/"+y+"."+EXTENSION);
            return ImageIO.read(imageURL);
        } catch (IOException e) {
            return ImageIO.read(getClass().getResource(ERROR_TILE_PATH));
        }
    }
}
