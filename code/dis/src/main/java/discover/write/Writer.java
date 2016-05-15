package discover.write;

import discover.Context;
import discover.IConstants;
import discover.listener.*;
import discover.tool.INDEX;
import discover.tool.PARSER;
import discover.tool.STRING;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class writes documents to the indexes, some in memory, and some on the disk.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
@Component
public class Writer implements IConsumer<IndexWriterEvent>, IProducer<IndexWriterEvent> {

    private static final long MAX_DOCUMENTS = 1000000;
    private static final long UNCOMMITTED_DOCUMENTS_BEFORE_COMMIT = 1000;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("discover.listener.ListenerManager")
    private ListenerManager listenerManager;

    private IndexWriter indexWriter;
    private AtomicLong uncommittedDocuments;

    public Writer() throws IOException {
        uncommittedDocuments = new AtomicLong();
        createIndexWriter();
    }

    private synchronized void createIndexWriter() throws IOException {
        indexWriter = INDEX.getIndexWriter();
    }

    @Override
    public void notify(final IndexWriterEvent writerEvent) {
        List<Map<Object, Object>> data = writerEvent.getData();
        if (data != null) {
            process(writerEvent.getContext(), data);
        }
        List<Document> documents = writerEvent.getDocuments();
        if (documents != null) {
            writeToIndex(documents);
            uncommittedDocuments.getAndAccumulate(documents.size(), (left, right) -> left + right);
        }
        if (uncommittedDocuments.get() > UNCOMMITTED_DOCUMENTS_BEFORE_COMMIT) {
            synchronized (this) {
                uncommittedDocuments.set(0);
                INDEX.commitMerge(indexWriter);
                logger.info("Num docs writer : {}", indexWriter.numDocs());

                try {
                    if (indexWriter.numDocs() > MAX_DOCUMENTS) {
                        try {
                            logger.info("Getting new index writer : {}", indexWriter.numDocs());
                            indexWriter = INDEX.getIndexWriter();
                        } catch (final IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } finally {
                    notifyAll();
                }

                // Fire JVM internal event for searcher to open on new directories
                Context context = writerEvent.getContext();
                Directory[] directories = new Directory[]{indexWriter.getDirectory()};
                IEvent<?, ?> searcherEvent = new OpenSearcherEvent(context, directories);
                listenerManager.fire(searcherEvent, true);
            }
        }
    }

    @Override
    public void fire(final IndexWriterEvent event) {
        listenerManager.fire(event, false);
    }

    List<Map<Object, Object>> process(final Context context, final List<Map<Object, Object>> data) {
        // Create the Lucene documents from the changed records
        List<Document> documents = createDocuments(data);
        if (documents.size() > 0) {
            logger.debug("Popping documents in grid : {}", documents.size());
            // Pop the documents in the grid to be indexed by all nodes
            IndexWriterEvent indexWriterEvent = new IndexWriterEvent(context, documents, null);
            fire(indexWriterEvent);
        }
        return data;
    }

    void writeToIndex(final List<Document> documents) {
        for (final Document document : documents) {
            try {
                logger.debug("Writing document : {}", document.get(IConstants.ID));
                indexWriter.addDocument(document);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    List<Document> createDocuments(final List<Map<Object, Object>> records) {
        List<Document> documents = new ArrayList<>();
        for (final Map<Object, Object> row : records) {
            Document document = new Document();
            for (final Map.Entry<Object, Object> mapEntry : row.entrySet()) {
                String fieldName = mapEntry.getKey().toString();
                String fieldValue = mapEntry.getValue() != null ? mapEntry.getValue().toString() : "";
                String parsedFieldValue = PARSER.parse(new ByteArrayInputStream(fieldValue.getBytes()));
                if (STRING.isNumeric(parsedFieldValue)) {
                    INDEX.addNumericField(fieldName, parsedFieldValue, true, 0, document);
                } else {
                    INDEX.addStringField(fieldName, parsedFieldValue, true, true, true, false, true, 0, document);
                }
            }
            documents.add(document);
        }
        return documents;
    }

}