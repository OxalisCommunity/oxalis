package eu.peppol.statistics;

import eu.peppol.start.identifier.AccessPointIdentifier;

import java.net.URL;

/**
 * Holds meta data information for a single access point.
 *
 * User: steinar
 * Date: 07.03.13
 * Time: 14:22
 */
public class AccessPointMetaData {

    private final AccessPointIdentifier accessPointIdentifier;
    private final String companyName;
    private final String orgNo;
    private final String description;
    private final URL url;

    public AccessPointMetaData(AccessPointIdentifier accessPointIdentifier, String companyName, String orgNo, String description, URL url) {
        if (accessPointIdentifier == null) {
            throw new IllegalArgumentException("All access points must be identified with a unique identifier");
        }
        this.accessPointIdentifier = accessPointIdentifier;
        this.companyName = companyName;
        this.orgNo = orgNo;
        this.description = description;
        this.url = url;
    }

    public AccessPointIdentifier getAccessPointIdentifier() {
        return accessPointIdentifier;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getOrgNo() {
        return orgNo;
    }

    public String getDescription() {
        return description;
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "AccessPointMetaData{" +
                "companyName='" + companyName + '\'' +
                ", orgNo='" + orgNo + '\'' +
                ", description='" + description + '\'' +
                ", url=" + url +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccessPointMetaData that = (AccessPointMetaData) o;

        if (companyName != null ? !companyName.equals(that.companyName) : that.companyName != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (orgNo != null ? !orgNo.equals(that.orgNo) : that.orgNo != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = companyName != null ? companyName.hashCode() : 0;
        result = 31 * result + (orgNo != null ? orgNo.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }
}
