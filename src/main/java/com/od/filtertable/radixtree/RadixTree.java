package com.od.filtertable.radixtree;

import com.od.filtertable.index.MutableCharSequence;
import com.od.filtertable.index.MutableSequence;
import com.od.filtertable.radixtree.visitor.CollectValuesVisitor;
import com.od.filtertable.radixtree.visitor.TreeVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: nick
 * Date: 07/05/13
 * Time: 19:00
 */
public class RadixTree<V> implements CharSequence {
    
    //the next node in the linked list of children for this node's parent
    RadixTree<V> nextPeer;
    
    //use of this field is overloaded to save memory / 1 reference per node instance
    //this will either be the first child node of a linked list of children, or, for terminal nodes, a collection of values
    Object payload; 

    private CharSequence immutableSequence;
    private short start;
    private short end;

    public RadixTree() {}
    
    /**
     * Add value to the tree under the key s
     */
    public void add(MutableCharSequence s, V value, TreeConfig<V> treeConfig) {
        if (s.length() == 0) {
            //since when adding initial s always ends with terminal char, this is a terminal node, add the value
            addValue(value, treeConfig.getValueSupplier());
        } else {
            //there are still chars to add, see if they match any existing children, if not insert a new child node
            ChildIterator<V> i = treeConfig.getIteratorPool().getIterator(this, isTerminalNode(treeConfig));
            try {
                doAdd(s, value, i, treeConfig);
            } finally {
                treeConfig.getIteratorPool().returnIterator(i);
            }
        }      
    }

    private void doAdd(MutableCharSequence s, V value, ChildIterator<V> i, TreeConfig<V> treeConfig) {
        boolean added = false;
        while (i.isValid()) {
            RadixTree<V> currentNode = i.getCurrentNode();
            int matchingChars = CharUtils.getSharedPrefixCount(s, currentNode);
            if ( matchingChars == s.length() /* must be a terminal node */
                || matchingChars == currentNode.getLabelLength()) {
                addToChild(s, value, i, matchingChars, treeConfig);
                added = true;
                break;
            } else if ( matchingChars > 0) {  //only part of the current node label matched
                split(i, s, value, matchingChars, treeConfig);
                added = true;
                break;              
            } else if ( CharUtils.compareFirstChar(s, currentNode, treeConfig) == -1) {
                insert(i, s, value, treeConfig.getValueSupplier());
                added = true;
                break;
            }
            i.next();
        }

        if (!added) {
            insert(i, s, value, treeConfig.getValueSupplier());
        }
    }

    private void addToChild(MutableCharSequence s, V value, ChildIterator<V> i, int matchingChars, TreeConfig<V> treeConfig) {
        s.incrementStart(matchingChars);        
        i.getCurrentNode().add(s, value, treeConfig);
        s.decrementStart(matchingChars);
    }

    private void split(ChildIterator<V> i, MutableCharSequence s, V value, int matchingChars, TreeConfig<V> treeConfig) {
        RadixTree<V> nodeToReplace = i.getCurrentNode();
        int labelLengthForNewChild = s.length() - matchingChars;

        RadixTree<V> replacementNode = new RadixTree<V>();
        replacementNode.setLabelFromNodePrefix(nodeToReplace, matchingChars);
        i.replace(replacementNode);

        RadixTree<V> replacementChild = new RadixTree<V>();
        replacementChild.setLabelFromNodeSuffix(nodeToReplace, matchingChars);
        replacementChild.payload = nodeToReplace.payload;

        RadixTree<V> newChild = new RadixTree<V>();
        newChild.setLabel(s.getImmutableBaseSequence(), s.getBaseSequenceEnd() - labelLengthForNewChild, s.getBaseSequenceEnd());
        newChild.addValue(value, treeConfig.getValueSupplier());

        boolean newChildFirst = CharUtils.compareFirstChar(newChild, replacementChild, treeConfig) == -1;
        RadixTree<V> firstChild = newChildFirst ? newChild : replacementChild;
        RadixTree<V> secondChild = newChildFirst ? replacementChild : newChild;
        
        replacementNode.payload = firstChild;
        firstChild.nextPeer = secondChild;
    }

    public void setLabel(CharSequence baseSequence, int start, int end) {
        this.immutableSequence = baseSequence;
        this.start = (short)start;
        this.end = (short)end;
    }

    private void insert(ChildIterator<V> i, MutableCharSequence s, V value, ValueSupplier<V> valueSupplier) {
        RadixTree<V> newNode = new RadixTree<V>();
        newNode.setLabel(s.getImmutableBaseSequence(), s.getBaseSequenceStart(), s.getBaseSequenceEnd());
        newNode.addValue(value, valueSupplier);
        i.insert(newNode);
    }

    private void addValue(V value, ValueSupplier<V> valueSupplier) {
        ValueSupplier.ValueSupplierResult r = valueSupplier.addValue(value, payload);
        payload = r.payload;
        r.clear();
    }

    /**
     * Get into target collection values from all nodes prefixed with char sequence
     */
    public Collection<V> get(CharSequence c, Collection<V> targetCollection, TreeConfig<V> treeConfig) {
        CollectValuesVisitor<V> collectValuesVisitor = new CollectValuesVisitor<V>(targetCollection, treeConfig);
        accept(new MutableSequence(c), collectValuesVisitor, treeConfig);
        return targetCollection;
    }
    
    /**
     * Get into target collection values from all nodes prefixed with char sequence, to a limit of maxResults values
     */
    public <R extends Collection<V>> R get(CharSequence c, R targetCollection, int maxResults, TreeConfig<V> treeConfig) {
        CollectValuesVisitor<V> collectValuesVisitor = new CollectValuesVisitor<V>(targetCollection, maxResults, treeConfig);
        accept(new MutableSequence(c), collectValuesVisitor, treeConfig);
        return targetCollection;
    }

    private void accept(MutableCharSequence s, TreeVisitor<V> visitor, TreeConfig<V> treeConfig) {
        ChildIteratorPool<V> iteratorPool = treeConfig.getIteratorPool();
        ChildIterator<V> i = iteratorPool.getIterator(this, isTerminalNode(treeConfig));
        try {
            if ( s.length() == 0) {
                accept(visitor, treeConfig);
            } else {
                //get results from all nodes which share a prefix
                while(i.isValid()) {
                    int sharedCharCount = CharUtils.getSharedPrefixCount(s, i.getCurrentNode());
                    if ( sharedCharCount > 0 ) {
                        s.incrementStart(sharedCharCount);
                        i.getCurrentNode().accept(s, visitor, treeConfig);
                        s.decrementStart(sharedCharCount);
                    } else if (CharUtils.compareFirstChar(s, i.getCurrentNode(), treeConfig) == -1) {
                        break;
                        //since alphabetical, if we already found at least one match, and the next match fails
                        //we can assume all subsequent will fail
                    }
                    i.next();
                }
            }
        } finally {
            iteratorPool.returnIterator(i);
        }
    }

    /**
     * Visit all nodes
     */
    public boolean accept(TreeVisitor v, TreeConfig<V> treeConfig) {
        boolean shouldContinue = v.visit(this);
        ChildIterator<V> i = treeConfig.getIteratorPool().getIterator(this, isTerminalNode(treeConfig));
        while(i.isValid() && shouldContinue) {
            shouldContinue = i.getCurrentNode().accept(v, treeConfig);
            i.next();
        }
        v.visitComplete(this);
        return shouldContinue;
    }

    /**
     * Remove value v from key s, if s exists in the tree 
     */
    public Object remove(MutableCharSequence s, V value, TreeConfig<V> treeConfig) {
        return removeFromTree(s, value, treeConfig);
    }

    private Object removeFromTree(MutableCharSequence c, V value, TreeConfig<V> treeConfig) {
        Object result = null;
        if ( c.length() == 0 && payload != null) {
            ValueSupplier.ValueSupplierResult<V> r = treeConfig.getValueSupplier().removeValue(value, payload);
            payload = r.payload;
            result = r.result;
            r.clear();
        } else {
            ChildIterator<V> i = treeConfig.getIteratorPool().getIterator(this, isTerminalNode(treeConfig));
            while(i.isValid()) {
                RadixTree<V> current = i.getCurrentNode();
                int matchingChars = CharUtils.getSharedPrefixCount(c, current);
                if ( matchingChars == current.getLabelLength()) {
                    c.incrementStart(matchingChars);
                    result = current.removeFromTree(c, value, treeConfig);
                    if ( current.shouldBeCollapsed(treeConfig) ) {
                        joinOrRemove(i, current, treeConfig);
                    }
                    break;
                }
                i.next();
            }
        }
        return result;
    }
    
    protected boolean shouldBeCollapsed(TreeConfig<V> treeConfig) {
        return (isTerminalNode(treeConfig) && payload == null) || isOnlyOneChild(treeConfig);    
    }

    private void joinOrRemove(ChildIterator<V> i, RadixTree<V> current, TreeConfig<V> treeConfig) {
        if ( current.isTerminalNode(treeConfig)) {
            i.removeCurrent();    
        } else {
            //the current node now has just a single child
            //we should replace it with a new node which combines the prefixes
            RadixTree<V> firstChild = (RadixTree<V>)current.payload;
            RadixTree<V> joined = new RadixTree<V>();
            int newLength = current.getLabelLength() + firstChild.getLabelLength();
            joined.setLabel(firstChild.getRootSequence(), firstChild.getEnd() - newLength, firstChild.getEnd());
            joined.payload = firstChild.payload;
            i.replace(joined);        
        }
    }

    public Collection<V> getValues(Collection<V> targetCollection, TreeConfig<V> treeConfig) {
        if ( isTerminalNode(treeConfig) && payload != null ) { //should only ever be adding to a terminal node 
            treeConfig.getValueSupplier().addValuesToCollection(targetCollection, payload);
        } 
        return targetCollection;
    }

    public boolean isTerminalNode(TreeConfig<V> treeConfig) {
        return 
          end != 0 /* root */ && treeConfig.isTerminalNode(this); 
          //immutableSequence.charAt(end - 1) == CharUtils.TERMINAL_CHAR; /* root node */
    }

    public boolean isOnlyOneChild(TreeConfig<V> treeConfig) {
        return ! isTerminalNode(treeConfig) && payload != null && ((RadixTree<V>)payload).nextPeer == null;
    }
    
    public String getLabel() {
        return CharUtils.getString(immutableSequence, start, end);
    }

    private void setLabelFromNodeSuffix(RadixTree base, int trimFromStart) {
        this.immutableSequence = base.getRootSequence();
        this.start = (short)(base.getStart() + trimFromStart);
        this.end = base.getEnd();
    }

    private void setLabelFromNodePrefix(RadixTree base, int length) {
        this.immutableSequence = base.getRootSequence();
        this.start = base.getStart();
        this.end = (short)(base.getStart() + length);
    }
    
    public int getLabelLength() {
        return end - start;
    }
    
    public CharSequence getRootSequence() {
        return immutableSequence;
    }
    
    public short getStart() {
        return start;
    }
    
    public short getEnd() {
        return end;
    }

    public int length() {
        return getLabelLength();
    }
    
    public char getLastChar() {
        return immutableSequence.charAt(end - 1);
    }

    @Override
    public char charAt(int index) {
        return immutableSequence.charAt(start + index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        int newLength = end - start;
        return new MutableSequence(immutableSequence, this.start + start, this.start + start + newLength);
    }
}
