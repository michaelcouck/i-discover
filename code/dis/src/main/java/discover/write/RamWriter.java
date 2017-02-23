package discover.write;

import discover.grid.DiskWriterEvent;
import discover.grid.IEvent;
import discover.grid.RamWriterEvent;
import ikube.toolkit.INDEX;
import ikube.toolkit.PARSER;
import ikube.toolkit.STRING;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class writes documents to the indexes.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
@Component
public class RamWriter extends Writer<RamWriterEvent> {

    @Value("${max-memory-size-for-ram-index:512}")
    private int maxMemorySize = 512;

    public RamWriter() throws IOException {
        super(RamWriterEvent.class);
        indexWriter = INDEX.getRamIndexWriter();
    }

    @Override
    public void notify(final IEvent<?, ?> event) {
        logger.info("Received event : ", event);

        RamWriterEvent ramWriterEvent = (RamWriterEvent) event;

        List<Document> documents = ramWriterEvent.getDocuments();
        writeToIndex(indexWriter, documents);

        documents = createDocuments(ramWriterEvent.getData());
        writeToIndex(indexWriter, documents);

        synchronized (this) {
            INDEX.commitMerge(indexWriter);
            logger.info("Num docs writer : {}", indexWriter.numDocs());
            // Check that we don't run out of memory
            long memorySize = indexWriter.ramSizeInBytes() / 1000000;
            if (memorySize > maxMemorySize) {
                try {
                    try {
                        // Fire all the documents in the ram index into the grid
                        List<Document> ramDocuments = getDocuments(indexWriter);
                        DiskWriterEvent diskWriterEvent = new DiskWriterEvent(event.getContext(), ramDocuments);
                        fire(diskWriterEvent, Boolean.FALSE);
                        // Open a new ram index, clear the memory
                        logger.info("Getting new index writer : {}", indexWriter.numDocs());
                        indexWriter = INDEX.getRamIndexWriter();
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                } finally {
                    notifyAll();
                }
            }
        }

        ramWriterEvent.getContext().setModification(new Timestamp(System.currentTimeMillis()));
    }

    private List<Document> getDocuments(final IndexWriter indexWriter) {
        try {
            DirectoryReader directoryReader = DirectoryReader.open(indexWriter.getDirectory());
            List<Document> documents = new ArrayList<>();
            for (int i = 0; i < indexWriter.numDocs(); i++) {
                documents.add(directoryReader.document(i));
            }
            return documents;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    List<Document> createDocuments(final List<Map<Object, Object>> records) {
        if (records == null) {
            return null;
        }
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

    public void setMaxMemorySize(final int maxMemorySize) {
        this.maxMemorySize = maxMemorySize;
    }

}