package eu.peppol.statistics;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * @author steinar
 *         Date: 08.04.13
 *         Time: 11:09
 */
public class EhCacheWrapper<K, V> implements CacheWrapper<K, V> {


    private final Ehcache ehcache;

    public EhCacheWrapper(final Ehcache ehcache) {
        this.ehcache = ehcache;
    }

    public void put(final K key, final V value) {
        getCache().put(new Element(key, value));
    }

    public V get(final K key) {
        Element element = getCache().get(key);
        if (element != null) {
            return (V) element.getValue();
        }
        return null;
    }

    public Ehcache getCache() {
        return ehcache;
    }
}