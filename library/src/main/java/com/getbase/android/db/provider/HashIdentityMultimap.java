package com.getbase.android.db.provider;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

class HashIdentityMultimap<K, V> {

  public static <K, V> HashIdentityMultimap<K, V> create() {
    return new HashIdentityMultimap<K, V>();
  }

  private final Map<K, Collection<V>> mInternalHashMap = Maps.newIdentityHashMap();

  public void put(K key, V value) {
    Collection<V> values = mInternalHashMap.get(key);
    if (values == null) {
      values = createCollection();
      mInternalHashMap.put(key, values);
    }
    values.add(value);
  }

  public Collection<V> get(K key) {
    return Objects.firstNonNull(mInternalHashMap.get(key), Collections.<V>emptyList());
  }

  protected Collection<V> createCollection() {
    return Lists.newArrayList();
  }

  public void putAll(HashIdentityMultimap<K, V> backRefsMultimap) {
    for (Entry<K, Collection<V>> entry : backRefsMultimap.mInternalHashMap.entrySet()) {
      for (V value : entry.getValue()) {
        put(entry.getKey(), value);
      }
    }
  }
}
