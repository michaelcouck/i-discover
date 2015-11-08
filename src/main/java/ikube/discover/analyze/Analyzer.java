package ikube.discover.analyze;

import ikube.discover.listener.AnalysisEvent;
import ikube.discover.listener.IConsumer;
import ikube.discover.listener.IProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will call and execute analyzers on the input data.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 24-08-2015
 */
// @Component
public class Analyzer implements IConsumer<AnalysisEvent>, IProducer<AnalysisEvent> {

    @SuppressWarnings("UnusedDeclaration")
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void notify(final AnalysisEvent analysisEvent) {
    }

    @Override
    public void fire(final AnalysisEvent analysisEvent) {
        Thread.interrupted();
    }

}