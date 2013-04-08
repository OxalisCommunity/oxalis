package eu.peppol.statistics.dao;

import eu.peppol.statistics.CacheWrapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

/**
 * @author steinar
 *         Date: 08.04.13
 *         Time: 16:22
 */
class TimeDimensionDao extends GenericDao<Date, Integer> {

    protected TimeDimensionDao(CacheWrapper<Date, Integer> cache) {
        super(cache);
    }

    @Override
    protected Integer insert(Connection con, Date date) {
        checkConnection(con);

        String sql = "insert into time_dimension(datum, year, month, day, hour) values(?,?,?,?,?)";

        PreparedStatement ps = null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        try {
            ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setDate(1, new java.sql.Date(date.getTime()));
            ps.setInt(2, cal.get(Calendar.YEAR));
            ps.setInt(3, cal.get(Calendar.MONTH));
            ps.setInt(4, cal.get(Calendar.DAY_OF_MONTH));
            ps.setInt(5, cal.get(Calendar.HOUR_OF_DAY));
            int rc = ps.executeUpdate();
            return fetchGeneratedKey(sql, ps);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to execute " + sql + " with param " + date);
        }
    }

    @Override
    protected Integer findById(Connection con, Date date) {
        String sql = "select time_id from time_dimension where datum=?";

        checkConnection(con);

        PreparedStatement ps = null;

        ps = prepare(con, sql);
        try {
            ps.setDate(1, new java.sql.Date(date.getTime()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Integer(rs.getInt(1));
            } else return null;
        } catch (SQLException e) {
            throw new IllegalStateException("Error during execution of " + sql + ", with param " + date, e);
        }
    }
}
