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
 
  private K first;
  private V second;

  public Pair(K first, V second) {
    this.first = first;
    this.second = second;
  }

  public K getfirst() {
    return first;
  }

  public V getsecond() {
    return second;
  }

  public void setfirst(K first) {
    this.first = first;
  }

  public void setsecond(V second) {
    this.second = second;
  }

  @Override
  public int compareTo(Pair<K, V> outro) {
    int cmpValor = this.second.compareTo(outro.second);
    
    if (cmpValor != 0) {
      return cmpValor;
    }
    return this.first.compareTo(outro.first);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    
    Pair<?, ?> outro = (Pair<?, ?>) obj;
    
    return Objects.equals(this.first, outro.first) &&
           Objects.equals(this.second, outro.second);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second);
  }

  @Override
  public String toString() {
    return "Pair(first=" + first + ", second=" + second + ")";
  }
}