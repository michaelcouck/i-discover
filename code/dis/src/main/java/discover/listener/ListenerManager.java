package discover.listener;

import discover.IConstants;
import discover.grid.ClusterManagerGridGain;
import discover.tool.THREAD;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 17-08-2015
 */
@Component
@EnableAsync
@Configuration
public class ListenerManager {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Qualifier("discover.listener.ListenerManager.listeners")
    private Map<String, Map<String, List<IConsumer<IEvent<?, ?>>>>> listeners = new HashMap<>();

    @Autowired
    @Qualifier("discover.cluster.IClusterManager")
    @SuppressWarnings({"SpringJavaAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
    private ClusterManagerGridGain clusterManager;

    public void add(final IConsumer<IEvent<?, ?>> listener, String type, String name) {
        synchronized (this) {
            get(type, name).add(listener);
        }
    }

    public void remove(final IConsumer<IEvent<?, ?>> listener, String type, String name) {
        synchronized (this) {
            get(type, name).remove(listener);
        }
    }

    List<IConsumer<IEvent<?, ?>>> get(final String type, final String name) {
        Map<String, List<IConsumer<IEvent<?, ?>>>> listenersForType = listeners.get(type);
        if (listenersForType == null) {
            listenersForType = new HashMap<>();
            listeners.put(type, listenersForType);
        }
        List<IConsumer<IEvent<?, ?>>> listenersForName = listenersForType.get(name);
        if (listenersForName == null) {
            listenersForName = new ArrayList<>();
            listenersForType.put(name, listenersForName);
        }
        return listenersForName;
    }

    public void fire(final IEvent<?, ?> event, final boolean local) {
        logger.debug("Received event : {}", ToStringBuilder.reflectionToString(event));
        if (local) {
            notify(event);
        } else {
            clusterManager.send(IConstants.GRID_NAME, event);
        }
    }

    public void notify(final IEvent<?, ?> event) {
        String type = event.getClass().getSimpleName();
        String name = event.getContext().getName();
        List<IConsumer<IEvent<?, ?>>> listeners = get(type, name);
        logger.debug("Notifying listeners : {}", listeners);
        for (final IConsumer<IEvent<?, ?>> listener : listeners) {
            final String jobName = Long.toString(System.nanoTime());
            class Notifier implements Runnable {
                public void run() {
                    try {
                        logger.debug("Notifying listener : {}", listener);
                        listener.notify(event);
                    } catch (final Exception e) {
                        logger.error(null, e);
                        throw new RuntimeException(e);
                    }
                }
            }
            THREAD.submit(jobName, new Notifier());
        }
    }

    public void addGridListeners() {
        THREAD.submit("wait-for-cluster-manager", () -> {
            THREAD.sleep(15000);
            addTopicListener();
            addQueueListener();
        });
    }

    public void addTopicListener() {
        if (clusterManager != null) {
            clusterManager.addTopicListener(IConstants.GRID_NAME, event -> fire(event, true));
            logger.info("Added topic listener : ");
        }
    }

    public void addQueueListener() {
        if (clusterManager != null) {
            clusterManager.addQueueListener(IConstants.GRID_NAME, event -> fire(event, true));
            logger.info("Added queue listener : ");
        }
    }

    public void setListeners(final Map<String, Map<String, List<IConsumer<IEvent<?, ?>>>>> listeners) {
        this.listeners = listeners;
    }

}