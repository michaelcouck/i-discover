package discover.grid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Tagging interface for components that consume events from the grid.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 17-08-2015
 */
public abstract class Consumer<E extends IEvent<?, ?>> implements IConsumer<IEvent<?, ?>> {

    private Class klassToRegisterForEvents;
    @Autowired
    @Qualifier("discover.cluster.IClusterManager")
    @SuppressWarnings({"SpringJavaAutowiringInspection", "SpringJavaAutowiredMembersInspection", "SpringAutowiredFieldsWarningInspection"})
    ClusterManagerGridGain clusterManager;

    Consumer(final Class<?> klassToRegisterForEvents) {
        this.klassToRegisterForEvents = klassToRegisterForEvents;
    }

    public void setClusterManager(ClusterManagerGridGain clusterManager) {
        this.clusterManager = clusterManager;
    }

    public void initialise() {
        clusterManager.addTopicListener(klassToRegisterForEvents.getSimpleName(), this);
    }

}