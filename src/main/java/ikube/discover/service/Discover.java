package ikube.discover.service;

import ikube.discover.search.Searcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

/**
 * Path looks like this: http://localhost:9080/ikube/service/search/json/xxx
 *
 * @author Michael couck
 * @version 01.00
 * @since 21-01-2012
 */
@Component
@Scope(Resource.REQUEST)
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_JSON)
@Path(Discover.EXPERIMENTAL)
public class Discover extends Resource {

    static final String EXPERIMENTAL = "/experimental";

    @Autowired
    @Qualifier("searchers")
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private Map<String, Searcher> searchers;

    /**
     * {@inheritDoc}
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response search(final Object search) throws IOException {
        String fieldName = null; // search.getSearchFields().get(0);
        String searchString = null; // search.getSearchStrings().get(0);
        Searcher searcher = null; // searchers.get(search.getIndexName());
        if (searcher == null) {
            return buildResponse("No searcher defined for : " + search);
        }
        return buildResponse(searcher.doSearch(fieldName, searchString));
    }

}