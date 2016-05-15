package discover.search;

import java.util.List;

public class Search {

    private String indexName;
    private List<String> searchStrings;
    private List<String> searchFields;

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(final String indexName) {
        this.indexName = indexName;
    }

    public List<String> getSearchStrings() {
        return searchStrings;
    }

    public void setSearchStrings(final List<String> searchStrings) {
        this.searchStrings = searchStrings;
    }

    public List<String> getSearchFields() {
        return searchFields;
    }

    public void setSearchFields(final List<String> searchFields) {
        this.searchFields = searchFields;
    }
}
