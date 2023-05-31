# simpleCache# SimpleCache


1. SimpleCache<K, V> 클래스:
    * 이 클래스는 캐시 기능을 제공하는 라이브러리의 핵심 클래스입니다. 제네릭으로 키(K)와 값(V)의 타입을 받습니다.
        * 필드:
          * maxSize: 캐시 객체가 담을 수 있는 최대 객체 변수 개수를 나타냅니다.
          * cache: 캐시 객체를 저장하는 맵입니다. LinkedHashMap을 사용하여 순서를 유지합니다.
          * lock, readLock, writeLock: 스레드 동기화를 위한 ReentrantReadWriteLock입니다.
          * evictionStrategy: 객체를 삭제할 때 사용할 victim 선택 전략을 담은 인터페이스입니다.
        * 생성자:maxSize와 evictionStrategy를 받아 객체를 초기화합니다.
        * put(K key, V value) 메서드: 주어진 키와 값을 캐시에 저장합니다. 기존에 같은 키가 존재하면 해당 값을 덮어씁니다.evictionStrategy에 따라 캐시 객체 개수를 관리하며, 최대 개수를 초과할 경우 victim을 선택하여 삭제합니다.
        * put(K key, V value, long ttl) 메서드:주어진 키와 값을 캐시에 저장하고, 유효 기간(TTL)을 설정합니다.유효 기간이 지난 캐시 객체는 자동으로 삭제됩니다.
        * get(K key) 메서드:주어진 키에 해당하는 값을 캐시에서 가져옵니다.값이 존재하지 않거나 유효 기간이 지난 경우 null을 반환합니다.
        * addAndGet(K key) 메서드:주어진 키에 해당하는 값을 1씩 증가시키고, 증가된 값을 반환합니다. 값이 숫자 타입이 아닐 경우 InvalidTargetObjectTypeException을 발생시킵니다.
      
        * EvictionStrategy<K, V> 인터페이스:
          * findVictim 메서드: 캐시 객체에서 victim을 선택하는 전략을 구현하는 메서드입니다.
          * onEviction 메서드: 객체가 캐시에서 삭제될 때 실행되는 콜백 메서드입니다.
2. CacheEntry<V> 클래스:
    * 이 클래스는 캐시 객체의 값과 유효 기간을 저장합니다.
    * value: 캐시 객체의 값입니다.
    * expirationTime: 캐시 객체의 유효 기간을 나타내는 시간입니다.
    * accessCount : 캐시의 사용 횟수를 카운팅합니다.
    * isExpired() 메서드: 캐시 객체의 유효 기간이 지났는지 확인합니다.
    * setValue(V value) 메서드: 캐시 객체의 값을 설정합니다.
    * getAccessCount() 메서드 : 캐시의 사용 횟수를 가져옵니다.
    * incrementAccessCount() 메서드 : 캐시의 사용 횟수값을 +1 증가시킵니다. 
    * InvalidTargetObjectTypeException 예외 클래스:
    * 캐시 객체의 값이 숫자 타입이 아닌 경우 발생시키는 예외 클래스입니다.



# TEST CODE
* 공통 테스트 메소드 : 
  * testPutAndGet: 캐시에 값을 저장하고, 저장한 값을 가져오는지 확인합니다. 캐시에 존재하지 않는 키에 대해서는 null을 반환하는지도 확인합니다.
  * testPutWithTTL: TTL(Time-To-Live)을 설정하여 값을 저장하고, TTL이 만료되었을 때 값이 삭제되는지 확인합니다.
  * testAddAndGet: 캐시에 값을 저장하고, 해당 값을 증가시키는 addAndGet 메소드를 사용하여 값을 가져오고, 증가한 값을 확인합니다.
  * testConcurrentCacheUsage: 다중 스레드 환경에서 캐시를 동시에 사용하는 경우를 테스트합니다. 각 스레드는 캐시의 addAndGet 메소드를 반복적으로 호출하여 값을 증가시키고, 예상되는 값과 일치하는지 확인합니다.

## **SimpleCacheLFUTest**
  * SimpleCacheLFUTest는 LFU (Least Frequently Used) 전략을 테스트하기 위한 테스트 케이스입니다. SimpleCache 클래스에 LFU 전략을 적용하고, 해당 전략을 테스트합니다.

  * 설정
  SimpleCache<String, Integer> 형태의 cache 객체를 생성하여 LFU 전략을 적용합니다.

  * 메소드
    * setUp() 메서드: 각 테스트 메서드가 실행되기 전에 호출되는 초기화 메서드입니다. 이 메서드에서는 SimpleCache 객체를 생성하고, 최근에 사용되지 않은 항목을 제거하는 LFU  방식의 LeastFrequentlyUsedEvictionStrategy 객체를 전달하여 캐시를 초기화합니다.
    * tearDown() 메서드 : 메서드는 각 테스트 메서드가 실행된 후에 호출되는 정리 메서드입니다. 이 메서드에서는 cache 변수에 할당된 SimpleCache 객체를 null로 설정하여 메모리에서 해제합니다.
    * testLFUEvictionStrategy: LFU 전략을 테스트하기 위해 캐시에 값을 저장하고, 특정 키를 더 자주 접근하여 사용 빈도를 증가시킵니다. 그 후, 새로운 값을 추가하여 LFU 전략에 따라 가장 사용 빈도가 적은 키가 제거되는지 확인합니다.
  * 내부 클래스
  LeastFrequentlyUsedEvictionStrategy: LFU 전략을 구현한 내부 클래스입니다. 캐시에서 사용 빈도가 가장 적은 항목을 찾아 반환하고, 항목이 제거될 때 로그를 출력합니다.

## **SimpleCacheFIFOTest**
* SimpleCacheLFUTest는 FIFO(First In First Out) 전략을 테스트하기 위한 테스트 케이스입니다. SimpleCache 클래스에 LRU 전략을 적용하고, 해당 전략을 테스트합니다.

* 설정
  SimpleCache<String, Integer> 형태의 cache 객체를 생성하여 LRU 전략을 적용합니다.

* 메소드
    * setUp() 메서드: 각 테스트 메서드가 실행되기 전에 호출되는 초기화 메서드입니다. 이 메서드에서는 SimpleCache 객체를 생성하고, 최근에 사용되지 않은 항목을 제거하는 LRU  방식의 LeastRecentlyUsedEvictionStrategy 객체를 전달하여 캐시를 초기화합니다.
    * tearDown() 메서드 : 메서드는 각 테스트 메서드가 실행된 후에 호출되는 정리 메서드입니다. 이 메서드에서는 cache 변수에 할당된 SimpleCache 객체를 null로 설정하여 메모리에서 해제합니다.
    * testLFUEvictionStrategy: LRU (Least Recently Used) 방식의 캐시 제거 전략을 테스트합니다. 이 메서드에서는 여러 개의 키-값 쌍을 캐시에 저장한 후, 캐시의 용량을 초과하는 새로운 키-값 쌍을 추가합니다. 이 때 LRU 전략에 따라 가장 오래된 항목이 제거되어야 합니다. 특정 키의 값을 get() 메서드로 확인하여 예상되는 대로 제거되었는지 확인합니다.
* 내부 클래스
  FirstInFirstOutEvictionStrategy: FIFO 전략을 구현한 내부 클래스입니다. 현재 구현에서는 캐시 맵에서 첫 번째 키를 선택하여 반환하고 있습니다. 이는 FIFO 전략에서 가장 오래 사용되지 않은 항목을 찾아 반환하고, 항목이 제거될 때 로그를 출력합니다.
