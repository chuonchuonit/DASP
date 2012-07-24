package ddth.dasp.framework.cache;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;

public abstract class AbstractCacheManager implements ICacheManager {

    public final static String CACHE_PROP_CAPACITY = "cache.capacity";
    public final static String CACHE_PROP_EXPIRE_AFTER_WRITE = "cache.expireAfterWrite";
    public final static String CACHE_PROP_EXPIRE_AFTER_ACCESS = "cache.expireAfterAccess";

    private long defaultCacheCapacity = ICacheManager.DEFAULT_CACHE_CAPACITY;
    private long defaultExpireAfterAccess = ICacheManager.DEFAULT_EXPIRE_AFTER_ACCESS;
    private long defaultExpireAfterWrite = ICacheManager.DEFAULT_EXPIRE_AFTER_WRITE;
    private ConcurrentMap<String, ICache> caches;
    private Map<String, Properties> cacheProperties;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        int numProcessores = Runtime.getRuntime().availableProcessors();
        MapMaker mm = new MapMaker();
        mm.concurrencyLevel(numProcessores);
        caches = mm.makeMap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        if (caches != null) {
            try {
                Iterator<Entry<String, ICache>> it = caches.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, ICache> entry = it.next();
                    entry.getValue().destroy();
                }
            } finally {
                caches.clear();
            }
            caches = null;
        }
    }

    public long getDefaultCacheCapacity() {
        return defaultCacheCapacity;
    }

    public void setDefaultCacheCapacity(long defaultCacheCapacity) {
        this.defaultCacheCapacity = defaultCacheCapacity;
    }

    public long getDefaultExpireAfterAccess() {
        return defaultExpireAfterAccess;
    }

    public void setDefaultExpireAfterAccess(long defaultExpireAfterAccess) {
        this.defaultExpireAfterAccess = defaultExpireAfterAccess;
    }

    public long getDefaultExpireAfterWrite() {
        return defaultExpireAfterWrite;
    }

    public void setDefaultExpireAfterWrite(long defaultExpireAfterWrite) {
        this.defaultExpireAfterWrite = defaultExpireAfterWrite;
    }

    public void setCacheProperties(Map<String, Properties> cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    /**
     * Gets a cache's properties
     * 
     * @param name
     * @return
     */
    protected Properties getCacheProperties(String name) {
        return cacheProperties != null ? cacheProperties.get(name) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICache getCache(String name) {
        return caches.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeCache(String name) {
        ICache cache = getCache(name);
        if (cache != null) {
            try {
                cache.destroy();
            } finally {
                caches.remove(name);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public ICache createCache(String name) {
        return createCache(name, defaultCacheCapacity, defaultExpireAfterWrite,
                defaultExpireAfterAccess);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public ICache createCache(String name, long capacity) {
        return createCache(name, capacity, defaultExpireAfterWrite, defaultExpireAfterAccess);
    }

    @Override
    synchronized public ICache createCache(String name, long capacity, long expireAfterWrite,
            long expireAfterAccess) {
        ICache cache = getCache(name);
        if (cache == null) {
            // check if custom cache settings exist
            long cacheCapacity = capacity;
            long cacheExpireAfterWrite = expireAfterWrite;
            long cacheExpireAfterAccess = expireAfterAccess;
            Properties cacheProps = getCacheProperties(name);
            if (cacheProps != null) {
                try {
                    cacheCapacity = Long.parseLong(CACHE_PROP_CAPACITY);
                } catch (Exception e) {
                    cacheCapacity = capacity;
                }
                try {
                    cacheExpireAfterWrite = Long.parseLong(CACHE_PROP_EXPIRE_AFTER_WRITE);
                } catch (Exception e) {
                    cacheExpireAfterWrite = expireAfterWrite;
                }
                try {
                    cacheExpireAfterAccess = Long.parseLong(CACHE_PROP_EXPIRE_AFTER_ACCESS);
                } catch (Exception e) {
                    cacheExpireAfterAccess = expireAfterAccess;
                }
            }
            cache = createCacheInternal(name, cacheCapacity, cacheExpireAfterWrite,
                    cacheExpireAfterAccess);
            caches.put(name, cache);
        }
        return cache;
    }

    /**
     * Creates a new cache instance. Convenient for sub-class to override.
     * 
     * @param name
     * @param capacity
     * @param expireAfterWrite
     * @param expireAfterAccess
     * @return
     */
    protected abstract ICache createCacheInternal(String name, long capacity,
            long expireAfterWrite, long expireAfterAccess);
}