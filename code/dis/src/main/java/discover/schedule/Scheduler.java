package discover.schedule;

import discover.Context;
import discover.grid.IEvent;
import discover.grid.Producer;
import discover.grid.StartDatabaseProcessingEvent;
import discover.grid.SystemMonitoringEvent;
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
public class Scheduler extends Producer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private List<Context> contexts;

    public Scheduler() {
        super(IEvent.class);
    }

    @Scheduled(initialDelay = 3000, fixedRate = 10000)
    public void systemSchedule() throws Exception {
        // Start the database(s) processing
        for (final Context context : contexts) {
            logger.info("Starting processing of : {}", context.getName());
            fire(new SystemMonitoringEvent(context), false);
        }
    }

    @Scheduled(initialDelay = 5000, fixedRate = 10000)
    public void databaseSchedule() throws Exception {
        // Start the database(s) processing
        for (final Context context : contexts) {
            logger.info("Starting processing of : {}", context.getName());
            fire(new StartDatabaseProcessingEvent(context), false);
        }
    }

    public void setContexts(final List<Context> contexts) {
        this.contexts = contexts;
    }

    @Override
    public void notify(IEvent event) {
        // Do nothing at the moment
    }

}