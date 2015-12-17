/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.as2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an instance of an AS2 Disposition header to be used in an MDN
 * <pre>
 *           automatic-action/MDN-sent-automatically; processed/error: Unknown recipient
 * </pre>
 *
 * @author steinar
 *         Date: 09.10.13
 *         Time: 21:06
 */
public class As2Disposition {

    public static Pattern pattern = Pattern.compile("(?i)(manual-action|automatic-action)\\s*/\\s*(MDN-sent-automatically|MDN-sent-manually)\\s*;\\s*(processed|failed)\\s*(/\\s*(error|warning|failure)\\s*:\\s*(.*)){0,1}");

    ActionMode actionMode;
    SendingMode sendingMode;
    DispositionType dispositionType;            // processed | failed
    /**
     * Optional. If present, a warning or an error was issued
     */
    DispositionModifier dispositionModifier;

    public ActionMode getActionMode() {
        return actionMode;
    }

    public SendingMode getSendingMode() {
        return sendingMode;
    }

    public DispositionType getDispositionType() {
        return dispositionType;
    }

    public DispositionModifier getDispositionModifier() {
        return dispositionModifier;
    }

    public As2Disposition(ActionMode actionMode, SendingMode sendingMode, DispositionType dispositionType) {
        this.actionMode = actionMode;
        this.sendingMode = sendingMode;
        this.dispositionType = dispositionType;
    }

    public As2Disposition(ActionMode actionMode, SendingMode sendingMode, DispositionType dispositionType, DispositionModifier dispositionModifier) {
        this.actionMode = actionMode;
        this.sendingMode = sendingMode;
        this.dispositionType = dispositionType;

        // Only processed/error or processed/warning is allowed
        if (dispositionType == DispositionType.PROCESSED && (dispositionModifier.prefix == DispositionModifier.Prefix.FAILURE)) {
            throw new IllegalArgumentException("DispositionType 'processed' does not allow a prefix of 'failed'. Only 'error' and 'warning' are allowed" );
        }
        this.dispositionModifier = dispositionModifier;
    }

    public static As2Disposition processed() {
        return new As2Disposition(ActionMode.AUTOMATIC, SendingMode.AUTOMATIC, DispositionType.PROCESSED);
    }

    public static As2Disposition processedWithWarning(String warningMessage) {
        return new As2Disposition(ActionMode.AUTOMATIC, SendingMode.AUTOMATIC, DispositionType.PROCESSED, DispositionModifier.warning(warningMessage));
    }

    public static As2Disposition processedWithError(String errorMessage) {
        return new As2Disposition(ActionMode.AUTOMATIC, SendingMode.AUTOMATIC, DispositionType.PROCESSED, DispositionModifier.error(errorMessage));
    }

    public static As2Disposition failed(String message) {
        return new As2Disposition(ActionMode.AUTOMATIC, SendingMode.AUTOMATIC,DispositionType.FAILED, DispositionModifier.failed(message));
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(actionMode.getTextValue() +"/"+ sendingMode.getTextValue() + "; " + dispositionType.getTextValue());
        if (dispositionModifier != null) {
            sb.append('/');
            sb.append(dispositionModifier.toString());
        }
        return sb.toString();
    }


    public static As2Disposition valueOf(String s) {
        if (s == null) s = "";
        s = s.trim();
        Matcher matcher = pattern.matcher(s);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("'" + s + "'" + " does not match pattern for As2Disposition");
        }

        String actionModeString = matcher.group(1);
        ActionMode actionMode = ActionMode.createFromTextValue(actionModeString);

        String sendingModeString = matcher.group(2);
        SendingMode sendingMode = SendingMode.createFromTextValue(sendingModeString);

        String dispositionTypeString = matcher.group(3);
        DispositionType dispositionType = DispositionType.valueOf(dispositionTypeString.toUpperCase());

        As2Disposition result;
        if (matcher.group(4) != null){
            DispositionModifier dispositionModifier;
            String dispositionModifierPrefixString = matcher.group(5);
            String dispositionModifierString = matcher.group(6);
            dispositionModifier = new DispositionModifier(DispositionModifier.Prefix.valueOf(dispositionModifierPrefixString.toUpperCase()), dispositionModifierString);
            result = new As2Disposition(actionMode,sendingMode, dispositionType, dispositionModifier);
        } else {
            result = new As2Disposition(actionMode,sendingMode, dispositionType);
        }

        return result;
    }

    public static enum ActionMode {
        MANUAL("manual-action"), AUTOMATIC("automatic-action");
        private final String textValue;

        ActionMode(String textValue) {

            this.textValue = textValue;
        }

        String getTextValue() {
            return textValue;
        }

        public static ActionMode createFromTextValue(String textValue) {
            for (ActionMode actionMode : values()) {
                if (actionMode.getTextValue().equalsIgnoreCase(textValue)) {
                    return actionMode;
                }
            }
            throw new IllegalArgumentException(textValue.toLowerCase() + " not recognised as a valid ActionMode");
        }

    }

    public static enum SendingMode {
        MANUAL("MDN-sent-manually"), AUTOMATIC("MDN-sent-automatically");
        private final String textValue;

        SendingMode(String textValue) {
            this.textValue = textValue;
        }

        public String getTextValue() {
            return textValue;
        }

        public static SendingMode createFromTextValue(String textValue) {
            for (SendingMode sendingMode : values()) {
                if (sendingMode.getTextValue().equalsIgnoreCase(textValue)) {
                    return sendingMode;
                }
            }
            throw new IllegalArgumentException(textValue.toLowerCase() + " not recognised as a valid ActionMode");
        }
    }

    public static enum DispositionType {
        PROCESSED("processed"), FAILED("failed");
        private final String textValue;

        DispositionType(String textValue) {
            this.textValue = textValue;
        }

        public String getTextValue() {
            return textValue;
        }

    }

    public static class DispositionModifier {

        public static enum Prefix {
            ERROR, WARNING, FAILURE;
        }

        private final Prefix prefix;
        private final String dispositionModifierExtension;

        DispositionModifier(Prefix prefix, String dispositionModifierExtension) {
            this.prefix = prefix;
            this.dispositionModifierExtension = dispositionModifierExtension;
        }

        public Prefix getPrefix() {
            return prefix;
        }

        public String getDispositionModifierExtension() {
            return dispositionModifierExtension;
        }

        public static DispositionModifier authenticationFailedError() {
            return new DispositionModifier(Prefix.ERROR, "authentication-failed");
        }

        public static DispositionModifier decompressionFailedError() {
            return new DispositionModifier(Prefix.ERROR, "decompression-failed");
        }

        public static DispositionModifier decryptionFailedError() {
            return new DispositionModifier(Prefix.ERROR, "decryption-failed");
        }

        public static DispositionModifier insufficientMessageSecurityError() {
            return new DispositionModifier(Prefix.ERROR, "insufficient-message-security");
        }

        public static DispositionModifier integrityCheckFailedError() {
            return new DispositionModifier(Prefix.ERROR, "integrity-check-failed");
        }

        public static DispositionModifier unexpectedProcessingError() {
            return new DispositionModifier(Prefix.ERROR, "unexpected-processing-error");
        }

        public static DispositionModifier warning(String description) {
            return new DispositionModifier(Prefix.WARNING, description);
        }

        public static DispositionModifier unsupportedFormatFailure() {
            return new DispositionModifier(Prefix.FAILURE, "unsupported format");
        }

        public static DispositionModifier unsupportedMicAlgorithms() {
            return new DispositionModifier(Prefix.FAILURE, "unsupported MIC-algorithms");
        }
        public static DispositionModifier failed(String description) {
            return new DispositionModifier(Prefix.FAILURE, description);
        }

        public static DispositionModifier error(String description) {
            return new DispositionModifier(Prefix.ERROR, description);
        }

        @Override
        public String toString() {
            return prefix + ": " + dispositionModifierExtension;
        }
    }
}
