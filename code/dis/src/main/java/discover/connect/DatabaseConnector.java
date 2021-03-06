package discover.connect;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import discover.Context;
import discover.grid.IEvent;
import discover.grid.Producer;
import discover.grid.RamWriterEvent;
import discover.grid.StartDatabaseProcessingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.sql.*;
import java.util.*;

/**
 * Reads the database finds all changed records, inserts, updates and deletes.
 * <p/>
 * The ssh tunnel works as follows:
 * <p/>
 * <pre>
 *     1) Open a tunnel from local port(let say 10000) => ikube.be remote port(and say 443)
 *     2) Open a database connection, which will go to the local port, i.e. in this case 10 000
 *     3) On the target ssh machine we get to port 443, but the port forwarding forwards to 1521
 * </pre>
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
public class DatabaseConnector extends Producer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Ssh components, to be used for the ssh tunnel **
     */
    private JSch jsch;
    private Properties config;
    private Session session;

    /**
     * SSH credentials and properties **
     */
    // Can be any port, the start port for the ssh tunnel
    @Value("${local-port:10000}")
    private int localPort = 10000;

    // The port that ssh is listening on the target machine
    @Value("${target-ssh-port:22}")
    private int targetSshPort = 22;

    // The userid for the ssh tunnel, i.e. the operating system userid
    @Value("${ssh-userid:laptop}")
    private String sshUserid = "laptop";

    // The password for the ssh tunnel, i.e. the operating system password
    @Value("${ssh-password:caherline}")
    private String sshPassword = "caherline";

    /**
     * Database credentials and items **
     */
    // The remote host for the tunnel and the database
    @Value("${remote-host-for-ssh-and-database:localhost}")
    private String remoteHostForSshAndDatabase = "localhost";

    // The database userid
    @Value("${database-userid:sa}")
    private String userid = "sa";

    // The database password
    @Value("${database-password:}")
    String password = "";

    // Must be the target port for the database
    @Value("${database-port:9082}")
    private int databasePort = 9082;

    // The url for the database
    @Value("${database-url:jdbc:h2:tcp://localhost:9082/i-discover;DB_CLOSE_ON_EXIT=FALSE}")
    private String url = "jdbc:h2:tcp://localhost:9082/i-discover;DB_CLOSE_ON_EXIT=FALSE";
    // And this we get from the driver
    private Connection connection;

    public DatabaseConnector() {
        super(StartDatabaseProcessingEvent.class);
        jsch = new JSch();
        config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("kex", "diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group-exchange-sha256");
        logger.info("Database : " + this);
    }

    /**
     * TODO: Set the last modification timestamp in an aspect around this, in the event of
     * TODO: a failure set the timestamp back to what it was. However do not lock the cache over
     * TODO: this method! Other nodes must still be able to set the timestamp
     */
    @Override
    @SuppressWarnings("JpaQueryApiInspection")
    public void notify(final IEvent event) {
        StartDatabaseProcessingEvent startDatabaseProcessingEvent = (StartDatabaseProcessingEvent) event;
        Context context = startDatabaseProcessingEvent.getContext();
        logger.info("Starting database on : " + context.getName());
        //noinspection EmptyFinallyBlock
        try {
            createSshTunnel();
            createDatabaseConnection();

            Timestamp from = context.getModification();
            context.setModification(new Timestamp(System.currentTimeMillis()));
            Timestamp to = context.getModification();

            String sql = "SELECT * FROM analysis WHERE timestamp >= ? AND timestamp < ? ORDER BY timestamp ASC";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setTimestamp(1, from);
            preparedStatement.setTimestamp(2, to);

            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            List<Map<Object, Object>> data = new ArrayList<>();

            boolean next = resultSet.next();
            if (next) {
                do {
                    Map<Object, Object> row = new HashMap<>();
                    for (int columnIndex = 1; columnIndex <= resultSetMetaData.getColumnCount(); columnIndex++) {
                        Object columnName = resultSetMetaData.getColumnName(columnIndex);
                        Object columnValue = resultSet.getObject(columnIndex);
                        row.put(columnName, columnValue);
                    }
                    data.add(row);
                    next = resultSet.next();
                    // TODO: Get the memory available, locally and remotely, and put the maximum data in the grid
                    boolean nextAndDataSizeLimit = (next && data.size() % 1000 == 0);
                    boolean nextAndDataSizeLimitOrNotNext = nextAndDataSizeLimit || !next;
                    if (nextAndDataSizeLimitOrNotNext) {
                        //noinspection ConstantConditions
                        logger.info("Fire event : " + nextAndDataSizeLimit + ", " + nextAndDataSizeLimitOrNotNext);
                        List<Map<Object, Object>> clonedData = new ArrayList<>(data);
                        // TODO: Failover - only fire event and carry on if successful
                        // TODO: Send this data batch to the node with the lowest cpu
                        fire(new RamWriterEvent(context, false, null, clonedData), false);
                        data.clear();
                    }
                } while (next);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            // TODO: Set the last modification timestamp back to the original if failed
        }
    }

    void createDatabaseConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, userid, password);
            logger.info("Connected JDBC through ssh tunnel : {}", url);
        }
    }

    void createSshTunnel() throws JSchException {
        if (session == null || !session.isConnected()) {
            session = jsch.getSession(sshUserid, remoteHostForSshAndDatabase, targetSshPort);
            session.setPassword(sshPassword);
            session.setConfig(config);
            session.connect();
            session.setPortForwardingL(localPort, remoteHostForSshAndDatabase, databasePort);
            logger.info("Created ssh tunnel : {}, {}", sshUserid, remoteHostForSshAndDatabase);
        }
    }

}