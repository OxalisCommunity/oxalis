package eu.peppol.persistence.sql;

import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.PeppolProcessTypeIdAcronym;
import eu.peppol.start.identifier.*;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.StatisticsGranularity;
import org.easymock.EasyMock;

import javax.sql.DataSource;

import java.sql.*;

import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 26.03.13
 *         Time: 10:38
 */
public class StatisticsRepositoryMySqlImplMockTest {
  @Test
    public void testPersist() throws Exception {

        RawStatisticsRepository repository = new RawStatisticsRepositoryMySqlImpl(createMockDataSource());

        RawStatistics rawStatistics = new RawStatistics.RawStatisticsBuilder()
                .accessPointIdentifier(new AccessPointIdentifier("AP_SendRegning"))
                .outbound()
                .sender(new ParticipantId("9908:810017902"))
                .receiver(new ParticipantId("9908:810017902"))
                .channel(new ChannelId("CH01"))
                .documentType(PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier())
                .profile(PeppolProcessTypeIdAcronym.INVOICE_ONLY.getPeppolProcessTypeId())
                .build();
        repository.persist(rawStatistics);
    }

	@Test
    public void testCreateSqlQueryText() throws Exception {
		RawStatisticsRepositoryMySqlImpl repository = new RawStatisticsRepositoryMySqlImpl(createMockDataSource());
        String s = repository.getRawStatisticsSqlQueryText(StatisticsGranularity.HOUR);
    }

    @Test
    public void testMySqlDateFormatYear() throws Exception {
        String s = RawStatisticsRepositoryMySqlImpl.mySqlDateFormat(StatisticsGranularity.YEAR);
        assertEquals(s, "%Y");
    }

    @Test
    public void testMySqlDateFormatMonth() throws Exception {
        String s = RawStatisticsRepositoryMySqlImpl.mySqlDateFormat(StatisticsGranularity.MONTH);
        assertEquals(s, "%Y-%m");
    }

    @Test
    public void testMySqlDateFormatDay() throws Exception {
        String s = RawStatisticsRepositoryMySqlImpl.mySqlDateFormat(StatisticsGranularity.DAY);
        assertEquals(s, "%Y-%m-%d");
    }

    @Test
    public void testMySqlDateFormatHour() throws Exception {
        String s = RawStatisticsRepositoryMySqlImpl.mySqlDateFormat(StatisticsGranularity.HOUR);
        assertEquals(s, "%Y-%m-%dT%h");
    }

    DataSource createMockDataSource() throws SQLException {
        DataSource ds = EasyMock.createMock(DataSource.class);
        Connection con = EasyMock.createMock(Connection.class);
        PreparedStatement ps = EasyMock.createMock(PreparedStatement.class);
        ResultSet rs = EasyMock.createMock(ResultSet.class);

        // Create connection
        EasyMock.expect(ds.getConnection()).andReturn(con);
        con.setAutoCommit(true);
        // Prepare statement
        EasyMock.expect(con.prepareStatement(EasyMock.isA(String.class), EasyMock.eq(Statement.RETURN_GENERATED_KEYS))).andReturn(ps);
        ps.setString(EasyMock.eq(1), EasyMock.isA(String.class));
        // Set date parameter of query
        ps.setTimestamp(EasyMock.eq(2), EasyMock.isA(java.sql.Timestamp.class));

        // Set all the other parameters of the query
        ps.setString(EasyMock.gt(1), EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(6);

        // Execute the insert
        EasyMock.expect(ps.executeUpdate()).andReturn(1);

        // Get generated keys
        EasyMock.expect(ps.getGeneratedKeys()).andReturn(rs);
        EasyMock.expect(rs.next()).andReturn(true);
        EasyMock.expect(rs.getInt(1)).andReturn(42);

        // Close result set
        rs.close();

        // Commit
        con.commit();

        // Close the connection
        con.close();

        EasyMock.replay(ds,con,ps,rs);
        return ds;
    }	
}
