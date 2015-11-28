package ikube.discover.grid;

import ikube.discover.AbstractTest;
import ikube.discover.IConstants;
import ikube.discover.tool.THREAD;
import ikube.discover.tool.URI;
import mockit.MockUp;
import org.gridgain.grid.*;
import org.gridgain.grid.cache.GridCache;
import org.gridgain.grid.compute.GridCompute;
import org.gridgain.grid.compute.GridComputeTask;
import org.gridgain.grid.messaging.GridMessaging;
import org.jetbrains.annotations.Nullable;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
public class ClusterManagerGridGainTest extends AbstractTest {

    static class GridGainMockUp extends MockUp<GridGain> {
        @mockit.Mock
        public static Grid grid(@Nullable String name) throws GridIllegalStateException {
            return Mockito.mock(Grid.class);
        }
    }

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
        //noinspection unchecked
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
        String key = "key";
        String value = "value";
        //noinspection unchecked
        when(gridCache.get(key)).thenReturn(value);
        Object gridValue = clusterManager.get(key);
        assertEquals(value, gridValue);
    }

    @Test
    public void put() throws GridException {
        String key = "key";
        String value = "value";
        clusterManager.put(key, value);
        //noinspection unchecked
        verify(gridCache, times(1)).put(key, value);
    }

    @Test
    public void remove() {
        clusterManager.remove(null);
        fail();
    }

    @Test
    public void clear() {
        clusterManager.clear(null);
        fail();
    }

    @Test
    public void getMap() {
        clusterManager.get(null, null);
        fail();
    }

    @Test
    public void putMap() {
        clusterManager.put(null, null, null);
        fail();
    }

    @Test
    public void removeMap() {
        clusterManager.remove(null, null);
        fail();
    }

    @Test
    public void destroy() {
        clusterManager.destroy();
        fail();
    }

}