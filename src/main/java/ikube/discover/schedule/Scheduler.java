package ikube.discover.schedule;

import ikube.discover.Context;
import ikube.discover.IConstants;
import ikube.discover.grid.ClusterManagerGridGain;
import ikube.discover.listener.IEvent;
import ikube.discover.listener.IProducer;
import ikube.discover.listener.StartDatabaseProcessingEvent;
import ikube.discover.listener.SystemMonitoringEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Document me when implemented.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
@Component
@EnableScheduling
public class Scheduler implements IProducer<IEvent<?, ?>> {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired(required = false)
    @Qualifier("ikube.grid.gg.ClusterManagerGridGain")
    @SuppressWarnings({"SpringJavaAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
    private ClusterManagerGridGain clusterManager;

    @Autowired
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<Context> contexts;

    @Override
    public void fire(final IEvent<?, ?> event) {
        clusterManager.send(IConstants.GRID_NAME, event);
    }

    @Scheduled(initialDelay = 3000, fixedRate = 10000)
    public void systemSchedule() throws Exception {
        // Start the database(s) processing
        for (final Context context : contexts) {
            logger.debug("Starting processing of : {}", context.getName());
            IEvent<?, ?> event = new SystemMonitoringEvent(context);
            fire(event);
        }
    }

    @Scheduled(initialDelay = 5000, fixedRate = 10000)
    public void databaseSchedule() throws Exception {
        // Start the database(s) processing
        for (final Context context : contexts) {
            logger.debug("Starting processing of : {}", context.getName());
            IEvent<?, ?> event = new StartDatabaseProcessingEvent(context);
            fire(event);
        }
    }

    public void setContexts(final List<Context> contexts) {
        this.contexts = contexts;
    }

    public void setClusterManager(final ClusterManagerGridGain clusterManager) {
        this.clusterManager = clusterManager;
    }

}