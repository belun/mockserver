package org.mockserver.matchers;

import org.junit.Test;
import org.mockserver.Line;
import org.mockserver.model.*;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockserver.model.BinaryBody.binary;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.ParameterBody.params;
import static org.mockserver.model.StringBody.*;

/**
 * @author jamesdbloom
 */
public class HttpRequestMatcherTest {

    @Test
    public void matchesMatchingMethod() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withMethod("HEAD")).matches(new HttpRequest().withMethod("HEAD")));
    }

    @Test
    public void matchesMatchingMethodRegex() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withMethod("P[A-Z]{2}")).matches(new HttpRequest().withMethod("PUT")));
    }

    @Test
    public void doesNotMatchIncorrectMethod() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withMethod("HEAD")).matches(new HttpRequest().withMethod("OPTIONS")));
    }

    @Test
    public void doesNotMatchIncorrectMethodRegex() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withMethod("P[A-Z]{2}")).matches(new HttpRequest().withMethod("POST")));
    }

    @Test
    public void matchesMatchingPath() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withPath("somePath")).matches(new HttpRequest().withPath("somePath")));
    }

    @Test
    public void matchesMatchingPathRegex() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withPath("someP[a-z]{3}")).matches(new HttpRequest().withPath("somePath")));
    }

    @Test
    public void doesNotMatchIncorrectPath() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withPath("somepath")).matches(new HttpRequest().withPath("pathsome")));
    }

    @Test
    public void doesNotMatchIncorrectPathRegex() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withPath("someP[a-z]{2}")).matches(new HttpRequest().withPath("somePath")));
    }

    @Test
    public void matchesMatchingQueryString() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withQueryStringParameters(new Parameter("someKey", "someValue"))).matches(new HttpRequest().withQueryStringParameter(new Parameter("someKey", "someValue"))));
    }

    @Test
    public void matchesMatchingQueryStringRegexKeyAndValue() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withQueryStringParameters(new Parameter("someK[a-z]{2}", "someV[a-z]{4}"))).matches(new HttpRequest().withQueryStringParameter(new Parameter("someKey", "someValue"))));
    }

    @Test
    public void matchesMatchingQueryStringRegexKey() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withQueryStringParameters(new Parameter("someK[a-z]{2}", "someValue"))).matches(new HttpRequest().withQueryStringParameter(new Parameter("someKey", "someValue"))));
    }

    @Test
    public void matchesMatchingQueryStringRegexValue() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withQueryStringParameters(new Parameter("someKey", "someV[a-z]{4}"))).matches(new HttpRequest().withQueryStringParameter(new Parameter("someKey", "someValue"))));
    }

    @Test
    public void doesNotMatchIncorrectQueryStringName() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withQueryStringParameters(new Parameter("someKey", "someValue"))).matches(new HttpRequest().withQueryStringParameter(new Parameter("someOtherKey", "someValue"))));
    }

    @Test
    public void doesNotMatchIncorrectQueryStringValue() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withQueryStringParameters(new Parameter("someKey", "someValue"))).matches(new HttpRequest().withQueryStringParameter(new Parameter("someKey", "someOtherValue"))));
    }

    @Test
    public void doesNotMatchIncorrectQueryStringRegexKeyAndValue() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withQueryStringParameters(new Parameter("someK[a-z]{5}", "someV[a-z]{2}"))).matches(new HttpRequest().withQueryStringParameter(new Parameter("someKey", "someValue"))));
    }

    @Test
    public void doesNotMatchIncorrectQueryStringRegexKey() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withQueryStringParameters(new Parameter("someK[a-z]{5}", "someValue"))).matches(new HttpRequest().withQueryStringParameter(new Parameter("someKey", "someValue"))));
    }

    @Test
    public void doesNotMatchIncorrectQueryStringRegexValue() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withQueryStringParameters(new Parameter("someKey", "someV[a-z]{2}"))).matches(new HttpRequest().withQueryStringParameter(new Parameter("someKey", "someValue"))));
    }

    @Test
    public void matchesMatchingQueryStringParameters() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withQueryStringParameters(new Parameter("name", "value"))).matches(new HttpRequest().withQueryStringParameters(new Parameter("name", "value"))));
    }

    @Test
    public void matchesMatchingQueryStringParametersWithRegex() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withQueryStringParameters(new Parameter("name", "v[a-z]{4}"))).matches(new HttpRequest().withQueryStringParameters(new Parameter("name", "value"))));
    }

    @Test
    public void queryStringParametersMatchesMatchingQueryString() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withQueryStringParameters(new Parameter("nameOne", "valueOne"))).matches(new HttpRequest().withQueryStringParameters(
                new Parameter("nameOne", "valueOne"),
                new Parameter("nameTwo", "valueTwo")
        )));
        assertTrue(new HttpRequestMatcher(new HttpRequest().withQueryStringParameters(new Parameter("nameTwo", "valueTwo"))).matches(new HttpRequest().withQueryStringParameters(
                new Parameter("nameOne", "valueOne"),
                new Parameter("nameTwo", "valueTwo")
        )));
        assertTrue(new HttpRequestMatcher(new HttpRequest().withQueryStringParameters(new Parameter("nameTwo", "valueTwo", "valueThree"))).matches(new HttpRequest().withQueryStringParameters(
                new Parameter("nameOne", "valueOne"),
                new Parameter("nameTwo", "valueTwo"),
                new Parameter("nameTwo", "valueThree")
        )));
        assertTrue(new HttpRequestMatcher(new HttpRequest().withQueryStringParameters(new Parameter("nameTwo", "valueTwo"))).matches(new HttpRequest().withQueryStringParameters(
                new Parameter("nameOne", "valueOne"),
                new Parameter("nameTwo", "valueTwo"),
                new Parameter("nameTwo", "valueThree")
        )));
        assertTrue(new HttpRequestMatcher(new HttpRequest().withQueryStringParameters(new Parameter("nameTwo", "valueThree"))).matches(new HttpRequest().withQueryStringParameters(
                new Parameter("nameOne", "valueOne"),
                new Parameter("nameTwo", "valueTwo"),
                new Parameter("nameTwo", "valueThree")
        )));
        assertTrue(new HttpRequestMatcher(new HttpRequest().withQueryStringParameters(new Parameter("nameTwo", "valueT[a-z]{0,10}"))).matches(new HttpRequest().withQueryStringParameters(
                new Parameter("nameOne", "valueOne"),
                new Parameter("nameTwo", "valueTwo"),
                new Parameter("nameTwo", "valueThree")
        )));
    }

    @Test
    public void bodyMatchesMatchingBodyParameters() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withBody(params(new Parameter("nameOne", "valueOne")))).matches(new HttpRequest().withBody(new ParameterBody(
                new Parameter("nameOne", "valueOne"),
                new Parameter("nameTwo", "valueTwo")
        ))));
        assertTrue(new HttpRequestMatcher(new HttpRequest().withBody(new ParameterBody(new Parameter("nameTwo", "valueTwo")))).matches(new HttpRequest().withBody(new ParameterBody(
                new Parameter("nameOne", "valueOne"),
                new Parameter("nameTwo", "valueTwo")
        ))));
        assertTrue(new HttpRequestMatcher(new HttpRequest().withBody(params(new Parameter("nameTwo", "valueTwo", "valueThree")))).matches(new HttpRequest().withBody(new ParameterBody(
                new Parameter("nameOne", "valueOne"),
                new Parameter("nameTwo", "valueTwo"),
                new Parameter("nameTwo", "valueThree")
        ))));
        assertTrue(new HttpRequestMatcher(new HttpRequest().withBody(new ParameterBody(new Parameter("nameTwo", "valueTwo")))).matches(new HttpRequest().withBody(new ParameterBody(
                new Parameter("nameOne", "valueOne"),
                new Parameter("nameTwo", "valueTwo"),
                new Parameter("nameTwo", "valueThree")
        ))));
        assertTrue(new HttpRequestMatcher(new HttpRequest().withBody(params(new Parameter("nameTwo", "valueThree")))).matches(new HttpRequest().withBody(new ParameterBody(
                new Parameter("nameOne", "valueOne"),
                new Parameter("nameTwo", "valueTwo"),
                new Parameter("nameTwo", "valueThree")
        ))));
        assertTrue(new HttpRequestMatcher(new HttpRequest().withBody(new ParameterBody(new Parameter("nameTwo", "valueT[a-z]{0,10}")))).matches(new HttpRequest().withBody(new ParameterBody(
                new Parameter("nameOne", "valueOne"),
                new Parameter("nameTwo", "valueTwo"),
                new Parameter("nameTwo", "valueThree")
        ))));
    }

    @Test
    public void doesNotMatchIncorrectParameterName() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withBody(new ParameterBody(new Parameter("name", "value")))).matches(new HttpRequest().withBody(new ParameterBody(new Parameter("name1", "value")))));
    }

    @Test
    public void doesNotMatchIncorrectParameterValue() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withBody(new ParameterBody(new Parameter("name", "value")))).matches(new HttpRequest().withBody(new ParameterBody(new Parameter("name", "value1")))));
    }

    @Test
    public void doesNotMatchIncorrectParameterValueRegex() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withBody(new ParameterBody(new Parameter("name", "va[0-9]{1}ue")))).matches(new HttpRequest().withBody(new ParameterBody(new Parameter("name", "value1")))));
    }

    @Test
    public void matchesMatchingBody() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withBody(new StringBody("somebody", Type.STRING))).matches(new HttpRequest().withBody("somebody")));
    }

    @Test
    public void doesNotMatchIncorrectBody() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withBody(exact("somebody"))).matches(new HttpRequest().withBody("bodysome")));
    }

    @Test
    public void matchesMatchingBodyRegex() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withBody(regex("some[a-z]{4}"))).matches(new HttpRequest().withBody("somebody")));
    }

    @Test
    public void doesNotMatchIncorrectBodyRegex() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withBody(regex("some[a-z]{3}"))).matches(new HttpRequest().withBody("bodysome")));
    }

    @Test
    public void matchesMatchingBodyXPath() {
        String matched = "" +
                "<element>" +
                "   <key>some_key</key>" +
                "   <value>some_value</value>" +
                "</element>";
        assertTrue(new HttpRequestMatcher(new HttpRequest().withBody(xpath("/element[key = 'some_key' and value = 'some_value']"))).matches(new HttpRequest().withBody(matched)));
    }

    @Test
    public void doesNotMatchIncorrectBodyXPath() {
        String matched = "" +
                "<element>" +
                "   <key>some_key</key>" +
                "</element>";
        assertFalse(new HttpRequestMatcher(new HttpRequest().withBody(xpath("/element[key = 'some_key' and value = 'some_value']"))).matches(new HttpRequest().withBody(matched)));
    }

    @Test
    public void matchesMatchingJSONBody() {
        String matched = "" +
                "{ " +
                "   \"some_field\": \"some_value\", " +
                "   \"some_other_field\": \"some_other_value\" " +
                "}";
        assertTrue(new HttpRequestMatcher(new HttpRequest().withBody(json("{ \"some_field\": \"some_value\" }"))).matches(new HttpRequest().withBody(matched)));
    }

    @Test
    public void doesNotMatchIncorrectJSONBody() {
        String matched = "" +
                "{ " +
                "   \"some_incorrect_field\": \"some_value\", " +
                "   \"some_other_field\": \"some_other_value\" " +
                "}";
        assertFalse(new HttpRequestMatcher(new HttpRequest().withBody(json("{ \"some_field\": \"some_value\" }"))).matches(new HttpRequest().withBody(matched)));
    }


    @Test
    public void matchesMatchingBinaryBody() {
        byte[] matched = "some binary value".getBytes();
        assertTrue(new HttpRequestMatcher(new HttpRequest().withBody(binary("some binary value".getBytes()))).matches(new HttpRequest().withBody(binary(matched))));
    }

    @Test
    public void doesNotMatchIncorrectBinaryBody() {
        byte[] matched = "some other binary value".getBytes();
        assertFalse(new HttpRequestMatcher(new HttpRequest().withBody(binary("some binary value".getBytes()))).matches(new HttpRequest().withBody(binary(matched))));
    }

    @Test
    public void matchesMatchingHeaders() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withHeaders(new Header("name", "value"))).matches(new HttpRequest().withHeaders(new Header("name", "value"))));
    }

    @Test
    public void matchesMatchingHeadersWithRegex() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withHeaders(new Header("name", ".*"))).matches(new HttpRequest().withHeaders(new Header("name", "value"))));
    }

    @Test
    public void doesNotMatchIncorrectHeaderName() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withHeaders(new Header("name", "value"))).matches(new HttpRequest().withHeaders(new Header("name1", "value"))));
    }

    @Test
    public void doesNotMatchIncorrectHeaderValue() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withHeaders(new Header("name", "value"))).matches(new HttpRequest().withHeaders(new Header("name", "value1"))));
    }

    @Test
    public void doesNotMatchIncorrectHeaderValueRegex() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withHeaders(new Header("name", "[0-9]{0,100}"))).matches(new HttpRequest().withHeaders(new Header("name", "value1"))));
    }

    @Test
    public void matchesMatchingCookies() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withCookies(new Cookie("name", "value"))).matches(new HttpRequest().withCookies(new Cookie("name", "value"))));
    }

    @Test
    public void matchesMatchingCookiesWithRegex() {
        assertTrue(new HttpRequestMatcher(new HttpRequest().withCookies(new Cookie("name", "[a-z]{0,20}lue"))).matches(new HttpRequest().withCookies(new Cookie("name", "value"))));
    }

    @Test
    public void doesNotMatchIncorrectCookieName() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withCookies(new Cookie("name", "value"))).matches(new HttpRequest().withCookies(new Cookie("name1", "value"))));
    }

    @Test
    public void doesNotMatchIncorrectCookieValue() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withCookies(new Cookie("name", "value"))).matches(new HttpRequest().withCookies(new Cookie("name", "value1"))));
    }

    @Test
    public void doesNotMatchIncorrectCookieValueRegex() {
        assertFalse(new HttpRequestMatcher(new HttpRequest().withCookies(new Cookie("name", "[A-Z]{0,10}"))).matches(new HttpRequest().withCookies(new Cookie("name", "value1"))));
    }

    @Test
    public void shouldReturnFormattedRequestWithStringBodyInToString() {
        assertEquals("{" + Line.SEPARATOR +
                        "  \"body\" : \"some_body\"," + Line.SEPARATOR +
                        "  \"headers\" : [ {" + Line.SEPARATOR +
                        "    \"name\" : \"name\"," + Line.SEPARATOR +
                        "    \"values\" : [ \"value\" ]" + Line.SEPARATOR +
                        "  } ]," + Line.SEPARATOR +
                        "  \"cookies\" : [ {" + Line.SEPARATOR +
                        "    \"name\" : \"name\"," + Line.SEPARATOR +
                        "    \"value\" : \"[A-Z]{0,10}\"" + Line.SEPARATOR +
                        "  } ]" + Line.SEPARATOR +
                        "}",
                new HttpRequestMatcher(
                        request()
                                .withBody("some_body")
                                .withHeaders(new Header("name", "value"))
                                .withCookies(new Cookie("name", "[A-Z]{0,10}"))
                ).toString()
        );
    }

    @Test
    public void shouldReturnFormattedRequestWithJsonBodyInToString() {
        assertEquals("{" + Line.SEPARATOR +
                        "  \"body\" : {" + Line.SEPARATOR +
                        "    \"type\" : \"JSON\"," + Line.SEPARATOR +
                        "    \"value\" : \"{ \\\"key\\\": \\\"some_value\\\" }\"" + Line.SEPARATOR +
                        "  }," + Line.SEPARATOR +
                        "  \"headers\" : [ {" + Line.SEPARATOR +
                        "    \"name\" : \"name\"," + Line.SEPARATOR +
                        "    \"values\" : [ \"value\" ]" + Line.SEPARATOR +
                        "  } ]," + Line.SEPARATOR +
                        "  \"cookies\" : [ {" + Line.SEPARATOR +
                        "    \"name\" : \"name\"," + Line.SEPARATOR +
                        "    \"value\" : \"[A-Z]{0,10}\"" + Line.SEPARATOR +
                        "  } ]" + Line.SEPARATOR +
                        "}",
                new HttpRequestMatcher(
                        request()
                                .withBody(json("{ \"key\": \"some_value\" }"))
                                .withHeaders(new Header("name", "value"))
                                .withCookies(new Cookie("name", "[A-Z]{0,10}"))
                ).toString()
        );
    }
}
