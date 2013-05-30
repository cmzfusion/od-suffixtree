package com.od.filtertable.suffixtree.visitor;

import com.od.filtertable.suffixtree.SuffixTree;

import java.io.PrintWriter;
import java.util.LinkedList;

/**
 * User: nick
 * Date: 22/05/13
 * Time: 17:33
 */
public class LoggingVisitor<V> implements SuffixTreeVisitor<V> {

    private int indentLevel = 0;
    private PrintWriter writer;
    private LinkedList<V> values = new LinkedList<V>();

    public LoggingVisitor(PrintWriter writer) {
        this.writer = writer;
    }
    
    @Override
    public boolean visit(SuffixTree<V> suffixTree) {
        indentLevel++;
        StringBuilder sb = new StringBuilder();
        addIndent(indentLevel, sb);
        sb.append(suffixTree.getLabel());
        values.clear();
        if ( suffixTree.getValues(values).size() > 0) {
            sb.append("\t vals: ");
            for (Object o : values) {
                sb.append(o.toString()).append(" ");
            }
        }
        sb.append("\n");
        writer.print(sb.toString());
        writer.flush();
        return true;
    }

    @Override
    public void visitComplete(SuffixTree<V> suffixTree) {
        indentLevel--;
    }

    private void addIndent(int level, StringBuilder sb) {
        for ( int loop=0; loop < level; loop++) {
            sb.append("  ");
        }
    }
}
