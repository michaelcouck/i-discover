package discover.grid;

import discover.IConstants;
import ikube.toolkit.THREAD;
import ikube.toolkit.URI;
import org.gridgain.grid.Grid;
import org.gridgain.grid.GridException;
import org.gridgain.grid.GridFuture;
import org.gridgain.grid.GridNode;
import org.gridgain.grid.cache.GridCache;
import org.gridgain.grid.cache.datastructures.GridCacheDataStructures;
import org.gridgain.grid.cache.datastructures.GridCacheQueue;
import org.gridgain.grid.compute.*;
import org.gridgain.grid.events.GridEvent;
import org.gridgain.grid.events.GridEvents;
import org.gridgain.grid.lang.GridBiPredicate;
import org.gridgain.grid.lang.GridPredicate;
import org.gridgain.grid.messaging.GridMessaging;
import org.gridgain.grid.resources.GridTaskContinuousMapperResource;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is the GridGain implementation of the grid manager.
 *
 * @author Michael Couck
 * @version 01.00
 * @see discover.grid.IClusterManager
 * @since 15-08-2014
 */
public class ClusterManagerGridGain extends AClusterManager {

    private Grid grid;

    public void initialize() throws GridException {
        ip = URI.getIp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public synchronized boolean lock(final String name) {
        try {
            GridCache<String, String> gridCache = grid.cache(name);
            return gridCache.lock(name, 250);
        } catch (final GridException e) {
            throw new RuntimeException(e);
        } finally {
            notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public synchronized boolean unlock(final String name) {
        try {
            GridCache<String, String> gridCache = grid.cache(IConstants.GRID_NAME);
            gridCache.unlock(IConstants.GRID_NAME);
            return Boolean.TRUE;
        } catch (final GridException e) {
            throw new RuntimeException(e);
        } finally {
            notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(final Serializable serializable) {
        try {
            GridMessaging gridMessaging = grid.message();
            gridMessaging.send(IConstants.GRID_NAME, serializable);
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Future<T> sendTask(final Callable<T> callable) {
        GridCompute gridCompute = grid.compute();
        GridFuture<?> gridFuture = gridCompute.call(callable);
        return wrapFuture(gridFuture);
    }

    @SuppressWarnings("unchecked")
    <T> Future<T> wrapFuture(final GridFuture<?> gridFuture) {
        return (Future<T>) THREAD.submit(IConstants.GRID_NAME, () -> {
            try {
                gridFuture.get();
            } catch (final GridException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Future<T> sendTaskTo(final Object server, final Callable<T> callable) {
        GridCompute gridCompute = grid.compute();
        Collection<GridNode> gridNodes = grid.nodes();

        final GridComputeJob gridComputeJob = new GridComputeJob() {

            @Override
            public void cancel() {
            }

            @Nullable
            @Override
            public Object execute() throws GridException {
                try {
                    return callable.call();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        AtomicReference<GridNode> gridNode = new AtomicReference<>();
        outer:
        for (final GridNode target : gridNodes) {
            Collection<String> addresses = target.addresses();
            for (final String address : addresses) {
                if (server.equals(address)) {
                    gridNode.set(target);
                    break outer;
                }
            }
        }

        GridComputeTask gridComputeTask = new GridComputeTask() {

            @SuppressWarnings("UnusedDeclaration")
            @GridTaskContinuousMapperResource
            private GridComputeTaskContinuousMapper gridComputeTaskContinuousMapper;

            @Nullable
            @Override
            public Map<? extends GridComputeJob, GridNode> map(final List subgrid, @Nullable final Object arg) throws GridException {
                gridComputeTaskContinuousMapper.send(gridComputeJob, gridNode.get());
                Map<GridComputeJob, GridNode> gridNodeMap = new HashMap<>();
                gridNodeMap.put(gridComputeJob, gridNode.get());
                return gridNodeMap;
            }

            @Override
            public GridComputeJobResultPolicy result(final GridComputeJobResult res, final List rcvd) throws GridException {
                return null;
            }

            @Nullable
            @Override
            public Object reduce(final List list) throws GridException {
                return null;
            }
        };
        return (Future<T>) gridCompute.execute(gridComputeTask, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Future<T> sendTaskToAll(final Callable<T> callable) {
        GridCompute gridCompute = grid.compute();
        GridFuture<T> gridFuture = (GridFuture<T>) gridCompute.broadcast(callable);
        return wrapFuture(gridFuture);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("ConstantConditions")
    public Object get(final Object key) {
        try {
            GridCache<Object, Object> gridCache = grid.cache(IConstants.GRID_NAME);
            return gridCache.get(key);
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public void put(final Object key, final Serializable value) {
        try {
            GridCache<Object, Object> gridCache = grid.cache(IConstants.GRID_NAME);
            gridCache.put(key, value);
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("ConstantConditions")
    public void remove(final Object key) {
        try {
            GridCache<Object, Object> gridCache = grid.cache(IConstants.GRID_NAME);
            gridCache.remove(key, get(key));
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public void clear(final String map) {
        try {
            GridCache<Object, Object> gridCache = grid.cache(map);
            gridCache.removeAll();
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public <T> T get(final String map, final Object key) {
        try {
            GridCache<Object, Object> gridCache = grid.cache(map);
            return (T) gridCache.get(key);
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public void put(final String map, final Object key, final Serializable value) {
        try {
            GridCache<Object, Object> gridCache = grid.cache(map);
            gridCache.put(key, value);
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("ConstantConditions")
    public void remove(final String map, final Object key) {
        try {
            GridCache<Object, Object> gridCache = grid.cache(map);
            gridCache.remove(key, get(key));
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addTopicListener(final String topic, final IConsumer<IEvent<?, ?>> listener) {
        GridMessaging gridMessaging = grid.message();
        GridBiPredicate<UUID, Object> gridBiPredicate = (uuid, event) -> {
            logger.debug("Message : {}, object : {}", uuid, event);
            listener.notify((IEvent<?, ?>) event);
            return Boolean.TRUE;
        };
        try {
            gridMessaging.remoteListen(topic, gridBiPredicate).get();
            logger.info("Added topic listeners : " + topic);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addQueueListener(final String queue, final IConsumer<IEvent<?, ?>> listener) {
        GridBiPredicate<UUID, GridEvent> gridBiPredicate = (uuid, gridEvent) -> {
            logger.debug("Event : {}", gridEvent);
            IEvent<?, ?> event = (IEvent<?, ?>) pop(queue);
            listener.notify(event);
            return Boolean.TRUE;
        };
        GridPredicate<GridEvent> gridPredicate = gridEvent -> {
            logger.debug("Event : {}", gridEvent);
            IEvent<?, ?> event = (IEvent<?, ?>) pop(queue);
            listener.notify(event);
            return Boolean.TRUE;
        };
        GridEvents gridEvents = grid.forCache(queue).events();
        try {
            gridEvents.remoteListen(gridBiPredicate, gridPredicate).get();
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    public void push(final String queue, final Object object) {
        try {
            GridCache gridCache = grid.cache(queue);
            GridCacheDataStructures gridCacheDataStructures = gridCache.dataStructures();
            GridCacheQueue<Object> gridQueue = gridCacheDataStructures.queue(queue, 1000000, true, true);
            //noinspection ConstantConditions
            // gridQueue.add(object);
            //noinspection ConstantConditions
            gridQueue.put(object);
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    Object pop(final String queue) {
        try {
            GridCache gridCache = grid.cache(queue);
            GridCacheDataStructures gridCacheDataStructures = gridCache.dataStructures();
            GridCacheQueue<Object> gridQueue = gridCacheDataStructures.queue(queue, 1000000, true, true);
            //noinspection ConstantConditions
            return gridQueue.take();
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void send(final Object topic, final Object object) {
        try {
            grid.message().send(topic, object);
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        try {
            grid.close();
        } catch (final GridException e) {
            throw new RuntimeException(e);
        }
    }

    public void setGrid(final Grid grid) {
        this.grid = grid;
    }

}