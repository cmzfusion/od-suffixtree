package com.od.radixtree;

import com.od.radixtree.map.RadixTreeMap;
import com.od.radixtree.map.SuffixTreeMap;
import org.chorusbdd.chorus.annotations.Handler;
import org.chorusbdd.chorus.annotations.Step;

/**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 10/06/13
 * Time: 21:31
 * To change this template use File | Settings | File Templates.
 */
@Handler("Suffix Tree Map")
public class SuffixTreeHandler extends AbstractRadixTreeHandler {

    @Step("I create a suffix tree")
    public void createIndex() {
        tree = new SuffixTreeMap<String>();
    }

    @Step("I create a suffix tree with multiple values per node")
    public void createMultipleValueTree() {
        tree = new SuffixTreeMap<String>(new HashSetValueSupplier<String>());
    }
    
}
