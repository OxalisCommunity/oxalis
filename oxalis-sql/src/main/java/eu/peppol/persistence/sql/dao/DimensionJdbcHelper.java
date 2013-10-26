package eu.peppol.persistence.sql.dao;

import eu.peppol.identifier.ParticipantId;
import eu.peppol.persistence.sql.EhCacheWrapper;
import eu.peppol.start.identifier.*;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import java.sql.Connection;
import java.util.Date;

/**
 * Creates a cache for each of the DBMS foreign keys used in the statistics data warehouse. For each foreign key,
 * a DAO is created, injecting the appropriate cache for that foreign key.
 *
 * Furthermore the <code>getKeyFor()</code> methods serve as wrappers for the DAOs, providing the functionality of
 * looking up the foreign key value for the supplied key, before hitting the DBMS.
 *
 * @author steinar
 *         Date: 08.04.13
 *         Time: 10:49
 */
public enum DimensionJdbcHelper {

    SINGLETON;

    private final AccessPointDimensionDao accessPointDimensionDao;

    private final ParticipantIdDimensionDao participantIdDimensionDao;
    private final DocumentTypeDimensionDao documentTypeDimensionDao;
    private final ChannelDimensionDao channelDimensionDao;
    private final ProfileDimensionDao profileDimensionDao;
    private final TimeDimensionDao timeDimensionDao;
    private final CacheManager cacheManager;

    DimensionJdbcHelper() {
        // Prevents EhCache from performing automatic updates
        System.setProperty("net.sf.ehcache.skipUpdateCheck", "true");
        cacheManager = CacheManager.create();

        // AccessPointIdentifier dimension
        Ehcache accessPointCache = cacheManager.addCacheIfAbsent(AccessPointIdentifier.class.getSimpleName());
        accessPointDimensionDao = new AccessPointDimensionDao(new EhCacheWrapper<AccessPointIdentifier, Integer>(accessPointCache));

        Ehcache participantIdCache = cacheManager.addCacheIfAbsent(ParticipantId.class.getSimpleName());
        participantIdDimensionDao = new ParticipantIdDimensionDao(new EhCacheWrapper<ParticipantId, Integer>(participantIdCache));

        Ehcache documentTypeCache = cacheManager.addCacheIfAbsent(PeppolDocumentTypeId.class.getSimpleName());
        documentTypeDimensionDao = new DocumentTypeDimensionDao(new EhCacheWrapper<PeppolDocumentTypeId, Integer>(documentTypeCache));

        Ehcache channelCache = cacheManager.addCacheIfAbsent(ChannelId.class.getSimpleName());
        channelDimensionDao = new ChannelDimensionDao(new EhCacheWrapper<ChannelId, Integer>(channelCache));

        Ehcache profileCache = cacheManager.addCacheIfAbsent(PeppolProcessTypeId.class.getSimpleName());
        profileDimensionDao = new ProfileDimensionDao(new EhCacheWrapper<PeppolProcessTypeId, Integer>(profileCache));

        Ehcache timeCache = cacheManager.addCacheIfAbsent(Date.class.getSimpleName());
        timeDimensionDao = new TimeDimensionDao(new EhCacheWrapper<Date, Integer>(timeCache));
    }

    public Integer getKeyFor(Connection con, AccessPointIdentifier accessPointIdentifier) {
        return accessPointDimensionDao.foreignKeyValueFor(con, accessPointIdentifier);
    }

    public Integer getKeyFor(Connection con, ParticipantId participantId) {
        return participantIdDimensionDao.foreignKeyValueFor(con, participantId);
    }

    public Integer getKeyFor(Connection con, PeppolDocumentTypeId peppolDocumentTypeId) {
        return documentTypeDimensionDao.foreignKeyValueFor(con, peppolDocumentTypeId);
    }

    /**
     *
     * @param con
     * @param channelId optional, i.e. may be null
     * @return either the foreign key or null of the channelId supplied was null.
     */
    public Integer getKeyFor(Connection con, ChannelId channelId) {
        return channelDimensionDao.foreignKeyValueFor(con, channelId);
    }

    public Integer getKeyFor(Connection con, PeppolProcessTypeId peppolProcessTypeId) {
        return profileDimensionDao.foreignKeyValueFor(con, peppolProcessTypeId);
    }

    public Integer getKeyFor(Connection con, Date date) {
        return timeDimensionDao.foreignKeyValueFor(con, date);
    }

    /**
     * Closes the ehCache and shuts down any of it's threads.
     */
    public void closeCache() {
        cacheManager.shutdown();
    }
}
