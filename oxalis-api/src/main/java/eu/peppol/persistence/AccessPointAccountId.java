package eu.peppol.persistence;

/**
 * @author steinar
 *         Date: 22.10.2016
 *         Time: 15.32
 */
public class AccessPointAccountId {
    private Integer id;

    public AccessPointAccountId(Integer id){
        if (id == null) {
            throw new IllegalArgumentException("id required");
        }

        this.id = id;
    }

    public static AccessPointAccountId valueOf(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id required");
        }
        return new AccessPointAccountId(Integer.parseInt(id));
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

        AccessPointAccountId that = (AccessPointAccountId) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }


    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
