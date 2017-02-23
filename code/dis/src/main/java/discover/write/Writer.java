package discover.write;

import discover.IConstants;
import discover.grid.IConsumer;
import discover.grid.IEvent;
import discover.grid.Producer;
import ikube.toolkit.INDEX;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.util.List;

public abstract class Writer<E extends IEvent<?, ?>> extends Producer<IEvent<?, ?>> implements IConsumer<IEvent<?, ?>> {

    protected IndexWriter indexWriter;

    Writer(final Class<?> klassToRegisterForEvents) {
        super(klassToRegisterForEvents);
    }

    @SuppressWarnings("EmptyFinallyBlock")
    void writeToIndex(final IndexWriter indexWriter, final List<Document> documents) {
        if (documents == null) {
            return;
        }
        try {
            for (final Document document : documents) {
                try {
                    logger.debug("Writing document : {}", document.get(IConstants.ID));
                    indexWriter.addDocument(document);
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    // Need to check what happened, and recover, possibly creating the document again
                }
            }
        } finally {
            INDEX.commitMerge(indexWriter);
        }
    }

}
