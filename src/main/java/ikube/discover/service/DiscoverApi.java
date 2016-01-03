package ikube.discover.service;

import ikube.discover.search.Search;
import ikube.discover.search.Searcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Path looks like this: http://localhost:8080/discover
 *
 * @author Michael couck
 * @version 01.00
 * @since 21-01-2012
 */
@Scope(ResourceApi.REQUEST)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path(DiscoverApi.DISCOVER)
public class DiscoverApi extends ResourceApi {

    static final String DISCOVER = "/discover";

    @Autowired
    @Qualifier("searchers")
    private Map<String, Searcher> searchers;

    /**
     * {@inheritDoc}
     */
    @POST
    public Response search(final Search search) throws IOException {
        String fieldName = search.getSearchFields().get(0);
        String searchString = search.getSearchStrings().get(0);
        Searcher searcher = searchers.get(search.getIndexName());
        if (searcher == null) {
            return buildResponse("No searcher defined for : " + search);
        }
        ArrayList<HashMap<String, String>> results = searcher.doSearch(fieldName, searchString);
        return buildResponse(results);
    }

}