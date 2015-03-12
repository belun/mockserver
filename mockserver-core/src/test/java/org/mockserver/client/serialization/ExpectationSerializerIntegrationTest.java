package org.mockserver.client.serialization;

import org.junit.Test;
import org.mockserver.Line;
import org.mockserver.client.serialization.model.*;
import org.mockserver.matchers.Times;
import org.mockserver.mock.Expectation;
import org.mockserver.model.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author jamesdbloom
 */
public class ExpectationSerializerIntegrationTest {


    @Test
    public void shouldIgnoreExtraFields() throws IOException {
        // given
        String requestBytes = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"path\": \"somePath\"," + Line.SEPARATOR +
                "        \"extra_field\": \"extra_value\"" + Line.SEPARATOR +
                "    }," + Line.SEPARATOR +
                "    \"httpResponse\": {" + Line.SEPARATOR +
                "        \"body\": \"someBody\"," + Line.SEPARATOR +
                "        \"extra_field\": \"extra_value\"" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        Expectation expectation = new ExpectationSerializer().deserialize(requestBytes);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setPath("somePath")
                )
                .setHttpResponse(
                        new HttpResponseDTO()
                                .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                )
                .buildObject(), expectation);
    }

    @Test
    public void shouldIgnoreEmptyStringObjects() throws IOException {
        // given
        String requestBytes = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"path\": \"somePath\"" + Line.SEPARATOR +
                "    }," + Line.SEPARATOR +
                "    \"httpResponse\": \"\"" + Line.SEPARATOR +
                "}");

        // when
        Expectation expectation = new ExpectationSerializer().deserialize(requestBytes);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setPath("somePath")
                )
                .buildObject(), expectation);
    }

    @Test
    public void shouldHandleNullPrimitives() throws IOException {
        // given
        String requestBytes = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"path\": \"somePath\"" + Line.SEPARATOR +
                "    }," + Line.SEPARATOR +
                "    \"httpResponse\": {" + Line.SEPARATOR +
                "        \"body\": \"someBody\"" + Line.SEPARATOR +
                "    }," + Line.SEPARATOR +
                "    \"times\": {" + Line.SEPARATOR +
                "        \"remainingTimes\": null," + Line.SEPARATOR +
                "        \"unlimited\": false" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        Expectation expectation = new ExpectationSerializer().deserialize(requestBytes);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setPath("somePath")
                )
                .setHttpResponse(
                        new HttpResponseDTO()
                                .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                )
                .setTimes(new TimesDTO(Times.exactly(0)))
                .buildObject(), expectation);
    }

    @Test
    public void shouldHandleEmptyPrimitives() throws IOException {
        // given
        String requestBytes = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"path\": \"somePath\"" + Line.SEPARATOR +
                "    }," + Line.SEPARATOR +
                "    \"httpResponse\": {" + Line.SEPARATOR +
                "        \"body\": \"someBody\"" + Line.SEPARATOR +
                "    }," + Line.SEPARATOR +
                "    \"times\": {" + Line.SEPARATOR +
                "        \"remainingTimes\": \"\"," + Line.SEPARATOR +
                "        \"unlimited\": false" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        Expectation expectation = new ExpectationSerializer().deserialize(requestBytes);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setPath("somePath")
                )
                .setHttpResponse(
                        new HttpResponseDTO()
                                .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                )
                .setTimes(new TimesDTO(Times.exactly(0)))
                .buildObject(), expectation);
    }

    @Test
    public void shouldHandleNullEnums() throws IOException {
        // given
        String requestBytes = ("{" + Line.SEPARATOR +
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
                "}");

        // when
        Expectation expectation = new ExpectationSerializer().deserialize(requestBytes);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setPath("somePath")
                )
                .setHttpResponse(
                        new HttpResponseDTO()
                                .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                                .setDelay(new DelayDTO(new Delay(null, 0)))
                )
                .buildObject(), expectation);
    }

    @Test
    public void shouldAllowSingleObjectForArray() throws IOException {
        // given
        String requestBytes = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"path\": \"somePath\"," + Line.SEPARATOR +
                "        \"extra_field\": \"extra_value\"" + Line.SEPARATOR +
                "    }," + Line.SEPARATOR +
                "    \"httpResponse\": {" + Line.SEPARATOR +
                "        \"body\": \"someBody\"," + Line.SEPARATOR +
                "        \"extra_field\": \"extra_value\"" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        Expectation[] expectations = new ExpectationSerializer().deserializeArray(requestBytes);

        // then
        assertArrayEquals(new Expectation[]{
                new ExpectationDTO()
                        .setHttpRequest(
                                new HttpRequestDTO()
                                        .setPath("somePath")
                        )
                        .setHttpResponse(
                                new HttpResponseDTO()
                                        .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                        )
                        .buildObject()
        }, expectations);
    }

    @Test
    public void shouldAllowMultipleObjectsForArray() throws IOException {
        // given
        String requestBytes = ("[" +
                "  {" + Line.SEPARATOR +
                "      \"httpRequest\": {" + Line.SEPARATOR +
                "          \"path\": \"somePath\"," + Line.SEPARATOR +
                "          \"extra_field\": \"extra_value\"" + Line.SEPARATOR +
                "      }," + Line.SEPARATOR +
                "      \"httpResponse\": {" + Line.SEPARATOR +
                "          \"body\": \"someBody\"," + Line.SEPARATOR +
                "          \"extra_field\": \"extra_value\"" + Line.SEPARATOR +
                "      }" + Line.SEPARATOR +
                "  }," +
                "  {" + Line.SEPARATOR +
                "      \"httpRequest\": {" + Line.SEPARATOR +
                "          \"path\": \"somePath\"," + Line.SEPARATOR +
                "          \"extra_field\": \"extra_value\"" + Line.SEPARATOR +
                "      }," + Line.SEPARATOR +
                "      \"httpResponse\": {" + Line.SEPARATOR +
                "          \"body\": \"someBody\"," + Line.SEPARATOR +
                "          \"extra_field\": \"extra_value\"" + Line.SEPARATOR +
                "      }" + Line.SEPARATOR +
                "  }," +
                "  {" + Line.SEPARATOR +
                "      \"httpRequest\": {" + Line.SEPARATOR +
                "          \"path\": \"somePath\"," + Line.SEPARATOR +
                "          \"extra_field\": \"extra_value\"" + Line.SEPARATOR +
                "      }," + Line.SEPARATOR +
                "      \"httpResponse\": {" + Line.SEPARATOR +
                "          \"body\": \"someBody\"," + Line.SEPARATOR +
                "          \"extra_field\": \"extra_value\"" + Line.SEPARATOR +
                "      }" + Line.SEPARATOR +
                "  }" +
                "]");
        Expectation expectation = new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setPath("somePath")
                )
                .setHttpResponse(
                        new HttpResponseDTO()
                                .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                )
                .buildObject();

        // when
        Expectation[] expectations = new ExpectationSerializer().deserializeArray(requestBytes);

        // then
        assertArrayEquals(new Expectation[]{
                expectation,
                expectation,
                expectation
        }, expectations);
    }

    @Test
    public void shouldDeserializeCompleteObjectWithResponse() throws IOException {
        // given
        String requestBytes = ("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"method\" : \"someMethod\"," + Line.SEPARATOR +
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
                "      \"value\" : \"someCookieValue\"" + Line.SEPARATOR +
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
                "      \"value\" : \"someCookieValue\"" + Line.SEPARATOR +
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

        // when
        Expectation expectation = new ExpectationSerializer().deserialize(requestBytes);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setMethod("someMethod")
                                .setPath("somePath")
                                .setQueryStringParameters(Arrays.asList(
                                        new ParameterDTO(new Parameter("queryStringParameterNameOne", "queryStringParameterValueOne_One", "queryStringParameterValueOne_Two")),
                                        new ParameterDTO(new Parameter("queryStringParameterNameTwo", "queryStringParameterValueTwo_One"))
                                ))
                                .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                                .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                                .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                )
                .setHttpResponse(
                        new HttpResponseDTO()
                                .setStatusCode(304)
                                .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                                .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                                .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                                .setDelay(
                                        new DelayDTO()
                                                .setTimeUnit(TimeUnit.MICROSECONDS)
                                                .setValue(1)
                                )
                )
                .setTimes(new TimesDTO(Times.exactly(5))).buildObject(), expectation);
    }

    @Test
    public void shouldDeserializeCompleteObjectWithForward() throws IOException {
        // given
        String requestBytes = ("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"method\" : \"someMethod\"," + Line.SEPARATOR +
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
                "      \"value\" : \"someCookieValue\"" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"headers\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someHeaderName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someHeaderValue\" ]" + Line.SEPARATOR +
                "    } ]" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpForward\" : {" + Line.SEPARATOR +
                "    \"host\" : \"someHost\"," + Line.SEPARATOR +
                "    \"port\" : 1234," + Line.SEPARATOR +
                "    \"scheme\" : \"HTTPS\"" +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 5," + Line.SEPARATOR +
                "    \"unlimited\" : false" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}");

        // when
        Expectation expectation = new ExpectationSerializer().deserialize(requestBytes);

        // then
        assertEquals(new ExpectationDTO()
                        .setHttpRequest(
                                new HttpRequestDTO()
                                        .setMethod("someMethod")
                                        .setPath("somePath")
                                        .setQueryStringParameters(Arrays.asList(
                                                new ParameterDTO(new Parameter("queryStringParameterNameOne", "queryStringParameterValueOne_One", "queryStringParameterValueOne_Two")),
                                                new ParameterDTO(new Parameter("queryStringParameterNameTwo", "queryStringParameterValueTwo_One"))
                                        ))
                                        .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                                        .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                                        .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                        )
                        .setHttpForward(
                                new HttpForwardDTO()
                                        .setHost("someHost")
                                        .setPort(1234)
                                        .setScheme(HttpForward.Scheme.HTTPS)
                        )
                        .setTimes(new TimesDTO(Times.exactly(5))).buildObject(), expectation
        );
    }

    @Test
    public void shouldDeserializeCompleteObjectWithCallback() throws IOException {
        // given
        String requestBytes = ("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"method\" : \"someMethod\"," + Line.SEPARATOR +
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
                "      \"value\" : \"someCookieValue\"" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"headers\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someHeaderName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someHeaderValue\" ]" + Line.SEPARATOR +
                "    } ]" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpCallback\" : {" + Line.SEPARATOR +
                "    \"callbackClass\" : \"someClass\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 5," + Line.SEPARATOR +
                "    \"unlimited\" : false" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}");

        // when
        Expectation expectation = new ExpectationSerializer().deserialize(requestBytes);

        // then
        assertEquals(new ExpectationDTO()
                        .setHttpRequest(
                                new HttpRequestDTO()
                                        .setMethod("someMethod")
                                        .setPath("somePath")
                                        .setQueryStringParameters(Arrays.asList(
                                                new ParameterDTO(new Parameter("queryStringParameterNameOne", "queryStringParameterValueOne_One", "queryStringParameterValueOne_Two")),
                                                new ParameterDTO(new Parameter("queryStringParameterNameTwo", "queryStringParameterValueTwo_One"))
                                        ))
                                        .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                                        .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                                        .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                        )
                        .setHttpCallback(
                                new HttpCallbackDTO()
                                        .setCallbackClass("someClass")
                        )
                        .setTimes(new TimesDTO(Times.exactly(5))).buildObject(), expectation
        );
    }

    @Test
    public void shouldDeserializePartialObject() throws IOException {
        // given
        String requestBytes = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"path\": \"somePath\"" + Line.SEPARATOR +
                "    }," + Line.SEPARATOR +
                "    \"httpResponse\": {" + Line.SEPARATOR +
                "        \"body\": \"someBody\"" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        Expectation expectation = new ExpectationSerializer().deserialize(requestBytes);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setPath("somePath")
                )
                .setHttpResponse(
                        new HttpResponseDTO()
                                .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                )
                .buildObject(), expectation);
    }

    @Test
    public void shouldDeserializeStringRegexBody() throws IOException {
        // given
        String requestBytes = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"path\": \"somePath\"," + Line.SEPARATOR +
                "        \"body\" : {" + Line.SEPARATOR +
                "            \"type\" : \"REGEX\"," + Line.SEPARATOR +
                "            \"value\" : \"some[a-zA-Z]*\"" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }," + Line.SEPARATOR +
                "    \"httpResponse\": {" + Line.SEPARATOR +
                "        \"body\": \"someBody\"" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        Expectation expectation = new ExpectationSerializer().deserialize(requestBytes);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setPath("somePath")
                                .setBody(new StringBodyDTO(new StringBody("some[a-zA-Z]*", Body.Type.REGEX)))
                )
                .setHttpResponse(
                        new HttpResponseDTO()
                                .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                )
                .buildObject(), expectation);
    }

    @Test
    public void shouldDeserializeParameterBody() throws IOException {
        // given
        String requestBytes = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"path\": \"somePath\"," + Line.SEPARATOR +
                "        \"body\" : {" + Line.SEPARATOR +
                "            \"type\" : \"PARAMETERS\"," + Line.SEPARATOR +
                "            \"parameters\" : [ {" + Line.SEPARATOR +
                "                    \"name\" : \"parameterOneName\"," + Line.SEPARATOR +
                "                    \"values\" : [ \"parameterOneValueOne\", \"parameterOneValueTwo\" ]" + Line.SEPARATOR +
                "                }, {" + Line.SEPARATOR +
                "                    \"name\" : \"parameterTwoName\"," + Line.SEPARATOR +
                "                    \"values\" : [ \"parameterTwoValue\" ]" + Line.SEPARATOR +
                "            } ]" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }," + Line.SEPARATOR +
                "    \"httpResponse\": {" + Line.SEPARATOR +
                "        \"body\": \"someBody\"" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        Expectation expectation = new ExpectationSerializer().deserialize(requestBytes);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setPath("somePath")
                                .setBody(new ParameterBodyDTO(new ParameterBody(
                                        new Parameter("parameterOneName", "parameterOneValueOne", "parameterOneValueTwo"),
                                        new Parameter("parameterTwoName", "parameterTwoValue")
                                )))
                )
                .setHttpResponse(
                        new HttpResponseDTO()
                                .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                )
                .buildObject(), expectation);
    }

    @Test
    public void shouldSerializeCompleteObjectWithResponse() throws IOException {
        // when
        String jsonExpectation = new ExpectationSerializer().serialize(new ExpectationDTO()
                        .setHttpRequest(
                                new HttpRequestDTO()
                                        .setMethod("someMethod")
                                        .setPath("somePath")
                                        .setQueryStringParameters(Arrays.asList(
                                                new ParameterDTO(new Parameter("queryStringParameterNameOne", "queryStringParameterValueOne_One", "queryStringParameterValueOne_Two")),
                                                new ParameterDTO(new Parameter("queryStringParameterNameTwo", "queryStringParameterValueTwo_One"))
                                        ))
                                        .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                                        .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                                        .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                        )
                        .setHttpResponse(
                                new HttpResponseDTO()
                                        .setStatusCode(304)
                                        .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                                        .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                                        .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                                        .setDelay(
                                                new DelayDTO()
                                                        .setTimeUnit(TimeUnit.MICROSECONDS)
                                                        .setValue(1)
                                        )
                        )
                        .setTimes(new TimesDTO(Times.exactly(5)))
                        .buildObject()
        );

        // then
        assertEquals("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"method\" : \"someMethod\"," + Line.SEPARATOR +
                "    \"path\" : \"somePath\"," + Line.SEPARATOR +
                "    \"queryStringParameters\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"queryStringParameterNameOne\"," + Line.SEPARATOR +
                "      \"values\" : [ \"queryStringParameterValueOne_One\", \"queryStringParameterValueOne_Two\" ]" + Line.SEPARATOR +
                "    }, {" + Line.SEPARATOR +
                "      \"name\" : \"queryStringParameterNameTwo\"," + Line.SEPARATOR +
                "      \"values\" : [ \"queryStringParameterValueTwo_One\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"body\" : \"someBody\"," + Line.SEPARATOR +
                "    \"cookies\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someCookieName\"," + Line.SEPARATOR +
                "      \"value\" : \"someCookieValue\"" + Line.SEPARATOR +
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
                "      \"value\" : \"someCookieValue\"" + Line.SEPARATOR +
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
                "}", jsonExpectation);
    }


    @Test
    public void shouldSerializeCompleteObjectWithForward() throws IOException {
        // when
        String jsonExpectation = new ExpectationSerializer().serialize(new ExpectationDTO()
                        .setHttpRequest(
                                new HttpRequestDTO()
                                        .setMethod("someMethod")
                                        .setPath("somePath")
                                        .setQueryStringParameters(Arrays.asList(
                                                new ParameterDTO(new Parameter("queryStringParameterNameOne", "queryStringParameterValueOne_One", "queryStringParameterValueOne_Two")),
                                                new ParameterDTO(new Parameter("queryStringParameterNameTwo", "queryStringParameterValueTwo_One"))
                                        ))
                                        .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                                        .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                                        .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                        )
                        .setHttpForward(
                                new HttpForwardDTO()
                                        .setHost("someHost")
                                        .setPort(1234)
                                        .setScheme(HttpForward.Scheme.HTTPS)
                        )
                        .setTimes(new TimesDTO(Times.exactly(5)))
                        .buildObject()
        );

        // then
        assertEquals("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"method\" : \"someMethod\"," + Line.SEPARATOR +
                "    \"path\" : \"somePath\"," + Line.SEPARATOR +
                "    \"queryStringParameters\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"queryStringParameterNameOne\"," + Line.SEPARATOR +
                "      \"values\" : [ \"queryStringParameterValueOne_One\", \"queryStringParameterValueOne_Two\" ]" + Line.SEPARATOR +
                "    }, {" + Line.SEPARATOR +
                "      \"name\" : \"queryStringParameterNameTwo\"," + Line.SEPARATOR +
                "      \"values\" : [ \"queryStringParameterValueTwo_One\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"body\" : \"someBody\"," + Line.SEPARATOR +
                "    \"cookies\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someCookieName\"," + Line.SEPARATOR +
                "      \"value\" : \"someCookieValue\"" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"headers\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someHeaderName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someHeaderValue\" ]" + Line.SEPARATOR +
                "    } ]" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpForward\" : {" + Line.SEPARATOR +
                "    \"host\" : \"someHost\"," + Line.SEPARATOR +
                "    \"port\" : 1234," + Line.SEPARATOR +
                "    \"scheme\" : \"HTTPS\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 5," + Line.SEPARATOR +
                "    \"unlimited\" : false" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}", jsonExpectation);
    }

    public void shouldSerializeCompleteObjectWithCallback() throws IOException {
        // when
        String jsonExpectation = new ExpectationSerializer().serialize(new ExpectationDTO()
                        .setHttpRequest(
                                new HttpRequestDTO()
                                        .setMethod("someMethod")
                                        .setPath("somePath")
                                        .setQueryStringParameters(Arrays.asList(
                                                new ParameterDTO(new Parameter("queryStringParameterNameOne", "queryStringParameterValueOne_One", "queryStringParameterValueOne_Two")),
                                                new ParameterDTO(new Parameter("queryStringParameterNameTwo", "queryStringParameterValueTwo_One"))
                                        ))
                                        .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                                        .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                                        .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                        )
                        .setHttpCallback(
                                new HttpCallbackDTO()
                                        .setCallbackClass("someClass")
                        )
                        .setTimes(new TimesDTO(Times.exactly(5)))
                        .buildObject()
        );

        // then
        assertEquals("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"method\" : \"someMethod\"," + Line.SEPARATOR +
                "    \"path\" : \"somePath\"," + Line.SEPARATOR +
                "    \"queryStringParameters\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"queryStringParameterNameOne\"," + Line.SEPARATOR +
                "      \"values\" : [ \"queryStringParameterValueOne_One\", \"queryStringParameterValueOne_Two\" ]" + Line.SEPARATOR +
                "    }, {" + Line.SEPARATOR +
                "      \"name\" : \"queryStringParameterNameTwo\"," + Line.SEPARATOR +
                "      \"values\" : [ \"queryStringParameterValueTwo_One\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"body\" : \"someBody\"," + Line.SEPARATOR +
                "    \"cookies\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someCookieName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someCookieValue\" ]" + Line.SEPARATOR +
                "    } ]," + Line.SEPARATOR +
                "    \"headers\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"someHeaderName\"," + Line.SEPARATOR +
                "      \"values\" : [ \"someHeaderValue\" ]" + Line.SEPARATOR +
                "    } ]" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpCallback\" : {" + Line.SEPARATOR +
                "    \"callbackClass\" : \"someClass\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 5," + Line.SEPARATOR +
                "    \"unlimited\" : false" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}", jsonExpectation);
    }

    @Test
    public void shouldSerializePartialRequestAndResponse() throws IOException {
        // when
        String jsonExpectation = new ExpectationSerializer().serialize(new ExpectationDTO()
                        .setHttpRequest(
                                new HttpRequestDTO()
                                        .setPath("somePath")
                        )
                        .setHttpResponse(
                                new HttpResponseDTO()
                                        .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                        )
                        .setTimes(new TimesDTO(Times.exactly(5)))
                        .buildObject()
        );

        // then
        assertEquals("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"path\" : \"somePath\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpResponse\" : {" + Line.SEPARATOR +
                "    \"body\" : \"someBody\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 5," + Line.SEPARATOR +
                "    \"unlimited\" : false" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}", jsonExpectation);
    }

    @Test
    public void shouldSerializeStringRegexBody() throws IOException {
        // when
        String jsonExpectation = new ExpectationSerializer().serialize(new ExpectationDTO()
                        .setHttpRequest(
                                new HttpRequestDTO()
                                        .setPath("somePath")
                                        .setBody(new StringBodyDTO(new StringBody("some[a-zA-Z]*", Body.Type.REGEX)))
                        )
                        .setHttpResponse(
                                new HttpResponseDTO()
                                        .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                        )
                        .setTimes(new TimesDTO(Times.exactly(5)))
                        .buildObject()
        );

        // then
        assertEquals("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"path\" : \"somePath\"," + Line.SEPARATOR +
                "    \"body\" : {" + Line.SEPARATOR +
                "      \"type\" : \"REGEX\"," + Line.SEPARATOR +
                "      \"value\" : \"some[a-zA-Z]*\"" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpResponse\" : {" + Line.SEPARATOR +
                "    \"body\" : \"someBody\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 5," + Line.SEPARATOR +
                "    \"unlimited\" : false" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}", jsonExpectation);
    }

    @Test
    public void shouldSerializeStringParameterBody() throws IOException {
        // when
        String jsonExpectation = new ExpectationSerializer().serialize(new ExpectationDTO()
                        .setHttpRequest(
                                new HttpRequestDTO()
                                        .setPath("somePath")
                                        .setBody(new ParameterBodyDTO(new ParameterBody(
                                                new Parameter("parameterOneName", "parameterOneValueOne", "parameterOneValueTwo"),
                                                new Parameter("parameterTwoName", "parameterTwoValue")
                                        )))
                        )
                        .setHttpResponse(
                                new HttpResponseDTO()
                                        .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                        )
                        .setTimes(new TimesDTO(Times.exactly(5)))
                        .buildObject()
        );

        // then
        assertEquals("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"path\" : \"somePath\"," + Line.SEPARATOR +
                "    \"body\" : {" + Line.SEPARATOR +
                "      \"type\" : \"PARAMETERS\"," + Line.SEPARATOR +
                "      \"parameters\" : [ {" + Line.SEPARATOR +
                "        \"name\" : \"parameterOneName\"," + Line.SEPARATOR +
                "        \"values\" : [ \"parameterOneValueOne\", \"parameterOneValueTwo\" ]" + Line.SEPARATOR +
                "      }, {" + Line.SEPARATOR +
                "        \"name\" : \"parameterTwoName\"," + Line.SEPARATOR +
                "        \"values\" : [ \"parameterTwoValue\" ]" + Line.SEPARATOR +
                "      } ]" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpResponse\" : {" + Line.SEPARATOR +
                "    \"body\" : \"someBody\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 5," + Line.SEPARATOR +
                "    \"unlimited\" : false" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}", jsonExpectation);
    }

    @Test
    public void shouldSerializePartialExpectation() throws IOException {
        // when
        String jsonExpectation = new ExpectationSerializer().serialize(new ExpectationDTO()
                        .setHttpRequest(
                                new HttpRequestDTO()
                                        .setPath("somePath")
                        )
                        .setHttpResponse(
                                new HttpResponseDTO()
                                        .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                        )
                        .buildObject()
        );

        // then
        assertEquals("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"path\" : \"somePath\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpResponse\" : {" + Line.SEPARATOR +
                "    \"body\" : \"someBody\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 1," + Line.SEPARATOR +
                "    \"unlimited\" : false" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}", jsonExpectation);
    }

    @Test
    public void shouldSerializePartialExpectationArray() throws IOException {
        // when
        Expectation expectation = new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setPath("somePath")
                )
                .setHttpResponse(
                        new HttpResponseDTO()
                                .setBody(new StringBodyDTO(new StringBody("someBody", Body.Type.STRING)))
                )
                .buildObject();
        String jsonExpectation = new ExpectationSerializer().serialize(new Expectation[]{
                expectation,
                expectation,
                expectation
        });

        // then
        assertEquals("[ {" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"path\" : \"somePath\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpResponse\" : {" + Line.SEPARATOR +
                "    \"body\" : \"someBody\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 1," + Line.SEPARATOR +
                "    \"unlimited\" : false" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}, {" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"path\" : \"somePath\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpResponse\" : {" + Line.SEPARATOR +
                "    \"body\" : \"someBody\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 1," + Line.SEPARATOR +
                "    \"unlimited\" : false" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}, {" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"path\" : \"somePath\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"httpResponse\" : {" + Line.SEPARATOR +
                "    \"body\" : \"someBody\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"remainingTimes\" : 1," + Line.SEPARATOR +
                "    \"unlimited\" : false" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "} ]", jsonExpectation);
    }
}
