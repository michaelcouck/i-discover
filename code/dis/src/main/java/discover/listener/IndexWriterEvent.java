package discover.listener;

import discover.Context;
import org.apache.lucene.document.Document;

import java.util.List;
import java.util.Map;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 17-08-2015
 */
public class IndexWriterEvent implements IEvent<Void, List<Map<Object, Object>>> {

    private Context context;

    private List<Document> documents;

    private List<Map<Object, Object>> data;
    public IndexWriterEvent(final Context context, final List<Document> documents, List<Map<Object, Object>> data) {
        this.context = context;
        this.documents = documents;
        this.data = data;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public Void getSource() {
        return null;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    @Override
    public List<Map<Object, Object>> getData() {
        return data;
    }

}