package ch.epfl.isochrone.math;

import static java.lang.Math.log;
import static java.lang.Math.sqrt;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Integer.signum;

/**
 * Classe utilitaire mathematique non-instanciable.
 */
public final class Math {

    private Math() {} // empeche l'instanciation

    /**
     * Calcule l'inverse du sinus hyperbolique.
     * 
     * @param   x
     * @return  asinh(x)
     */
    public static double asinh(double x) {
        return  log(x+sqrt(1 + x*x));
    }

    /**
     * Calcule la fonction haversin.
     * 
     * @param   x
     * @return  haversin(x)
     */
    public static double haversin(double x) {
        return  pow(sin(x/2), 2);
    }

    /**
     * Calcule la division entiere non-tronquee (par defaut).
     * 
     * @param   n
     *          Le numerateur de la division.
     * @param   d
     *          Le denominateur de la division.
     * @return  Le quotient de n/d.
     */
    public static int divF(int n, int d) {
        return n/d-i(n, d);
    }

    /**
     * Calcule le reste de la division entiere non-tronquee (par defaut).
     * 
     * @param   n
     *          Le numerateur de la division.
     * @param   d
     *          Le denominateur de la division.
     * @return  Le reste de la division n/d.
     */
    public static int modF(int n, int d) {
        return n%d + i(n, d)*d;
    }

    // correcteur de division et de reste negatif
    private static int i(int n, int d) {
        return (signum(n%d) == -signum(d)) ? 1: 0;
    }
}