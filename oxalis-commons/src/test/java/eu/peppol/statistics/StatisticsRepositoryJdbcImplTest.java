package eu.peppol.statistics;

import eu.peppol.start.identifier.*;
import org.easymock.EasyMock;
import static  org.easymock.EasyMock.*;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.sql.DataSource;

import java.sql.*;
import java.util.Date;

import static org.testng.Assert.assertTrue;

/**
 * User: steinar
 * Date: 08.02.13
 * Time: 14:11
 */
public class StatisticsRepositoryJdbcImplTest {

    private DataSource dataSource;


    @Test
    public void testPersist() throws Exception {

        StatisticsRepository repository = new StatisticsRepositoryJdbcImpl(createMockDataSource());

        RawStatistics rawStatistics = new RawStatistics.Builder()
                .accessPointIdentifier(new AccessPointIdentifier("AP_SendRegning"))
                .OUT()
                .sender(new ParticipantId("9908:810017902"))
                .receiver(new ParticipantId("9908:810017902"))
                .channel(new ChannelId("CH01"))
                .documentType(PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier())
                .profile(PeppolProcessTypeIdAcronym.INVOICE_ONLY.getPeppolProcessTypeId())
                .build();
        repository.persist(rawStatistics);
    }


    DataSource createMockDataSource() throws SQLException {
        DataSource ds = EasyMock.createMock(DataSource.class);
        Connection con = EasyMock.createMock(Connection.class);
        PreparedStatement ps = createMock(PreparedStatement.class);
        ResultSet rs = createMock(ResultSet.class);

        // Create connection
        expect(ds.getConnection()).andReturn(con);
        con.setAutoCommit(true);
        // Prepare statement
        expect(con.prepareStatement(isA(String.class), eq(Statement.RETURN_GENERATED_KEYS))).andReturn(ps);
        ps.setString(eq(1), isA(String.class));
        // Set date parameter of query
        ps.setTimestamp(eq(2), isA(java.sql.Timestamp.class));

        // Set all the other parameters of the query
        ps.setString(gt(1), isA(String.class));
        expectLastCall().times(6);

        // Execute the insert
        expect(ps.executeUpdate()).andReturn(1);

        // Get generated keys
        expect(ps.getGeneratedKeys()).andReturn(rs);
        expect(rs.next()).andReturn(true);
        expect(rs.getInt(1)).andReturn(42);

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
