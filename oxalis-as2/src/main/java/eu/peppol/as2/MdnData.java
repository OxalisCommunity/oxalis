package eu.peppol.as2;

import java.util.Date;

/**
 * Holds the data in a Message Disposition Notification (MDN)
 *
 * @author steinar
 *         Date: 09.10.13
 *         Time: 21:01
 */
public class MdnData {


    private final String subject;
    private final As2SystemIdentifier as2From;
    private final As2SystemIdentifier as2To;
    private final As2Disposition as2Disposition;
    private final String mic;
    private Date date;

    private MdnData(Builder builder) {
        this.subject = builder.subject;
        this.as2From = builder.as2From;
        this.as2To = builder.as2To;
        this.as2Disposition = builder.disposition;
        this.mic = builder.mic;
        this.date = builder.date;

    }

    public String getSubject() {
        return subject;
    }

    public As2SystemIdentifier getAs2From() {
        return as2From;
    }

    public As2SystemIdentifier getAs2To() {
        return as2To;
    }

    public As2Disposition getAs2Disposition() {
        return as2Disposition;
    }

    public String getMic() {
        return mic;
    }

    public Date getDate() {
        return date;
    }

    public static class Builder {
        String subject;
        As2SystemIdentifier as2From;
        As2SystemIdentifier as2To;

        As2Disposition disposition;

        String mic;
        Date date;

        public Builder date(Date date){
            this.date = date;
            return this;
        }

        Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        Builder as2From(As2SystemIdentifier as2From) {
            this.as2From = as2From;
            return this;
        }

        Builder as2To(As2SystemIdentifier as2To) {
            this.as2To = as2To;
            return this;
        }

        Builder disposition(As2Disposition disposition) {
            this.disposition = disposition;
            return this;
        }

        Builder mic(String mic) {
            this.mic = mic;
            return this;
        }

        MdnData build() {
            required(as2From, "as2From");
            required(as2To, "as2To");
            required(disposition, "disposition");

            return new MdnData(this);
        }


        private void required(Object object, String name) {
            if (object == null) {
                throw new IllegalStateException("Required property '" + name + "' not set.");
            }
        }
    }
}
