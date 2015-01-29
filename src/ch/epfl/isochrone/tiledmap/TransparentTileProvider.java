package ch.epfl.isochrone.tiledmap;

/**
 * Un filtre de fournisseur de tuile redefinissant la transparence de chaque pixel.
 */
public final class TransparentTileProvider extends FilteringTileProvider {

    private final int opacity;

    /**
     * Constructeur de filtre de transparence.
     * 
     * @param   originalProvider
     *          Le fournisseur de tuile original.
     * @param   opacity
     *          La nouvelle opacite.
     * @throws  IllegalArgumentException
     *          En cas d'opacite non compris dans l'intervalle [0,1].
     */
    public TransparentTileProvider(TileProvider originalProvider, double opacity) {
        super(originalProvider);

        if (opacity < 0 || opacity > 1)
            throw new IllegalArgumentException("l'opacite doit etre comprise entre [0,1]");

        this.opacity = (int) Math.round(255*opacity);
    }

    /**
     * Applique une transformation de transparence.
     * 
     * @param   argb
     *          La couleur d'origine.
     * @return  La couleur avec la transparence redefinie.
     */
    @Override
    public int transformARGB(int argb) {
        return argb & 0x00FFFFFF | opacity << 24;
    }

}
