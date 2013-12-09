package eu.peppol.persistence.sql.dao;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import eu.peppol.persistence.sql.CacheWrapper;
import eu.peppol.persistence.sql.RawStatisticsRepositoryJdbcImpl;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.util.GlobalConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 08.04.13
 *         Time: 11:51
 */

@Test(groups = {"integration"})
public class AccessPointDimensionDaoTest {

    private RawStatisticsRepositoryJdbcImpl instance;
    private MysqlDataSource dataSource;

    private Integer sequence = 1;
    private HashMap<AccessPointIdentifier,Integer> cache;
    private Connection connection;

    Integer hitRate = 0;
    private AccessPointDimensionDao accessPointDimensionDao;

    @BeforeTest(groups = {"integration"})
    public void setUp() {

        GlobalConfiguration configuration = GlobalConfiguration.getInstance();

        dataSource = new MysqlDataSource();
        dataSource.setURL(configuration.getJdbcConnectionURI());
        dataSource.setUser(configuration.getJdbcUsername());
        dataSource.setPassword(configuration.getJdbcPassword());
    }

    @BeforeMethod
    public void startTx() throws SQLException {
        connection = dataSource.getConnection();
        connection.setAutoCommit(false);

        cache = new HashMap<AccessPointIdentifier, Integer>();

        CacheWrapper<AccessPointIdentifier, Integer> cacheWrapper = createCache();
        accessPointDimensionDao = new AccessPointDimensionDao(cacheWrapper);
    }

    @Test(groups = {"integration"})
    public void testInsertAndFind() throws Exception {

        AccessPointIdentifier sampleApId = new AccessPointIdentifier("STEINAR-TEST");

        // Inserts new entry into database, cache is not affected
        Integer generatedPk = accessPointDimensionDao.insert(connection, sampleApId);
        assertNotNull(generatedPk);

        // Loads the newly inserted entry, cache is still not modified
        Integer id = accessPointDimensionDao.findById(connection, sampleApId);
        assertNotNull(id);

        assertEquals(generatedPk, id);

        // Attempts to load via cache, which will not work the first time
        Integer hitRateBefore = hitRate;
        Integer cachedValue = accessPointDimensionDao.foreignKeyValueFor(connection, sampleApId);

        assertEquals(cachedValue, id);

        assertEquals(hitRateBefore, hitRate);
    }


    @Test
    public void testFindViaCache() {

        // Insert new value, which should not have been found in the cache
        Integer hitRate1 = hitRate;
        Integer foundId = accessPointDimensionDao.foreignKeyValueFor(connection, AccessPointIdentifier.TEST);
        assertEquals(hitRate1, hitRate);    // No change expected yet

        Integer hitRate2 = hitRate;
        Integer foundId2 = accessPointDimensionDao.foreignKeyValueFor(connection, AccessPointIdentifier.TEST);
        assertEquals(hitRate, new Integer(hitRate2+1)); // A cache hit is expected
    }

    /**
     * Provides a simple miniscule implementation of a CacheWrapper, which is based upon a simple HashMap rather than
     * the full fledged EhCache.
     *
     * @return
     */
    private CacheWrapper<AccessPointIdentifier, Integer> createCache() {
        return new CacheWrapper<AccessPointIdentifier, Integer>() {

                @Override
                public void put(AccessPointIdentifier key, Integer value) {
                    cache.put(key, value);
                }

                @Override
                public Integer get(AccessPointIdentifier key) {
                    if (cache.containsKey(key)) {
                        hitRate++;
                    }
                    return cache.get(key);
                }


            };
    }

}
