package discover.grid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tagging interface for components that produce events and publish them to the grid.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 19-08-2015
 */
public abstract class Producer<E extends IEvent<?, ?>> extends Consumer<IEvent<?, ?>> implements IProducer<E> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public Producer(final Class<?> klassToRegisterForEvents) {
        super(klassToRegisterForEvents);
    }

    /**
     * Fires the event in the grid.
     *
     * @param event the event to publish to the grid
     */
    public void fire(final E event, boolean local) {
        clusterManager.send(event.getClass().getSimpleName(), event);
    }

}