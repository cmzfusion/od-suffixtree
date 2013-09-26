package com.od.radixtree;

/**
 * Created with IntelliJ IDEA.
 * User: GA2EBBU
 * Date: 26/09/13
 * Time: 16:23
 */
public interface ValueFilter<V> {

    ValueFilter NULL_VALUE_FILTER = new ValueFilter() {
        public boolean include(Object value) {
            return true;
        }
    };

    boolean include(V value);
}
