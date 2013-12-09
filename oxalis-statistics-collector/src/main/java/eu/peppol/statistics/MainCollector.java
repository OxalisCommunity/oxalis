package eu.peppol.statistics;

import eu.peppol.statistics.repository.DownloadRepository;
import eu.peppol.util.OxalisVersion;
import joptsimple.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * <pre>
 *     --download --repository _dir_name_ --ap _file_of_access_points
 *     --process --repository _dir_name_
 * </pre>
 *
 * @author steinar
 *         Date: 21.03.13
 *         Time: 08:48
 */
public class MainCollector {


    public static final String AP_LIST_OPTION = "ap";

    public static void main(String[] args) {

        OptionParser optionParser = createOptionsParser();
        OptionSet optionSet = optionParser.parse(args);

        if (optionSet.specs().size() == 0) {
            printUsage(optionParser);
        } else {

            DownloadRepository downloadRepository = getDownloadRepository(optionSet);

            // Locates and loads the list of access points to contact
            AccessPointMetaDataCollection accessPointMetaDataCollection = getAccessPointMetaDataCollection(optionSet);

            if (optionSet.has("download")) {

                System.out.println("Downloading from " + accessPointMetaDataCollection.getAccessPointMetaDataList().size() + " access points .....");
                download(downloadRepository, accessPointMetaDataCollection);

            } else if (optionSet.has("process")) {

                System.out.println("Importing and processing aggregated statistics from " + downloadRepository.getRootDirectory().getAbsolutePath());

                loadProcessAndSave(downloadRepository);
            } else {
                printUsage(optionParser);
            }
        }
    }

    private static void loadProcessAndSave(DownloadRepository downloadRepository) {
        StatisticsImporter statisticsImporter = new StatisticsImporter(downloadRepository);
        List<StatisticsImporter.ImportResult> importResults = statisticsImporter.loadSaveAndArchive();
        for (StatisticsImporter.ImportResult importResult : importResults) {

            System.out.printf("%-10s %s %s\n", importResult.getResultCode(), importResult.getRepositoryEntry().getContentsFile(),
                    importResult.getResultCode() == StatisticsImporter.ImportResult.ResultCode.FAILED ? importResult.getMessage() + ", cause=" + importResult.getCause() : "");
            if (importResult.getCause() != null) {
                importResult.getCause().printStackTrace(System.err);
            }
        }
    }

    private static void download(DownloadRepository downloadRepository, AccessPointMetaDataCollection accessPointMetaDataCollection) {
        StatisticsDownloader statisticsDownloader = new StatisticsDownloader(downloadRepository);


        // Performs the actual download
        List<DownloadResult> downloadResults = statisticsDownloader.download(accessPointMetaDataCollection.getAccessPointMetaDataList());

        // Reports the results
        for (DownloadResult downloadResult : downloadResults) {
            System.out.printf("%-20s %-130s %5dms %4d %s \n",
                    downloadResult.getAccessPointIdentifier(),
                    downloadResult.getDownloadUrl(),
                    downloadResult.getElapsedTimeInMillis(),
                    downloadResult.getHttpResultCode() != null ? downloadResult.getHttpResultCode() : -1,
                    downloadResult.getTaskFailureCause() == null ? "OK" : downloadResult.getTaskFailureCause().getMessage());
        }
    }

    private static AccessPointMetaDataCollection getAccessPointMetaDataCollection(OptionSet optionSet) {
        AccessPointMetaDataCollection accessPointMetaDataCollection = null;
        if (optionSet.has(AP_LIST_OPTION)) {
            File apList = (File) optionSet.valueOf(AP_LIST_OPTION);
            accessPointMetaDataCollection = new AccessPointMetaDataCollection(apList);
        } else {
            accessPointMetaDataCollection = AccessPointMetaDataCollection.loadIncludedListOfAccessPoints();
        }
        return accessPointMetaDataCollection;
    }

    private static DownloadRepository getDownloadRepository(OptionSet optionSet) {
        // Determines the location of the file based repository
        File repository = (File) optionSet.valueOf("repository");
        return new DownloadRepository(repository);
    }

    private static void printUsage(OptionParser optionParser) {
        try {

            System.err.println("oxalis-collector: " + OxalisVersion.getVersion());
            System.err.println();

            System.err.printf("Downloads and processes statistics from designated PEPPOL Access Points\n\n");
            System.err.printf("Typical command line options: --download --repository /myrepository\n\n");
            System.err.printf("A complete list of options:\n\n");

            optionParser.printHelpOn(System.err);
        } catch (IOException e) {
            // Not much more we can do
            throw new IllegalStateException("Unable to print usage information ", e);
        }
    }

    static OptionParser createOptionsParser() {

        OptionParser parser = new OptionParser();

        parser.accepts("download", "Downloads statistics from all access points");
        parser.accepts(AP_LIST_OPTION, "List of PEPPOL Access Points to download content from ").withRequiredArg().ofType(File.class);
        parser.accepts("process", "Processes the downloaded contents");

        parser.acceptsAll(Arrays.asList("help","h","?")).forHelp();

        parser.accepts("repository", "Directory holding the file based repository of downloaded contents")
                .requiredIf("download")
                .requiredIf("process")
                .withRequiredArg().ofType(File.class);

        return parser;
    }

}
