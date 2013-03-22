package eu.peppol.statistics;

/**
 * @author steinar
 *         Date: 12.03.13
 *         Time: 10:32
 */
public class DownloadResult {
    private final AccessPointMetaData accessPointMetaData;
    private String downloadedContents;
    private Exception taskFailureCause;

    public DownloadResult(AccessPointMetaData accessPointMetaData) {

        this.accessPointMetaData = accessPointMetaData;
    }

    public void setDownloadedContents(String contents) {
        this.downloadedContents = contents;
    }

    public String getDownloadedContents() {
        return downloadedContents;
    }

    public void setTaskFailureCause(Exception taskFailureCause) {
        this.taskFailureCause = taskFailureCause;
    }

    public AccessPointMetaData getAccessPointMetaData() {
        return accessPointMetaData;
    }

    public Exception getTaskFailureCause() {
        return taskFailureCause;
    }

    @Override
    public String toString() {
        return "DownloadResult{" +
                "accessPointMetaData=" + accessPointMetaData +
                ", downloadedContents='" + downloadedContents + '\'' +
                ", taskFailureCause=" + taskFailureCause +
                '}';
    }
}
