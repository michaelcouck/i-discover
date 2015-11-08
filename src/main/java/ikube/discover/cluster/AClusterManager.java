package ikube.discover.cluster;

import ikube.discover.database.IDataBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This base class for the cluster managers just has common methods and the ip and address that each cluster manager will use to distinguish
 * the servers, added to the port.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 17.07.12
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public abstract class AClusterManager implements IClusterManager {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The textual representation of the ip address for this server.
     */
    protected String ip;
    /**
     * The address or unique identifier for this server.
     */
    protected String address;

    @Autowired
    protected IDataBase dataBase;

}
