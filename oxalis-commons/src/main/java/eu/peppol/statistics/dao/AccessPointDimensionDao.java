package eu.peppol.statistics.dao;

import eu.peppol.start.identifier.AccessPointIdentifier;
import eu.peppol.statistics.CacheWrapper;

import java.sql.Connection;

/**
 * @author steinar
 *         Date: 08.04.13
 *         Time: 11:32
 */
class AccessPointDimensionDao extends GenericDao<AccessPointIdentifier, Integer>{

    protected AccessPointDimensionDao(CacheWrapper<AccessPointIdentifier, Integer> cache) {
        super(cache);
    }

    @Override
    protected Integer insert(Connection con, AccessPointIdentifier accessPointIdentifier) {

        String sql = "insert into ap_dimension (ap_code) values(?)";
        return insertAndReturnGeneratedKey(con, sql, accessPointIdentifier.toString());
    }

    @Override
    protected Integer findById(Connection con, AccessPointIdentifier id) {
        String sql = "select ap_id from ap_dimension where ap_code=?";
        return selectForeignFor(con, sql, id.toString());
    }

}
