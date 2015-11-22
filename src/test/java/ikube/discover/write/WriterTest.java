package ikube.discover.write;

import com.jcraft.jsch.JSchException;
import ikube.discover.AbstractTest;
import ikube.discover.tool.THREAD;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-07-2015
 */
public class WriterTest extends AbstractTest {

    @Spy
    private Writer writer;

    @Test
    public void writeToIndex() throws IOException {
        int iterations = 100;
        for (int i = iterations; i > 0; i--) {
            Document document = new Document();
            writer.writeToIndex(Arrays.asList(document));
        }
        THREAD.sleep(1000);
        IndexWriter indexWriter = (IndexWriter) Whitebox.getInternalState(writer, "indexWriter"); // Deencapsulation.getField(writer,
        // IndexWriter.class);
        assertEquals("There should be a few documents in the index : ", iterations, indexWriter.numDocs());
    }

    @Test
    @Ignore
    public void createDocuments() throws SQLException, JSchException {
        List<Map<Object, Object>> records = Arrays.asList(
                getMap(new Object[]{"one", "two", "three"}, new Object[]{"one", "two", "three"}),
                getMap(new Object[]{"two", "three", "four"}, new Object[]{"two", "three", "four"}),
                getMap(new Object[]{"three", "four", "five"}, new Object[]{"three", "four", "five"}));
        List<Document> documents = writer.createDocuments(records);
        assertEquals("one", documents.get(0).getField("one").stringValue());
        assertEquals("five", documents.get(2).getField("five").stringValue());
    }

    private Map<Object, Object> getMap(final Object[] keys, final Object[] values) {
        Map<Object, Object> map = new HashMap<Object, Object>();
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

}