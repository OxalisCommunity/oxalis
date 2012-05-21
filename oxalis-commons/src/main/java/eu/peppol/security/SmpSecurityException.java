/* Created by steinar on 14.05.12 at 00:46 */
package eu.peppol.security;

/**
 * Thrown when something goes wrong during the verification of a SMP response.
 *
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class SmpSecurityException extends RuntimeException {


    public SmpSecurityException(String msg) {
        super(msg);
    }
}
