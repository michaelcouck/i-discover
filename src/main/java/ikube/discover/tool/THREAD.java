package ikube.discover.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * This class just has a method that will wait for a list of threads to finish and
 * an executor service that will execute 'threads' and return futures that can be waited for
 * by the callers.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12-02-2011
 */
public final class THREAD {

    private static class ShutdownThread extends Thread {
        public void run() {
            try {
                THREAD.destroy();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(THREAD.class);
    /**
     * Executes the 'threads' and returns a future.
     */
    private static ExecutorService EXECUTOR_SERVICE;
    /**
     * The number of times to try to cancel the future(s).
     */
    private static int MAX_RETRY_COUNT = 3;
    /**
     * A list of futures by name so we can kill them. The map contains the name of the caller,
     * which must be unique, and the futures that were spawned by the caller. In this way the caller
     * or an observer of the futures can cancel them using the name.
     */
    private static Map<String, List<Future<?>>> FUTURES;
    private static Map<String, ForkJoinPool> FORK_JOIN_POOLS;

    /**
     * This method initializes the executor service, and the thread pool that will execute runnables.
     */
    public static void initialize() {
        if (EXECUTOR_SERVICE != null && !EXECUTOR_SERVICE.isShutdown() && FUTURES != null && FORK_JOIN_POOLS != null) {
            LOGGER.info("Executor service already initialized : ");
            return;
        }
        EXECUTOR_SERVICE = Executors.newCachedThreadPool();
        FUTURES = Collections.synchronizedMap(new HashMap<String, List<Future<?>>>());
        FORK_JOIN_POOLS = Collections.synchronizedMap(new HashMap<String, ForkJoinPool>());
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
    }

    /**
     * This method will submit the runnable and add it to a map so the caller can cancel the future if necessary.
     *
     * @param name     the name to assign to the future to be able to cancel it if necessary
     * @param runnable the runnable to schedule for running
     * @return the future that is being submitted to execute
     */
    public static Future<?> submit(final String name, final Runnable runnable) {
        if (EXECUTOR_SERVICE == null || EXECUTOR_SERVICE.isShutdown()) {
            LOGGER.info("Executor service is shutdown : " + runnable);
            return null;
        }
        Future<?> future = EXECUTOR_SERVICE.submit(runnable);
        if (name != null) {
            getFutures(name).add(future);
        } else {
            getFutures(THREAD.class.getSimpleName()).add(future);
        }
        return future;
    }

    /**
     * This method will submit the callable and add it to a map so the caller can cancel the future if necessary.
     *
     * @param name     the name to assign to the future to be able to cancel it if necessary
     * @param callable the runnable to schedule for running
     * @return the future that is being submitted to execute
     */
    public static Future<?> submit(final String name, final Callable<?> callable) {
        if (EXECUTOR_SERVICE == null || EXECUTOR_SERVICE.isShutdown()) {
            LOGGER.info("Executor service is shutdown : " + callable);
            return null;
        }
        Future<?> future = EXECUTOR_SERVICE.submit(callable);
        if (name != null) {
            getFutures(name).add(future);
        } else {
            getFutures(THREAD.class.getSimpleName()).add(future);
        }
        return future;
    }

    /**
     * This method will terminate the future(s) with the specified name, essentially interrupting it and remove
     * it from the list. In the case where this future is running an action the action will terminate abruptly. Note
     * that futures typically run in groups of three or four, and are keyed by the name, so all the futures in
     * the group need to be cancelled.
     *
     * @param name the name that was assigned to the future when it was submitted for execution
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static void destroy(final String name) {
        List<Future<?>> futures = getFutures(name);
        synchronized (futures) {
            for (final Future<?> future : futures) {
                if (future == null) {
                    LOGGER.info("Future null : WTF? Can't be : ");
                    continue;
                }
                if (future.isDone() || future.isCancelled()) {
                    LOGGER.debug("Future done : " + future + ", " + name);
                    continue;
                }
                int maxRetryCount = MAX_RETRY_COUNT;
                while (maxRetryCount-- > 0) {
                    if (future.cancel(true) || future.isCancelled() || future.isDone()) {
                        LOGGER.debug("Cancelled future : " + name + ", " + future + ", " + maxRetryCount);
                        break;
                    }
                    THREAD.sleep(1);
                }
                if (!future.isCancelled() && !future.isDone()) {
                    LOGGER.warn("Couldn't cancel future : " + name + ", " + future + ", " + maxRetryCount);
                }
            }
            futures.clear();
        }
        cancelForkJoinPool(name);
    }

    /**
     * This method will wait for all the futures to finish their logic.
     *
     * @param futures the futures to wait for in seconds
     * @param seconds and the maximum amount of time to wait in seconds
     */
    public static <T> void waitForFutures(final List<Future<T>> futures, final long seconds) {
        for (final Future<?> future : futures) {
            waitForFuture(future, seconds);
        }
    }

    /**
     * This method, similar to the {@link THREAD#waitForFutures(java.util.List, long)},
     * halts the current thread, to wait for the futures to complete, but the signature is not parameterized, making it
     * easier to access
     *
     * @param futures the futures to wait for in seconds
     * @param seconds and the maximum amount of time to wait in seconds
     */
    public static void waitForAnonymousFutures(final List<Future> futures, final long seconds) {
        for (final Future<?> future : futures) {
            waitForFuture(future, seconds);
        }
    }

    /**
     * This method will wait for the future to finish doing it's work. In the event the future is interrupted,
     * for example by the executor service closing down and interrupting all it's threads, it will return immediately.
     * If the future takes too long and passes the maximum wait time, then the method will return immediately.
     *
     * @param future  the future to wait for in seconds
     * @param seconds the maximum amount of time to wait in seconds
     */
    public static void waitForFuture(final Future<?> future, final long seconds) {
        if (future == null) {
            LOGGER.info("Future null returning : ");
            return;
        }
        try {
            future.get(seconds, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            LOGGER.info("Coitus interruptus... : " + e.getMessage());
        } catch (final TimeoutException e) {
            LOGGER.info("Timed out waiting for future : " + e.getMessage());
        } catch (final CancellationException e) {
            LOGGER.debug("Future cancelled : " + e.getMessage());
        } catch (final Exception e) {
            LOGGER.error("Exception waiting for future : ", e);
        }
    }

    /**
     * This method iterates through the list of threads looking for one that is still alive and joins it. Once
     * all the threads have finished then this method will return to the caller indicating that all the threads
     * have finished.
     *
     * @param threads the threads to wait for
     */
    public static void waitForThreads(final Collection<Thread> threads) {
        if (threads == null) {
            LOGGER.warn("Threads null : ");
            return;
        }
        outer:
        do {
            for (final Thread thread : threads) {
                if (thread.isAlive()) {
                    try {
                        thread.join(1000);
                    } catch (final InterruptedException e) {
                        LOGGER.error("Interrupted waiting for thread : " + thread + ", this thread : " + Thread.currentThread(), e);
                    }
                    continue outer;
                }
            }
            break;
        } while (true);
    }

    /**
     * This method will just sleep for the specified time without the interrupted exception needing to be caught.
     *
     * @param milliSeconds the time for the current thread to sleep in milli seconds
     */
    public static void sleep(final long milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * This method will just wait for the specified time without the interrupted exception needing to be caught. When
     * a thread goes into wait in a synchronized block he releases the lock.
     *
     * @param milliSeconds the time for the current thread to sleep in milli seconds
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void waitABit(final long milliSeconds) {
        try {
            Thread.currentThread().wait(milliSeconds);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public static synchronized ForkJoinPool cancelForkJoinPool(final String name) {
        ForkJoinPool forkJoinPool = FORK_JOIN_POOLS.remove(name);
        LOGGER.debug("Terminating fork join pool : " + name + ", " + forkJoinPool);
        if (forkJoinPool != null) {
            try {
                forkJoinPool.shutdownNow();
            } catch (final CancellationException e) {
                LOGGER.error("Cancelling fork join pool error : " + forkJoinPool, e);
            }
        }
        return forkJoinPool;
    }

    public static void cancelAllForkJoinPools() {
        if (FORK_JOIN_POOLS == null) {
            return;
        }
        Set<String> names = new TreeSet<String>(FORK_JOIN_POOLS.keySet());
        for (final String name : names) {
            cancelForkJoinPool(name);
        }
    }

    public static synchronized ForkJoinPool getForkJoinPool(final String name, final int threads) {
        ForkJoinPool forkJoinPool = FORK_JOIN_POOLS.get(name);
        if (forkJoinPool == null) {
            forkJoinPool = new ForkJoinPool(threads);
            FORK_JOIN_POOLS.put(name, forkJoinPool);
        }
        return forkJoinPool;
    }

    public static ForkJoinPool executeForkJoinTasks(final String name, final int threads, final ForkJoinTask<?>... forkJoinTasks) {
        ForkJoinPool forkJoinPool = getForkJoinPool(name, threads);
        for (final ForkJoinTask<?> forkJoinTask : forkJoinTasks) {
            // forkJoinPool.invoke(forkJoinTask);
            forkJoinPool.execute(forkJoinTask);
        }
        return forkJoinPool;
    }

    /**
     * This method will destroy the thread pool. All threads that are currently running will be interrupted,
     * and should catch this exception and exit the run method.
     */
    public static synchronized void destroy() {
        if (EXECUTOR_SERVICE == null || EXECUTOR_SERVICE.isShutdown() || FUTURES == null || FORK_JOIN_POOLS == null) {
            LOGGER.debug("Executor service already shutdown : ");
            return;
        }

        // Destroy all the futures one by one
        Collection<String> futureNames = new ArrayList<String>(FUTURES.keySet());
        for (String futureName : futureNames) {
            destroy(futureName);
        }

        // Shut down the executor service
        EXECUTOR_SERVICE.shutdown();
        try {
            int maxRetryCount = MAX_RETRY_COUNT;
            while (!EXECUTOR_SERVICE.awaitTermination(10, TimeUnit.SECONDS) && maxRetryCount-- > 0) {
                List<Runnable> runnables = EXECUTOR_SERVICE.shutdownNow();
                LOGGER.info("Still waiting to shutdown : " + runnables);
                EXECUTOR_SERVICE.shutdown();
                EXECUTOR_SERVICE.shutdownNow();
            }
        } catch (final InterruptedException e) {
            LOGGER.error("Executor service thread interrupted : ", e);
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }

        // Cancel all the fork join pools
        cancelAllForkJoinPools();

        // Check that there are not runnables still active
        List<Runnable> runnables = EXECUTOR_SERVICE.shutdownNow();
        if (!runnables.isEmpty()) {
            LOGGER.info("Not running runnables : " + runnables);
        }

        // Clear everything just to be sure
        FUTURES.clear();
        FORK_JOIN_POOLS.clear();

        // And finally null all references to pools of threads
        EXECUTOR_SERVICE = null;
        FUTURES = null;
        FORK_JOIN_POOLS = null;
    }

    public static boolean isInitialized() {
        boolean serviceNull = EXECUTOR_SERVICE == null;
        boolean futuresNull = FUTURES == null;
        boolean poolsNull = FORK_JOIN_POOLS == null;
        return !serviceNull && !futuresNull && !poolsNull;
    }

    protected static List<Future<?>> getFutures(final String name) {
        List<Future<?>> futures;
        if (FUTURES != null) {
            futures = FUTURES.get(name);
            if (futures == null) {
                futures = Collections.synchronizedList(new ArrayList<Future<?>>());
                FUTURES.put(name, futures);
            }
        } else {
            futures = Collections.synchronizedList(new ArrayList<Future<?>>());
        }
        return futures;
    }

}