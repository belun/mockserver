package org.mockserver.model;

import junit.framework.TestCase;
import org.junit.Test;
import org.mockserver.Line;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockserver.model.HttpCallback.callback;

/**
 * @author jamesdbloom
 */
public class HttpCallbackTest {

    @Test
    public void shouldAlwaysCreateNewObject() {
        assertEquals(new HttpCallback().callback(), callback());
        assertNotSame(callback(), callback());
    }

    @Test
    public void returnsHost() {
        assertEquals("some_class", new HttpCallback().withCallbackClass("some_class").getCallbackClass());
    }

    @Test
    public void shouldReturnFormattedRequestInToString() {
        TestCase.assertEquals("{" + Line.SEPARATOR +
                        "  \"callbackClass\" : \"some_class\"" + Line.SEPARATOR +
                        "}",
                callback()
                        .withCallbackClass("some_class")
                        .toString()
        );
    }
}
