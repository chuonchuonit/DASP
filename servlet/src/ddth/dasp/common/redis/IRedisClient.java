package ddth.dasp.common.redis;

/**
 * A Redis (http://redis.io) client API.
 * 
 * @author Thanh B. Nguyen <btnguyen2k@gmail.com>
 */
public interface IRedisClient {

    public final static int DEFAULT_REDIS_PORT = 6379;

    /**
     * Initializes this Redis client before use. The Redis client is not usable
     * until this method is called.
     */
    public void init();

    /**
     * Destroys this Redis client. The Redis client is no longer usable after
     * calling this method.
     */
    public void destroy();

    /**
     * Closes this Redis client, but donot destroy it.
     */
    public void close();

    /* Redis API */
    /**
     * "Ping" the Redis server.
     * 
     * @return
     */
    public String ping();

    /**
     * Updates expiry time of a Redis key.
     * 
     * @param key
     * @param ttlSeconds
     *            time to live (a.k.a "expiry after write") in seconds
     */
    public void expire(String key, int ttlSeconds);

    /**
     * Deletes value(s) from Redis server.
     * 
     * @param keys
     */
    public void delete(String... keys);

    /**
     * Gets a value from Redis server.
     * 
     * @param key
     * @return
     */
    public String get(String key);

    /**
     * Gets a value from Redis server.
     * 
     * @param key
     * @return
     */
    public byte[] getAsBinary(String key);

    /**
     * Sets a value to Redis server.
     * 
     * @param key
     * @param value
     * @param ttlSeconds
     *            time to live (a.k.a "expiry after write") in seconds
     */
    public void set(String key, String value, int ttlSeconds);

    /**
     * Sets a value to Redis server.
     * 
     * @param key
     * @param value
     * @param ttlSeconds
     *            time to live (a.k.a "expiry after write") in seconds
     */
    public void set(String key, byte[] value, int ttlSeconds);

    /**
     * Deletes values from a Redis hash.
     * 
     * @param mapName
     * @param fieldNames
     */
    public void hashDelete(String mapName, String... fieldNames);

    /**
     * Gets number of elements of a Redis hash.
     * 
     * @param mapName
     * @return
     */
    public long hashSize(String mapName);

    /**
     * Gets a field value from a Redis hash.
     * 
     * @param mapName
     * @param fieldName
     * @return
     */
    public String hashGet(String mapName, String fieldName);

    /**
     * Gets a field value from a Redis hash.
     * 
     * @param mapName
     * @param fieldName
     * @return
     */
    public byte[] hashGetAsBinary(String mapName, String fieldName);

    /**
     * Sets a field value of a Redis hash.
     * 
     * @param mapName
     * @param fieldName
     * @param value
     * @param ttlSeconds
     *            time to live (a.k.a "expiry after write") in seconds
     */
    public void hashSet(String mapName, String fieldName, String value, int ttlSeconds);

    /**
     * Sets a field value of a Redis hash.
     * 
     * @param mapName
     * @param fieldName
     * @param value
     * @param ttlSeconds
     *            time to live (a.k.a "expiry after write") in seconds
     */
    public void hashSet(String mapName, String fieldName, byte[] value, int ttlSeconds);

    /**
     * Pushes a message to head of a list.
     * 
     * @param listName
     * @param messages
     */
    public void listPush(String listName, String... messages);

    /**
     * Pushes a message to head of a list.
     * 
     * @param listName
     * @param messages
     */
    public void listPush(String listName, byte[]... message);

    /**
     * Pushes a message to head of a list.
     * 
     * @param listName
     * @param ttlSeconds
     * @param messages
     */
    public void listPush(String listName, int ttlSeconds, String... messages);

    /**
     * Pushes a message to head of a list.
     * 
     * @param listName
     * @param ttlSeconds
     * @param messages
     */
    public void listPush(String listName, int ttlSeconds, byte[]... messages);

    /**
     * Pops a message from tail of a list.
     * 
     * @param listName
     * @return
     */
    public String listPop(String listName);

    /**
     * Pops a message from tail of a list.
     * 
     * @param listName
     * @return
     */
    public byte[] listPopAsBinary(String listName);

    /**
     * Gets a list's size.
     * 
     * @param listName
     * @return
     */
    public long listSize(String listName);

    /**
     * Publishes a message to a channel.
     * 
     * @param channelName
     * @param message
     */
    public void publish(String channelName, String message);

    /**
     * Publishes a message to a channel.
     * 
     * @param channelName
     * @param message
     */
    public void publish(String channelName, byte[] message);

    /**
     * Subscribes to a channel.
     * 
     * Note: This method is a blocking operation!
     * 
     * @param channelName
     * @param messageListener
     * @return
     */
    public boolean subscribe(String channelName, IMessageListener messageListener);

    /**
     * Unsubscribes from a channel.
     * 
     * @param channelName
     * @param messageListener
     * @return
     */
    public boolean unsubscribe(String channelName, IMessageListener messageListener);
    /* Redis API */
}
