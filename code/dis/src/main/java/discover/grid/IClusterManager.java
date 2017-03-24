package discover.grid;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * This is the interface that will synchronize and coordinate the servers in the grid. The
 * implementations are critical to the functioning of Ikube. Along with this is the functionality that
 * will distribute the searches in the grid, as well as the analytics. This class, because it controls the grid, will
 * also be responsible for the persistence of the searches in the database. Typically the grid functionality will be
 * used as a write delay to the database.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
public interface IClusterManager {

    /**
     * This method will lock, or try to lock the grid.
     *
     * @param name the name of the lock, must be unique
     * @return whether the grid was successfully locked
     */
    boolean lock(final String name);

    /**
     * Unlocks the grid. The server can only unlock the grid if it already has the lock.
     *
     * @param name the name of the lock, must be unique
     * @return whether the grid was unlocked by this server
     */
    boolean unlock(final String name);

    /**
     * Sends a message to the grid. Messages may include actions that this server is working on, or a lock attempt, or the server object to stay in the
     * grid club.
     *
     * @param serializable the object to send to the grid
     */
    void sendMessage(final Serializable serializable);

    /**
     * This method will post a callable to the a member in the grid. The result can be gotten from
     * the future that is returned. Keep in mind that on the remote node, there is no
     * dependency injection, so you have to get everything on that side when you get there.
     * <p>
     * This method will randomly select a node to execute the task on. Over time the tasks should be
     * evenly distributed over the nodes in the grid, essentially load balancing the stress over the grid.
     *
     * @param callable the callable that will be executed on a random target node
     * @return the future from the member that the callable will be executed on
     */
    <T> Future<T> sendTask(final Callable<T> callable);

    /**
     * This method, similar to the above, will send a task to a particular server.
     *
     * @param server   the server to send the task to
     * @param callable the callable to execute remotely
     * @param <T>      the type of result expected from the remote server
     * @return the return value for the execution of the logic
     */
    <T> Future<T> sendTaskTo(final Object server, final Callable<T> callable);

    /**
     * Similar to the above this method will execute a task on a target member of the grid, except that
     * this method will execute the same task on all nodes in the grid. This can be useful when, for example
     * when there are models being dynamically trained and all the servers need to stay in synch, so they all get
     * the updated analysis object to re-train from, and consequently persist the model to their local file system.
     *
     * @param callable the callable that will be executed on the all nodes of the grid
     * @return the futures from all the members that the callable is be executed on
     */
    <T> Future<T> sendTaskToAll(final Callable<T> callable);

    /**
     * Returns the object in the Ikube map with the specified key.
     *
     * @param key the key for the object int he distributed map
     * @return the object corresponding to the key, could be null
     */
    Object get(final Object key);

    /**
     * Puts an object in the distributed map, using the key specified, the key need to be universally unique of course.
     *
     * @param key    the key to associate with the object
     * @param object the object to put in the map, also needs to be serializable
     */
    void put(final Object key, final Serializable object);

    /**
     * This method will remove an object from the grid.
     *
     * @param key the key of the object to remove
     */
    void remove(final Object key);

    /**
     * Clears the distributed data from teh grid for the map name.
     *
     * @param map the name of the distributed map to clear across the grid
     */
    void clear(final String map);

    /**
     * Returns the object in the specified map with the specified key.
     *
     * @param key the key for the object int he distributed map
     * @param map the name of the cache where the object is stored
     * @return the object corresponding to the key, could be null
     */
    <T> T get(final String map, final Object key);

    /**
     * Puts an object in the specified distributed map, using the key specified, the key need to be universally unique of course.
     *
     * @param map    the name of the cache where the object is stored
     * @param key    the key to associate with the object
     * @param object the object to put in the map, also needs to be serializable
     */
    void put(final String map, final Object key, final Serializable object);

    /**
     * This method will remove an object from the grid.
     *
     * @param map the name of the cache where the object is stored
     * @param key the key of the object to remove
     */
    void remove(final String map, final Object key);


    /**
     * This method will release any resources and close down the grid manager gracefully.
     */
    void destroy();

}