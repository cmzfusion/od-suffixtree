package com.od.radixtree.map;

import com.od.radixtree.*;
import com.od.radixtree.sequence.CharSequenceWithIntTerminatorAdapter;
import com.od.radixtree.sequence.CharSequenceWithTerminalNode;
import com.od.radixtree.visitor.StringCompressionVisitor;

import java.util.Collection;
import java.util.LinkedList;

/**
 * User: nick
 * Date: 04/06/13
 * Time: 08:31
 * 
 * A RadixTree / compact prefix tree based Map
 */
public class RadixTreeMap<V> implements TrieMap<V> {
    
    private RadixTree<V> radixTree = new RadixTree<V>();
    
    
    private CharSequenceWithIntTerminatorAdapter intTerminatorAdapter = new CharSequenceWithIntTerminatorAdapter();
    private MutableSequence mutableSequence = new MutableSequence();

    private final TreeConfig<V> treeConfig;

    public RadixTreeMap() {
        treeConfig = new TreeConfig<V>(new ChildIteratorPool<V>(), new SingleValueSupplier<V>());
    }
    
    public RadixTreeMap(ValueSupplier<V> valueSupplier) {
        treeConfig = new TreeConfig<V>(new ChildIteratorPool<V>(), valueSupplier);    
    }
    
    @Override
    public void put(CharSequence s, V value) {
        mutableSequence.setSegment(new CharSequenceWithTerminalNode(s));
        radixTree.add(mutableSequence, value, treeConfig);
    }

    /**
     * Remove value corresponding to key s
     * @return old value, if there was a value in the map for this key
     */
    @Override
    public V remove(CharSequence s) {
        mutableSequence.setSegment(new CharSequenceWithTerminalNode(s));
        return (V)radixTree.remove(mutableSequence, null, treeConfig);
    }

    @Override
    public V get(CharSequence s) {
        mutableSequence.setSegment(new CharSequenceWithTerminalNode(s));
        LinkedList<V> result = new LinkedList<V>();
        radixTree.get(mutableSequence, result, treeConfig);
        assert(result.size() < 2);
        return result.size() > 0 ? result.get(0) : null;
    }
    
    @Override
    public <E extends Collection<V>> E getStartingWith(CharSequence s, E collection) {
        return getStartingWith(s, collection, Integer.MAX_VALUE, (ValueFilter<V>) ValueFilter.NULL_VALUE_FILTER);
    }
    
    @Override
    public <E extends Collection<V>> E getStartingWith(CharSequence s, E collection, int maxItems) {
       return getStartingWith(s, collection, maxItems, (ValueFilter<V>) ValueFilter.NULL_VALUE_FILTER);
    }

    @Override
    public <E extends Collection<V>> E getStartingWith(CharSequence s, E collection, int maxItems, ValueFilter<V> valueFilter) {
        intTerminatorAdapter.setCharSequence(s);
        mutableSequence.setSegment(intTerminatorAdapter); //do not add terminal node
        radixTree.get(mutableSequence, collection, maxItems, treeConfig, valueFilter);
        return collection;
    }

    @Override
    public void accept(TreeVisitor<V> v) {
        radixTree.accept(v, treeConfig);
    }
    
    public void compress() {
        StringCompressionVisitor v = new StringCompressionVisitor();
        radixTree.accept(v, treeConfig);
    }

    @Override
    public TreeConfig<V> getTreeConfig() {
        return treeConfig;
    }
}
