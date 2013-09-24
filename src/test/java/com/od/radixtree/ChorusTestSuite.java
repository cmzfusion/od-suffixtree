package com.od.radixtree;

import org.chorusbdd.chorus.ChorusSuite;
import org.junit.runner.RunWith;

/**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 06/05/13
 * Time: 20:08
 * To change this template use File | Settings | File Templates.
 */
@RunWith(ChorusSuite.class)
public class ChorusTestSuite {

    public static String getChorusArgs() {
        return "-f src/test/java/com/od/filtertable/radixtree -h com.od -e ";        
    }
}
