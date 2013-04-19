package eu.peppol.persistence.sql;

/**
 * @author steinar
 *         Date: 08.04.13
 *         Time: 11:08
 */
public interface CacheWrapper<K, V>
{
    void put(K key, V value);

    V get(K key);
}