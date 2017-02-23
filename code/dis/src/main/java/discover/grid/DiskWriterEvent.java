package discover.grid;

import discover.Context;
import org.apache.lucene.document.Document;

import java.util.List;
import java.util.Map;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-08-2016
 */
public class DiskWriterEvent implements IEvent<Boolean, List<Map<Object, Object>>> {

    private Context context;
    private List<Document> documents;

    public DiskWriterEvent(final Context context, final List<Document> documents) {
        this.context = context;
        this.documents = documents;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public Boolean getSource() {
        return null;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    @Override
    public List<Map<Object, Object>> getData() {
        return null;
    }

}