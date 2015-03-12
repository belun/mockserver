package org.mockserver.filters;

import org.junit.Test;
import org.mockserver.Line;
import org.mockserver.verify.VerificationSequence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockserver.model.HttpRequest.request;

/**
 * @author jamesdbloom
 */
public class LogFilterVerificationSequenceTest {

    @Test
    public void shouldPassVerificationWithNullRequest() {
        // given
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(request("one"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("three"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("four"));

        // then
        assertThat(logFilter.verify((VerificationSequence) null), is(""));
    }

    @Test
    public void shouldPassVerificationSequenceWithNoRequest() {
        // given
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(request("one"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("three"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("four"));

        // then
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(

                                )
                ),
                is(""));
    }

    @Test
    public void shouldPassVerificationSequenceWithOneRequest() {
        // given
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(request("one"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("three"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("four"));

        // then
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("one")
                                )
                ),
                is(""));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("multi")
                                )
                ),
                is(""));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("three")
                                )
                ),
                is(""));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("four")
                                )
                ),
                is(""));
    }

    @Test
    public void shouldPassVerificationSequenceWithTwoRequests() {
        // given
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(request("one"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("three"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("four"));

        // then - next to each other
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("one"),
                                        request("multi")
                                )
                ),
                is(""));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("multi"),
                                        request("three")
                                )
                ),
                is(""));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("three"),
                                        request("multi")
                                )
                ),
                is(""));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("multi"),
                                        request("four")
                                )
                ),
                is(""));
        // then - not next to each other
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("one"),
                                        request("three")
                                )
                ),
                is(""));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("one"),
                                        request("four")
                                )
                ),
                is(""));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("multi"),
                                        request("multi")
                                )
                ),
                is(""));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("three"),
                                        request("four")
                                )
                ),
                is(""));
    }

    @Test
    public void shouldFailVerificationSequenceWithOneRequest() {
        // given
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(request("one"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("three"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("four"));

        // then
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("five")
                                )
                ),
                is("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"five\"" + Line.SEPARATOR +
                        "} ]> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]>"));
    }

    @Test
    public void shouldFailVerificationSequenceWithTwoRequestsWrongOrder() {
        // given
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(request("one"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("three"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("four"));

        // then - next to each other
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("multi"),
                                        request("one")
                                )
                ),
                is("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "} ]> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]>"));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("four"),
                                        request("multi")
                                )
                ),
                is("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "} ]> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]>"));
        // then - not next to each other
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("three"),
                                        request("one")
                                )
                ),
                is("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "} ]> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]>"));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("four"),
                                        request("one")
                                )
                ),
                is("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "} ]> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]>"));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("four"),
                                        request("three")
                                )
                ),
                is("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "} ]> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]>"));
    }

    @Test
    public void shouldFailVerificationSequenceWithTwoRequestsFirstIncorrect() {
        // given
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(request("one"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("three"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("four"));

        // then - next to each other
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("zero"),
                                        request("multi")
                                )
                ),
                is("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"zero\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "} ]> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]>"));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("zero"),
                                        request("three")
                                )
                ),
                is("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"zero\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "} ]> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]>"));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("zero"),
                                        request("four")
                                )
                ),
                is("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"zero\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]>"));
    }

    @Test
    public void shouldFailVerificationSequenceWithTwoRequestsSecondIncorrect() {
        // given
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(request("one"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("three"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("four"));

        // then - next to each other
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("one"),
                                        request("five")
                                )
                ),
                is("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"five\"" + Line.SEPARATOR +
                        "} ]> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]>"));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("multi"),
                                        request("five")
                                )
                ),
                is("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"five\"" + Line.SEPARATOR +
                        "} ]> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]>"));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("three"),
                                        request("five")
                                )
                ),
                is("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"five\"" + Line.SEPARATOR +
                        "} ]> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]>"));
    }

    @Test
    public void shouldFailVerificationSequenceWithThreeRequestsWrongOrder() {
        // given
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(request("one"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("three"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("four"));

        // then - next to each other
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("one"),
                                        request("four"),
                                        request("multi")
                                )
                ),
                is("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "} ]> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]>"));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("one"),
                                        request("multi"),
                                        request("one")
                                )
                ),
                is("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "} ]> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]>"));
        // then - not next to each other
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("four"),
                                        request("one"),
                                        request("multi")
                                )
                ),
                is("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "} ]> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]>"));
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("multi"),
                                        request("three"),
                                        request("one")
                                )
                ),
                is("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "} ]> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]>"));

    }

    @Test
    public void shouldFailVerificationSequenceWithThreeRequestsDuplicateMissing() {
        // given
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(request("one"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("three"));
        logFilter.onRequest(request("multi"));
        logFilter.onRequest(request("four"));

        // then
        assertThat(logFilter.verify(
                        new VerificationSequence()
                                .withRequests(
                                        request("multi"),
                                        request("multi"),
                                        request("multi")
                                )
                ),
                is("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "} ]> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"one\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"three\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"multi\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"four\"" + Line.SEPARATOR +
                        "} ]>"));
    }

}
