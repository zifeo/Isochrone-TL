package ch.epfl.isochrone.timetable;

import static ch.epfl.isochrone.math.Math.divF;
import static ch.epfl.isochrone.math.Math.modF;

/**
 * Une date selon le calendrier gregorien.
 * Classe immuable. Possede deux enumerations (jour de la semaine et mois).
 */
public final class Date implements Comparable<Date> {

    private final int day, year;
    private final Month month;

    /**
     * Enumeration des 7 jours de la semaine.
     */
    public enum DayOfWeek {MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY};

    /**
     * Enumeration des 12 mois de l'annee.
     */
    public enum Month {JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER};

    /**
     * Constructeur public d'une date selon un jour, un mois (enumeration) et une annee.
     * 
     * @param   day
     *          Le jour compris dans l'intervalle [1, daysInMonth].
     * @param   month
     *          Le mois venant de l'enumeration.
     * @param   year
     *          L'annee.
     * @throws  IllegalArgumentException
     *          En cas de jours non compris dans [1, daysInMonth].
     */
    public Date(int day, Month month, int year) {

        int daysInMonth = daysInMonth(month, year);
        if (day < 1 || day > daysInMonth)
            throw new IllegalArgumentException("le jour doit etre compris dans [1, "+daysInMonth+"] : "+day);

        this.day = day;
        this.month = month;
        this.year = year;
    }

    /**
     * Constructeur public d'une date selon un jour, un numero de mois et une annee.
     * 
     * @param   day
     *          Le jour compris dans l'intervalle [1, daysInMonth].
     * @param   month
     *          Le numero de mois compris dans l'intervalle [1, 12].
     * @param   year
     *          L'annee.
     * @throws  IllegalArgumentException
     *          En cas de mois non compris dans [1, 12].
     */
    public Date(int day, int month, int year) {
        this(day, intToMonth(month), year); // intToMonth lance deja IllegalArgumentException en cas de mois non compris dans [1, 12]
    }

    /**
     * Constructeur public d'une date selon une date Java.
     * 
     * @param   date
     *          La date java.
     */
    @SuppressWarnings("deprecation")
    public Date(java.util.Date date) {
        this(date.getDate(), date.getMonth()+1, date.getYear() + 1900); // correction des donnees de la date
    }

    /**
     * Accesseur en lecture du jour.
     * 
     * @return  Le jour.
     */
    public int day() {
        return day;
    }

    /**
     * Accesseur en lecture du mois (enumeration).
     * 
     * @return  Le mois.
     */
    public Month month() {
        return month;
    }

    /**
     * Accesseur en lecture du numero du mois.
     * 
     * @return  Le numero du mois.
     */
    public int intMonth() {
        return monthToInt(month);
    }

    /**
     * Accesseur en lecture de l'annee.
     * 
     * @return  L'annee.
     */
    public int year() {
        return year;
    }

    /**
     * Retourne le jour de la semaine correspondant a cette date (enumeration).
     * 
     * @return  Le jour de la semaine (enumeration).
     */
    public DayOfWeek dayOfWeek() {
        int dayElapsed = fixed()-1;
        return DayOfWeek.values()[modF(dayElapsed, 7)];
    }

    /**
     * Retourne une nouvelle date en fonction de cette date et du nombre de jours donnee.
     * 
     * @param   daysDiff
     *          Le nombre de jours de difference.
     * @return  date
     *          La nouvelle date.
     */
    public Date relative(int daysDiff) {
        return fixedToDate(fixed() + daysDiff);
    }

    /**
     * Convertit cette date en date java.
     * 
     * @return  La date Java.
     */
    @SuppressWarnings("deprecation")
    public java.util.Date toJavaDate() {
        return new java.util.Date(year - 1900, month.ordinal(), day); // correction des donnees pour la date Java
    }

    /**
     * Retourne une representation textuelle de la date (entiers sur 2 ou 4 chiffres).
     *
     * @return  year-month-day
     */
    @Override
    public String toString() {
        return String.format("%d-%02d-%02d", year, monthToInt(month), day);
    }

    /**
     * Compare la date avec la date donnee et retourne vrai ssi les deux dates representent le meme jour.
     *
     * @param   that
     *          La date de comparaison.
     * @return  Vrai, ssi les dates representent le meme jour, faux sinon.
     */
    @Override
    public boolean equals(Object that) {
        return that != null && this.getClass() == that.getClass() && this.fixed() == ((Date) that).fixed();
    }

    /**
     * Calcule le code de hachage de la date en fonction du nombre de jour ecoulees depuis l'an 0.
     *
     * @return  Le nombre de jour ecoulees depuis l'an 0.
     */
    @Override
    public int hashCode() {
        return fixed();
    }

    /**
     * Determine si la date est anterieure, posterieure ou egale a la date donnee.
     * 
     * @param   that
     *          La date de comparaison.
     * @return  -1 si la date est anterieure a la date donnee,
     *          0 si la date est egale a la date donnee,
     *          +1 si la date est posterieure a la date donnee.
     */
    public int compareTo(Date that) {
        return Integer.signum(this.fixed() - that.fixed()); // Signum car l'enonce demande -1,0 ou 1 mais pas -,0,+
    }

    // convertit un numero de mois (enumeration)
    private static Month intToMonth(int m) {

        if (m < 1 || m > 12)
            throw new IllegalArgumentException("le mois doit etre compris dans [1, 12] : "+m);

        return Month.values()[m-1];
    }

    // convertit un mois (enumeration) en numero de mois
    private static int monthToInt(Month m) {
        return m.ordinal()+1;
    }

    // retourne vrai ssi l'annee est bissextile, faux sinon
    private static boolean isLeapYear(int y) {
        return (modF(y, 4) == 0 && modF(y, 100) != 0) || modF(y, 400) == 0;
    }

    // retourne le nombre de jour dans le mois en fonction de l'annee.
    private static int daysInMonth(Month m, int y) {
        switch (m) {
        case APRIL:
        case JUNE:
        case SEPTEMBER:
        case NOVEMBER:
            return 30;

        case FEBRUARY:
            return (isLeapYear(y)) ? 29: 28;

        default:
            return 31;
        }
    }

    // retourne le nombre de jours ecoules depuis l'an 0
    private static int dateToFixed(int d, int m, int y) {    
        int correction, y0 = y-1;

        if (m <= 2) {
            correction = 0;
        } else if (m > 2 && isLeapYear(y)) {
            correction = -1;
        } else {
            correction = -2;
        }

        return 365*y0 + divF(y0, 4) - divF(y0, 100) + divF(y0, 400) + divF((367*m - 362), 12) + d + correction;
    }

    // (polymorphisme) evite les conversions (numero de mois, mois)
    private static int dateToFixed(int d, Month m, int y) {    
        return dateToFixed(d, monthToInt(m), y);
    }

    // retourne une date en fonction du nombre de jours ecoules depuis l'an 0
    private static Date fixedToDate(int n) {
        int d0 = n-1;
        int n400 = divF(d0, 146097);
        int d1 = modF(d0, 146097);
        int n100 = divF(d1, 36524);
        int d2 = modF(d1, 36524);
        int n4 = divF(d2, 1461);
        int d3 = modF(d2, 1461);
        int n1 = divF(d3, 365);
        int y0 = 400*n400 + 100*n100 + 4*n4 + n1;
        int y = (n100 == 4 || n1 == 4) ? y0: y0+1;

        int p = n - dateToFixed(1, Month.JANUARY, y);
        int g3 = dateToFixed(1, Month.MARCH, y);

        if (n < g3)
            p += 0;
        else if (n >= g3 && isLeapYear(y))
            p += 1;
        else
            p += 2;

        int m = divF((12*p + 373), 367);
        int d = n - dateToFixed(1, m, y) + 1;

        return new Date(d, m, y);
    }

    // retourne le nombre de jours de la date (instance) ecoules depuis l'an 0
    private int fixed() {
        return dateToFixed(day, month, year);
    }
}