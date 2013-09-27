package com.od.radixtree.map;

import com.od.radixtree.TreeConfig;
import com.od.radixtree.TreeVisitor;
import com.od.radixtree.ValueFilter;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 10/06/13
 * Time: 21:25
 * To change this template use File | Settings | File Templates.
 */
public interface TrieMap<V> {
    
    void put(CharSequence s, V value);

    V remove(CharSequence s);

    V get(CharSequence s);

    <E extends Collection<V>> E getStartingWith(CharSequence s, E collection);
    
    <E extends Collection<V>> E getStartingWith(CharSequence s, E collection, int maxItems);    

    <E extends Collection<V>> E getStartingWith(CharSequence s, E collection, int maxItems, ValueFilter<V> valueFilter);

    void accept(TreeVisitor<V> v);

    /**
     * Compress the string instances used for labels in this trie structure
     * 
     * Storage of small strings is extremely memory inefficient in the JVM - 
     * Generally this compression combines edge labels into a smaller number or long String
     * instances using appropriate offsets for the edge start/end.
     * 
     * It may be beneficial to call this method once the trie structure has been fully populated.
     * This will only have a beneficial effect if all other references to the Strings or CharSequence
     * instances used to build the trie have been de-referenced and garbage collected. 
     * 
     * Once compression is performed the total memory used by the TrieMap should be 
     * significantly less than the memory required to store the equivalent set of String keys.
     */
    void compress();

    TreeConfig<V> getTreeConfig();
}
