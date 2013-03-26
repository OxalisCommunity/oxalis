package eu.peppol.statistics;

import eu.peppol.statistics.repository.DownloadRepository;
import eu.peppol.statistics.repository.RepositoryEntry;

import java.util.Collection;

/**
 * Parses an XML document into an object graph, which is persisted into a
 * data warehouse snow flake scheme.
 *
 * @author steinar
 *         Date: 22.03.13
 *         Time: 17:50
 */
public class StatisticsImporter {

    private final DownloadRepository downloadRepository;

    public StatisticsImporter(DownloadRepository downloadRepository) {

        this.downloadRepository = downloadRepository;
    }

    public void loadSaveAndArchive() {

        // Iterates the downloaded contents
        Collection<RepositoryEntry> repositoryEntries = downloadRepository.listDownloadedData();
        for (RepositoryEntry repositoryEntry : repositoryEntries) {

            // Parse into list of object graphs


            // Save each object graph into the database

            // Archive contents
        }
    }
}
