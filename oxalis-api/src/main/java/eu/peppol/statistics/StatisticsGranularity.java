package eu.peppol.statistics;

/**
 * Represents granularity of statistics data.
 *
 * @author steinar
 *         Date: 26.03.13
 *         Time: 09:17
 */
public enum StatisticsGranularity {

    YEAR("Y"),MONTH("M"), DAY("D"), HOUR("H");

    private final String abbreviation;

    StatisticsGranularity(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public static StatisticsGranularity valueForAbbreviation(String abbreviation) {
        if (abbreviation == null) {
            throw new IllegalArgumentException("null string is an invalid abbreviation for statistics granularity");
        }

        for (StatisticsGranularity granularity : values()) {
            if (granularity.abbreviation.equalsIgnoreCase(abbreviation)) {
                return granularity;
            }
        }

        throw new IllegalArgumentException("Invalid abbreviation for statistics granularity: " + abbreviation);
    }
}
