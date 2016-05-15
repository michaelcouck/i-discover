package discover.grid;

import ikube.discover.AbstractTest;
import discover.IConstants;
import discover.listener.IConsumer;
import discover.tool.THREAD;
import discover.tool.URI;
import mockit.MockUp;
import org.gridgain.grid.*;
import org.gridgain.grid.cache.GridCache;
import org.gridgain.grid.cache.datastructures.GridCacheDataStructures;
import org.gridgain.grid.cache.datastructures.GridCacheQueue;
import org.gridgain.grid.compute.GridCompute;
import org.gridgain.grid.compute.GridComputeTask;
import org.gridgain.grid.events.GridEvents;
import org.gridgain.grid.lang.GridBiPredicate;
import org.gridgain.grid.lang.GridPredicate;
import org.gridgain.grid.messaging.GridMessaging;
import org.jetbrains.annotations.Nullable;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-08-2014
 */
@SuppressWarnings("unchecked")
public class ClusterManagerGridGainTest extends AbstractTest {

    @SuppressWarnings("UnusedDeclaration")
    static class GridGainMockUp extends MockUp<GridGain> {
        @mockit.Mock
        public static Grid grid(@Nullable String name) throws GridIllegalStateException {
            return mock(Grid.class);
        }
    }

    String key = "key";
    String value = "value";

    @Mock
    private Grid grid;
    @Mock
    private GridNode gridNode;
    @Mock
    private GridCache gridCache;
    @Mock
    private GridCompute gridCompute;
    @Mock
    private GridMessaging gridMessaging;
    @Mock
    private Callable callable;
    @Mock
    private GridFuture gridFuture;
    @Spy
    @InjectMocks
    private ClusterManagerGridGain clusterManager;

    @BeforeClass
    public static void beforeClass() {
        THREAD.initialize();
    }

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        when(grid.cache(IConstants.GRID_NAME)).thenReturn(gridCache);
    }

    @AfterClass
    public static void afterClass() {
        THREAD.destroy();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void lock() throws Exception {
        when(gridCache.lock(any(), anyInt())).thenReturn(Boolean.TRUE);
        boolean locked = clusterManager.lock(IConstants.GRID_NAME);
        assertTrue(locked);
    }

    @Test
    public void unlock() throws Exception {
        boolean unlocked = clusterManager.unlock(IConstants.GRID_NAME);
        assertTrue(unlocked);
    }

    @Test
    public void sendMessage() throws GridException {
        String message = "message";
        when(grid.message()).thenReturn(gridMessaging);
        clusterManager.sendMessage(message);
        verify(gridMessaging, times(1)).send(IConstants.GRID_NAME, message);
    }

    @Test
    public void sendTask() {
        when(grid.compute()).thenReturn(gridCompute);
        when(gridCompute.call(callable)).thenReturn(gridFuture);
        when(gridFuture.isDone()).thenReturn(Boolean.TRUE);
        Future future = clusterManager.sendTask(callable);
        assertNotNull(future);
    }

    @Test
    public void wrapFuture() {
        Future future = clusterManager.wrapFuture(gridFuture);
        assertNotNull(future);
    }

    @Test
    public void sendTaskTo() {
        when(grid.compute()).thenReturn(gridCompute);
        when(grid.nodes()).thenReturn(Arrays.asList(gridNode));
        when(gridNode.addresses()).thenReturn(Arrays.asList(URI.getIp()));
        clusterManager.sendTaskTo(URI.getIp(), callable);
        verify(gridCompute, times(1)).execute(any(GridComputeTask.class), any(null));
    }

    @Test
    public void sendTaskToAll() {
        when(grid.compute()).thenReturn(gridCompute);
        when(gridCompute.broadcast(callable)).thenReturn(gridFuture);
        Future future = clusterManager.sendTaskToAll(callable);
        assertNotNull(future);
    }

    @Test
    public void get() throws GridException {
        when(gridCache.get(key)).thenReturn(value);
        Object gridValue = clusterManager.get(key);
        assertEquals(value, gridValue);
    }

    @Test
    public void put() throws GridException {
        clusterManager.put(key, value);
        verify(gridCache, times(1)).put(key, value);
    }

    @Test
    public void remove() throws GridException {
        clusterManager.remove(key);
        verify(gridCache, times(1)).remove(any(Object.class), any(Object.class));
    }

    @Test
    public void clear() throws GridException {
        clusterManager.clear(IConstants.GRID_NAME);
        verify(gridCache, times(1)).removeAll();
    }

    @Test
    public void getMap() throws GridException {
        clusterManager.get(IConstants.GRID_NAME, key);
        verify(gridCache, times(1)).get(key);
    }

    @Test
    public void putMap() throws GridException {
        clusterManager.put(IConstants.GRID_NAME, key, value);
        verify(gridCache, times(1)).put(key, value);
    }

    @Test
    public void removeMap() throws GridException {
        clusterManager.remove(IConstants.GRID_NAME, key);
        verify(gridCache, times(1)).remove(any(Object.class), any(Object.class));
    }

    @Test
    public void addTopicListener() throws GridException {
        IConsumer consumer = mock(IConsumer.class);
        when(grid.message()).thenReturn(gridMessaging);
        when(gridMessaging.remoteListen(any(String.class), any(GridBiPredicate.class))).thenReturn(gridFuture);
        clusterManager.addTopicListener(IConstants.GRID_NAME, consumer);
        verify(gridFuture, times(1)).get();
    }

    @Test
    public void addQueueListener() throws GridException {
        IConsumer consumer = mock(IConsumer.class);
        GridProjection gridProjection = mock(GridProjection.class);
        GridEvents gridEvents = mock(GridEvents.class);
        when(grid.forCache(IConstants.GRID_NAME)).thenReturn(gridProjection);
        when(gridProjection.events()).thenReturn(gridEvents);
        when(gridEvents.remoteListen(any(GridBiPredicate.class), any(GridPredicate.class))).thenReturn(gridFuture);
        clusterManager.addQueueListener(IConstants.GRID_NAME, consumer);
        verify(gridFuture, times(1)).get();
    }

    @Test
    public void push() throws GridException {
        GridCacheDataStructures gridCacheDataStructures = mock(GridCacheDataStructures.class);
        GridCacheQueue<Object> gridQueue = mock(GridCacheQueue.class);
        when(gridCache.dataStructures()).thenReturn(gridCacheDataStructures);
        when(gridCacheDataStructures.queue(any(String.class), anyInt(), anyBoolean(), anyBoolean())).thenReturn(gridQueue);
        clusterManager.push(IConstants.GRID_NAME, value);
        verify(gridQueue, times(1)).put(value);
    }

    @Test
    public void pop() throws GridException {
        GridCacheDataStructures gridCacheDataStructures = mock(GridCacheDataStructures.class);
        GridCacheQueue<Object> gridQueue = mock(GridCacheQueue.class);
        when(gridCache.dataStructures()).thenReturn(gridCacheDataStructures);
        when(gridCacheDataStructures.queue(any(String.class), anyInt(), anyBoolean(), anyBoolean())).thenReturn(gridQueue);
        clusterManager.pop(IConstants.GRID_NAME);
        verify(gridQueue, times(1)).take();
    }

    @Test
    public void send() throws GridException {
        when(grid.message()).thenReturn(gridMessaging);
        clusterManager.send(IConstants.GRID_NAME, value);
        verify(gridMessaging, times(1)).send(IConstants.GRID_NAME, value);
    }

    @Test
    public void destroy() throws GridException {
        clusterManager.destroy();
        verify(grid, times(1)).close();
    }

}