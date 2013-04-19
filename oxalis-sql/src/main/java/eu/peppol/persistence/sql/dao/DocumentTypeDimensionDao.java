package eu.peppol.persistence.sql.dao;

import eu.peppol.persistence.sql.CacheWrapper;
import eu.peppol.start.identifier.PeppolDocumentTypeId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author steinar
 *         Date: 08.04.13
 *         Time: 14:42
 */
class DocumentTypeDimensionDao extends GenericDao<PeppolDocumentTypeId, Integer> {

    protected DocumentTypeDimensionDao(CacheWrapper<PeppolDocumentTypeId, Integer> cache) {
        super(cache);
    }

    @Override
    protected Integer insert(Connection con, PeppolDocumentTypeId id) {
        checkConnection(con);
        String sql = "insert into document_dimension (document_type, localname, root_name_space, customization, version) values(?,?,?,?,?)";
        PreparedStatement ps = null;

        try {
            ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, id.toString());
            ps.setString(2, id.getLocalName());
            ps.setString(3, id.getRootNameSpace());
            ps.setString(4, id.getCustomizationIdentifier().toString());
            ps.setString(5, id.getVersion());
            int rc = ps.executeUpdate();
            return fetchGeneratedKey(sql, ps);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to execute " + sql + " with param " + id, e);
        }

    }

    @Override
    protected Integer findById(Connection con, PeppolDocumentTypeId id) {
        String sql = "select document_id from document_dimension where document_type=?";

        return selectForeignFor(con, sql, id.toString());
    }
}
