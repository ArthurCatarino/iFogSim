package org.fog.entities.dataEstructures;

import java.util.Objects;

/**
 * Uma classe genérica para armazenar um par (Chave, Valor),
 * onde 'key' é o identificador e 'value' é a prioridade.
 * * Para ser usada em um TreeSet ordenado, K e V devem ser
 * comparáveis (Comparable).
 *
 * @param <K> O tipo da Chave (key), ex: String
 * @param <V> O tipo do Valor (value/prioridade), ex: Integer
 */
public class Pair<K extends Comparable<K>, V extends Comparable<V>> 
    implements Comparable<Pair<K, V>> {
 
  private K key;
  private V value;

  public Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
  }

  public void setKey(K key) {
    this.key = key;
  }

  public void setValue(V value) {
    this.value = value;
  }

  @Override
  public int compareTo(Pair<K, V> outro) {
    int cmpValor = this.value.compareTo(outro.value);
    
    if (cmpValor != 0) {
      return cmpValor;
    }
    return this.key.compareTo(outro.key);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    
    Pair<?, ?> outro = (Pair<?, ?>) obj;
    
    return Objects.equals(this.key, outro.key) &&
           Objects.equals(this.value, outro.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }

  @Override
  public String toString() {
    return "Pair(key=" + key + ", value=" + value + ")";
  }
}