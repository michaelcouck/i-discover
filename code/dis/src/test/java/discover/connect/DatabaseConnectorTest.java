package discover.connect;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import discover.AbstractTest;
import discover.Context;
import discover.grid.RamWriterEvent;
import discover.grid.StartDatabaseProcessingEvent;
import mockit.Deencapsulation;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
public class DatabaseConnectorTest extends AbstractTest {

    @Spy
    @InjectMocks
    private DatabaseConnector databaseConnector;

    @Mock
    private Session session;
    @Mock
    private Connection connection;
    @Mock
    private ResultSet resultSet;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSetMetaData resultSetMetaData;

    @Mock
    private Context context;
    @Mock
    private StartDatabaseProcessingEvent startDatabaseProcessingEvent;

    @Test
    @SuppressWarnings({"unchecked", "Convert2Lambda"})
    public void readChangedRecords() throws JSchException, SQLException {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                // Do nothing...
                return Boolean.TRUE;
            }
        }).when(databaseConnector).createSshTunnel();
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                // Do nothing...
                return Boolean.TRUE;
            }
        }).when(databaseConnector).createDatabaseConnection();
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                // Do nothing...
                return Boolean.TRUE;
            }
        }).when(session).disconnect();

        final AtomicReference atomicReference = new AtomicReference(null);

        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        Mockito.when(resultSet.getMetaData()).thenReturn(resultSetMetaData, resultSetMetaData, resultSetMetaData);
        Mockito.when(resultSetMetaData.getColumnCount()).thenReturn(3, 3, 3);
        Mockito.when(resultSetMetaData.getColumnName(1)).thenReturn("one");
        Mockito.when(resultSetMetaData.getColumnName(2)).thenReturn("two");
        Mockito.when(resultSetMetaData.getColumnName(3)).thenReturn("three");

        Mockito.when(resultSet.getObject(1)).thenReturn("one", "two", "three");
        Mockito.when(resultSet.getObject(2)).thenReturn("two", "three", "four");
        Mockito.when(resultSet.getObject(3)).thenReturn("three", "four", "five");

        Mockito.when(startDatabaseProcessingEvent.getContext()).thenReturn(context);
        Mockito.when(context.getModification()).thenReturn(new Timestamp(System.currentTimeMillis()));

        databaseConnector.notify(startDatabaseProcessingEvent);
        RamWriterEvent indexWriterEvent = (RamWriterEvent) atomicReference.get();
        List<Map<Object, Object>> changedRecords = indexWriterEvent.getData();
        assertEquals("1:1", "one", changedRecords.get(0).get("one"));
        assertEquals("3:3", "five", changedRecords.get(2).get("three"));
    }

    @Test
    @Ignore
    public void createSshTunnel() throws JSchException {
        // sshUserid, remoteHostForSshAndDatabase, targetSshPort
        Deencapsulation.setField(databaseConnector, "sshUserid", "laptop");
        Deencapsulation.setField(databaseConnector, "sshPassword", "caherline");
        Deencapsulation.setField(databaseConnector, "remoteHostForSshAndDatabase", "localhost");
        Deencapsulation.setField(databaseConnector, "targetSshPort", 22);
        databaseConnector.createSshTunnel();
    }

}