package org.codehaus.xharness.log;

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LineBufferTest extends TestCase {
    public LineBufferTest(String name) {
        super(name);
    }

    public static void main(String[] args) throws Exception {
        if (System.getProperty("gui") != null) {
            String[] newArgs = new String[args.length + 2];
            System.arraycopy(args, 0, newArgs, 0, args.length);
            newArgs[args.length] = LineBufferTest.class.getName();
            newArgs[args.length + 1] = "-noloading";
            junit.swingui.TestRunner.main(newArgs);
        } else {
            String[] newArgs = new String[args.length + 1];
            System.arraycopy(args, 0, newArgs, 0, args.length);
            newArgs[args.length] = LineBufferTest.class.getName();
            junit.textui.TestRunner.main(newArgs);
        }
    }

    public static Test suite() {
        return new TestSuite(LineBufferTest.class);
    }
    
    public void testCtor() throws Exception {
        LineBuffer buffer = new LineBuffer();
        assertEquals("Wrong default priority", 0, buffer.getDefaultPriority());
        buffer = new LineBuffer(333);
        assertEquals("Wrong default priority", 333, buffer.getDefaultPriority());
    }
    
    public void testClear() throws Exception {
        LineBuffer buffer = new LineBuffer();
        buffer.logLine("foo");
        assertEquals("Wrong log", "foo", buffer.toString());
        buffer.clear();
        assertEquals("Wrong log", "", buffer.toString());
    }
    
    public void testLogLine() throws Exception {
        LineBuffer buffer = new LineBuffer(1);
        buffer.logLine("foo");
        buffer.logLine("FOO");
        buffer.logLine(null);
        buffer.logLine(0, "bar"); 
        buffer.logLine(2, "\nspam\reggs\f");
        buffer.logLine(2, "\n");
        buffer.logLine(2, "");
        buffer.logLine(2, "\fbacon\r");
        assertEquals("Wrong log", "foo\nFOO", buffer.toString(1));
        assertEquals("Wrong log", "bar", buffer.toString(0));
        assertEquals("Wrong log", "spam\neggs\nbacon", buffer.toString(2));
    }
    
    public void testMergeLine() throws Exception {
        LineBuffer buffer = new LineBuffer(1);
        buffer.logLine("foo");
        buffer.logLine(2, "bar"); 
        buffer.mergeLine(1, 2, null);
        buffer.mergeLine(1, 2, "foo");
        buffer.mergeLine(1, 0, "spam\nfoo");
        buffer.mergeLine(1, 3, "foo\neggs");
        buffer.mergeLine(1, 2, "bar");
        assertEquals("Wrong log", "foo", buffer.toString(1));
        assertEquals("Wrong log", "bar\nbar", buffer.toString(2));
        assertEquals("Wrong log", "spam", buffer.toString(0));
        assertEquals("Wrong log", "eggs", buffer.toString(3));
    }
    
    public void testToString() throws Exception {
        LineBuffer buffer = new LineBuffer();
        buffer.logLine(1, "foo");
        buffer.logLine(2, "bar"); 
        buffer.logLine(3, "spam"); 
        buffer.logLine(4, "eggs"); 
        buffer.logLine(1, "FOO");
        assertEquals("Wrong log", "foo\nbar\nspam\neggs\nFOO", buffer.toString());
        assertEquals("Wrong log", "foo\nFOO", buffer.toString(1));
        assertEquals("Wrong log", "bar", buffer.toString(2));
        assertEquals("Wrong log", "spam", buffer.toString(3));
        assertEquals("Wrong log", "eggs", buffer.toString(4));
        assertEquals("Wrong log", "foo\nFOO", buffer.toString(1, 1));
        assertEquals("Wrong log", "bar", buffer.toString(2, 2));
        assertEquals("Wrong log", "spam", buffer.toString(3, 3));
        assertEquals("Wrong log", "eggs", buffer.toString(4, 4));
        assertEquals("Wrong log", "foo\nbar\nFOO", buffer.toString(-10, 2));
        assertEquals("Wrong log", "bar\nspam", buffer.toString(2, 3));
        assertEquals("Wrong log", "spam\neggs", buffer.toString(3, 30));
        assertEquals("Wrong log", "foo bar spam eggs FOO", buffer.toString(' ', ""));
        assertEquals("Wrong log", 
                     "abcfooxabcbarxabcspamxabceggsxabcFOO", 
                     buffer.toString('x', "abc"));
        assertEquals("Wrong log", 
                     "1: foo-2: bar-3: spam-4: eggs-1: FOO", 
                     buffer.toString('-', null));
        assertEquals("Wrong log", "*foox*FOO", buffer.toString('x', "*", 1));
        assertEquals("Wrong log", "*foox*barx*FOO", buffer.toString('x', "*", 1, 2));
    }
    
    public void testGetPriority() throws Exception {
        LineBuffer buffer = new LineBuffer(100);
        assertEquals("Wrong default prio", 100, buffer.getDefaultPriority());
        assertEquals("Wrong max prio", Integer.MIN_VALUE, buffer.getMaxPriority());
        assertEquals("Wrong min prio", Integer.MAX_VALUE, buffer.getMinPriority());
        buffer.logLine(50, "foo");
        assertEquals("Wrong default prio", 100, buffer.getDefaultPriority());
        assertEquals("Wrong max prio", 50, buffer.getMaxPriority());
        assertEquals("Wrong min prio", 50, buffer.getMinPriority());
        buffer.logLine("bar");
        assertEquals("Wrong default prio", 100, buffer.getDefaultPriority());
        assertEquals("Wrong max prio", 100, buffer.getMaxPriority());
        assertEquals("Wrong min prio", 50, buffer.getMinPriority());
        buffer.logLine(150, "spam");
        assertEquals("Wrong default prio", 100, buffer.getDefaultPriority());
        assertEquals("Wrong max prio", 150, buffer.getMaxPriority());
        assertEquals("Wrong min prio", 50, buffer.getMinPriority());
    }
    
    public void testIterator() throws Exception {
        LineBuffer buffer = new LineBuffer();
        buffer.logLine(1, "foo");
        buffer.logLine(2, "spam"); 
        buffer.logLine(1, "bar"); 
        buffer.logLine(2, "eggs"); 
        buffer.logLine(3, "FOO");
        
        LogLine line;
        Iterator iter = buffer.iterator();
        
        assertTrue("Out of elements", iter.hasNext());
        line = (LogLine)iter.next();
        assertEquals("Wrong priority", 1, line.getPriority());
        assertEquals("Wrong element", "foo", line.getText());

        assertTrue("Out of elements", iter.hasNext());
        line = (LogLine)iter.next();
        assertEquals("Wrong priority", 2, line.getPriority());
        assertEquals("Wrong element", "spam", line.getText());

        assertTrue("Out of elements", iter.hasNext());
        line = (LogLine)iter.next();
        assertEquals("Wrong priority", 1, line.getPriority());
        line.setPriority(3);
        assertEquals("Wrong priority", 3, line.getPriority());
        assertEquals("Wrong element", "bar", line.getText());

        assertTrue("Out of elements", iter.hasNext());
        line = (LogLine)iter.next();
        assertEquals("Wrong priority", 2, line.getPriority());
        assertEquals("Wrong element", "eggs", line.getText());

        assertTrue("Out of elements", iter.hasNext());
        line = (LogLine)iter.next();
        assertEquals("Wrong priority", 3, line.getPriority());
        assertEquals("Wrong element", "FOO", line.getText());

        assertTrue("Too many elements", !iter.hasNext());

        iter = buffer.iterator(2);
        
        assertTrue("Out of elements", iter.hasNext());
        line = (LogLine)iter.next();
        assertEquals("Wrong priority", 2, line.getPriority());
        assertEquals("Wrong element", "spam", line.getText());

        try {
            iter.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException uoe) {
        	// pass
        }

        assertTrue("Out of elements", iter.hasNext());
        line = (LogLine)iter.next();
        assertEquals("Wrong priority", 2, line.getPriority());
        assertEquals("Wrong element", "eggs", line.getText());
        
        assertTrue("Too many elements", !iter.hasNext());

        iter = buffer.iterator(1, 2);
        
        assertTrue("Out of elements", iter.hasNext());
        line = (LogLine)iter.next();
        assertEquals("Wrong priority", 1, line.getPriority());
        assertEquals("Wrong element", "foo", line.getText());

        assertTrue("Out of elements", iter.hasNext());
        line = (LogLine)iter.next();
        assertEquals("Wrong priority", 2, line.getPriority());
        assertEquals("Wrong element", "spam", line.getText());

        assertTrue("Out of elements", iter.hasNext());
        line = (LogLine)iter.next();
        assertEquals("Wrong priority", 2, line.getPriority());
        assertEquals("Wrong element", "eggs", line.getText());

        assertTrue("Too many elements", !iter.hasNext());
    }
}
