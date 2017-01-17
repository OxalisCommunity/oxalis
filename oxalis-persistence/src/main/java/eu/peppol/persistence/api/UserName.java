/* Created by steinar on 01.01.12 at 14:29 */
package eu.peppol.persistence.api;

/**
 * Value object representing username
 *
 * @author adam
 */
public class UserName {
    private final String username;

    public UserName(String username) {
        if (username == null) {
            throw new IllegalArgumentException("username required");
        }
        this.username = username.toLowerCase();
    }


    public String stringValue() {
        return username.toString();
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("UserName");
        sb.append("{username='").append(username).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserName userName = (UserName) o;

        if (username != null ? !username.equals(userName.username) : userName.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}
