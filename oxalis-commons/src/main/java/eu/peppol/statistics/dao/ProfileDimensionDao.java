package eu.peppol.statistics.dao;

import eu.peppol.start.identifier.PeppolProcessTypeId;
import eu.peppol.statistics.CacheWrapper;

import java.sql.Connection;

/**
 * @author steinar
 *         Date: 08.04.13
 *         Time: 15:02
 */
class ProfileDimensionDao extends GenericDao<PeppolProcessTypeId,Integer> {

    protected ProfileDimensionDao(CacheWrapper<PeppolProcessTypeId, Integer> cache) {
        super(cache);
    }

    @Override
    protected Integer insert(Connection con, PeppolProcessTypeId id) {

        String sql = "insert into profile_dimension (profile) values(?)";
        return insertAndReturnGeneratedKey(con, sql, id.toString());
    }

    @Override
    protected Integer findById(Connection con, PeppolProcessTypeId id) {
        String sql = "select profile_id from profile_dimension where profile=?";
        return selectForeignFor(con, sql, id.toString());
    }
}
