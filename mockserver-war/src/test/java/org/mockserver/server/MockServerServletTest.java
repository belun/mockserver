package org.mockserver.server;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockserver.client.serialization.ExpectationSerializer;
import org.mockserver.client.serialization.HttpRequestSerializer;
import org.mockserver.client.serialization.VerificationSequenceSerializer;
import org.mockserver.client.serialization.VerificationSerializer;
import org.mockserver.filters.LogFilter;
import org.mockserver.mappers.HttpServletToMockServerRequestMapper;
import org.mockserver.mappers.MockServerToHttpServletResponseMapper;
import org.mockserver.matchers.Times;
import org.mockserver.mock.Expectation;
import org.mockserver.mock.MockServerMatcher;
import org.mockserver.mock.action.ActionHandler;
import org.mockserver.model.*;
import org.mockserver.verify.Verification;
import org.mockserver.verify.VerificationSequence;
import org.mockserver.verify.VerificationTimes;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockserver.model.HttpRequest.request;

/**
 * @author jamesdbloom
 */
public class MockServerServletTest {

    @Mock
    private MockServerMatcher mockMockServerMatcher;
    @Mock
    private HttpServletToMockServerRequestMapper mockHttpServletToMockServerRequestMapper;
    @Mock
    private MockServerToHttpServletResponseMapper mockMockServerToHttpServletResponseMapper;
    @Mock
    private ExpectationSerializer mockExpectationSerializer;
    @Mock
    private HttpRequestSerializer mockHttpRequestSerializer;
    @Mock
    private VerificationSerializer mockVerificationSerializer;
    @Mock
    private VerificationSequenceSerializer mockVerificationSequenceSerializer;
    @Mock
    private ActionHandler mockActionHandler;
    @Mock
    private LogFilter mockLogFilter;
    @InjectMocks
    private MockServerServlet mockServerServlet;

    @Before
    public void setupTestFixture() {
        mockServerServlet = new MockServerServlet();

        initMocks(this);
    }

    @Test
    public void shouldReturnMatchedExpectation() {
        // given
        HttpRequest request = new HttpRequest().withPath("somepath");
        HttpResponse response = new HttpResponse().withHeaders(new Header("name", "value")).withBody("somebody");
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("GET", "somepath");

        when(mockHttpServletToMockServerRequestMapper.mapHttpServletRequestToMockServerRequest(any(HttpServletRequest.class))).thenReturn(request);
        when(mockMockServerMatcher.handle(any(HttpRequest.class))).thenReturn(response);
        when(mockActionHandler.processAction(any(HttpResponse.class), any(HttpRequest.class))).thenReturn(response);

        // when
        mockServerServlet.doGet(httpServletRequest, httpServletResponse);

        // then
        when(mockHttpServletToMockServerRequestMapper.mapHttpServletRequestToMockServerRequest(httpServletRequest)).thenReturn(request);
        verify(mockMockServerMatcher).handle(request);
        when(mockActionHandler.processAction(response, request)).thenReturn(response);
        verify(mockMockServerToHttpServletResponseMapper).mapMockServerResponseToHttpServletResponse(response, httpServletResponse);
        assertThat(httpServletResponse.getStatus(), is(200));
    }

    @Test
    public void shouldForwardMatchedExpectation() throws IOException {
        // given
        HttpRequest request = new HttpRequest().withPath("somepath");
        HttpForward forward = new HttpForward().withHost("some-host").withPort(1234);
        HttpResponse response = new HttpResponse();
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("GET", "somepath");

        when(mockHttpServletToMockServerRequestMapper.mapHttpServletRequestToMockServerRequest(any(HttpServletRequest.class))).thenReturn(request);
        when(mockMockServerMatcher.handle(any(HttpRequest.class))).thenReturn(forward);
        when(mockActionHandler.processAction(any(HttpForward.class), any(HttpRequest.class))).thenReturn(response);

        // when
        mockServerServlet.doGet(httpServletRequest, httpServletResponse);

        // then
        when(mockHttpServletToMockServerRequestMapper.mapHttpServletRequestToMockServerRequest(httpServletRequest)).thenReturn(request);
        verify(mockMockServerMatcher).handle(request);
        when(mockActionHandler.processAction(forward, request)).thenReturn(response);
        verify(mockMockServerToHttpServletResponseMapper).mapMockServerResponseToHttpServletResponse(response, httpServletResponse);
        assertThat(httpServletResponse.getStatus(), is(200));
    }

    @Test
    public void shouldCallbackMatchedExpectation() throws IOException {
        // given
        HttpRequest request = new HttpRequest().withPath("somepath");
        HttpCallback callback = new HttpCallback().withCallbackClass("some-class");
        HttpResponse response = new HttpResponse();
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("GET", "somepath");

        when(mockHttpServletToMockServerRequestMapper.mapHttpServletRequestToMockServerRequest(any(HttpServletRequest.class))).thenReturn(request);
        when(mockMockServerMatcher.handle(any(HttpRequest.class))).thenReturn(callback);
        when(mockActionHandler.processAction(any(HttpCallback.class), any(HttpRequest.class))).thenReturn(response);

        // when
        mockServerServlet.doGet(httpServletRequest, httpServletResponse);

        // then
        when(mockHttpServletToMockServerRequestMapper.mapHttpServletRequestToMockServerRequest(httpServletRequest)).thenReturn(request);
        verify(mockMockServerMatcher).handle(request);
        when(mockActionHandler.processAction(callback, request)).thenReturn(response);
        verify(mockMockServerToHttpServletResponseMapper).mapMockServerResponseToHttpServletResponse(response, httpServletResponse);
        assertThat(httpServletResponse.getStatus(), is(200));
    }

    @Test
    public void setupExpectation() throws IOException {
        // given
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("PUT", "/expectation");
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        HttpRequest httpRequest = mock(HttpRequest.class);
        Times times = mock(Times.class);
        Expectation expectation = new Expectation(httpRequest, times).thenRespond(new HttpResponse());

        String requestBytes = "requestBytes";
        httpServletRequest.setContent(requestBytes.getBytes());
        when(mockExpectationSerializer.deserialize(requestBytes)).thenReturn(expectation);
        when(mockMockServerMatcher.when(same(httpRequest), same(times))).thenReturn(expectation);

        // when
        mockServerServlet.doPut(httpServletRequest, httpServletResponse);

        // then
        verify(mockMockServerMatcher).when(same(httpRequest), same(times));
        assertEquals(HttpServletResponse.SC_CREATED, httpServletResponse.getStatus());
    }

    @Test
    public void setupExpectationFromJSONWithAllDefault() throws IOException {
        // given
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("PUT", "/expectation");
        String jsonExpectation = "{" +
                "    \"httpRequest\": {" +
                "        \"method\": \"\", " +
                "        \"path\": \"\", " +
                "        \"body\": \"\", " +
                "        \"headers\": [ ], " +
                "        \"cookies\": [ ] " +
                "    }, " +
                "    \"httpResponse\": {" +
                "        \"statusCode\": 200, " +
                "        \"body\": \"\", " +
                "        \"cookies\": [ ], " +
                "        \"headers\": [ ], " +
                "        \"delay\": {" +
                "            \"timeUnit\": \"MICROSECONDS\", " +
                "            \"value\": 0" +
                "        }" +
                "    }, " +
                "    \"times\": {" +
                "        \"remainingTimes\": 1, " +
                "        \"unlimited\": true" +
                "    }" +
                "}";
        httpServletRequest.setContent(jsonExpectation.getBytes());

        // when
        new MockServerServlet().doPut(httpServletRequest, httpServletResponse);

        // then
        assertEquals(httpServletResponse.getStatus(), HttpServletResponse.SC_CREATED);
    }

    @Test
    public void setupExpectationFromJSONWithAllEmpty() throws IOException {
        // given
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("PUT", "/expectation");
        String jsonExpectation = "{" +
                "    \"httpRequest\": { }," +
                "    \"httpResponse\": { }," +
                "    \"times\": { }" +
                "}";
        httpServletRequest.setContent(jsonExpectation.getBytes());

        // when
        new MockServerServlet().doPut(httpServletRequest, httpServletResponse);

        // then
        assertEquals(httpServletResponse.getStatus(), HttpServletResponse.SC_CREATED);
    }

    @Test
    public void setupExpectationFromJSONWithPartiallyEmptyFields() throws IOException {
        // given
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("PUT", "/expectation");
        String jsonExpectation = "{" +
                "    \"httpRequest\": {" +
                "        \"path\": \"\"" +
                "    }, " +
                "    \"httpResponse\": {" +
                "        \"body\": \"\"" +
                "    }, " +
                "    \"times\": {" +
                "        \"remainingTimes\": 1, " +
                "        \"unlimited\": true" +
                "    }" +
                "}";
        httpServletRequest.setContent(jsonExpectation.getBytes());

        // when
        new MockServerServlet().doPut(httpServletRequest, httpServletResponse);

        // then
        assertEquals(httpServletResponse.getStatus(), HttpServletResponse.SC_CREATED);
    }

    @Test
    public void shouldClearExpectations() throws IOException {
        // given
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("PUT", "/clear");
        HttpRequest httpRequest = new HttpRequest();

        String requestBytes = "requestBytes";
        httpServletRequest.setContent(requestBytes.getBytes());
        when(mockHttpRequestSerializer.deserialize(requestBytes)).thenReturn(httpRequest);

        // when
        mockServerServlet.doPut(httpServletRequest, httpServletResponse);

        // then
        verify(mockMockServerMatcher).clear(httpRequest);
        verifyNoMoreInteractions(mockHttpServletToMockServerRequestMapper);
    }

    @Test
    public void shouldResetMockServer() throws IOException {
        // given
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("PUT", "/reset");
        Expectation expectation = new Expectation(new HttpRequest(), Times.unlimited()).thenRespond(new HttpResponse());

        String requestBytes = "requestBytes";
        httpServletRequest.setContent(requestBytes.getBytes());
        when(mockExpectationSerializer.deserialize(requestBytes)).thenReturn(expectation);

        // when
        mockServerServlet.doPut(httpServletRequest, httpServletResponse);

        // then
        verify(mockMockServerMatcher).reset();
        verifyNoMoreInteractions(mockHttpServletToMockServerRequestMapper);
    }

    @Test
    public void shouldDumpAllExpectationsToLog() throws IOException {
        // given
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("PUT", "/dumpToLog");
        HttpRequest httpRequest = new HttpRequest();

        String requestBytes = "requestBytes";
        httpServletRequest.setContent(requestBytes.getBytes());
        when(mockHttpRequestSerializer.deserialize(requestBytes)).thenReturn(httpRequest);

        // when
        mockServerServlet.doPut(httpServletRequest, httpServletResponse);

        // then
        verify(mockHttpRequestSerializer).deserialize(requestBytes);
        verify(mockMockServerMatcher).dumpToLog(httpRequest);
        verifyNoMoreInteractions(mockHttpServletToMockServerRequestMapper);
    }

    @Test
    public void shouldRetrieveExpectationsMockServer() throws IOException {
        // given
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("PUT", "/retrieve");
        Expectation expectation = new Expectation(new HttpRequest(), Times.unlimited()).thenRespond(new HttpResponse());

        httpServletRequest.setContent("requestBytes".getBytes());
        when(mockHttpRequestSerializer.deserialize(anyString())).thenReturn(expectation.getHttpRequest());
        when(mockLogFilter.retrieve(any(HttpRequest.class))).thenReturn(new Expectation[]{expectation});
        when(mockExpectationSerializer.serialize(any(Expectation[].class))).thenReturn("expectations_response");

        // when
        mockServerServlet.doPut(httpServletRequest, httpServletResponse);

        // then
        verify(mockLogFilter).retrieve(expectation.getHttpRequest());
        assertThat(httpServletResponse.getContentAsByteArray(), is("expectations_response".getBytes()));
        assertThat(httpServletResponse.getStatus(), is(HttpStatusCode.OK_200.code()));
        verifyNoMoreInteractions(mockHttpServletToMockServerRequestMapper);
    }

    @Test
    public void shouldVerifyRequestNotMatching() throws IOException {
        // given
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("PUT", "/verify");
        Verification verification = new Verification().withRequest(new HttpRequest()).withTimes(VerificationTimes.once());

        String requestBytes = "requestBytes";
        httpServletRequest.setContent(requestBytes.getBytes());
        when(mockVerificationSerializer.deserialize(requestBytes)).thenReturn(verification);
        when(mockLogFilter.verify(verification)).thenReturn("verification_error");

        // when
        mockServerServlet.doPut(httpServletRequest, httpServletResponse);

        // then
        verify(mockLogFilter).verify(verification);
        assertThat(httpServletResponse.getContentAsString(), is("verification_error"));
        assertThat(httpServletResponse.getStatus(), is(HttpStatusCode.NOT_ACCEPTABLE_406.code()));
        verifyNoMoreInteractions(mockHttpServletToMockServerRequestMapper);
    }

    @Test
    public void shouldVerifyRequestMatching() throws IOException {
        // given
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("PUT", "/verify");
        Verification verification = new Verification().withRequest(new HttpRequest()).withTimes(VerificationTimes.once());

        String requestBytes = "requestBytes";
        httpServletRequest.setContent(requestBytes.getBytes());
        when(mockVerificationSerializer.deserialize(requestBytes)).thenReturn(verification);
        when(mockLogFilter.verify(verification)).thenReturn("");

        // when
        mockServerServlet.doPut(httpServletRequest, httpServletResponse);

        // then
        verify(mockLogFilter).verify(verification);
        assertThat(httpServletResponse.getContentAsString(), is(""));
        assertThat(httpServletResponse.getStatus(), is(HttpStatusCode.ACCEPTED_202.code()));
        verifyNoMoreInteractions(mockHttpServletToMockServerRequestMapper);
    }

    @Test
    public void shouldVerifySequenceRequestNotMatching() throws IOException {
        // given
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("PUT", "/verifySequence");
        VerificationSequence verification = new VerificationSequence().withRequests(request("one"), request("two"));

        String requestBytes = "requestBytes";
        httpServletRequest.setContent(requestBytes.getBytes());
        when(mockVerificationSequenceSerializer.deserialize(requestBytes)).thenReturn(verification);
        when(mockLogFilter.verify(verification)).thenReturn("verification_error");

        // when
        mockServerServlet.doPut(httpServletRequest, httpServletResponse);

        // then
        verify(mockLogFilter).verify(verification);
        assertThat(httpServletResponse.getContentAsString(), is("verification_error"));
        assertThat(httpServletResponse.getStatus(), is(HttpStatusCode.NOT_ACCEPTABLE_406.code()));
        verifyNoMoreInteractions(mockHttpServletToMockServerRequestMapper);
    }

    @Test
    public void shouldVerifySequenceRequestMatching() throws IOException {
        // given
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("PUT", "/verifySequence");
        VerificationSequence verification = new VerificationSequence().withRequests(request("one"), request("two"));

        String requestBytes = "requestBytes";
        httpServletRequest.setContent(requestBytes.getBytes());
        when(mockVerificationSequenceSerializer.deserialize(requestBytes)).thenReturn(verification);
        when(mockLogFilter.verify(verification)).thenReturn("");

        // when
        mockServerServlet.doPut(httpServletRequest, httpServletResponse);

        // then
        verify(mockLogFilter).verify(verification);
        assertThat(httpServletResponse.getContentAsString(), is(""));
        assertThat(httpServletResponse.getStatus(), is(HttpStatusCode.ACCEPTED_202.code()));
        verifyNoMoreInteractions(mockHttpServletToMockServerRequestMapper);
    }
}
