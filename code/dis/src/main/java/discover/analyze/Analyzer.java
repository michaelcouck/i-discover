package discover.analyze;

import discover.listener.AnalysisEvent;
import discover.listener.IConsumer;
import discover.listener.IProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will call and execute analyzers on the input data.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 24-08-2015
 */
@SuppressWarnings("UnusedDeclaration")
public class Analyzer implements IConsumer<AnalysisEvent>, IProducer<AnalysisEvent> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void notify(final AnalysisEvent analysisEvent) {
    }

    @Override
    public void fire(final AnalysisEvent analysisEvent) {
        Thread.interrupted();
    }

}