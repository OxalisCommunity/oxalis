package eu.peppol.statistics.dao;

import eu.peppol.start.identifier.ParticipantId;
import eu.peppol.statistics.CacheWrapper;

import java.sql.Connection;

/**
 * @author steinar
 *         Date: 08.04.13
 *         Time: 14:25
 */
class ParticipantIdDimensionDao extends GenericDao<ParticipantId, Integer> {

    protected ParticipantIdDimensionDao(CacheWrapper<ParticipantId, Integer> cache) {
        super(cache);
    }

    @Override
    protected Integer insert(Connection con, ParticipantId id) {
        checkConnection(con);

        String sql = "insert into ppid_dimension (ppid) values(?)";
        return insertAndReturnGeneratedKey(con, sql, id.stringValue());
    }

    @Override
    protected Integer findById(Connection con, ParticipantId id) {

        String sql = "select ppid_id from ppid_dimension where ppid=?";
        return selectForeignFor(con, sql, id.stringValue());
    }
}
