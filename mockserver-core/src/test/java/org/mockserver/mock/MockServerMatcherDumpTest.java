package org.mockserver.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockserver.Line;
import org.mockserver.client.serialization.Base64Converter;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.slf4j.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author jamesdbloom
 */
public class MockServerMatcherDumpTest {

    @Mock
    private Logger requestLogger;
    @InjectMocks
    private MockServerMatcher mockServerMatcher;

    @Before
    public void prepareTestFixture() {
        mockServerMatcher = new MockServerMatcher();
        initMocks(this);
    }

    @Test
    public void shouldWriteAllExpectationsToTheLog() {
        // given
        mockServerMatcher
                .when(
                        new HttpRequest()
                                .withPath("some_path")
                )
                .thenRespond(
                        new HttpResponse()
                                .withBody("some_response_body")
                );
        mockServerMatcher
                .when(
                        new HttpRequest()
                                .withPath("some_other_path")
                )
                .thenRespond(
                        new HttpResponse()
                                .withBody("some_other_response_body")
                );

        // when
        mockServerMatcher.dumpToLog(null);

        // then
        verify(requestLogger).warn("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"path\" : \"some_path\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpResponse\" : {" + Line.SEPARATOR +
                "    \"statusCode\" : 200," + Line.SEPARATOR +
                "    \"body\" : \"some_response_body\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 0," + Line.SEPARATOR +
                "    \"unlimited\" : true" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}");
        verify(requestLogger).warn("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"path\" : \"some_other_path\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpResponse\" : {" + Line.SEPARATOR +
                "    \"statusCode\" : 200," + Line.SEPARATOR +
                "    \"body\" : \"some_other_response_body\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 0," + Line.SEPARATOR +
                "    \"unlimited\" : true" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}");
    }

    @Test
    public void shouldWriteOnlyMatchingExpectationsToTheLog() {
        // given
        mockServerMatcher
                .when(
                        new HttpRequest()
                                .withPath("some_path")
                )
                .thenRespond(
                        new HttpResponse()
                                .withBody("some_response_body")
                );
        mockServerMatcher
                .when(
                        new HttpRequest()
                                .withPath("some_other_path")
                )
                .thenRespond(
                        new HttpResponse()
                                .withBody("some_other_response_body")
                );

        // when
        mockServerMatcher.dumpToLog(new HttpRequest().withPath("some_path"));

        // then
        verify(requestLogger).warn("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"path\" : \"some_path\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpResponse\" : {" + Line.SEPARATOR +
                "    \"statusCode\" : 200," + Line.SEPARATOR +
                "    \"body\" : \"some_response_body\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 0," + Line.SEPARATOR +
                "    \"unlimited\" : true" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}");
        verifyNoMoreInteractions(requestLogger);
    }


    @Test
    public void shouldCorrectlyMatchRegexForResponsesWithStatusCode() {
        // when
        String result = mockServerMatcher.cleanBase64Response("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"path\" : \"some_path\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpResponse\" : {" + Line.SEPARATOR +
                "    \"statusCode\" : 200," + Line.SEPARATOR +
                "    \"body\" : \"" + Base64Converter.stringToBase64Bytes("some_response_body".getBytes()) + "\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 0," + Line.SEPARATOR +
                "    \"unlimited\" : true" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}");

        // then
        assertThat(result, is("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"path\" : \"some_path\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpResponse\" : {" + Line.SEPARATOR +
                "    \"statusCode\" : 200," + Line.SEPARATOR +
                "    \"body\" : \"some_response_body\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 0," + Line.SEPARATOR +
                "    \"unlimited\" : true" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}"));
    }

    @Test
    public void shouldCorrectlyMatchRegexForResponsesWithDelay() {
        // when
        String result = mockServerMatcher.cleanBase64Response("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"path\": \"somePath\"" + Line.SEPARATOR +
                "    }," + Line.SEPARATOR +
                "    \"httpResponse\": {" + Line.SEPARATOR +
                "        \"body\": \"" + Base64Converter.stringToBase64Bytes("someBody".getBytes()) + "\"," + Line.SEPARATOR +
                "        \"delay\": {" + Line.SEPARATOR +
                "            \"timeUnit\": null," + Line.SEPARATOR +
                "            \"value\": null" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // then
        assertThat(result, is("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"path\": \"somePath\"" + Line.SEPARATOR +
                "    }," + Line.SEPARATOR +
                "    \"httpResponse\": {" + Line.SEPARATOR +
                "        \"body\": \"someBody\"," + Line.SEPARATOR +
                "        \"delay\": {" + Line.SEPARATOR +
                "            \"timeUnit\": null," + Line.SEPARATOR +
                "            \"value\": null" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}"));
    }

    @Test
    public void shouldCorrectlyMatchRegexForComplexResponses() {
        // when
        String result = mockServerMatcher.cleanBase64Response("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"method\" : \"someMethod\"," + Line.SEPARATOR +
                "    \"url\" : \"http://www.example.com\"," + Line.SEPARATOR +
                "    \"path\" : \"somePath\"," + Line.SEPARATOR +
                "    \"queryStringParameters\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"queryStringParameterNameOne\"," + Line.SEPARATOR +
                "      \"values\" : [ \"queryStringParameterValueOne_One\", \"queryStringParameterValueOne_Two\" ]" + Line.SEPARATOR +
                "    }, {" + Line.SEPARATOR +
                "      \"name\" : \"queryStringParameterNameTwo\"," + Line.SEPARATOR +
                "      \"values\" : [ \"queryStringParameterValueTwo_One\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"body\" : {" + Line.SEPARATOR +
                "      \"type\" : \"STRING\"," + Line.SEPARATOR +
                "      \"value\" : \"someBody\"" + Line.SEPARATOR +
                "    }," + Line.SEPARATOR +
                "    \"cookies\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someCookieName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someCookieValue\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"headers\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someHeaderName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someHeaderValue\" ]" + Line.SEPARATOR +
                "    } ]" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpResponse\" : {" + Line.SEPARATOR +
                "    \"statusCode\" : 304," + Line.SEPARATOR +
                "    \"body\" : \"" + Base64Converter.stringToBase64Bytes("someBody".getBytes()) + "\"," + Line.SEPARATOR +
                "    \"cookies\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someCookieName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someCookieValue\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"headers\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someHeaderName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someHeaderValue\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"delay\" : {" + Line.SEPARATOR +
                "      \"timeUnit\" : \"MICROSECONDS\"," + Line.SEPARATOR +
                "      \"value\" : 1" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 5," + Line.SEPARATOR +
                "    \"unlimited\" : false" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}");

        // then
        assertThat(result, is("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"method\" : \"someMethod\"," + Line.SEPARATOR +
                "    \"url\" : \"http://www.example.com\"," + Line.SEPARATOR +
                "    \"path\" : \"somePath\"," + Line.SEPARATOR +
                "    \"queryStringParameters\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"queryStringParameterNameOne\"," + Line.SEPARATOR +
                "      \"values\" : [ \"queryStringParameterValueOne_One\", \"queryStringParameterValueOne_Two\" ]" + Line.SEPARATOR +
                "    }, {" + Line.SEPARATOR +
                "      \"name\" : \"queryStringParameterNameTwo\"," + Line.SEPARATOR +
                "      \"values\" : [ \"queryStringParameterValueTwo_One\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"body\" : {" + Line.SEPARATOR +
                "      \"type\" : \"STRING\"," + Line.SEPARATOR +
                "      \"value\" : \"someBody\"" + Line.SEPARATOR +
                "    }," + Line.SEPARATOR +
                "    \"cookies\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someCookieName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someCookieValue\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"headers\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someHeaderName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someHeaderValue\" ]" + Line.SEPARATOR +
                "    } ]" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpResponse\" : {" + Line.SEPARATOR +
                "    \"statusCode\" : 304," + Line.SEPARATOR +
                "    \"body\" : \"someBody\"," + Line.SEPARATOR +
                "    \"cookies\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someCookieName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someCookieValue\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"headers\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someHeaderName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someHeaderValue\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"delay\" : {" + Line.SEPARATOR +
                "      \"timeUnit\" : \"MICROSECONDS\"," + Line.SEPARATOR +
                "      \"value\" : 1" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 5," + Line.SEPARATOR +
                "    \"unlimited\" : false" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}"));
    }

    @Test
    public void shouldCorrectlyMatchNotRegexForComplex() {
        // when
        String result = mockServerMatcher.cleanBase64Response("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"method\" : \"someMethod\"," + Line.SEPARATOR +
                "    \"url\" : \"http://www.example.com\"," + Line.SEPARATOR +
                "    \"path\" : \"somePath\"," + Line.SEPARATOR +
                "    \"queryStringParameters\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"queryStringParameterNameOne\"," + Line.SEPARATOR +
                "      \"values\" : [ \"queryStringParameterValueOne_One\", \"queryStringParameterValueOne_Two\" ]" + Line.SEPARATOR +
                "    }, {" + Line.SEPARATOR +
                "      \"name\" : \"queryStringParameterNameTwo\"," + Line.SEPARATOR +
                "      \"values\" : [ \"queryStringParameterValueTwo_One\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"body\" : {" + Line.SEPARATOR +
                "      \"type\" : \"STRING\"," + Line.SEPARATOR +
                "      \"value\" : \"some_body\"" + Line.SEPARATOR +
                "    }," + Line.SEPARATOR +
                "    \"cookies\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someCookieName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someCookieValue\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"headers\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someHeaderName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someHeaderValue\" ]" + Line.SEPARATOR +
                "    } ]" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpResponse\" : {" + Line.SEPARATOR +
                "    \"statusCode\" : 304," + Line.SEPARATOR +
                "    \"body\" : \"some_body\"," + Line.SEPARATOR +
                "    \"cookies\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someCookieName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someCookieValue\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"headers\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someHeaderName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someHeaderValue\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"delay\" : {" + Line.SEPARATOR +
                "      \"timeUnit\" : \"MICROSECONDS\"," + Line.SEPARATOR +
                "      \"value\" : 1" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 5," + Line.SEPARATOR +
                "    \"unlimited\" : false" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}");

        // then
        assertThat(result, is("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"method\" : \"someMethod\"," + Line.SEPARATOR +
                "    \"url\" : \"http://www.example.com\"," + Line.SEPARATOR +
                "    \"path\" : \"somePath\"," + Line.SEPARATOR +
                "    \"queryStringParameters\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"queryStringParameterNameOne\"," + Line.SEPARATOR +
                "      \"values\" : [ \"queryStringParameterValueOne_One\", \"queryStringParameterValueOne_Two\" ]" + Line.SEPARATOR +
                "    }, {" + Line.SEPARATOR +
                "      \"name\" : \"queryStringParameterNameTwo\"," + Line.SEPARATOR +
                "      \"values\" : [ \"queryStringParameterValueTwo_One\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"body\" : {" + Line.SEPARATOR +
                "      \"type\" : \"STRING\"," + Line.SEPARATOR +
                "      \"value\" : \"some_body\"" + Line.SEPARATOR +
                "    }," + Line.SEPARATOR +
                "    \"cookies\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someCookieName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someCookieValue\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"headers\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someHeaderName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someHeaderValue\" ]" + Line.SEPARATOR +
                "    } ]" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpResponse\" : {" + Line.SEPARATOR +
                "    \"statusCode\" : 304," + Line.SEPARATOR +
                "    \"body\" : \"some_body\"," + Line.SEPARATOR +
                "    \"cookies\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someCookieName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someCookieValue\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"headers\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someHeaderName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someHeaderValue\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"delay\" : {" + Line.SEPARATOR +
                "      \"timeUnit\" : \"MICROSECONDS\"," + Line.SEPARATOR +
                "      \"value\" : 1" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 5," + Line.SEPARATOR +
                "    \"unlimited\" : false" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}"));
    }
}
