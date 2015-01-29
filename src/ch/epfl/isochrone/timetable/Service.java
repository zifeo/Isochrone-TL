package ch.epfl.isochrone.timetable;

import java.util.HashSet;
import java.util.Set;

/**
 * Un service de transports publics (jours ou les lignes sont actives).
 * Classe immuable. Possede un constructeur.
 */
public final class Service {

    private final String name;
    private final Date startingDate, endingDate;
    private final Set<Date.DayOfWeek> operatingDays;
    private final Set<Date> excludedDates, includedDates;

    /**
     * Constructeur public d'un service.
     * 
     * @param   name
     *          Nom du service.
     * @param   startingDate
     *          Date de debut du service.
     * @param   endingDate
     *          Date de fin du service.
     * @param   operatingDays
     *          Les jour d'operation du service.
     * @param   excludedDates
     *          Les dates (exceptions) ou le service n'est pas fonctionnel.
     * @param   includedDates
     *          Les dates (exceptions) ou le service est fonctionnel.
     * @throws  IllegalArgumentException
     *          En cas de date de debut posterieure a celle de fin.
     */
    public Service(String name, Date startingDate, Date endingDate, Set<Date.DayOfWeek> operatingDays, Set<Date> excludedDates, Set<Date> includedDates) {

        if (startingDate.compareTo(endingDate) > 0)
            throw new IllegalArgumentException("La date de debut ne doit pas etre posterieure a celle de fin : "+startingDate+" et "+endingDate);

        this.name = name;
        this.startingDate = startingDate;
        this.endingDate = endingDate;
        this.operatingDays = new HashSet<>(operatingDays);
        this.excludedDates = new HashSet<>(excludedDates);
        this.includedDates = new HashSet<>(includedDates);
    }

    /**
     * Accesseur en lecture du nom du service.
     * 
     * @return  Le nom du service.
     */
    public String name() {
        return name;
    }

    /**
     * Retourne vrai ssi le service est operationnel le jour donne (c'est-a-dire
     * compris dans les jours operationels et ne faisant pas defaut aux exceptions), faux sinon.
     * 
     * @param   date
     *          La date a verifier.
     * @return  Vrai ssi le service est operationnel le jour donne, faux sinon.
     */
    public boolean isOperatingOn(Date date) {
        return  startingDate.compareTo(date) <= 0 &&
                endingDate.compareTo(date) >= 0 &&
                ((operatingDays.contains(date.dayOfWeek()) &&
                        !excludedDates.contains(date)) ||
                        includedDates.contains(date));
    }

    /**
     * Retourne une representation textuelle du service.
     * 
     * @return  Le nom du service.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Batisseur de service.
     */
    public static class Builder {

        private final String name;
        private final Date startingDate, endingDate;
        private final Set<Date.DayOfWeek> operatingDays;
        private final Set<Date> excludedDates, includedDates;

        /**
         * Constructeur public d'un batisseur de service.
         * 
         * @param   name
         *          Le nom du service en construction.
         * @param   startingDate
         *          La date de debut du service en construction.
         * @param   endingDate
         *          La date de fin du service en construction.
         * @throws  IllegalArgumentException
         *          En cas de date de debut posterieure a celle de fin.
         */
        public Builder(String name, Date startingDate, Date endingDate) {

            if (startingDate.compareTo(endingDate) > 0)
                throw new IllegalArgumentException("La date de debut ne doit pas etre posterieure a celle de fin : "+startingDate+" et "+endingDate);

            this.name = name;
            this.startingDate = startingDate;
            this.endingDate = endingDate;
            this.operatingDays = new HashSet<>();
            this.excludedDates = new HashSet<>();
            this.includedDates = new HashSet<>();
        }

        /**
         * Accesseur en lecture du nom du service en construction.
         * 
         * @return  Le nom du service en construction.
         */
        public String name() {
            return name;
        }

        /**
         * Ajoute un jour d'operation (enumeration) au constructeur du service.
         * Permet les appels chaines.
         * 
         * @param   day
         *          Le jour de la semaine a ajouter (enumeration).
         * @return  Le batisseur.
         */
        public Builder addOperatingDay(Date.DayOfWeek day) {
            operatingDays.add(day);
            return this;
        }

        /**
         * Ajoute une date a exclure du batisseur de service.
         * Permet les appels chaines.
         * 
         * @param   date
         *          La date a exclure du service en construction.
         * @return  Le batisseur.
         * @throws  IllegalArgumentException
         *          En cas de date non comprise dans l'intervalle [startingDate, endingDate].
         * @throws  IllegalArgumentException
         *          En cas de date deja inclue (exception).
         */
        public Builder addExcludedDate(Date date) {

            if (startingDate.compareTo(date) > 0 || endingDate.compareTo(date) < 0)
                throw new IllegalArgumentException("la date exclue doit etre dans ["+startingDate+", "+endingDate+"] : "+date);
            if (includedDates.contains(date))
                throw new IllegalArgumentException("la date exclue fait deja partie des dates inclues : "+date);

            excludedDates.add(date);
            return this;
        }

        /**
         * Ajoute une date a inclure au batisseur de service.
         * Permet les appels chaines.
         * 
         * @param   date
         *          La date a inclure au service en construction.
         * @return  Le batisseur.
         * @throws  IllegalArgumentException
         *          En cas de date non comprise dans l'intervalle [startingDate, endingDate].
         * @throws  IllegalArgumentException
         *          En cas de date deja exclue (exception).
         */
        public Builder addIncludedDate(Date date) {

            if (startingDate.compareTo(date) > 0 || endingDate.compareTo(date) < 0)
                throw new IllegalArgumentException("la date inclue doit etre dans ["+startingDate+", "+endingDate+"] : "+date);
            if (excludedDates.contains(date))
                throw new IllegalArgumentException("la date inclue fait deja partie des dates exclues : "+date);

            includedDates.add(date);
            return this;
        }

        /**
         * Construit un service a partir du batisseur.
         * 
         * @return  Le service.
         */
        public Service build() {
            return new Service(name, startingDate, endingDate, operatingDays, excludedDates, includedDates);
        }
    }
}