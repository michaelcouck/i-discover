package discover.search;

import ikube.discover.AbstractTest;
import discover.tool.THREAD;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.lucene.index.DirectoryReader.open;
import static org.junit.Assert.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-07-2015
 */
public class SearcherTest extends AbstractTest {

    @Spy
    private Searcher searcher;
    @Mock
    private IndexReader indexReader;

    @BeforeClass
    public static void beforeClass() {
        THREAD.initialize();
    }

    @AfterClass
    public static void afterClass() {
        THREAD.destroy();
    }

    @Test
    public void openSearcher() throws IOException {
        int numberOfDirectories = 3;
        Directory[] directories = getDirectories(numberOfDirectories);
        searcher.openSearcher(directories);

        IndexSearcher indexSearcher = (IndexSearcher) Whitebox.getInternalState(searcher, "indexSearcher");
        assertNotNull(indexSearcher);

        MultiReader multiReader = (MultiReader) indexSearcher.getIndexReader();
        assertNotNull(multiReader);
        assertEquals(numberOfDirectories * numberOfDirectories, multiReader.numDocs());
    }

    @Test
    @Ignore
    public void doSearch() throws IOException {
        openSearcher();
        ArrayList<HashMap<String, String>> results = searcher.doSearch("float-1", "1");
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    public void closeSearcher() throws IOException {
        final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        Directory[] directories = getDirectories(3);
        IndexReader indexReader = new MultiReader(open(directories[0]), open(directories[1]), open(directories[2]));

        indexReader.addReaderClosedListener(reader -> {
            System.out.println("Reader : " + reader);
            atomicBoolean.set(Boolean.TRUE);
        });

        searcher.closeSearcher(indexReader);
        THREAD.sleep(6000);

        assertTrue(atomicBoolean.get());
    }

}