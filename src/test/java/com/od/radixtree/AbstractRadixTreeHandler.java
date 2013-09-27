package com.od.radixtree;

import com.od.radixtree.map.TrieMap;
import com.od.radixtree.visitor.KeySetVisitor;
import com.od.radixtree.visitor.LoggingVisitor;
import com.od.radixtree.visitor.NodeCountVisitor;
import org.chorusbdd.chorus.annotations.Step;
import org.chorusbdd.chorus.util.assertion.ChorusAssert;

import java.io.PrintWriter;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 10/06/13
 * Time: 21:27
 * To change this template use File | Settings | File Templates.
 */
public class AbstractRadixTreeHandler extends ChorusAssert {
    
    protected TrieMap<String> tree;

    private String removedValue;

    @Step("I add a value (.*) under key (.*)")
    public void addAValue(String value, String key) {
        tree.put(key, value);
    }

    @Step("I remove the value under key (.*)")
    public void remove(String key) {
        removedValue = tree.remove(key);
    }

    @Step("the removed value was (.*)")
    public void checkRemovedValue(String key) {
        assertTrue((key.equals("null") && removedValue == null) || removedValue.equals(key));
    }

    @Step("I show the tree structure")
    public void showTheTreeStucture() {
        LoggingVisitor v = new LoggingVisitor(new PrintWriter(System.out), tree.getTreeConfig());
        tree.accept(v);
    }

    @Step("the radix tree contains keys (.*)")
    public void checkContainsKeys(String keys) {
//        tree.compress();
        List<String> expected = getExpectedList(keys);

        KeySetVisitor<String> v = new KeySetVisitor<String>();
        tree.accept(v);
        List<String> actual = new ArrayList<String>(v.getLabels());

        assertEquals("Expected " + keys, expected, actual);
    }

    @Step("the number of nodes is (\\d+)")
    public void countNodes(int number) {
        NodeCountVisitor n = new NodeCountVisitor();
        tree.accept(n);

        //-1 since we don't include the root node in the count for this test
        assertEquals("Expect " + number + " nodes", number, n.getNodeCount() - 1);
    }

    @Step("a search for (.*) returns (.*)")
    public void doSearch(String key, String values) {
        orderedSearch(key, values, Integer.MAX_VALUE);
    }

    @Step("a search for (.*) gives the set (.*)")
    public void doSearchForSet(String key, String values) {
        unorderedSearch(key, values, Integer.MAX_VALUE);
    }

    @Step("a search with maxItems=(\\d+) for (.*) returns (.*)")
    public void doSearch(int maxItems, String key, String values) {
        orderedSearch(key, values, maxItems);
    }

    @Step("a search with maxItems=(\\d+) for (.*) gives (\\d) items")
    public void doSearchForSetMaxValues(int maxItems, String key, int itemCount) {
        HashSet<String> collection = new HashSet<String>();
        tree.getStartingWith(key, collection, maxItems);
        assertEquals("Expecting " + itemCount + " items", itemCount, collection.size());
    }

    private void orderedSearch(String key, String values, int maxItems) {
        List<String> expected = getExpectedList(values);

        ArrayList<String> collection = new ArrayList<String>();

        doSearchAndCompare(key, maxItems, expected, collection);
    }

    private void unorderedSearch(String key, String values, int maxItems) {
        Set<String> expected = getExpectedSet(values);

        HashSet<String> collection = new HashSet<String>();

        doSearchAndCompare(key, maxItems, expected, collection);
    }

    private void doSearchAndCompare(String key, int maxItems, Collection<String> expected, Collection<String> collection) {
        Collection<String> actual = maxItems == Integer.MAX_VALUE ?
                tree.getStartingWith(key, collection) :
                tree.getStartingWith(key, collection, maxItems);

        assertEquals("expected values in search results", expected, actual);
    }

    private List<String> getExpectedList(String values) {
        StringTokenizer st = new StringTokenizer(values, ",");
        List<String> expected = new ArrayList<String>();
        while(st.hasMoreTokens()) {
            expected.add(st.nextToken().trim());
        }
        return expected;
    }

    private Set<String> getExpectedSet(String values) {
        StringTokenizer st = new StringTokenizer(values, ",");
        Set<String> expected = new HashSet<String>();
        while(st.hasMoreTokens()) {
            expected.add(st.nextToken().trim());
        }
        return expected;
    }
}
