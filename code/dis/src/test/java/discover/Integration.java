package discover;

import com.jcraft.jsch.JSchException;
import discover.database.IDataBase;
import discover.database.model.Analysis;
import discover.search.Searcher;
import discover.tool.LOGGING;
import discover.tool.THREAD;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-07-2015
 */
@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings("SpringContextConfigurationInspection")
@ContextConfiguration(locations = {"file:src/main/resources/experimental/spring.xml"})
public class Integration {

    static {
        LOGGING.configure();
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private boolean shuttingDown;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private Searcher searcher;

    @Autowired
    @Qualifier("discover.database.IDataBase")
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private IDataBase dataBase;

    @Before
    public void before() {
        THREAD.initialize();
        dataBase.removeBatch(dataBase.find(Analysis.class, 0, Integer.MAX_VALUE));
    }

    @After
    public void after() {
        shuttingDown = Boolean.TRUE;
        dataBase.removeBatch(dataBase.find(Analysis.class, 0, Integer.MAX_VALUE));
    }

    @Test
    public void process() throws SQLException, JSchException {
        addRules();
        addSearches();
        THREAD.sleep(1000 * 600);
    }

    private void addSearches() {
        class Searcher implements Runnable {
            public void run() {
                THREAD.sleep(15000);
                do {
                    Analysis rule = dataBase.find(Analysis.class, 0, 1).get(0);
                    String id = Double.toString(rule.getId());
                    ArrayList<HashMap<String, String>> results = searcher.doSearch("ID", id);
                    logger.info("Results : " + results.size());
                    Assert.assertEquals("Must be two results, the hit and the statistics : ", 2, results.size());
                    THREAD.sleep(10);
                } while (!shuttingDown);
            }
        }
        THREAD.submit("searcher", new Searcher());
    }

    private void addRules() {
        class Persister implements Runnable {
            public void run() {
                long count;
                Random random = new Random();
                do {
                    int insertsPerSecond = random.nextInt(1000);
                    long start = System.currentTimeMillis();
                    ArrayList<Analysis> rules = new ArrayList<>();
                    for (int i = 0; i < insertsPerSecond; i++) {
                        Analysis rule = new Analysis();
                        rule.setAction("action");
                        rule.setIndexContext("index-context");
                        rule.setPredicate("predicate");
                        rule.setServer("192.168.1.40");
                        rule.setTimestamp(new Timestamp(System.currentTimeMillis()));
                        rules.add(rule);
                    }

                    logger.info("Persisting batch {}", rules.size());
                    dataBase.persistBatch(rules);

                    count = dataBase.count(Analysis.class);
                    long sleep = Math.abs((start + 1000) - System.currentTimeMillis());
                    if (count % 1000 == 0) {
                        logger.error("Rule count : {}, sleeping for : {}", count, sleep);
                    }
                    logger.info("Sleeping for {}", sleep);
                    THREAD.sleep(sleep);
                } while (!shuttingDown);
            }
        }
        THREAD.submit("rule-persister", new Persister());
    }

}