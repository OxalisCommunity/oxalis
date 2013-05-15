package jooq;

import eu.peppol.jdbc.OxalisDataSourceFactoryProvider;
import org.jooq.*;
import org.jooq.util.mysql.MySQLDSL;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.sql.DataSource;

import static org.jooq.impl.DSL.fieldByName;
import static org.jooq.impl.DSL.tableByName;

/**
 * @author steinar
 *         Date: 22.04.13
 *         Time: 15:16
 */
@Test(groups={"integration"})
public class JooqTest {

    private DataSource dataSource;

    @BeforeMethod
    public void loadDataSource() {
        dataSource = OxalisDataSourceFactoryProvider.getInstance().getDataSource();
    }

    @Test
    public void testSqmpleSQL() throws Exception {
        DSLContext dslContext = MySQLDSL.using(dataSource, SQLDialect.MYSQL);

        final SelectOnConditionStep<Record16<java.sql.Date, Integer, Integer, Integer, Integer, String, String, String, String, String, String, String, String, String, String, Integer>> selectJoinStep = (SelectOnConditionStep<Record16<java.sql.Date, Integer, Integer, Integer, Integer, String, String, String, String, String, String, String, String, String, String, Integer>>) dslContext.select(
                fieldByName(java.sql.Date.class,"datum"),
                fieldByName(Integer.class,"year"),
                fieldByName(Integer.class,"month"),
                fieldByName(Integer.class,"day"),
                fieldByName(Integer.class,"hour"),
                fieldByName(String.class,"ap_code"),
                fieldByName(String.class, "ppid"),
                fieldByName(String.class,"document_type"),
                fieldByName(String.class, "localname"),
                fieldByName(String.class,"root_name_space"),
                fieldByName(String.class, "customization"),
                fieldByName(String.class, "version"),
                fieldByName(String.class,"profile"),
                fieldByName(String.class,"channel"),
                fieldByName(String.class,"direction"),
                fieldByName(Integer.class,"counter"))
                .from(tableByName("message_fact"))
                .naturalJoin(tableByName("time_dimension"))
                .naturalJoin(tableByName("ap_dimension"))
                .naturalJoin(tableByName("ppid_dimension"))
                .naturalJoin(tableByName("document_dimension"))
                .leftOuterJoin(tableByName("profile_dimension"))
                    .on(fieldByName("profile_dimension", "profile_id").equal(fieldByName("message_fact", "profile_id")))
                .leftOuterJoin(tableByName("channel_dimension"))
                    .on(fieldByName("channel_dimension", "channel_id").equal(fieldByName("message_fact", "channel_id")))
                .where(fieldByName("datum").ge("2013-01-01").and(fieldByName("datum").le("2013-04-23")))
                ;

        String sql = selectJoinStep.getSQL();
        System.err.println("SQL:" + sql);

        Result<?> records = selectJoinStep.fetch();
        for (Record record : records) {
            Field<?> id = record.field(1);
            System.err.println("ID:" + record.getValue("datum"));
        }
    }
}
