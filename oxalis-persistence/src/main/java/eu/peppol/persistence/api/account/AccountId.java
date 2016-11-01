/* Created by steinar on 01.01.12 at 14:29 */
package eu.peppol.persistence.api.account;

public class AccountId {
    private Integer id;

    public AccountId(Integer id){
        if (id == null) {
            throw new IllegalArgumentException("id required");
        }

        this.id = id;
    }

    public static AccountId valueOf(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id required");
        }
        return new AccountId(Integer.parseInt(id));
    }

    public Integer toInteger(){
        return id;
    }


    public String toString() {
        return id.toString();
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountId that = (AccountId) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }


    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
