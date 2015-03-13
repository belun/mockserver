package org.mockserver.integration.server;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.Line;
import org.mockserver.client.netty.NettyHttpClient;
import org.mockserver.client.netty.SocketConnectionException;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.*;
import org.mockserver.verify.VerificationTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.*;
import static org.mockserver.configuration.SystemProperties.bufferSize;
import static org.mockserver.configuration.SystemProperties.maxTimeout;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.matchers.Times.once;
import static org.mockserver.model.BinaryBody.binary;
import static org.mockserver.model.Cookie.cookie;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpCallback.callback;
import static org.mockserver.model.HttpForward.forward;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.notFoundResponse;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.OutboundHttpRequest.outboundRequest;
import static org.mockserver.model.Parameter.param;
import static org.mockserver.model.ParameterBody.params;
import static org.mockserver.model.StringBody.*;

/**
 * @author jamesdbloom
 */
public abstract class AbstractClientServerIntegrationTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected static MockServerClient mockServerClient;
    protected static String servletContext = "";
    protected List<String> headersToIgnore = Arrays.asList(
            "server",
            "expires",
            "date",
            "host",
            "connection",
            "user-agent",
            "content-type",
            "content-length",
            "accept-encoding",
            "transfer-encoding"
    );
    // http client
    private NettyHttpClient httpClient = new NettyHttpClient();

    public AbstractClientServerIntegrationTest() {
        bufferSize(1024);
        maxTimeout(TimeUnit.SECONDS.toMillis(10));
    }

    public abstract int getMockServerPort();

    public abstract int getMockServerSecurePort();

    public abstract int getTestServerPort();

    protected String calculatePath(String path) {
        return "/" + path;
    }

    @Before
    public void resetServer() {
        mockServerClient.reset();
    }

    @Test
    public void clientCanCallServerForSimpleResponse() {
        // when
        mockServerClient.when(request()).respond(response().withBody("some_body"));

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body"),
                makeRequest(
                        request()
                                .withPath(calculatePath("")),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body"),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerForForwardInHTTP() {
        // when
        mockServerClient
                .when(
                        request()
                                .withPath(calculatePath("echo"))
                )
                .forward(
                        forward()
                                .withHost("127.0.0.1")
                                .withPort(getTestServerPort())
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withHeaders(
                                header("x-test", "test_headers_and_body")
                        )
                        .withBody("an_example_body_http"),
                makeRequest(
                        request()
                                .withPath(calculatePath("echo"))
                                .withMethod("POST")
                                .withHeaders(
                                        header("x-test", "test_headers_and_body")
                                )
                                .withBody("an_example_body_http"),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withHeaders(
                                header("x-test", "test_headers_and_body")
                        )
                        .withBody("an_example_body_https"),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("echo"))
                                .withMethod("POST")
                                .withHeaders(
                                        header("x-test", "test_headers_and_body")
                                )
                                .withBody("an_example_body_https"),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerForForwardInHTTPS() {
        // when
        mockServerClient
                .when(
                        request()
                                .withPath(calculatePath("echo"))
                )
                .forward(
                        forward()
                                .withHost("127.0.0.1")
                                .withPort(getTestServerPort())
                                .withScheme(HttpForward.Scheme.HTTPS)
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withHeaders(
                                header("x-test", "test_headers_and_body")
                        )
                        .withBody("an_example_body_http"),
                makeRequest(
                        request()
                                .withPath(calculatePath("echo"))
                                .withMethod("POST")
                                .withHeaders(
                                        header("x-test", "test_headers_and_body")
                                )
                                .withBody("an_example_body_http"),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withHeaders(
                                header("x-test", "test_headers_and_body")
                        )
                        .withBody("an_example_body_https"),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("echo"))
                                .withMethod("POST")
                                .withHeaders(
                                        header("x-test", "test_headers_and_body")
                                )
                                .withBody("an_example_body_https"),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerForResponseThenForward() {
        // when
        mockServerClient
                .when(
                        request()
                                .withPath(calculatePath("echo")),
                        once()
                )
                .forward(
                        forward()
                                .withHost("127.0.0.1")
                                .withPort(getTestServerPort())
                );
        mockServerClient
                .when(
                        request()
                                .withPath(calculatePath("test_headers_and_body")),
                        once()
                )
                .respond(
                        response()
                                .withBody("some_body")
                );

        // then
        // - forward
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withHeaders(
                                header("x-test", "test_headers_and_body")
                        )
                        .withBody("an_example_body"),
                makeRequest(
                        request()
                                .withPath(calculatePath("echo"))
                                .withMethod("POST")
                                .withHeaders(
                                        header("x-test", "test_headers_and_body")
                                )
                                .withBody("an_example_body"),
                        headersToIgnore)
        );
        // - respond
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body"),
                makeRequest(
                        request()
                                .withPath(calculatePath("test_headers_and_body")),
                        headersToIgnore)
        );
        // - no response or forward
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withPath(calculatePath("test_headers_and_body")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerForCallbackInHTTP() {
        // when
        mockServerClient
                .when(
                        request()
                                .withPath(calculatePath("callback"))
                )
                .callback(
                        callback()
                                .withCallbackClass("org.mockserver.integration.callback.PrecannedTestExpectationCallback")
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withHeaders(
                                header("x-callback", "test_callback_header")
                        )
                        .withBody("a_callback_response"),
                makeRequest(
                        request()
                                .withPath(calculatePath("callback"))
                                .withMethod("POST")
                                .withHeaders(
                                        header("x-test", "test_headers_and_body")
                                )
                                .withBody("an_example_body_http"),
                        headersToIgnore
                )
        );

        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withHeaders(
                                header("x-callback", "test_callback_header")
                        )
                        .withBody("a_callback_response"),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("callback"))
                                .withMethod("POST")
                                .withHeaders(
                                        header("x-test", "test_headers_and_body")
                                )
                                .withBody("an_example_body_https"),
                        headersToIgnore
                )
        );
    }

    @Test
    public void clientCanCallServerForResponseWithNoBody() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_path"))
                )
                .respond(
                        response()
                                .withStatusCode(200)
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code()),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path"))
                                .withMethod("POST"),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code()),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("some_path"))
                                .withMethod("POST"),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerMatchPath() {
        // when
        mockServerClient
                .when(
                        request()
                                .withPath(calculatePath("some_path1"))
                )
                .respond(
                        response()
                                .withBody("some_body1")
                );
        mockServerClient
                .when(
                        request()
                                .withPath(calculatePath("some_path2"))
                )
                .respond(
                        response()
                                .withBody("some_body2")
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body2"),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path2")),
                        headersToIgnore)
        );
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body1"),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path1")),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body2"),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("some_path2")),
                        headersToIgnore)
        );
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body1"),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("some_path1")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerMatchPathXTimes() {
        // when
        mockServerClient
                .when(
                        request()
                                .withPath(calculatePath("some_path")), exactly(2)
                )
                .respond(
                        response()
                                .withBody("some_body")
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body"),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path")),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body"),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("some_path")),
                        headersToIgnore)
        );
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path")),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("some_path")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanVerifyRequestsReceived() {
        // when
        mockServerClient
                .when(
                        request()
                                .withPath(calculatePath("some_path")), exactly(2)
                )
                .respond(
                        response()
                                .withBody("some_body")
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body"),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path")),
                        headersToIgnore)
        );
        mockServerClient.verify(request()
                .withPath(calculatePath("some_path")));
        mockServerClient.verify(request()
                .withPath(calculatePath("some_path")), VerificationTimes.exactly(1));

        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body"),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("some_path")),
                        headersToIgnore)
        );
        mockServerClient.verify(request().withPath(calculatePath("some_path")), VerificationTimes.atLeast(1));
        mockServerClient.verify(request().withPath(calculatePath("some_path")), VerificationTimes.exactly(2));
    }

    @Test
    public void clientCanVerifyRequestsReceivedWithNoBody() {
        // when
        mockServerClient.when(request().withPath(calculatePath("some_path")), exactly(2)).respond(response());

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code()),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path")),
                        headersToIgnore)
        );
        mockServerClient.verify(request()
                .withPath(calculatePath("some_path")));
        mockServerClient.verify(request()
                .withPath(calculatePath("some_path")), VerificationTimes.exactly(1));
    }

    @Test
    public void clientCanVerifyNotEnoughRequestsReceived() {
        // when
        mockServerClient.when(request().withPath(calculatePath("some_path")), exactly(2)).respond(response().withBody("some_body"));

        // then
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body"),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path")),
                        headersToIgnore)
        );
        try {
            mockServerClient.verify(request()
                    .withPath(calculatePath("some_path")), VerificationTimes.atLeast(2));
            fail();
        } catch (AssertionError ae) {
            assertThat(ae.getMessage(), startsWith("Request not found at least 2 times, expected:<{" + Line.SEPARATOR +
                    "  \"path\" : \"" + calculatePath("some_path") + "\"" + Line.SEPARATOR +
                    "}> but was:<{" + Line.SEPARATOR +
                    "  \"method\" : \"GET\"," + Line.SEPARATOR +
                    "  \"path\" : \"" + calculatePath("some_path") + "\"," + Line.SEPARATOR));
        }
    }

    @Test
    public void clientCanVerifyTooManyRequestsReceived() {
        // when
        mockServerClient.when(request().withPath(calculatePath("some_path")), exactly(2)).respond(response().withBody("some_body"));

        // then
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body"),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path")),
                        headersToIgnore)
        );
        try {
            mockServerClient.verify(request()
                    .withPath(calculatePath("some_path")), VerificationTimes.exactly(0));
            fail();
        } catch (AssertionError ae) {
            assertThat(ae.getMessage(), startsWith("Request not found exactly 0 times, expected:<{" + Line.SEPARATOR +
                    "  \"path\" : \"" + calculatePath("some_path") + "\"" + Line.SEPARATOR +
                    "}> but was:<{" + Line.SEPARATOR +
                    "  \"method\" : \"GET\"," + Line.SEPARATOR +
                    "  \"path\" : \"" + calculatePath("some_path") + "\"," + Line.SEPARATOR));
        }
    }

    @Test
    public void clientCanVerifyNotMatchingRequestsReceived() {
        // when
        mockServerClient.when(request().withPath(calculatePath("some_path")), exactly(2)).respond(response().withBody("some_body"));

        // then
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body"),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path")),
                        headersToIgnore)
        );
        try {
            mockServerClient.verify(request()
                    .withPath(calculatePath("some_other_path")), VerificationTimes.exactly(2));
            fail();
        } catch (AssertionError ae) {
            assertThat(ae.getMessage(), startsWith("Request not found exactly 2 times, expected:<{" + Line.SEPARATOR +
                    "  \"path\" : \"" + calculatePath("some_other_path") + "\"" + Line.SEPARATOR +
                    "}> but was:<{" + Line.SEPARATOR +
                    "  \"method\" : \"GET\"," + Line.SEPARATOR +
                    "  \"path\" : \"" + calculatePath("some_path") + "\"," + Line.SEPARATOR));
        }
    }

    @Test
    public void clientCanVerifySequenceOfRequestsReceived() {
        // when
        mockServerClient.when(request().withPath(calculatePath("some_path.*")), exactly(6)).respond(response().withBody("some_body"));

        // then
        // - in http
        assertEquals(
                response("some_body"),
                makeRequest(
                        request().withPath(calculatePath("some_path_one")),
                        headersToIgnore)
        );
        assertEquals(
                response("some_body"),
                makeRequest(
                        request().withPath(calculatePath("some_path_two")),
                        headersToIgnore)
        );
        assertEquals(
                response("some_body"),
                makeRequest(
                        request().withPath(calculatePath("some_path_three")),
                        headersToIgnore)
        );
        mockServerClient.verify(request(calculatePath("some_path_one")), request(calculatePath("some_path_three")));
        mockServerClient.verify(request(calculatePath("some_path_one")), request(calculatePath("some_path_two")));
        mockServerClient.verify(request(calculatePath("some_path_one")), request(calculatePath("some_path_two")), request(calculatePath("some_path_three")));

        // - in https
        assertEquals(
                response("some_body"),
                makeRequest(
                        request().setSecure(true)
                                .withPath(calculatePath("some_path_one")),
                        headersToIgnore)
        );
        assertEquals(
                response("some_body"),
                makeRequest(
                        request().setSecure(true)
                                .withPath(calculatePath("some_path_two")),
                        headersToIgnore)
        );
        assertEquals(
                response("some_body"),
                makeRequest(
                        request().setSecure(true)
                                .withPath(calculatePath("some_path_three")),
                        headersToIgnore)
        );
        mockServerClient.verify(request(calculatePath("some_path_one")), request(calculatePath("some_path_three")));
        mockServerClient.verify(request(calculatePath("some_path_one")), request(calculatePath("some_path_two")));
        mockServerClient.verify(request(calculatePath("some_path_one")), request(calculatePath("some_path_two")), request(calculatePath("some_path_three")));
    }

    @Test
    public void clientCanVerifySequenceOfRequestsReceivedEvenThoseNotMatchingAnException() {
        // when
        mockServerClient.when(request().withPath(calculatePath("some_path.*")), exactly(4)).respond(response().withBody("some_body"));

        // then
        // - in http
        assertEquals(
                response("some_body"),
                makeRequest(
                        request().withPath(calculatePath("some_path_one")),
                        headersToIgnore)
        );
        assertEquals(
                notFoundResponse(),
                makeRequest(
                        request().withPath(calculatePath("not_found")),
                        headersToIgnore)
        );
        assertEquals(
                response("some_body"),
                makeRequest(
                        request().withPath(calculatePath("some_path_three")),
                        headersToIgnore)
        );
        mockServerClient.verify(request(calculatePath("some_path_one")), request(calculatePath("some_path_three")));
        mockServerClient.verify(request(calculatePath("some_path_one")), request(calculatePath("not_found")));
        mockServerClient.verify(request(calculatePath("some_path_one")), request(calculatePath("not_found")), request(calculatePath("some_path_three")));

        // - in https
        assertEquals(
                response("some_body"),
                makeRequest(
                        request().setSecure(true)
                                .withPath(calculatePath("some_path_one")),
                        headersToIgnore)
        );
        assertEquals(
                notFoundResponse(),
                makeRequest(
                        request().setSecure(true)
                                .withPath(calculatePath("not_found")),
                        headersToIgnore)
        );
        assertEquals(
                response("some_body"),
                makeRequest(
                        request().setSecure(true)
                                .withPath(calculatePath("some_path_three")),
                        headersToIgnore)
        );
        mockServerClient.verify(request(calculatePath("some_path_one")), request(calculatePath("some_path_three")));
        mockServerClient.verify(request(calculatePath("some_path_one")), request(calculatePath("not_found")));
        mockServerClient.verify(request(calculatePath("some_path_one")), request(calculatePath("not_found")), request(calculatePath("some_path_three")));
    }

    @Test
    public void clientCanVerifySequenceOfRequestsNotReceived() {
        // when
        mockServerClient.when(request().withPath(calculatePath("some_path.*")), exactly(6)).respond(response().withBody("some_body"));

        // then
        // - in http
        assertEquals(
                response("some_body"),
                makeRequest(
                        request().withPath(calculatePath("some_path_one")),
                        headersToIgnore)
        );
        assertEquals(
                response("some_body"),
                makeRequest(
                        request().withPath(calculatePath("some_path_two")),
                        headersToIgnore)
        );
        assertEquals(
                response("some_body"),
                makeRequest(
                        request().withPath(calculatePath("some_path_three")),
                        headersToIgnore)
        );
        try {
            mockServerClient.verify(request(calculatePath("some_path_two")), request(calculatePath("some_path_one")));
            fail();
        } catch (AssertionError ae) {
            assertThat(ae.getMessage(), startsWith("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                    "  \"path\" : \"" + calculatePath("some_path_two") + "\"" + Line.SEPARATOR +
                    "}, {" + Line.SEPARATOR +
                    "  \"path\" : \"" + calculatePath("some_path_one") + "\"" + Line.SEPARATOR +
                    "} ]> but was:<[ {" + Line.SEPARATOR +
                    "  \"method\" : \"GET\"," + Line.SEPARATOR +
                    "  \"path\" : \"" + calculatePath("some_path_one") + "\"," + Line.SEPARATOR));
        }
        try {
            mockServerClient.verify(request(calculatePath("some_path_three")), request(calculatePath("some_path_two")));
            fail();
        } catch (AssertionError ae) {
            assertThat(ae.getMessage(), startsWith("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                    "  \"path\" : \"" + calculatePath("some_path_three") + "\"" + Line.SEPARATOR +
                    "}, {" + Line.SEPARATOR +
                    "  \"path\" : \"" + calculatePath("some_path_two") + "\"" + Line.SEPARATOR +
                    "} ]> but was:<[ {" + Line.SEPARATOR +
                    "  \"method\" : \"GET\"," + Line.SEPARATOR +
                    "  \"path\" : \"" + calculatePath("some_path_one") + "\"," + Line.SEPARATOR));
        }
        try {
            mockServerClient.verify(request(calculatePath("some_path_four")));
            fail();
        } catch (AssertionError ae) {
            assertThat(ae.getMessage(), startsWith("Request sequence not found, expected:<[ {" + Line.SEPARATOR +
                    "  \"path\" : \"" + calculatePath("some_path_four") + "\"" + Line.SEPARATOR +
                    "} ]> but was:<[ {" + Line.SEPARATOR +
                    "  \"method\" : \"GET\"," + Line.SEPARATOR +
                    "  \"path\" : \"" + calculatePath("some_path_one") + "\"," + Line.SEPARATOR));
        }
    }

    @Test
    public void clientCanCallServerMatchBodyWithXPath() {
        // when
        mockServerClient.when(request().withBody(xpath("/bookstore/book[price>35]/price")), exactly(2)).respond(response().withBody("some_body"));

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body"),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path"))
                                .withMethod("POST")
                                .withBody(new StringBody("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "<bookstore>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "<book category=\"COOKING\">" + Line.SEPARATOR +
                                        "  <title lang=\"en\">Everyday Italian</title>" + Line.SEPARATOR +
                                        "  <author>Giada De Laurentiis</author>" + Line.SEPARATOR +
                                        "  <year>2005</year>" + Line.SEPARATOR +
                                        "  <price>30.00</price>" + Line.SEPARATOR +
                                        "</book>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "<book category=\"CHILDREN\">" + Line.SEPARATOR +
                                        "  <title lang=\"en\">Harry Potter</title>" + Line.SEPARATOR +
                                        "  <author>J K. Rowling</author>" + Line.SEPARATOR +
                                        "  <year>2005</year>" + Line.SEPARATOR +
                                        "  <price>29.99</price>" + Line.SEPARATOR +
                                        "</book>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "<book category=\"WEB\">" + Line.SEPARATOR +
                                        "  <title lang=\"en\">Learning XML</title>" + Line.SEPARATOR +
                                        "  <author>Erik T. Ray</author>" + Line.SEPARATOR +
                                        "  <year>2003</year>" + Line.SEPARATOR +
                                        "  <price>39.95</price>" + Line.SEPARATOR +
                                        "</book>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "</bookstore>", Body.Type.STRING)),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body"),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("some_path"))
                                .withMethod("POST")
                                .withBody(new StringBody("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "<bookstore>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "<book category=\"COOKING\">" + Line.SEPARATOR +
                                        "  <title lang=\"en\">Everyday Italian</title>" + Line.SEPARATOR +
                                        "  <author>Giada De Laurentiis</author>" + Line.SEPARATOR +
                                        "  <year>2005</year>" + Line.SEPARATOR +
                                        "  <price>30.00</price>" + Line.SEPARATOR +
                                        "</book>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "<book category=\"CHILDREN\">" + Line.SEPARATOR +
                                        "  <title lang=\"en\">Harry Potter</title>" + Line.SEPARATOR +
                                        "  <author>J K. Rowling</author>" + Line.SEPARATOR +
                                        "  <year>2005</year>" + Line.SEPARATOR +
                                        "  <price>29.99</price>" + Line.SEPARATOR +
                                        "</book>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "<book category=\"WEB\">" + Line.SEPARATOR +
                                        "  <title lang=\"en\">Learning XML</title>" + Line.SEPARATOR +
                                        "  <author>Erik T. Ray</author>" + Line.SEPARATOR +
                                        "  <year>2003</year>" + Line.SEPARATOR +
                                        "  <price>39.95</price>" + Line.SEPARATOR +
                                        "</book>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "</bookstore>", Body.Type.STRING)),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerMatchBodyWithJson() {
        // when
        mockServerClient.when(request().withBody(json("{" + Line.SEPARATOR +
                "    \"GlossDiv\": {" + Line.SEPARATOR +
                "        \"title\": \"S\", " + Line.SEPARATOR +
                "        \"GlossList\": {" + Line.SEPARATOR +
                "            \"GlossEntry\": {" + Line.SEPARATOR +
                "                \"ID\": \"SGML\", " + Line.SEPARATOR +
                "                \"SortAs\": \"SGML\", " + Line.SEPARATOR +
                "                \"GlossTerm\": \"Standard Generalized Markup Language\", " + Line.SEPARATOR +
                "                \"Acronym\": \"SGML\", " + Line.SEPARATOR +
                "                \"Abbrev\": \"ISO 8879:1986\", " + Line.SEPARATOR +
                "                \"GlossDef\": {" + Line.SEPARATOR +
                "                    \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\", " + Line.SEPARATOR +
                "                    \"GlossSeeAlso\": [" + Line.SEPARATOR +
                "                        \"GML\", " + Line.SEPARATOR +
                "                        \"XML\"" + Line.SEPARATOR +
                "                    ]" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                \"GlossSee\": \"markup\"" + Line.SEPARATOR +
                "            }" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}")), exactly(2)).respond(response().withBody("some_body"));

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body"),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path"))
                                .withMethod("POST")
                                .withBody("{" + Line.SEPARATOR +
                                        "    \"title\": \"example glossary\", " + Line.SEPARATOR +
                                        "    \"GlossDiv\": {" + Line.SEPARATOR +
                                        "        \"title\": \"S\", " + Line.SEPARATOR +
                                        "        \"GlossList\": {" + Line.SEPARATOR +
                                        "            \"GlossEntry\": {" + Line.SEPARATOR +
                                        "                \"ID\": \"SGML\", " + Line.SEPARATOR +
                                        "                \"SortAs\": \"SGML\", " + Line.SEPARATOR +
                                        "                \"GlossTerm\": \"Standard Generalized Markup Language\", " + Line.SEPARATOR +
                                        "                \"Acronym\": \"SGML\", " + Line.SEPARATOR +
                                        "                \"Abbrev\": \"ISO 8879:1986\", " + Line.SEPARATOR +
                                        "                \"GlossDef\": {" + Line.SEPARATOR +
                                        "                    \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\", " + Line.SEPARATOR +
                                        "                    \"GlossSeeAlso\": [" + Line.SEPARATOR +
                                        "                        \"GML\", " + Line.SEPARATOR +
                                        "                        \"XML\"" + Line.SEPARATOR +
                                        "                    ]" + Line.SEPARATOR +
                                        "                }, " + Line.SEPARATOR +
                                        "                \"GlossSee\": \"markup\"" + Line.SEPARATOR +
                                        "            }" + Line.SEPARATOR +
                                        "        }" + Line.SEPARATOR +
                                        "    }" + Line.SEPARATOR +
                                        "}"),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body"),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("some_path"))
                                .withMethod("POST")
                                .withBody("{" + Line.SEPARATOR +
                                        "    \"title\": \"example glossary\", " + Line.SEPARATOR +
                                        "    \"GlossDiv\": {" + Line.SEPARATOR +
                                        "        \"title\": \"S\", " + Line.SEPARATOR +
                                        "        \"GlossList\": {" + Line.SEPARATOR +
                                        "            \"GlossEntry\": {" + Line.SEPARATOR +
                                        "                \"ID\": \"SGML\", " + Line.SEPARATOR +
                                        "                \"SortAs\": \"SGML\", " + Line.SEPARATOR +
                                        "                \"GlossTerm\": \"Standard Generalized Markup Language\", " + Line.SEPARATOR +
                                        "                \"Acronym\": \"SGML\", " + Line.SEPARATOR +
                                        "                \"Abbrev\": \"ISO 8879:1986\", " + Line.SEPARATOR +
                                        "                \"GlossDef\": {" + Line.SEPARATOR +
                                        "                    \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\", " + Line.SEPARATOR +
                                        "                    \"GlossSeeAlso\": [" + Line.SEPARATOR +
                                        "                        \"GML\", " + Line.SEPARATOR +
                                        "                        \"XML\"" + Line.SEPARATOR +
                                        "                    ]" + Line.SEPARATOR +
                                        "                }, " + Line.SEPARATOR +
                                        "                \"GlossSee\": \"markup\"" + Line.SEPARATOR +
                                        "            }" + Line.SEPARATOR +
                                        "        }" + Line.SEPARATOR +
                                        "    }" + Line.SEPARATOR +
                                        "}"),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanSetupExpectationForPDF() throws IOException {
        // when
        byte[] pdfBytes = new byte[1024];
        IOUtils.readFully(getClass().getClassLoader().getResourceAsStream("test.pdf"), pdfBytes);
        mockServerClient
                .when(
                        request()
                                .withPath(calculatePath("ws/rest/user/[0-9]+/document/[0-9]+\\.pdf"))
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.OK_200.code())
                                .withHeaders(
                                        header(HttpHeaders.CONTENT_TYPE, MediaType.PDF.toString()),
                                        header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"test.pdf\"; filename=\"test.pdf\""),
                                        header(HttpHeaders.CACHE_CONTROL, "must-revalidate, post-check=0, pre-check=0")
                                )
                                .withBody(binary(pdfBytes))
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withHeaders(
                                header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"test.pdf\"; filename=\"test.pdf\""),
                                header(HttpHeaders.CACHE_CONTROL, "must-revalidate, post-check=0, pre-check=0")
                        )
                        .withBody(binary(pdfBytes)),
                makeRequest(
                        request()
                                .withPath(calculatePath("ws/rest/user/1/document/2.pdf"))
                                .withMethod("GET"),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withHeaders(
                                header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"test.pdf\"; filename=\"test.pdf\""),
                                header(HttpHeaders.CACHE_CONTROL, "must-revalidate, post-check=0, pre-check=0")
                        )
                        .withBody(binary(pdfBytes)),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("ws/rest/user/1/document/2.pdf"))
                                .withMethod("GET"),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanSetupExpectationForPNG() throws IOException {
        // when
        byte[] pngBytes = new byte[1024];
        IOUtils.readFully(getClass().getClassLoader().getResourceAsStream("test.png"), pngBytes);
        mockServerClient
                .when(
                        request()
                                .withPath(calculatePath("ws/rest/user/[0-9]+/icon/[0-9]+\\.png"))
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.OK_200.code())
                                .withHeaders(
                                        header(HttpHeaders.CONTENT_TYPE, MediaType.PNG.toString()),
                                        header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"test.png\"; filename=\"test.png\"")
                                )
                                .withBody(binary(pngBytes))
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withHeaders(
                                header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"test.png\"; filename=\"test.png\"")
                        )
                        .withBody(binary(pngBytes)),
                makeRequest(
                        request()
                                .withPath(calculatePath("ws/rest/user/1/icon/1.png"))
                                .withMethod("GET"),
                        headersToIgnore)
        );

        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withHeaders(
                                header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"test.png\"; filename=\"test.png\"")
                        )
                        .withBody(binary(pngBytes)),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("ws/rest/user/1/icon/1.png"))
                                .withMethod("GET"),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanSetupExpectationForPDFAsBinaryBody() throws IOException {
        // when
        byte[] pdfBytes = new byte[1024];
        IOUtils.readFully(getClass().getClassLoader().getResourceAsStream("test.pdf"), pdfBytes);
        mockServerClient
                .when(
                        request().withBody(binary(pdfBytes))
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.OK_200.code())
                                .withHeaders(
                                        header(HttpHeaders.CONTENT_TYPE, MediaType.PDF.toString()),
                                        header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"test.pdf\"; filename=\"test.pdf\""),
                                        header(HttpHeaders.CACHE_CONTROL, "must-revalidate, post-check=0, pre-check=0")
                                )
                                .withBody(binary(pdfBytes))
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withHeaders(
                                header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"test.pdf\"; filename=\"test.pdf\""),
                                header(HttpHeaders.CACHE_CONTROL, "must-revalidate, post-check=0, pre-check=0")
                        )
                        .withBody(binary(pdfBytes)),
                makeRequest(
                        request()
                                .withPath(calculatePath("ws/rest/user/1/document/2.pdf"))
                                .withBody(binary(pdfBytes))
                                .withMethod("POST"),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withHeaders(
                                header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"test.pdf\"; filename=\"test.pdf\""),
                                header(HttpHeaders.CACHE_CONTROL, "must-revalidate, post-check=0, pre-check=0")
                        )
                        .withBody(binary(pdfBytes)),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("ws/rest/user/1/document/2.pdf"))
                                .withBody(binary(pdfBytes))
                                .withMethod("POST"),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanSetupExpectationForPNGAsBinaryBody() throws IOException {
        // when
        byte[] pngBytes = new byte[1024];
        IOUtils.readFully(getClass().getClassLoader().getResourceAsStream("test.png"), pngBytes);
        mockServerClient
                .when(
                        request().withBody(binary(pngBytes))
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.OK_200.code())
                                .withHeaders(
                                        header(HttpHeaders.CONTENT_TYPE, MediaType.PNG.toString()),
                                        header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"test.png\"; filename=\"test.png\"")
                                )
                                .withBody(binary(pngBytes))
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withHeaders(
                                header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"test.png\"; filename=\"test.png\"")
                        )
                        .withBody(binary(pngBytes)),
                makeRequest(
                        request()
                                .withPath(calculatePath("ws/rest/user/1/icon/1.png"))
                                .withBody(binary(pngBytes))
                                .withMethod("POST"),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withHeaders(
                                header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"test.png\"; filename=\"test.png\"")
                        )
                        .withBody(binary(pngBytes)),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("ws/rest/user/1/icon/1.png"))
                                .withBody(binary(pngBytes))
                                .withMethod("POST"),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerMatchPathWithDelay() {
        // when
        mockServerClient.when(
                request()
                        .withPath(calculatePath("some_path1"))
        ).respond(
                response()
                        .withBody("some_body1")
                        .withDelay(new Delay(TimeUnit.MILLISECONDS, 10))
        );
        mockServerClient.when(
                request()
                        .withPath(calculatePath("some_path2"))
        ).respond(
                response()
                        .withBody("some_body2")
                        .withDelay(new Delay(TimeUnit.MILLISECONDS, 20))
        );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body2"),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path2")),
                        headersToIgnore)
        );
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body1"),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path1")),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body2"),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("some_path2")),
                        headersToIgnore)
        );
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body1"),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("some_path1")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerPositiveMatchForGETAndMatchingPath() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_pathRequest"))
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_bodyResponse")
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse"),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse"),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .setSecure(true)
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerPositiveMatchForGETAndMatchingPathAndBody() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withBody("some_bodyRequest")
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_bodyResponse")
                                .withHeaders(header("headerNameResponse", "headerValueResponse"))
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withHeaders(
                                header("headerNameResponse", "headerValueResponse")
                        ),
                makeRequest(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody("some_bodyRequest")
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withHeaders(
                                header("headerNameResponse", "headerValueResponse")
                        ),
                makeRequest(
                        request()
                                .withMethod("POST")
                                .setSecure(true)
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody("some_bodyRequest")
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerPositiveMatchForGETAndMatchingPathAndParameters() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_bodyResponse")
                                .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                        .withHeaders(
                                header("Set-Cookie", "cookieNameResponse=cookieValueResponse")
                        ),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                        .withHeaders(
                                header("Set-Cookie", "cookieNameResponse=cookieValueResponse")
                        ),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .setSecure(true)
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerPositiveMatchForGETAndMatchingPathAndHeaders() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_pathRequest"))
                                .withHeaders(
                                        header("requestHeaderNameOne", "requestHeaderValueOne_One", "requestHeaderValueOne_Two"),
                                        header("requestHeaderNameTwo", "requestHeaderValueTwo")
                                )
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_bodyResponse")
                                .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                        .withHeaders(
                                header("Set-Cookie", "cookieNameResponse=cookieValueResponse")
                        ),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_pathRequest"))
                                .withHeaders(
                                        header("requestHeaderNameOne", "requestHeaderValueOne_One", "requestHeaderValueOne_Two"),
                                        header("requestHeaderNameTwo", "requestHeaderValueTwo")
                                )
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                        .withHeaders(
                                header("Set-Cookie", "cookieNameResponse=cookieValueResponse")
                        ),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .setSecure(true)
                                .withPath(calculatePath("some_pathRequest"))
                                .withHeaders(
                                        header("requestHeaderNameOne", "requestHeaderValueOne_One", "requestHeaderValueOne_Two"),
                                        header("requestHeaderNameTwo", "requestHeaderValueTwo")
                                )
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerPositiveMatchForGETAndMatchingPathAndCookies() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_pathRequest"))
                                .withCookies(
                                        cookie("requestCookieNameOne", "requestCookieValueOne"),
                                        cookie("requestCookieNameTwo", "requestCookieValueTwo")
                                )
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_bodyResponse")
                                .withCookies(
                                        cookie("responseCookieNameOne", "responseCookieValueOne"),
                                        cookie("responseCookieNameTwo", "responseCookieValueTwo")
                                )
                );

        // then
        // - in http - cookie objects
        HttpResponse actual = makeRequest(
                request()
                        .withMethod("GET")
                        .withPath(calculatePath("some_pathRequest"))
                        .withHeaders(
                                header("headerNameRequest", "headerValueRequest")
                        )
                        .withCookies(
                                cookie("requestCookieNameOne", "requestCookieValueOne"),
                                cookie("requestCookieNameTwo", "requestCookieValueTwo")
                        ),
                headersToIgnore);
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withCookies(
                                cookie("responseCookieNameOne", "responseCookieValueOne"),
                                cookie("responseCookieNameTwo", "responseCookieValueTwo")
                        )
                        .withHeaders(
                                header("Set-Cookie", "responseCookieNameOne=responseCookieValueOne", "responseCookieNameTwo=responseCookieValueTwo")
                        ),
                actual
        );
        // - in http - cookie header
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withCookies(
                                cookie("responseCookieNameOne", "responseCookieValueOne"),
                                cookie("responseCookieNameTwo", "responseCookieValueTwo")
                        )
                        .withHeaders(
                                header("Set-Cookie", "responseCookieNameOne=responseCookieValueOne", "responseCookieNameTwo=responseCookieValueTwo")
                        ),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_pathRequest"))
                                .withHeaders(
                                        header("headerNameRequest", "headerValueRequest"),
                                        header("Cookie", "requestCookieNameOne=requestCookieValueOne; requestCookieNameTwo=requestCookieValueTwo")
                                ),
                        headersToIgnore)
        );
        // - in https - cookie objects
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withCookies(
                                cookie("responseCookieNameOne", "responseCookieValueOne"),
                                cookie("responseCookieNameTwo", "responseCookieValueTwo")
                        )
                        .withHeaders(
                                header("Set-Cookie", "responseCookieNameOne=responseCookieValueOne", "responseCookieNameTwo=responseCookieValueTwo")
                        ),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .setSecure(true)
                                .withPath(calculatePath("some_pathRequest"))
                                .withHeaders(
                                        header("headerNameRequest", "headerValueRequest")
                                )
                                .withCookies(
                                        cookie("requestCookieNameOne", "requestCookieValueOne"),
                                        cookie("requestCookieNameTwo", "requestCookieValueTwo")
                                ),
                        headersToIgnore)
        );
        // - in https - cookie header
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withCookies(
                                cookie("responseCookieNameOne", "responseCookieValueOne"),
                                cookie("responseCookieNameTwo", "responseCookieValueTwo")
                        )
                        .withHeaders(
                                header("Set-Cookie", "responseCookieNameOne=responseCookieValueOne", "responseCookieNameTwo=responseCookieValueTwo")
                        ),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .setSecure(true)
                                .withPath(calculatePath("some_pathRequest"))
                                .withHeaders(
                                        header("headerNameRequest", "headerValueRequest"),
                                        header("Cookie", "requestCookieNameOne=requestCookieValueOne; requestCookieNameTwo=requestCookieValueTwo")
                                ),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerPositiveMatchForPOSTAndMatchingPathAndParameters() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValueOne", "queryStringParameterOneValueTwo"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(params(param("bodyParameterName", "bodyParameterValue")))
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_body")
                );

        // then
        // - in http - url query string
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_body"),
                makeRequest(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValueOne", "queryStringParameterOneValueTwo"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(params(param("bodyParameterName", "bodyParameterValue"))),
                        headersToIgnore)
        );
        // - in https - query string parameter objects
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_body"),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValueOne", "queryStringParameterOneValueTwo"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(params(param("bodyParameterName", "bodyParameterValue"))),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerPositiveMatchForPOSTAndMatchingPathBodyAndQueryParameters() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValueOne", "queryStringParameterOneValueTwo"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody("some_bodyRequest")
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_bodyResponse")
                                .withHeaders(header("headerNameResponse", "headerValueResponse"))
                                .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                );

        // then
        // - in http - url query string
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                        .withHeaders(
                                header("headerNameResponse", "headerValueResponse"),
                                header("Set-Cookie", "cookieNameResponse=cookieValueResponse")
                        ),
                makeRequest(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValueOne", "queryStringParameterOneValueTwo"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody("some_bodyRequest")
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
        // - in http - query string parameter objects
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                        .withHeaders(
                                header("headerNameResponse", "headerValueResponse"),
                                header("Set-Cookie", "cookieNameResponse=cookieValueResponse")
                        ),
                makeRequest(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValueOne", "queryStringParameterOneValueTwo"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody("some_bodyRequest")
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
        // - in https - url string and query string parameter objects
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                        .withHeaders(
                                header("headerNameResponse", "headerValueResponse"),
                                header("Set-Cookie", "cookieNameResponse=cookieValueResponse")
                        ),
                makeRequest(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValueOne", "queryStringParameterOneValueTwo"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody("some_bodyRequest")
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerPositiveMatchForPOSTAndMatchingPathBodyParametersAndQueryParameters() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withBody(params(
                                        param("bodyParameterOneName", "Parameter One Value One", "Parameter One Value Two"),
                                        param("bodyParameterTwoName", "Parameter Two")
                                ))
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_bodyResponse")
                                .withHeaders(header("headerNameResponse", "headerValueResponse"))
                                .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                );

        // then
        // - in http - body string
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                        .withHeaders(
                                header("headerNameResponse", "headerValueResponse"),
                                header("Set-Cookie", "cookieNameResponse=cookieValueResponse")
                        ),
                makeRequest(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withBody(new StringBody("bodyParameterOneName=Parameter+One+Value+One" +
                                        "&bodyParameterOneName=Parameter+One+Value+Two" +
                                        "&bodyParameterTwoName=Parameter+Two", Body.Type.STRING))
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
        // - in http - body parameter objects
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                        .withHeaders(
                                header("headerNameResponse", "headerValueResponse"),
                                header("Set-Cookie", "cookieNameResponse=cookieValueResponse")
                        ),
                makeRequest(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withBody(params(
                                        param("bodyParameterOneName", "Parameter One Value One", "Parameter One Value Two"),
                                        param("bodyParameterTwoName", "Parameter Two")
                                ))
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
        // - in https - url string and query string parameter objects
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                        .withHeaders(
                                header("headerNameResponse", "headerValueResponse"),
                                header("Set-Cookie", "cookieNameResponse=cookieValueResponse")
                        ),
                makeRequest(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withBody(params(
                                        param("bodyParameterOneName", "Parameter One Value One", "Parameter One Value Two"),
                                        param("bodyParameterTwoName", "Parameter Two")
                                ))
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerPositiveMatchForPUTAndMatchingPathBodyParametersAndHeadersAndCookies() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("PUT")
                                .withPath(calculatePath("some_pathRequest"))
                                .withBody(new StringBody("bodyParameterOneName=Parameter+One+Value+One" +
                                        "&bodyParameterOneName=Parameter+One+Value+Two" +
                                        "&bodyParameterTwoName=Parameter+Two", Body.Type.STRING))
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest"))
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_bodyResponse")
                                .withHeaders(header("headerNameResponse", "headerValueResponse"))
                                .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                );

        // then
        // - in http - body string
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                        .withHeaders(
                                header("headerNameResponse", "headerValueResponse"),
                                header("Set-Cookie", "cookieNameResponse=cookieValueResponse")
                        ),
                makeRequest(
                        request()
                                .withMethod("PUT")
                                .withPath(calculatePath("some_pathRequest"))

                                .withBody(new StringBody("bodyParameterOneName=Parameter+One+Value+One" +
                                        "&bodyParameterOneName=Parameter+One+Value+Two" +
                                        "&bodyParameterTwoName=Parameter+Two", Body.Type.STRING))
                                .withHeaders(
                                        header("headerNameRequest", "headerValueRequest"),
                                        header("Cookie", "cookieNameRequest=cookieValueRequest")
                                ),
                        headersToIgnore)
        );
        // - in http - body parameter objects
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withBody("some_bodyResponse")
                        .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                        .withHeaders(
                                header("headerNameResponse", "headerValueResponse"),
                                header("Set-Cookie", "cookieNameResponse=cookieValueResponse")
                        ),
                makeRequest(
                        request()
                                .withMethod("PUT")
                                .withPath(calculatePath("some_pathRequest"))
                                .withBody(params(
                                        param("bodyParameterOneName", "Parameter One Value One", "Parameter One Value Two"),
                                        param("bodyParameterTwoName", "Parameter Two")
                                ))
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerNegativeMatchBodyOnly() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_body"))
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue"))
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_body")
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue"))
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_other_body"))
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue")),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .setSecure(true)
                                .withPath(calculatePath("some_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_other_body"))
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerNegativeMatchXPathBodyOnly() {
        // when
        mockServerClient.when(request().withBody(new StringBody("/bookstore/book[price>35]/price", Body.Type.XPATH)), exactly(2)).respond(response().withBody("some_body"));

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("some_path"))
                                .withMethod("POST")
                                .withBody(new StringBody("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "<bookstore>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "<book category=\"COOKING\">" + Line.SEPARATOR +
                                        "  <title lang=\"en\">Everyday Italian</title>" + Line.SEPARATOR +
                                        "  <author>Giada De Laurentiis</author>" + Line.SEPARATOR +
                                        "  <year>2005</year>" + Line.SEPARATOR +
                                        "  <price>30.00</price>" + Line.SEPARATOR +
                                        "</book>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "<book category=\"CHILDREN\">" + Line.SEPARATOR +
                                        "  <title lang=\"en\">Harry Potter</title>" + Line.SEPARATOR +
                                        "  <author>J K. Rowling</author>" + Line.SEPARATOR +
                                        "  <year>2005</year>" + Line.SEPARATOR +
                                        "  <price>29.99</price>" + Line.SEPARATOR +
                                        "</book>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "<book category=\"WEB\">" + Line.SEPARATOR +
                                        "  <title lang=\"en\">Learning XML</title>" + Line.SEPARATOR +
                                        "  <author>Erik T. Ray</author>" + Line.SEPARATOR +
                                        "  <year>2003</year>" + Line.SEPARATOR +
                                        "  <price>31.95</price>" + Line.SEPARATOR +
                                        "</book>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "</bookstore>", Body.Type.STRING)),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("some_path"))
                                .withMethod("POST")
                                .withBody(new StringBody("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "<bookstore>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "<book category=\"COOKING\">" + Line.SEPARATOR +
                                        "  <title lang=\"en\">Everyday Italian</title>" + Line.SEPARATOR +
                                        "  <author>Giada De Laurentiis</author>" + Line.SEPARATOR +
                                        "  <year>2005</year>" + Line.SEPARATOR +
                                        "  <price>30.00</price>" + Line.SEPARATOR +
                                        "</book>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "<book category=\"CHILDREN\">" + Line.SEPARATOR +
                                        "  <title lang=\"en\">Harry Potter</title>" + Line.SEPARATOR +
                                        "  <author>J K. Rowling</author>" + Line.SEPARATOR +
                                        "  <year>2005</year>" + Line.SEPARATOR +
                                        "  <price>29.99</price>" + Line.SEPARATOR +
                                        "</book>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "<book category=\"WEB\">" + Line.SEPARATOR +
                                        "  <title lang=\"en\">Learning XML</title>" + Line.SEPARATOR +
                                        "  <author>Erik T. Ray</author>" + Line.SEPARATOR +
                                        "  <year>2003</year>" + Line.SEPARATOR +
                                        "  <price>31.95</price>" + Line.SEPARATOR +
                                        "</book>" + Line.SEPARATOR +
                                        "" + Line.SEPARATOR +
                                        "</bookstore>", Body.Type.STRING)),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerNegativeMatchJsonBodyOnly() {
        // when
        mockServerClient.when(request().withBody(json("{" + Line.SEPARATOR +
                "    \"title\": \"example glossary\", " + Line.SEPARATOR +
                "    \"GlossDiv\": {" + Line.SEPARATOR +
                "        \"title\": \"wrong_value\", " + Line.SEPARATOR +
                "        \"GlossList\": {" + Line.SEPARATOR +
                "            \"GlossEntry\": {" + Line.SEPARATOR +
                "                \"ID\": \"SGML\", " + Line.SEPARATOR +
                "                \"SortAs\": \"SGML\", " + Line.SEPARATOR +
                "                \"GlossTerm\": \"Standard Generalized Markup Language\", " + Line.SEPARATOR +
                "                \"Acronym\": \"SGML\", " + Line.SEPARATOR +
                "                \"Abbrev\": \"ISO 8879:1986\", " + Line.SEPARATOR +
                "                \"GlossDef\": {" + Line.SEPARATOR +
                "                    \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\", " + Line.SEPARATOR +
                "                    \"GlossSeeAlso\": [" + Line.SEPARATOR +
                "                        \"GML\", " + Line.SEPARATOR +
                "                        \"XML\"" + Line.SEPARATOR +
                "                    ]" + Line.SEPARATOR +
                "                }, " + Line.SEPARATOR +
                "                \"GlossSee\": \"markup\"" + Line.SEPARATOR +
                "            }" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}")), exactly(2)).respond(response().withBody("some_body"));

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path"))
                                .withMethod("POST")
                                .withBody("{" + Line.SEPARATOR +
                                        "    \"title\": \"example glossary\", " + Line.SEPARATOR +
                                        "    \"GlossDiv\": {" + Line.SEPARATOR +
                                        "        \"title\": \"S\", " + Line.SEPARATOR +
                                        "        \"GlossList\": {" + Line.SEPARATOR +
                                        "            \"GlossEntry\": {" + Line.SEPARATOR +
                                        "                \"ID\": \"SGML\", " + Line.SEPARATOR +
                                        "                \"SortAs\": \"SGML\", " + Line.SEPARATOR +
                                        "                \"GlossTerm\": \"Standard Generalized Markup Language\", " + Line.SEPARATOR +
                                        "                \"Acronym\": \"SGML\", " + Line.SEPARATOR +
                                        "                \"Abbrev\": \"ISO 8879:1986\", " + Line.SEPARATOR +
                                        "                \"GlossDef\": {" + Line.SEPARATOR +
                                        "                    \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\", " + Line.SEPARATOR +
                                        "                    \"GlossSeeAlso\": [" + Line.SEPARATOR +
                                        "                        \"GML\", " + Line.SEPARATOR +
                                        "                        \"XML\"" + Line.SEPARATOR +
                                        "                    ]" + Line.SEPARATOR +
                                        "                }, " + Line.SEPARATOR +
                                        "                \"GlossSee\": \"markup\"" + Line.SEPARATOR +
                                        "            }" + Line.SEPARATOR +
                                        "        }" + Line.SEPARATOR +
                                        "    }" + Line.SEPARATOR +
                                        "}"),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("some_path"))
                                .withMethod("POST")
                                .withBody("{" + Line.SEPARATOR +
                                        "    \"title\": \"example glossary\", " + Line.SEPARATOR +
                                        "    \"GlossDiv\": {" + Line.SEPARATOR +
                                        "        \"title\": \"S\", " + Line.SEPARATOR +
                                        "        \"GlossList\": {" + Line.SEPARATOR +
                                        "            \"GlossEntry\": {" + Line.SEPARATOR +
                                        "                \"ID\": \"SGML\", " + Line.SEPARATOR +
                                        "                \"SortAs\": \"SGML\", " + Line.SEPARATOR +
                                        "                \"GlossTerm\": \"Standard Generalized Markup Language\", " + Line.SEPARATOR +
                                        "                \"Acronym\": \"SGML\", " + Line.SEPARATOR +
                                        "                \"Abbrev\": \"ISO 8879:1986\", " + Line.SEPARATOR +
                                        "                \"GlossDef\": {" + Line.SEPARATOR +
                                        "                    \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\", " + Line.SEPARATOR +
                                        "                    \"GlossSeeAlso\": [" + Line.SEPARATOR +
                                        "                        \"GML\", " + Line.SEPARATOR +
                                        "                        \"XML\"" + Line.SEPARATOR +
                                        "                    ]" + Line.SEPARATOR +
                                        "                }, " + Line.SEPARATOR +
                                        "                \"GlossSee\": \"markup\"" + Line.SEPARATOR +
                                        "            }" + Line.SEPARATOR +
                                        "        }" + Line.SEPARATOR +
                                        "    }" + Line.SEPARATOR +
                                        "}"),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerNegativeMatchPathOnly() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_body"))
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue"))
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_body")
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue"))
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_other_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_body"))
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue")),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .setSecure(true)
                                .withPath(calculatePath("some_other_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_body"))
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerNegativeMatchQueryStringParameterNameOnly() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValueOne", "queryStringParameterOneValueTwo"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody("some_bodyRequest")
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_bodyResponse")
                                .withHeaders(header("headerNameResponse", "headerValueResponse"))
                                .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                );

        // then
        // wrong query string parameter name
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("OTHERQueryStringParameterOneName", "queryStringParameterOneValueOne", "queryStringParameterOneValueTwo"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody("some_bodyRequest")
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerNegativeMatchBodyParameterNameOnly() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withBody(params(
                                        param("bodyParameterOneName", "Parameter One Value One", "Parameter One Value Two"),
                                        param("bodyParameterTwoName", "Parameter Two")
                                ))
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_bodyResponse")
                                .withHeaders(header("headerNameResponse", "headerValueResponse"))
                                .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                );

        // then
        // wrong query string parameter name
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withBody(params(
                                        param("OTHERBodyParameterOneName", "Parameter One Value One", "Parameter One Value Two"),
                                        param("bodyParameterTwoName", "Parameter Two")
                                ))
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
        // wrong query string parameter name
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withBody(new StringBody("OTHERBodyParameterOneName=Parameter+One+Value+One" +
                                        "&bodyParameterOneName=Parameter+One+Value+Two" +
                                        "&bodyParameterTwoName=Parameter+Two", Body.Type.STRING))
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerNegativeMatchQueryStringParameterValueOnly() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValueOne", "queryStringParameterOneValueTwo"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody("some_bodyRequest")
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_bodyResponse")
                                .withHeaders(header("headerNameResponse", "headerValueResponse"))
                                .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                );

        // then
        // wrong query string parameter value
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "OTHERqueryStringParameterOneValueOne", "queryStringParameterOneValueTwo"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody("some_bodyRequest")
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerNegativeMatchBodyParameterValueOnly() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withBody(params(
                                        param("bodyParameterOneName", "Parameter One Value One", "Parameter One Value Two"),
                                        param("bodyParameterTwoName", "Parameter Two")
                                ))
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_bodyResponse")
                                .withHeaders(header("headerNameResponse", "headerValueResponse"))
                                .withCookies(cookie("cookieNameResponse", "cookieValueResponse"))
                );

        // then
        // wrong body parameter value
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withBody(params(
                                        param("bodyParameterOneName", "Other Parameter One Value One", "Parameter One Value Two"),
                                        param("bodyParameterTwoName", "Parameter Two")
                                ))
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
        // wrong body parameter value
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("POST")
                                .withPath(calculatePath("some_pathRequest"))
                                .withBody(new StringBody("bodyParameterOneName=Other Parameter+One+Value+One" +
                                        "&bodyParameterOneName=Parameter+One+Value+Two" +
                                        "&bodyParameterTwoName=Parameter+Two", Body.Type.STRING))
                                .withHeaders(header("headerNameRequest", "headerValueRequest"))
                                .withCookies(cookie("cookieNameRequest", "cookieValueRequest")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerNegativeMatchCookieNameOnly() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_body"))
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue"))
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_body")
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue"))
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_body"))
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieOtherName", "cookieValue")),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .setSecure(true)
                                .withPath(calculatePath("some_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_body"))
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieOtherName", "cookieValue")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerNegativeMatchCookieValueOnly() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_body"))
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue"))
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_body")
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue"))
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_body"))
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieOtherValue")),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .setSecure(true)
                                .withPath(calculatePath("some_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_body"))
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieOtherValue")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerNegativeMatchHeaderNameOnly() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_body"))
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue"))
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_body")
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue"))
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_body"))
                                .withHeaders(header("headerOtherName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue")),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .setSecure(true)
                                .withPath(calculatePath("some_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_body"))
                                .withHeaders(header("headerOtherName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanCallServerNegativeMatchHeaderValueOnly() {
        // when
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_body"))
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue"))
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                                .withBody("some_body")
                                .withHeaders(header("headerName", "headerValue"))
                                .withCookies(cookie("cookieName", "cookieValue"))
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .withPath(calculatePath("some_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_body"))
                                .withHeaders(header("headerName", "headerOtherValue"))
                                .withCookies(cookie("cookieName", "cookieValue")),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withMethod("GET")
                                .setSecure(true)
                                .withPath(calculatePath("some_path"))
                                .withQueryStringParameters(
                                        param("queryStringParameterOneName", "queryStringParameterOneValue"),
                                        param("queryStringParameterTwoName", "queryStringParameterTwoValue")
                                )
                                .withBody(exact("some_body"))
                                .withHeaders(header("headerName", "headerOtherValue"))
                                .withCookies(cookie("cookieName", "cookieValue")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanClearServerExpectations() {
        // given
        mockServerClient
                .when(
                        request()
                                .withPath(calculatePath("some_path1"))
                )
                .respond(
                        response()
                                .withBody("some_body1")
                );
        mockServerClient
                .when(
                        request()
                                .withPath(calculatePath("some_path2"))
                )
                .respond(
                        response()
                                .withBody("some_body2")
                );

        // when
        mockServerClient
                .clear(
                        request()
                                .withPath(calculatePath("some_path1"))
                );

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body2"),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path2")),
                        headersToIgnore)
        );
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path1")),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("some_body2"),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("some_path2")),
                        headersToIgnore)
        );
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("some_path1")),
                        headersToIgnore)
        );
    }

    @Test
    public void clientCanResetServerExpectations() {
        // given
        mockServerClient
                .when(
                        request()
                                .withPath(calculatePath("some_path1"))
                )
                .respond(
                        response()
                                .withBody("some_body1")
                );
        mockServerClient
                .when(
                        request()
                                .withPath(calculatePath("some_path2"))
                )
                .respond(
                        response()
                                .withBody("some_body2")
                );

        // when
        mockServerClient.reset();

        // then
        // - in http
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path1")),
                        headersToIgnore)
        );
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .withPath(calculatePath("some_path2")),
                        headersToIgnore)
        );
        // - in https
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("some_path1")),
                        headersToIgnore)
        );
        assertEquals(
                response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()),
                makeRequest(
                        request()
                                .setSecure(true)
                                .withPath(calculatePath("some_path2")),
                        headersToIgnore)
        );
    }

    protected HttpResponse makeRequest(HttpRequest httpRequest, Collection<String> headersToIgnore) {
        int attemptsRemaining = 10;
        while (attemptsRemaining > 0) {
            try {
                int port = (httpRequest.isSecure() ? getMockServerSecurePort() : getMockServerPort());
                HttpResponse httpResponse = httpClient.sendRequest(outboundRequest("localhost", port, servletContext, httpRequest));
                List<Header> headers = new ArrayList<Header>();
                for (Header header : httpResponse.getHeaders()) {
                    if (!headersToIgnore.contains(header.getName().toLowerCase())) {
                        headers.add(header);
                    }
                }
                httpResponse.withHeaders(headers);
                return httpResponse;
            } catch (SocketConnectionException caught) {
                attemptsRemaining--;
                logger.info("Retrying connection to mock server, attempts remaining: " + attemptsRemaining);
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }
        throw new RuntimeException("Failed to send request:\n" + httpRequest);
    }
}
