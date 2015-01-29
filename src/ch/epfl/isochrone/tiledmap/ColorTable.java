package ch.epfl.isochrone.tiledmap;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Une table de couleur.
 * Classe immuable. Possede un constructeur.
 */
public final class ColorTable {

    private final int timeFrame;
    private final List<Color> slicesColors;

    /**
     * Constructeur de table de couleurs.
     * 
     * @param   timeFrame
     *          Le temps entre chaque tranche en seconde.
     * @param   slicesColors
     *          Les couleurs associees aux tranches.
     * @throws  IllegalArgumentException
     *          En cas de temps entre les tranches negatif ou nul.
     */
    public ColorTable(int timeFrame, List<Color> slicesColors) {

        if (timeFrame <= 0)
            throw new IllegalArgumentException("Le temps entre les tranches ne doit pas etre negatif ou nul :"+timeFrame);

        this.timeFrame = timeFrame;
        this.slicesColors = Collections.unmodifiableList(slicesColors);
    }

    /**
     * Accesseur en lecture du temps entre chaque tranche.
     * 
     * @return  Le temps entre chaque tranche en seconde.
     */
    public int getTimeFrame() {
        return timeFrame;
    }

    /**
     * Retourne le nombre de tranches dans la table de couleurs.
     * 
     * @return  Le nombre de tranches/couleurs.
     */
    public int getSliceCount() {
        return slicesColors.size();
    }

    /**
     * Retourne la couleur correspondante a la tranche indiquee.
     * 
     * @param   id
     *          Le numero de la tranche.
     * @return  La couleur associee.
     * @throws  IllegalArgumentException
     *          En cas de tranche non inclue de l'intervalle [0,getSliceCount()-1].
     */
    public Color getSliceColor(int id) {

        if (id < 0 || id >= getSliceCount())
            throw new IndexOutOfBoundsException("La tranche doit etre comprise dans [0,"+(getSliceCount()-1)+"] : "+id);

        return slicesColors.get(id); // color est immuable
    }

    /**
     * Batisseur de table de couleurs.
     */
    public static class Builder {

        private final int sliceDuration;
        private final List<Color> slicesColors;

        /**
         * Constructeur du batisseur de table de couleurs.
         * 
         * @param   sliceDuration
         *          Le temps entre chaque tranche en seconde.
         * @throws  IllegalArgumentException
         *          En cas de temps entre les tranches negatif ou nul.
         */
        public Builder(int sliceDuration) {

            if (sliceDuration <= 0)
                throw new IllegalArgumentException("Le temps entre les tranches ne doit pas etre negatif ou nul :"+sliceDuration);

            this.sliceDuration = sliceDuration;
            this.slicesColors = new ArrayList<>();
        }

        /**
         * Ajoute une couleur a la table de couleur en commencant par le fond.
         * 
         * @param   r
         *          La composante rouge en pourcent.
         * @param   g
         *          La composante verte en pourcent.
         * @param   b
         *          La composante bleue en pourcent.
         * @return  Le batisseur.
         * @throws  IllegalArgumentException
         *          En cas de composant non compris dans l'intervalle [0,1].
         *          
         */
        public Builder addColor(double r, double g, double b) {

            if (r < 0 || r > 1 || g < 0 || g > 1 || b < 0 || b > 1)
                throw new IllegalArgumentException("Les composants doivent etre compris dans [0,1] : "+r+","+g+","+b);

            addColor(new Color((float) r, (float) g, (float) b));
            return this;
        }

        /**
         * Ajoute une couleur a la table de couleur en commencant par le fond.
         * 
         * @return  Le batisseur.         *          
         */
        public Builder addColor(Color color) {
            slicesColors.add(color);
            return this;
        }

        /**
         * Construit la table de couleurs a partir du batisseur.
         * 
         * @return  La table de couleurs.
         */
        public ColorTable build() {
            return new ColorTable(sliceDuration, slicesColors);
        }
    }
}
