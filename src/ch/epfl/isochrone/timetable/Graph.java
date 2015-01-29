package ch.epfl.isochrone.timetable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Un graphe dont les noeuds representent les arrets et les arcs les trajets.
 * Classe immuable. Possede un constructeur.
 */
public final class Graph {

    private final Set<Stop> stops;
    private final Map<Stop, List<GraphEdge>> outgoingEdges;

    /**
     * Constructeur prive du graphe et ajoute l'ensemble des arrets et des arcs sortants de ces arrets.
     * Non instanciable sans son constructeur.
     * 
     * @param   stops
     *          L'ensemble des arrets.
     * @param   outgoingEdges
     *          L'ensemble des arcs sortant des arrets.
     */
    private Graph(Set<Stop> stops, Map<Stop, List<GraphEdge>> outgoingEdges) {
        this.stops = stops; // pas de copie, car le constructeur s'en occupe deja
        this.outgoingEdges = new HashMap<>(outgoingEdges);
    }

    /**
     * Retourne l'arbre du chemin le plus rapide depuis un arret de depart et un temps de depart selon l'algorithme Dijkstra.
     * 
     * @param   startingStop
     *          L'arret de depart.
     * @param   departureTime
     *          Le temps de depart.
     * @return  L'arbre du chemin le plus rapide depuis l'arret donne.
     * @throws  IllegalArgumentException
     *          En cas d'arret non present dans le graphe.
     * @throws  IllegalArgumentException
     *          En cas de temps negatif.         
     */
    public FastestPathTree fastestPaths(Stop startingStop, int departureTime) {

        if (!stops.contains(startingStop))
            throw new IllegalArgumentException("l'arret doit faire partie du graphe : "+startingStop);
        if (departureTime < 0)
            throw new IllegalArgumentException("le temps de depart ne doit pas etre negatif : "+departureTime);

        final FastestPathTree.Builder b = new FastestPathTree.Builder(startingStop, departureTime); // final pour y acceder dans la classe anonyme ci-dessous

        // comparateur d'un stop à un autre en fonction du temps d'arrivee dans le constructeur l'arbre des chemins les plus rapides
        Comparator<Stop> comparator = new Comparator<Stop>() {
            @Override
            public int compare(Stop s1, Stop s2) {
                return b.arrivalTime(s1) - b.arrivalTime(s2);
            }
        };

        final PriorityQueue<Stop> remainingStops = new PriorityQueue<>(stops.size(), comparator);
        remainingStops.addAll(stops); // on ajoute tous les stops dans la queue

        Stop currentStop;
        while ((currentStop = remainingStops.poll()) != null) {

            if (b.arrivalTime(currentStop) == SecondsPastMidnight.INFINITE)
                break; // les voisins d'un arret non atteignable, ne seront pas plus atteingable via cet arret, on arrete donc la recherche

            if (!outgoingEdges.containsKey(currentStop))
                continue;

            for (GraphEdge neighbourEdge : outgoingEdges.get(currentStop)) {

                Stop destination = neighbourEdge.destination();
                int earliestArrivalTime = neighbourEdge.earliestArrivalTime(b.arrivalTime(currentStop));

                if (earliestArrivalTime < b.arrivalTime(destination)) {                                        
                    remainingStops.remove(destination); // on enleve et rajoute le stop pour qu'il puisse mettre a jour son emplacement
                    b.setArrivalTime(destination, earliestArrivalTime, currentStop);
                    remainingStops.add(destination);
                }
            }
        }

        return b.build();
    }

    /**
     * Batisseur de graphe.
     */
    public final static class Builder {

        private final Set<Stop> stops;
        private final Map<Stop, Map<Stop, GraphEdge.Builder>> edgeBuilders;

        /**
         * Constructeur public d'un graphe.
         * 
         * @param   stops
         *          L'ensemble des arrets.
         */
        public Builder(Set<Stop> stops) {
            this.stops = new HashSet<>(stops);
            this.edgeBuilders = new HashMap<>();
        }

        /**
         * Ajoute un arc entre deux arrets (dans un seul sens) à un temps de depart et d'arrive donne.
         * Permets les appels chaines.
         * 
         * @param   fromStop
         *          L'arret de depart.
         * @param   toStop
         *          L'arret d'arrivee.
         * @param   departureTime
         *          Le temps de depart.
         * @param   arrivalTime
         *          Le temps d'arrivee.
         * @return  Le batisseur.
         * @throws  IllegalArgumentException
         *          En cas d'arret de depart n'appartenant pas au graphe.
         * @throws  IllegalArgumentException
         *          En cas d'arret d'arrivee n'appartenant pas au graphe.
         * @throws  IllegalArgumentException
         *          En cas de temps negatif.
         * @throws  IllegalArgumentException
         *          En cas de temps d'arrivee anterieur au temps de depart.
         */
        public Builder addTripEdge(Stop fromStop, Stop toStop, int departureTime, int arrivalTime) {

            if (!stops.contains(fromStop))
                throw new IllegalArgumentException("l'arret de depart ne fait pas partie du graphe : "+fromStop);
            if (!stops.contains(toStop))
                throw new IllegalArgumentException("l'arret d'arrive ne fait pas partie du graphe : "+toStop);
            if (departureTime < 0 || arrivalTime < 0)
                throw new IllegalArgumentException("l'heure ne doit pas etre negative : "+departureTime+" et "+arrivalTime);
            if (arrivalTime < departureTime)
                throw new IllegalArgumentException("l'heure d'arrivee doit etre posterieure a celle de depart");

            GraphEdge.Builder builder = getEdgeBuilder(fromStop, toStop);
            builder.addTrip(departureTime, arrivalTime);

            return this;
        }

        /**
         * Ajoute le temps de marche (dans les deux sens) entre chaques noeuds du graphe en fonction d'un temps de marche maximum et d'une vitesse de marche.
         * Permet les appels chaines.
         * 
         * @param   maxWalkingTime
         *          Le temps de marche maximum.
         * @param   walkingSpeed
         *          vitesse de marche en unite correspondante
         * @return  Le batisseur.
         * @throws  IllegalArgumentException
         *          en cas de temps de marche negatif
         * @throws  IllegalArgumentException
         *          en cas de vitesse de marche negative ou nulle
         */
        public Builder addAllWalkEdges(int maxWalkingTime, double walkingSpeed) {

            if (maxWalkingTime < 0)
                throw new IllegalArgumentException("le temps de marche doit etre positif : "+maxWalkingTime);
            if (walkingSpeed <= 0)
                throw new IllegalArgumentException("la vitesse de marche doit etre positif non nulle : "+walkingSpeed);

            final int length = stops.size();
            final Stop[] allStops = stops.toArray(new Stop[length]);

            for (int i = 0; i < length; ++i) { // parcourt en O(log(n)) de tous les arrets
                for (int j = i+1; j < length; ++j) {

                    Stop s1 = allStops[i];
                    Stop s2 = allStops[j];

                    int walkingTime = (int) Math.round(s1.position().distanceTo(s2.position())/walkingSpeed);

                    if (walkingTime <= maxWalkingTime) {
                        getEdgeBuilder(s1, s2).setWalkingTime(walkingTime);
                        getEdgeBuilder(s2, s1).setWalkingTime(walkingTime);
                    } // sinon rien, car GraphEdge.Builder initialise d'origine la valeur de marche a -1
                }
            }

            return this;
        }

        /**
         * Construit un graphe a partir du batisseur.
         * 
         * @return  Le graphe.
         */
        public Graph build() {

            final Map<Stop, List<GraphEdge>> outgoingEdges = new HashMap<>();

            for (Entry<Stop, Map<Stop, GraphEdge.Builder>> entry : edgeBuilders.entrySet()) { // parcourt les arrets et construit les arcs              
                Stop fromStop = entry.getKey();                
                outgoingEdges.put(fromStop, new LinkedList<GraphEdge>());

                for (GraphEdge.Builder edge : entry.getValue().values()) {
                    outgoingEdges.get(fromStop).add(edge.build());
                }
            }
            return new Graph(stops, outgoingEdges);
        }

        // cree ou retrouve un batisseur d' arc entre deux arrets
        private GraphEdge.Builder getEdgeBuilder(Stop fromStop, Stop toStop) {

            if (!edgeBuilders.containsKey(fromStop)) { // s'il n'y a pas de table de batisseur, on la cree
                edgeBuilders.put(fromStop, new HashMap<Stop, GraphEdge.Builder>());
            }
            if (!edgeBuilders.get(fromStop).containsKey(toStop)) { // s'il n'y a pas de batisseur, on le cree
                edgeBuilders.get(fromStop).put(toStop, new GraphEdge.Builder(toStop));
            }
            return edgeBuilders.get(fromStop).get(toStop);
        }
    }
}