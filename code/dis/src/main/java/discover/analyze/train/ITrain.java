package discover.analyze.train;

import java.io.IOException;

/**
 * TODO: JavaDoc
 *
 * @param <R> the result from the training
 * @author Michael Couck
 * @version 01.00
 * @since 01-01-2016
 */
public interface ITrain<R> {

    /**
     * TODO: JavaDoc
     */
    R train() throws IOException;

}
