package eu.peppol.persistence.api.account;

public class CustomerId {
    private Integer id;

    public CustomerId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id required");
        }
        this.id = Integer.parseInt(id);
    }

    public CustomerId(Integer id){
        if (id == null) {
            throw new IllegalArgumentException("id required");
        }

        this.id = id;
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

        CustomerId that = (CustomerId) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }


    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
