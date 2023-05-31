package com.hr.simplecache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SimpleCache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(SimpleCache.class);
    private final int maxSize;
    private final Map<K, CacheEntry<V>> cache;

    private final ReentrantReadWriteLock lock;
    private final Lock readLock;
    private final Lock writeLock;
    private final EvictionStrategy<K, V> evictionStrategy;

    public SimpleCache(int maxSize, EvictionStrategy<K, V> evictionStrategy) {

        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<>(maxSize, 0.75f, true);
        this.lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
        this.evictionStrategy = evictionStrategy;
    }

    /**
     * 캐시에서put(key, value) 메서드를 통해 값을 저장합니다.
     * key와 value는 모든Java 객체가가능합니다.
     * put(key, value, TTL)과 같이 세번째 인자를 받아 캐시 유효기간을 지정 할 수 있습니다.
     * - TTL을 넘기지않을경우, 해당key의 TTL은 무제한입니다.
     *  key가 동일 할 경우 기존값을 덮어쓰고 기존값을반환합니다.
     */
    public void put(K key, V value) {
        logger.info(" Putting value in cache - Key: {}, Value: {}", key, value);
        put(key, value, 0L);
    }

    public void put(K key, V value, long ttl) {
        logger.info("Putting value in cache with TTL - Key: {}, Value: {}, TTL: {}", key, value, ttl);
        writeLock.lock();
        try {
            CacheEntry<V> entry = new CacheEntry<>(value, ttl);
            CacheEntry<V> oldEntry = cache.put(key, entry);
            if (oldEntry != null) {
                evictionStrategy.onEviction(key, oldEntry.getValue());
            }
            while (cache.size() > maxSize) {
                K victimKey = evictionStrategy.findVictim(cache);
                CacheEntry<V> victimEntry = cache.remove(victimKey);
                if (victimEntry != null) {
                    evictionStrategy.onEviction(victimKey, victimEntry.getValue());
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     *캐시에서get(key) 메서드를통해저장한값을 가져옵니다. 값이없을경우null 을 반환합니다.
     */
    public V get(K key) {
        logger.info("Getting value from cache - Key: {}", key);
        readLock.lock();
        try {
            CacheEntry<V> entry = cache.get(key);
            if (entry != null ){
                entry.incrementAccessCount();
                if (entry.isExpired()) {
                    cache.remove(key);
                    return null;
                }
                return entry.getValue();
            } else {
                return null;
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     *
     * addAndGet(key) 메서드를통해해당key 값에해당하는값을하나증가해서저장하고 반환합니다.
     * atomic하게 동작해야합니다. value에 1을 더할수없는숫자타입이아닌경우
     * InvalidTargetObjectTypeException을 발생시킵니다
     */
    public long addAndGet(K key) throws InvalidTargetObjectTypeException {
        logger.info("Adding and getting value from cache - Key: {}", key);

        writeLock.lock();
        try {
            CacheEntry<V> entry = cache.get(key);
            if (entry == null) {
                throw new NullPointerException("No value present for key " + key);
            }
            V value = entry.getValue();
            if (value instanceof Number) {
                Number newValue = ((Number) value).longValue() + 1L;
                entry.setValue((V) newValue);
                return newValue.longValue();
            } else {
                throw new InvalidTargetObjectTypeException("Value is not a number type");
            }
        } finally {
            writeLock.unlock();
        }
    }



    public interface EvictionStrategy<K, V> {

        K findVictim(Map<K, CacheEntry<V>> cache);

        void onEviction(K key, V value);

    }

    public static class CacheEntry<V> {

        private V value;
        private final long expirationTime;
        private int accessCount;


        public CacheEntry(V value, long ttl) {
            this.value = value;
            this.accessCount = 0;
            this.expirationTime = ttl > 0 ? System.currentTimeMillis() + ttl : Long.MAX_VALUE;
        }

        public V getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() >= expirationTime;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public int getAccessCount() {
            return accessCount;
        }

        public void incrementAccessCount() {
            accessCount++;
        }

    }

    public static class InvalidTargetObjectTypeException extends RuntimeException {

        public InvalidTargetObjectTypeException(String message) {
            super(message);
        }
    }

}
