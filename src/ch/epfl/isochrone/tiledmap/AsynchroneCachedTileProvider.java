package ch.epfl.isochrone.tiledmap;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Un fournisseur de tuiles avec cache asynchrone.
 */
public final class AsynchroneCachedTileProvider implements TileProvider {

    private final TileProvider originalProvider;
    private final ExecutorService pool;
    private final TileCache cache;
    private final Set<Long> loading;

    /**
     * Constructeur public du fournisseur qui s'occupe de mettre en cache de facon asynchrone les tuiles du fournisseur original.
     * 
     * @param   originalProvider
     *          Le fournisseur de tuiles original
     */
    public AsynchroneCachedTileProvider(TileProvider originalProvider) {
        this.originalProvider = originalProvider;
        /*
         * L'utilisation de `newCachedThreadPool` permet l'utilisation rapide et consécutive d'un nombre de threads non limité.
         * Or, dans les régles d'utilisation de Open Street Map, il est stipulé qu'on ne peut pas lancer plus deux téléchargements
         * simultanés. Nous sommes allés sur le chat IRC de Open Street Map pour demander des conseils sur l'implémentation de la
         * concurrence en respectant ladite règle d'utilisation et un des administrateurs, TMCW, nous a autorisé à utiliser la fonction
         * `newCachedThreadPool` pour l'utilisation qu'on est fait lors du rendu du bonus du projet.
         * 
         * Si cela n'avait pas été possible, une autre pool avec une queue bornée aurait été utilisée :
         * > this.queue = new ArrayBlockingQueue<>(32);
         * > this.pool = new ThreadPoolExecutor(2, 2, 10L, TimeUnit.SECONDS, queue);
         * 
         * La queue aurait été vidée s'il y avait plus de 32 tuiles en cours de chargement :
         * > try {
         * >     **adding**
         * > } catch (RejectedExecutionException e) {
         * >     queue.clear();
         * >     loading.clear();
         * > }
         * 
         */
        this.pool = Executors.newCachedThreadPool();
        this.cache = new TileCache(100); // 100 images en cache
        this.loading = new HashSet<>();
    }
    
    /**
     * Retrouve la tuile aux coordonnees donnees dans le cache ou appelle le fournisseur d'origine de facon asynchrone.
     * 
     * @param   zoom
     *          Le niveau de zoom de la tuile.
     * @param   x
     *          La coordonnee x de la tuile.
     * @param   y
     *          La coordonnee y de la tuile.
     * @throws  IllegalArgumentException
     *          En cas de zoom negatif.
     * @return  La tuile demandee.       
     */
    @Override
    public Tile tileAt(final int zoom, final int x, final int y) {
        
        if (zoom < 0)
            throw new IllegalArgumentException("le zoom ne doit pas etre negatif : "+zoom);

        Tile tile = cache.get(zoom, x, y);

        if (tile != null)
            return tile;

        // si la tuile n'est pas deja dans la pool
        final long id = TileCache.encodeTileCoordinates(zoom, x, y);
        if (!loading.contains(id)) {
                loading.add(id);
                pool.submit(new Runnable(){
                    @Override
                    public void run() {
                        cache.put(originalProvider.tileAt(zoom, x, y));
                        loading.remove(id);
                    }
                });
        }
        return Tile.LOADING;
    }

    @Override
    public void finalize() throws Throwable {
        pool.shutdown();
        super.finalize();
    }
}