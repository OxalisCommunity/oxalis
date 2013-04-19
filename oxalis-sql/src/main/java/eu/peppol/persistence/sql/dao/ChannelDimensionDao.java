package eu.peppol.persistence.sql.dao;

import eu.peppol.start.identifier.ChannelId;
import eu.peppol.statistics.CacheWrapper;

import java.sql.Connection;

/**
 * @author steinar
 *         Date: 08.04.13
 *         Time: 14:53
 */
class ChannelDimensionDao extends GenericDao<ChannelId, Integer> {

    protected ChannelDimensionDao(CacheWrapper<ChannelId, Integer> cache) {
        super(cache);
    }

    @Override
    protected Integer insert(Connection con, ChannelId id) {
        checkConnection(con);

        String sql = "insert into channel_dimension (channel) values(?)";
        return insertAndReturnGeneratedKey(con, sql, id.stringValue());
    }

    @Override
    protected Integer findById(Connection con, ChannelId id) {

        String sql = "select channel_id from channel_dimension where channel=?";
        return selectForeignFor(con, sql, id.stringValue());
    }
}
