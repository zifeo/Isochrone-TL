package ch.epfl.isochrone.timetable;

/**
 * Classe utilitaire temporelle non-instanciable.
 */
public final class SecondsPastMidnight {

    public static final int INFINITE = 200000;

    private SecondsPastMidnight() {} // empeche l'instanciation

    /**
     * Convertit un triplet de temps en spm (secondes apres minuit).
     * 
     * @param   hours
     *          L'heure a convertir.
     * @param   minutes
     *          Les minutes a convertir.
     * @param   seconds
     *          Les secondes a convertir.
     * @throws  IllegalArgumentException
     *          En cas d'heures non comprises dans l'intervalle [0, 29].
     * @throws  IllegalArgumentException
     *          En cas de minutes non comprises dans l'intervalle [0, 59].
     * @throws  IllegalArgumentException
     *          En cas de secondes non comprises dans l'intervalle [0, 59].
     * @return  Les secondes apres minuit.
     */
    public static int fromHMS(int hours, int minutes, int seconds) {

        if (hours < 0 || hours > 29)
            throw new IllegalArgumentException("les heures doivent etre comprises dans [0, 29] : "+hours);
        if (minutes < 0 || minutes > 59)
            throw new IllegalArgumentException("les minutes doivent etre comprises dans [0, 59] : "+minutes);
        if (seconds < 0 || seconds > 59)
            throw new IllegalArgumentException("les secondes doivent etre comprises dans [0, 59] : "+seconds);

        return 3600*hours + 60*minutes + seconds;
    }

    /**
     * Convertit une date Java en spm (secondes apres minuit).
     * 
     * @param   date
     *          La date Java.
     * @return  Le nombre de secondes apres minuit.
     */
    @SuppressWarnings("deprecation")
    public static int fromJavaDate(java.util.Date date) {
        return date.getHours()*3600 + date.getMinutes()*60 + date.getSeconds();
    }

    /**
     * Convertit des spm en heures.
     * 
     * @param   spm
     *          Le nombre de secondes apres minuit.
     * @throws  IllegalArgumentException
     *          En cas de secondes non comprises dans l'intervalle [0, 107999].
     * @return  Le nombre d'heures.
     */
    public static int hours(int spm) {

        if (spm < 0 || spm > 107999)
            throw new IllegalArgumentException("les secondes doivent etre comprises dans [0, 107999] : "+spm);

        return spm/3600;
    }

    /**
     * Convertit des spm en minutes.
     * 
     * @param   spm
     *          Le nombre de secondes apres minuit.
     * @throws  IllegalArgumentException
     *          En cas de secondes non comprises dans l'intervalle [0, 107999].
     * @return  Le nombre de minutes.
     */
    public static int minutes(int spm) {

        if (spm < 0 || spm > 107999)
            throw new IllegalArgumentException("les secondes doivent etre comprises dans [0, 107999] : "+spm);

        return spm%3600/60;
    }

    /**
     * Convertit des spm en secondes.
     * 
     * @param   spm
     *          Le nombre de secondes apres minuit.
     * @throws  IllegalArgumentException
     *          En cas de secondes non comprises dans l'intervalle [0, 107999].
     * @return  Le nombre de secondes
     */
    public static int seconds(int spm) {

        if (spm < 0 || spm > 107999)
            throw new IllegalArgumentException("les secondes doivent etre comprises dans [0, 107999] : "+spm);

        return spm%60;
    }

    /**
     * Retourne une representation textuelle de l'heure (entiers sur 2 chiffres).
     * 
     * @param   spm
     *          Le nombre de secondes apres minuit.
     * @return  heures:minutes:seconds
     */
    public static String toString(int spm) {
        return String.format("%02d:%02d:%02d", hours(spm), minutes(spm), seconds(spm));
    }
}