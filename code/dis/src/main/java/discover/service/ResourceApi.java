package discover.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

/**
 * This is the base class for all web services, common logic and properties.
 *
 * @author Michael couck
 * @version 01.00
 * @since 20-11-2012
 */
public abstract class ResourceApi {

    public static final String REQUEST = "request";

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * This method will build the response object, setting the headers for cross site JavaScript
     * operations, and for all the method types of the resource. The underlying Json converter will be
     * either Jackson or Gson, depending on the configuration.
     *
     * @param object the entity response object, the object that will be converted into Json for the client
     * @return the response object that will be used as the mechanism for transferring the entity to the client
     */
    protected Response buildResponse(final Object object) {
        Response.ResponseBuilder responseBuilder = Response
                .status(Response.Status.OK)//
                .header("Access-Control-Allow-Origin", "*") //
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        return responseBuilder.entity(object).build();
    }

}