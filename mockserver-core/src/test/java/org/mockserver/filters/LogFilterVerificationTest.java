package org.mockserver.filters;

import org.junit.Test;
import org.mockserver.Line;
import org.mockserver.model.HttpRequest;
import org.mockserver.verify.Verification;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockserver.verify.VerificationTimes.atLeast;
import static org.mockserver.verify.VerificationTimes.exactly;

/**
 * @author jamesdbloom
 */
public class LogFilterVerificationTest {

    @Test
    public void shouldPassVerificationWithNullRequest() {
        // given
        HttpRequest httpRequest = new HttpRequest().withPath("some_path");
        HttpRequest otherHttpRequest = new HttpRequest().withPath("some_other_path");
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(httpRequest);
        logFilter.onRequest(otherHttpRequest);
        logFilter.onRequest(httpRequest);

        // then
        assertThat(logFilter.verify((Verification) null), is(""));
    }

    @Test
    public void shouldPassVerificationWithDefaultTimes() {
        // given
        HttpRequest httpRequest = new HttpRequest().withPath("some_path");
        HttpRequest otherHttpRequest = new HttpRequest().withPath("some_other_path");
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(httpRequest);
        logFilter.onRequest(otherHttpRequest);
        logFilter.onRequest(httpRequest);

        // then
        assertThat(logFilter.verify(
                        new Verification()
                                .withRequest(
                                        new HttpRequest()
                                                .withPath("some_path")
                                )
                ),
                is(""));
        assertThat(logFilter.verify(
                        new Verification()
                                .withRequest(
                                        new HttpRequest()
                                                .withPath("some_other_path")
                                )
                ),
                is(""));
    }

    @Test
    public void shouldPassVerificationWithAtLeastTwoTimes() {
        // given
        HttpRequest httpRequest = new HttpRequest().withPath("some_path");
        HttpRequest otherHttpRequest = new HttpRequest().withPath("some_other_path");
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(httpRequest);
        logFilter.onRequest(otherHttpRequest);
        logFilter.onRequest(httpRequest);

        // then
        assertThat(logFilter.verify(
                        new Verification()
                                .withRequest(
                                        new HttpRequest().withPath("some_path")
                                )
                                .withTimes(atLeast(2))
                ),
                is(""));
    }

    @Test
    public void shouldPassVerificationWithAtLeastZeroTimes() {
        // given
        HttpRequest httpRequest = new HttpRequest().withPath("some_path");
        HttpRequest otherHttpRequest = new HttpRequest().withPath("some_other_path");
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(httpRequest);
        logFilter.onRequest(otherHttpRequest);
        logFilter.onRequest(httpRequest);

        // then
        assertThat(logFilter.verify(
                        new Verification()
                                .withRequest(
                                        new HttpRequest().withPath("some_non_matching_path")
                                )
                                .withTimes(atLeast(0))
                ),
                is(""));
    }

    @Test
    public void shouldPassVerificationWithExactlyTwoTimes() {
        // given
        HttpRequest httpRequest = new HttpRequest().withPath("some_path");
        HttpRequest otherHttpRequest = new HttpRequest().withPath("some_other_path");
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(httpRequest);
        logFilter.onRequest(otherHttpRequest);
        logFilter.onRequest(httpRequest);

        // then
        assertThat(logFilter.verify(
                        new Verification()
                                .withRequest(
                                        new HttpRequest()
                                                .withPath("some_path")
                                )
                                .withTimes(exactly(2))
                ),
                is(""));
    }

    @Test
    public void shouldPassVerificationWithExactlyZeroTimes() {
        // given
        HttpRequest httpRequest = new HttpRequest().withPath("some_path");
        HttpRequest otherHttpRequest = new HttpRequest().withPath("some_other_path");
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(httpRequest);
        logFilter.onRequest(otherHttpRequest);
        logFilter.onRequest(httpRequest);

        // then
        assertThat(logFilter.verify(
                        new Verification()
                                .withRequest(
                                        new HttpRequest()
                                                .withPath("some_non_matching_path")
                                )
                                .withTimes(exactly(0))
                ),
                is(""));
    }

    @Test
    public void shouldFailVerificationWithNullRequest() {
        // given
        LogFilter logFilter = new LogFilter();

        // then
        assertThat(logFilter.verify((Verification) null), is(""));
    }

    @Test
    public void shouldFailVerificationWithDefaultTimes() {
        // given
        HttpRequest httpRequest = new HttpRequest().withPath("some_path");
        HttpRequest otherHttpRequest = new HttpRequest().withPath("some_other_path");
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(httpRequest);
        logFilter.onRequest(otherHttpRequest);
        logFilter.onRequest(httpRequest);

        // then
        assertThat(logFilter.verify(
                        new Verification()
                                .withRequest(
                                        new HttpRequest().withPath("some_non_matching_path")
                                )
                ),
                is("Request not found at least once, expected:<{" + Line.SEPARATOR +
                        "  \"path\" : \"some_non_matching_path\"" + Line.SEPARATOR +
                        "}> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"some_path\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"some_other_path\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"some_path\"" + Line.SEPARATOR +
                        "} ]>"));
    }

    @Test
    public void shouldFailVerificationWithAtLeastTwoTimes() {
        // given
        HttpRequest httpRequest = new HttpRequest().withPath("some_path");
        HttpRequest otherHttpRequest = new HttpRequest().withPath("some_other_path");
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(httpRequest);
        logFilter.onRequest(otherHttpRequest);
        logFilter.onRequest(httpRequest);

        // then
        assertThat(logFilter.verify(
                        new Verification()
                                .withRequest(
                                        new HttpRequest().withPath("some_other_path")
                                )
                                .withTimes(atLeast(2))
                ),
                is("Request not found at least 2 times, expected:<{" + Line.SEPARATOR +
                        "  \"path\" : \"some_other_path\"" + Line.SEPARATOR +
                        "}> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"some_path\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"some_other_path\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"some_path\"" + Line.SEPARATOR +
                        "} ]>"));
    }

    @Test
    public void shouldFailVerificationWithExactTwoTimes() {
        // given
        HttpRequest httpRequest = new HttpRequest().withPath("some_path");
        HttpRequest otherHttpRequest = new HttpRequest().withPath("some_other_path");
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(httpRequest);
        logFilter.onRequest(otherHttpRequest);
        logFilter.onRequest(httpRequest);

        // then
        assertThat(logFilter.verify(
                        new Verification()
                                .withRequest(
                                        new HttpRequest()
                                                .withPath("some_other_path")
                                )
                                .withTimes(exactly(2))
                ),
                is("Request not found exactly 2 times, expected:<{" + Line.SEPARATOR +
                        "  \"path\" : \"some_other_path\"" + Line.SEPARATOR +
                        "}> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"some_path\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"some_other_path\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"some_path\"" + Line.SEPARATOR +
                        "} ]>"));
    }

    @Test
    public void shouldFailVerificationWithExactOneTime() {
        // given
        LogFilter logFilter = new LogFilter();

        // then
        assertThat(logFilter.verify(
                        new Verification()
                                .withRequest(
                                        new HttpRequest()
                                                .withPath("some_other_path")
                                )
                                .withTimes(exactly(1))
                ),
                is("Request not found exactly once, expected:<{" + Line.SEPARATOR +
                        "  \"path\" : \"some_other_path\"" + Line.SEPARATOR +
                        "}> but was:<>"));
    }

    @Test
    public void shouldFailVerificationWithExactZeroTimes() {
        // given
        HttpRequest httpRequest = new HttpRequest().withPath("some_path");
        HttpRequest otherHttpRequest = new HttpRequest().withPath("some_other_path");
        LogFilter logFilter = new LogFilter();

        // when
        logFilter.onRequest(httpRequest);
        logFilter.onRequest(otherHttpRequest);
        logFilter.onRequest(httpRequest);

        // then
        assertThat(logFilter.verify(
                        new Verification()
                                .withRequest(
                                        new HttpRequest()
                                                .withPath("some_other_path")
                                )
                                .withTimes(exactly(0))
                ),
                is("Request not found exactly 0 times, expected:<{" + Line.SEPARATOR +
                        "  \"path\" : \"some_other_path\"" + Line.SEPARATOR +
                        "}> but was:<[ {" + Line.SEPARATOR +
                        "  \"path\" : \"some_path\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"some_other_path\"" + Line.SEPARATOR +
                        "}, {" + Line.SEPARATOR +
                        "  \"path\" : \"some_path\"" + Line.SEPARATOR +
                        "} ]>"));
    }
}
