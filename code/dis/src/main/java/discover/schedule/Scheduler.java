package discover.schedule;

import discover.Context;
import discover.IConstants;
import discover.grid.ClusterManagerGridGain;
import discover.listener.IEvent;
import discover.listener.IProducer;
import discover.listener.StartDatabaseProcessingEvent;
import discover.listener.SystemMonitoringEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * Document me when implemented.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
@EnableScheduling
public class Scheduler implements IProducer<IEvent<?, ?>> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private List<Context> contexts;
    private ClusterManagerGridGain clusterManager;

    @Override
    public void fire(final IEvent<?, ?> event) {
        logger.info("Scheduler : " + this + ", " + clusterManager);
        clusterManager.send(IConstants.GRID_NAME, event);
    }

    @Scheduled(initialDelay = 3000, fixedRate = 10000)
    public void systemSchedule() throws Exception {
        // Start the database(s) processing
        for (final Context context : contexts) {
            logger.info("Starting processing of : {}", context.getName());
            IEvent<?, ?> event = new SystemMonitoringEvent(context);
            fire(event);
        }
    }

    @Scheduled(initialDelay = 5000, fixedRate = 10000)
    public void databaseSchedule() throws Exception {
        // Start the database(s) processing
        for (final Context context : contexts) {
            logger.info("Starting processing of : {}", context.getName());
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