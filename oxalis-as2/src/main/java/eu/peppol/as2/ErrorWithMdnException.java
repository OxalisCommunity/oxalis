package eu.peppol.as2;

/**
 * Thrown when an error implies that a disposition type of "failed" should be returned rather
 * than "processed".
 *
 * @author steinar
 *         Date: 20.10.13
 *         Time: 11:36
 */
public class ErrorWithMdnException extends Exception {
    private final MdnData mdnData;

    public ErrorWithMdnException(MdnData mdnData) {

        // The error message is contained in the disposition modifier.
        super(mdnData.getAs2Disposition().getDispositionModifier().toString());
        this.mdnData = mdnData;
    }

    public ErrorWithMdnException(MdnData mdnData, Exception e) {
        super(mdnData.getAs2Disposition().getDispositionModifier().toString(), e);
        this.mdnData = mdnData;
    }

    public MdnData getMdnData() {
        return mdnData;
    }
}
