package ikube.discover.search;

import ikube.discover.tool.THREAD;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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
public class SearcherTest {

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
        Directory[] directories = null; // getDirectories(numberOfDirectories);
        searcher.openSearcher(directories);

        IndexSearcher indexSearcher = (IndexSearcher) Whitebox.getInternalState(searcher, "indexSearcher");
        assertNotNull(indexSearcher);

        MultiReader multiReader = (MultiReader) indexSearcher.getIndexReader();
        assertNotNull(multiReader);
        assertEquals(numberOfDirectories * numberOfDirectories, multiReader.numDocs());
    }

    @Test
    public void doSearch() throws IOException {
        openSearcher();
        ArrayList<HashMap<String, String>> results = searcher.doSearch("float-1", "1");
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    public void closeSearcher() throws IOException {
        final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        Directory[] directories = null; //getDirectories(3);
        IndexReader indexReader = new MultiReader(open(directories[0]), open(directories[1]), open(directories[2]));

        indexReader.addReaderClosedListener(new IndexReader.ReaderClosedListener() {
            @Override
            public void onClose(final IndexReader reader) {
                System.out.println("Reader : " + reader);
                atomicBoolean.set(Boolean.TRUE);
            }
        });

        searcher.closeSearcher(indexReader);
        THREAD.sleep(6000);

        assertTrue(atomicBoolean.get());
    }

}