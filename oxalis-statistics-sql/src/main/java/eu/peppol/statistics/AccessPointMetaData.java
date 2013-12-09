package eu.peppol.statistics;

import eu.peppol.identifier.AccessPointIdentifier;

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
    private final URL accessPointServiceUrl;
    private final URL statisticsUrl;

    public AccessPointMetaData(AccessPointIdentifier accessPointIdentifier, String companyName, String orgNo, String description, URL accessPointServiceUrl, URL statisticsUrl) {
        if (accessPointIdentifier == null) {
            throw new IllegalArgumentException("All access points must be identified with a unique identifier");
        }
        this.accessPointIdentifier = accessPointIdentifier;
        this.companyName = companyName;
        this.orgNo = orgNo;
        this.description = description;
        this.accessPointServiceUrl = accessPointServiceUrl;
        this.statisticsUrl = statisticsUrl;
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

    public URL getAccessPointServiceUrl() {
        return accessPointServiceUrl;
    }

    public URL getStatisticsUrl() {
        return statisticsUrl;
    }

    @Override
    public String toString() {
        return "AccessPointMetaData{" +
                "accessPointIdentifier=" + accessPointIdentifier +
                ", companyName='" + companyName + '\'' +
                ", orgNo='" + orgNo + '\'' +
                ", description='" + description + '\'' +
                ", accessPointServiceUrl=" + accessPointServiceUrl +
                ", statisticsUrl=" + statisticsUrl +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccessPointMetaData that = (AccessPointMetaData) o;

        if (!accessPointIdentifier.equals(that.accessPointIdentifier)) return false;
        if (accessPointServiceUrl != null ? !accessPointServiceUrl.equals(that.accessPointServiceUrl) : that.accessPointServiceUrl != null)
            return false;
        if (companyName != null ? !companyName.equals(that.companyName) : that.companyName != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (orgNo != null ? !orgNo.equals(that.orgNo) : that.orgNo != null) return false;
        if (statisticsUrl != null ? !statisticsUrl.equals(that.statisticsUrl) : that.statisticsUrl != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = accessPointIdentifier.hashCode();
        result = 31 * result + (companyName != null ? companyName.hashCode() : 0);
        result = 31 * result + (orgNo != null ? orgNo.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (accessPointServiceUrl != null ? accessPointServiceUrl.hashCode() : 0);
        result = 31 * result + (statisticsUrl != null ? statisticsUrl.hashCode() : 0);
        return result;
    }
}
