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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the AS2 Header <code>Disposition-notifications-options</code>
 *
 * The following input string should yield an As2DispositionNotificationOptions with two parameters:
 * <pre>
 * Disposition-notification-options: signed-receipt-protocol=required,pkcs7-signature;
 *  signed-receipt-micalg=required,sha1,md5
 * </pre>
 *
 * The two parameters are:
 * <ol>
 *     <li>signed-receipt-protocol</li>
 *     <li>signed-receipt-micalg</li>
 * </ol>
 *
 * @author steinar
 *         Date: 17.10.13
 *         Time: 21:08
 */
public class As2DispositionNotificationOptions {

    private final List<Parameter> parameterList;

    public static final Logger log = LoggerFactory.getLogger(As2DispositionNotificationOptions.class);

    public As2DispositionNotificationOptions(List<Parameter> parameterList) {
        this.parameterList = parameterList;
    }

    public List<Parameter> getParameterList() {
        return parameterList;
    }

    public static As2DispositionNotificationOptions getDefault() {
        return valueOf("signed-receipt-protocol=required,pkcs7-signature; signed-receipt-micalg=required,sha1");
    }

    public static As2DispositionNotificationOptions valueOf(String s)  {

        if (s == null) {
            throw new IllegalArgumentException("Can not parse Multipart empty disposition-notification-options");
        }

        Pattern pattern = Pattern.compile("(signed-receipt-protocol|signed-receipt-micalg)\\s*=\\s*(required|optional)\\s*,\\s*([^;]*)");

        List<Parameter> parameterList = new ArrayList<Parameter>();

        log.debug("Inspecting " + s);
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            if (matcher.groupCount() != 3) {
                throw new IllegalStateException("Internal error: Invalid group count in RegEx for parameter match in disposition-notification-options.");
            }
            String attributeName = matcher.group(1);
            String importanceName = matcher.group(2);
            String value = matcher.group(3);

            Attribute attribute = Attribute.fromString(attributeName);
            Importance importance = Importance.valueOf(importanceName.trim().toUpperCase());

            Parameter parameter = new Parameter(attribute, importance, value);
            parameterList.add(parameter);
        }

        if (parameterList.isEmpty()) {
            throw new IllegalArgumentException("Unable to create " + As2DispositionNotificationOptions.class.getSimpleName() + " from '" + s + "'");
        }

        return new As2DispositionNotificationOptions(parameterList);
    }


    Parameter getParameterFor(Attribute attribute) {
        for (Parameter parameter : parameterList) {
            if (parameter.attribute == attribute) {
                return parameter;
            }
        }
        return null;
    }

    /**
     * From the official AS2 spec page 22 :
     * The "signed-receipt-micalg" parameter is a list of MIC algorithms
     * preferred by the requester for use in signing the returned receipt.
     * The list of MIC algorithms SHOULD be honored by the recipient from left to right.
     */
    public Parameter getSignedReceiptMicalg() {
        return getParameterFor(Attribute.SIGNED_RECEIPT_MICALG);
    }

    /**
     * @return Use the preferred mic algorithm for signed receipt, for PEPPOL AS2 this should be "sha1"
     */
    public String getPreferredSignedReceiptMicAlgorithmName() {
        String preferredAlgorithm = "" + getSignedReceiptMicalg().getTextValue();   // text value could be "sha1, md5"
        return preferredAlgorithm.split(",")[0].trim();
    }

    public Parameter getSignedReceiptProtocol() {
        return getParameterFor(Attribute.SIGNED_RECEIPT_PROTOCOL);
    }

    @Override
    public String toString() {
        return getSignedReceiptProtocol().toString() + "; " + getSignedReceiptMicalg().toString();
    }

    static class Parameter {
        Attribute attribute;
        Importance importance;
        String textValue;

        Attribute getAttribute() {
            return attribute;
        }

        Importance getImportance() {
            return importance;
        }

        String getTextValue() {
            return textValue;
        }

        Parameter(Attribute attribute, Importance importance, String textValue) {
            this.attribute = attribute;
            this.importance = importance;
            this.textValue = textValue;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("")
            .append(attribute).append("=")
            .append(importance)
            .append(",").append(textValue);

            return sb.toString();
        }
    }

    static enum Attribute {
        
        SIGNED_RECEIPT_PROTOCOL("signed-receipt-protocol"),
        SIGNED_RECEIPT_MICALG("signed-receipt-micalg");
        private final String text;

        Attribute(String text) {

            this.text = text;
        }

        /** This is needed as the textual representation of each enum value, contains dashes */
        static Attribute fromString(String s) {
            if (s == null) {
                throw new IllegalArgumentException("String value required");
            }
            for (Attribute attribute : values()) {
                if (attribute.text.equalsIgnoreCase(s)) {
                    return attribute;
                }
            }

            throw new IllegalArgumentException(s + " not recognized as an attribute of As2DispositionNotificationOptions");
        }

        @Override
        public String toString() {
            return text;
        }
    }

    static enum Importance {
        REQUIRED, OPTIONAL;


        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
