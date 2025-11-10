package org.fog.entities.dataEstructures;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class IndexedMinPQ<K extends Comparable<K>, V extends Comparable<V>> {

    private Map<K, Pair<K, V>> map = new HashMap<>();
    private TreeSet<Pair<K, V>> treeSet = new TreeSet<>();

    // Pega o pair com o menor valor
    public Pair<K,V> getMinPair() {
        if (treeSet.isEmpty()) {
            return null;
        }
        return treeSet.first(); // O(1) ou O(log n)
    }

    // Adiciona ou ATUALIZA o valor de uma chave
    public void upsert(K chave, V valor) {
        // 1. Verifica se a chave já existe
        Pair<K, V> parExistente = map.get(chave);

        if (parExistente != null) {
            // 2. Se existe, REMOVE do TreeSet
            // O(log n)
            treeSet.remove(parExistente); 

            // 3. Atualiza o valor
            parExistente.setValue(valor);

            // 4. RE-ADICIONA no TreeSet (na nova posição)
            // O(log n)
            treeSet.add(parExistente);

        } else {
            // 5. Se não existe, cria um novo Par
            Pair<K, V> novoPar = new Pair<>(chave, valor);
            map.put(chave, novoPar);
            treeSet.add(novoPar); // O(log n)
        }
    }

    // Remove o par com menor valor
    public K pollMinKey() {
        if (treeSet.isEmpty()) {
            return null;
        }
        Pair<K, V> minPar = treeSet.pollFirst(); // O(log n)
        map.remove(minPar.getKey()); // O(1)
        return minPar.getKey();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}