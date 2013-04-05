package eu.peppol.statistics;

import eu.peppol.start.identifier.AccessPointIdentifier;

import java.net.URL;

/**
 * @author steinar
 *         Date: 12.03.13
 *         Time: 10:32
 */
public class DownloadResult {
    private final AccessPointIdentifier accessPointIdentifier;
    private final URL downloadUrl;
    private Integer httpResultCode = null;
    private Exception taskFailureCause = null;
    private long elapsedTimeInMillis;

    public DownloadResult(AccessPointIdentifier accessPointIdentifier, URL downloadUrl) {

        this.accessPointIdentifier = accessPointIdentifier;
        this.downloadUrl = downloadUrl;
    }

    public URL getDownloadUrl() {
        return downloadUrl;
    }

    public void setHttpResultCode(Integer contents) {
        this.httpResultCode = contents;
    }

    public Integer getHttpResultCode() {
        return httpResultCode;
    }

    public void setTaskFailureCause(Exception taskFailureCause) {
        this.taskFailureCause = taskFailureCause;
    }

    public AccessPointIdentifier getAccessPointIdentifier() {
        return accessPointIdentifier;
    }


    public Exception getTaskFailureCause() {
        return taskFailureCause;
    }

    @Override
    public String toString() {
        return "DownloadResult{" +
                "accessPointIdentifier=" + accessPointIdentifier +
                ", httpResultCode=" + httpResultCode +
                ", taskFailureCause=" + taskFailureCause +
                '}';
    }

    public void setElapsedTimeInMillis(long elapsedTimeInMillis) {
        this.elapsedTimeInMillis = elapsedTimeInMillis;
    }

    public long getElapsedTimeInMillis() {
        return elapsedTimeInMillis;
    }
}
