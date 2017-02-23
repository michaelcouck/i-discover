package discover.grid;

import discover.Context;
import org.apache.lucene.document.Document;

import java.util.List;
import java.util.Map;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 17-08-2015
 */
public class RamWriterEvent implements IEvent<Boolean, List<Map<Object, Object>>> {

    private Context context;
    private List<Document> documents;
    private Boolean ramEventSource;
    private List<Map<Object, Object>> data;

    public RamWriterEvent(final Context context, final Boolean ramEventSource, final List<Document> documents, List<Map<Object, Object>> data) {
        this.context = context;
        this.documents = documents;
        this.ramEventSource = ramEventSource;
        this.data = data;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public Boolean getSource() {
        return ramEventSource;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    @Override
    public List<Map<Object, Object>> getData() {
        return data;
    }

}