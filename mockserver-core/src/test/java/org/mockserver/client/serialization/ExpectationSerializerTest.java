package org.mockserver.client.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author jamesdbloom
 */
public class ExpectationSerializerTest {

    private final Expectation fullExpectation = new Expectation(
            new HttpRequest()
                    .withMethod("GET")
                    .withPath("somePath")
                    .withQueryStringParameters(new Parameter("queryParameterName", Arrays.asList("queryParameterValue")))
                    .withBody(new StringBody("somebody", Body.Type.STRING))
                    .withHeaders(new Header("headerName", "headerValue"))
                    .withCookies(new Cookie("cookieName", "cookieValue")),
            Times.once()
    ).thenRespond(new HttpResponse()
            .withStatusCode(304)
            .withBody("responseBody")
            .withHeaders(new Header("headerName", "headerValue"))
            .withCookies(new Cookie("cookieName", "cookieValue"))
            .withDelay(new Delay(TimeUnit.MICROSECONDS, 1)));
    private final ExpectationDTO fullExpectationDTO = new ExpectationDTO()
            .setHttpRequest(
                    new HttpRequestDTO()
                            .setMethod("GET")
                            .setPath("somePath")
                            .setQueryStringParameters(Arrays.<ParameterDTO>asList((ParameterDTO) new ParameterDTO(new Parameter("queryParameterName", Arrays.asList("queryParameterValue")))))
                            .setBody(BodyDTO.createDTO(new StringBody("somebody", Body.Type.STRING)))
                            .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("headerName", Arrays.asList("headerValue")))))
                            .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("cookieName", "cookieValue"))))
            )
            .setHttpResponse(
                    new HttpResponseDTO()
                            .setStatusCode(304)
                            .setBody(new StringBodyDTO(new StringBody("responseBody", Body.Type.STRING)))
                            .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("headerName", Arrays.asList("headerValue")))))
                            .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("cookieName", "cookieValue"))))
                            .setDelay(
                                    new DelayDTO()
                                            .setTimeUnit(TimeUnit.MICROSECONDS)
                                            .setValue(1)
                            )
            )
            .setTimes(new TimesDTO(Times.once()));
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ObjectWriter objectWriter;
    @InjectMocks
    private ExpectationSerializer expectationSerializer;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setupTestFixture() {
        expectationSerializer = spy(new ExpectationSerializer());

        initMocks(this);
    }

    @Test
    public void shouldSerializeObject() throws IOException {
        // given
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(objectWriter);

        // when
        expectationSerializer.serialize(fullExpectation);

        // then
        verify(objectMapper).writerWithDefaultPrettyPrinter();
        verify(objectWriter).writeValueAsString(fullExpectationDTO);
    }

    @Test
    public void shouldSerializeFullObjectWithResponseAsJava() throws IOException {
        // when
        assertEquals(Line.SEPARATOR +
                        "new MockServerClient()" + Line.SEPARATOR +
                        "        .when(" + Line.SEPARATOR +
                        "                request()" + Line.SEPARATOR +
                        "                        .withMethod(\"GET\")" + Line.SEPARATOR +
                        "                        .withPath(\"somePath\")" + Line.SEPARATOR +
                        "                        .withHeaders(" + Line.SEPARATOR +
                        "                                new Header(\"requestHeaderNameOne\", \"requestHeaderValueOneOne\", \"requestHeaderValueOneTwo\")," + Line.SEPARATOR +
                        "                                new Header(\"requestHeaderNameTwo\", \"requestHeaderValueTwo\")" + Line.SEPARATOR +
                        "                        )" + Line.SEPARATOR +
                        "                        .withCookies(" + Line.SEPARATOR +
                        "                                new Cookie(\"requestCookieNameOne\", \"requestCookieValueOne\")," + Line.SEPARATOR +
                        "                                new Cookie(\"requestCookieNameTwo\", \"requestCookieValueTwo\")" + Line.SEPARATOR +
                        "                        )" + Line.SEPARATOR +
                        "                        .withQueryStringParameters(" + Line.SEPARATOR +
                        "                                new QueryStringParameter(\"requestQueryStringParameterNameOne\", \"requestQueryStringParameterValueOneOne\", \"requestQueryStringParameterValueOneTwo\")," + Line.SEPARATOR +
                        "                                new QueryStringParameter(\"requestQueryStringParameterNameTwo\", \"requestQueryStringParameterValueTwo\")" + Line.SEPARATOR +
                        "                        )" + Line.SEPARATOR +
                        "                        .withBody(new StringBody(\"somebody\", Body.Type.STRING))," + Line.SEPARATOR +
                        "                Times.once()" + Line.SEPARATOR +
                        "        )" + Line.SEPARATOR +
                        "        .thenRespond(" + Line.SEPARATOR +
                        "                response()" + Line.SEPARATOR +
                        "                        .withStatusCode(304)" + Line.SEPARATOR +
                        "                        .withHeaders(" + Line.SEPARATOR +
                        "                                new Header(\"responseHeaderNameOne\", \"responseHeaderValueOneOne\", \"responseHeaderValueOneTwo\")," + Line.SEPARATOR +
                        "                                new Header(\"responseHeaderNameTwo\", \"responseHeaderValueTwo\")" + Line.SEPARATOR +
                        "                        )" + Line.SEPARATOR +
                        "                        .withCookies(" + Line.SEPARATOR +
                        "                                new Cookie(\"responseCookieNameOne\", \"responseCookieValueOne\")," + Line.SEPARATOR +
                        "                                new Cookie(\"responseCookieNameTwo\", \"responseCookieValueTwo\")" + Line.SEPARATOR +
                        "                        )" + Line.SEPARATOR +
                        "                        .withBody(\"responseBody\")" + Line.SEPARATOR +
                        "        );",
                expectationSerializer.serializeAsJava(
                        new Expectation(
                                new HttpRequest()
                                        .withMethod("GET")
                                        .withPath("somePath")
                                        .withQueryStringParameters(
                                                new Parameter("requestQueryStringParameterNameOne", "requestQueryStringParameterValueOneOne", "requestQueryStringParameterValueOneTwo"),
                                                new Parameter("requestQueryStringParameterNameTwo", "requestQueryStringParameterValueTwo")
                                        )
                                        .withHeaders(
                                                new Header("requestHeaderNameOne", "requestHeaderValueOneOne", "requestHeaderValueOneTwo"),
                                                new Header("requestHeaderNameTwo", "requestHeaderValueTwo")
                                        )
                                        .withCookies(
                                                new Cookie("requestCookieNameOne", "requestCookieValueOne"),
                                                new Cookie("requestCookieNameTwo", "requestCookieValueTwo")
                                        )
                                        .withBody(new StringBody("somebody", Body.Type.STRING)),
                                Times.once()
                        ).thenRespond(
                                new HttpResponse()
                                        .withStatusCode(304)
                                        .withHeaders(
                                                new Header("responseHeaderNameOne", "responseHeaderValueOneOne", "responseHeaderValueOneTwo"),
                                                new Header("responseHeaderNameTwo", "responseHeaderValueTwo")
                                        )
                                        .withCookies(
                                                new Cookie("responseCookieNameOne", "responseCookieValueOne"),
                                                new Cookie("responseCookieNameTwo", "responseCookieValueTwo")
                                        )
                                        .withBody("responseBody")
                        )
                )
        );
    }

    @Test
    public void shouldSerializeFullObjectWithParameterBodyResponseAsJava() throws IOException {
        // when
        assertEquals(Line.SEPARATOR +
                        "new MockServerClient()" + Line.SEPARATOR +
                        "        .when(" + Line.SEPARATOR +
                        "                request()" + Line.SEPARATOR +
                        "                        .withBody(" + Line.SEPARATOR +
                        "                                new ParameterBody(" + Line.SEPARATOR +
                        "                                        new Parameter(\"requestBodyParameterNameOne\", \"requestBodyParameterValueOneOne\", \"requestBodyParameterValueOneTwo\")," + Line.SEPARATOR +
                        "                                        new Parameter(\"requestBodyParameterNameTwo\", \"requestBodyParameterValueTwo\")" + Line.SEPARATOR +
                        "                                )" + Line.SEPARATOR +
                        "                        )," + Line.SEPARATOR +
                        "                Times.once()" + Line.SEPARATOR +
                        "        )" + Line.SEPARATOR +
                        "        .thenRespond(" + Line.SEPARATOR +
                        "                response()" + Line.SEPARATOR +
                        "                        .withStatusCode(200)" + Line.SEPARATOR +
                        "                        .withBody(\"responseBody\")" + Line.SEPARATOR +
                        "        );",
                expectationSerializer.serializeAsJava(
                        new Expectation(
                                new HttpRequest()
                                        .withBody(
                                                new ParameterBody(
                                                        new Parameter("requestBodyParameterNameOne", "requestBodyParameterValueOneOne", "requestBodyParameterValueOneTwo"),
                                                        new Parameter("requestBodyParameterNameTwo", "requestBodyParameterValueTwo")
                                                )
                                        ),
                                Times.once()
                        ).thenRespond(
                                new HttpResponse()
                                        .withBody("responseBody")
                        )
                )
        );
    }

    @Test
    public void shouldSerializeFullObjectWithBinaryBodyResponseAsJava() throws IOException {
        // when
        assertEquals(Line.SEPARATOR +
                        "new MockServerClient()" + Line.SEPARATOR +
                        "        .when(" + Line.SEPARATOR +
                        "                request()" + Line.SEPARATOR +
                        "                        .withBody(new byte[0]) /* note: not possible to generate code for binary data */," + Line.SEPARATOR +
                        "                Times.once()" + Line.SEPARATOR +
                        "        )" + Line.SEPARATOR +
                        "        .thenRespond(" + Line.SEPARATOR +
                        "                response()" + Line.SEPARATOR +
                        "                        .withStatusCode(200)" + Line.SEPARATOR +
                        "                        .withBody(\"responseBody\")" + Line.SEPARATOR +
                        "        );",
                expectationSerializer.serializeAsJava(
                        new Expectation(
                                new HttpRequest()
                                        .withBody(
                                                new BinaryBody(new byte[0])
                                        ),
                                Times.once()
                        ).thenRespond(
                                new HttpResponse()
                                        .withBody("responseBody")
                        )
                )
        );
    }

    @Test
    public void shouldSerializeFullObjectWithForwardAsJava() throws IOException {
        // when
        assertEquals(Line.SEPARATOR +
                        "new MockServerClient()" + Line.SEPARATOR +
                        "        .when(" + Line.SEPARATOR +
                        "                request()" + Line.SEPARATOR +
                        "                        .withMethod(\"GET\")" + Line.SEPARATOR +
                        "                        .withPath(\"somePath\")" + Line.SEPARATOR +
                        "                        .withHeaders(" + Line.SEPARATOR +
                        "                                new Header(\"requestHeaderNameOne\", \"requestHeaderValueOneOne\", \"requestHeaderValueOneTwo\")," + Line.SEPARATOR +
                        "                                new Header(\"requestHeaderNameTwo\", \"requestHeaderValueTwo\")" + Line.SEPARATOR +
                        "                        )" + Line.SEPARATOR +
                        "                        .withCookies(" + Line.SEPARATOR +
                        "                                new Cookie(\"requestCookieNameOne\", \"requestCookieValueOne\")," + Line.SEPARATOR +
                        "                                new Cookie(\"requestCookieNameTwo\", \"requestCookieValueTwo\")" + Line.SEPARATOR +
                        "                        )" + Line.SEPARATOR +
                        "                        .withQueryStringParameters(" + Line.SEPARATOR +
                        "                                new QueryStringParameter(\"requestQueryStringParameterNameOne\", \"requestQueryStringParameterValueOneOne\", \"requestQueryStringParameterValueOneTwo\")," + Line.SEPARATOR +
                        "                                new QueryStringParameter(\"requestQueryStringParameterNameTwo\", \"requestQueryStringParameterValueTwo\")" + Line.SEPARATOR +
                        "                        )" + Line.SEPARATOR +
                        "                        .withBody(new StringBody(\"somebody\", Body.Type.STRING))," + Line.SEPARATOR +
                        "                Times.once()" + Line.SEPARATOR +
                        "        )" + Line.SEPARATOR +
                        "        .thenForward(" + Line.SEPARATOR +
                        "                forward()" + Line.SEPARATOR +
                        "                        .withHost(\"some_host\")" + Line.SEPARATOR +
                        "                        .withPort(9090)" + Line.SEPARATOR +
                        "                        .withScheme(HttpForward.Scheme.HTTPS)" + Line.SEPARATOR +
                        "        );",
                expectationSerializer.serializeAsJava(
                        new Expectation(
                                new HttpRequest()
                                        .withMethod("GET")
                                        .withPath("somePath")
                                        .withQueryStringParameters(
                                                new Parameter("requestQueryStringParameterNameOne", "requestQueryStringParameterValueOneOne", "requestQueryStringParameterValueOneTwo"),
                                                new Parameter("requestQueryStringParameterNameTwo", "requestQueryStringParameterValueTwo")
                                        )
                                        .withHeaders(
                                                new Header("requestHeaderNameOne", "requestHeaderValueOneOne", "requestHeaderValueOneTwo"),
                                                new Header("requestHeaderNameTwo", "requestHeaderValueTwo")
                                        )
                                        .withCookies(
                                                new Cookie("requestCookieNameOne", "requestCookieValueOne"),
                                                new Cookie("requestCookieNameTwo", "requestCookieValueTwo")
                                        )
                                        .withBody(new StringBody("somebody", Body.Type.STRING)),
                                Times.once()
                        ).thenForward(
                                new HttpForward()
                                        .withHost("some_host")
                                        .withPort(9090)
                                        .withScheme(HttpForward.Scheme.HTTPS)
                        )
                )
        );
    }

    @Test
    public void shouldSerializeFullObjectWithCallbackAsJava() throws IOException {
        // when
        assertEquals(Line.SEPARATOR +
                        "new MockServerClient()" + Line.SEPARATOR +
                        "        .when(" + Line.SEPARATOR +
                        "                request()" + Line.SEPARATOR +
                        "                        .withMethod(\"GET\")" + Line.SEPARATOR +
                        "                        .withPath(\"somePath\")" + Line.SEPARATOR +
                        "                        .withHeaders(" + Line.SEPARATOR +
                        "                                new Header(\"requestHeaderNameOne\", \"requestHeaderValueOneOne\", \"requestHeaderValueOneTwo\")," + Line.SEPARATOR +
                        "                                new Header(\"requestHeaderNameTwo\", \"requestHeaderValueTwo\")" + Line.SEPARATOR +
                        "                        )" + Line.SEPARATOR +
                        "                        .withCookies(" + Line.SEPARATOR +
                        "                                new Cookie(\"requestCookieNameOne\", \"requestCookieValueOne\")," + Line.SEPARATOR +
                        "                                new Cookie(\"requestCookieNameTwo\", \"requestCookieValueTwo\")" + Line.SEPARATOR +
                        "                        )" + Line.SEPARATOR +
                        "                        .withQueryStringParameters(" + Line.SEPARATOR +
                        "                                new QueryStringParameter(\"requestQueryStringParameterNameOne\", \"requestQueryStringParameterValueOneOne\", \"requestQueryStringParameterValueOneTwo\")," + Line.SEPARATOR +
                        "                                new QueryStringParameter(\"requestQueryStringParameterNameTwo\", \"requestQueryStringParameterValueTwo\")" + Line.SEPARATOR +
                        "                        )" + Line.SEPARATOR +
                        "                        .withBody(new StringBody(\"somebody\", Body.Type.STRING))," + Line.SEPARATOR +
                        "                Times.once()" + Line.SEPARATOR +
                        "        )" + Line.SEPARATOR +
                        "        .thenCallback(" + Line.SEPARATOR +
                        "                callback()" + Line.SEPARATOR +
                        "                        .withCallbackClass(\"some_class\")" + Line.SEPARATOR +
                        "        );",
                expectationSerializer.serializeAsJava(
                        new Expectation(
                                new HttpRequest()
                                        .withMethod("GET")
                                        .withPath("somePath")
                                        .withQueryStringParameters(
                                                new Parameter("requestQueryStringParameterNameOne", "requestQueryStringParameterValueOneOne", "requestQueryStringParameterValueOneTwo"),
                                                new Parameter("requestQueryStringParameterNameTwo", "requestQueryStringParameterValueTwo")
                                        )
                                        .withHeaders(
                                                new Header("requestHeaderNameOne", "requestHeaderValueOneOne", "requestHeaderValueOneTwo"),
                                                new Header("requestHeaderNameTwo", "requestHeaderValueTwo")
                                        )
                                        .withCookies(
                                                new Cookie("requestCookieNameOne", "requestCookieValueOne"),
                                                new Cookie("requestCookieNameTwo", "requestCookieValueTwo")
                                        )
                                        .withBody(new StringBody("somebody", Body.Type.STRING)),
                                Times.once()
                        ).thenCallback(
                                new HttpCallback()
                                        .withCallbackClass("some_class")
                        )
                )
        );
    }

    @Test
    public void shouldEscapeJSONBodies() throws IOException {
        // when
        assertEquals("" + Line.SEPARATOR +
                        "new MockServerClient()" + Line.SEPARATOR +
                        "        .when(" + Line.SEPARATOR +
                        "                request()" + Line.SEPARATOR +
                        "                        .withPath(\"somePath\")" + Line.SEPARATOR +
                        "                        .withBody(new StringBody(\"[" + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "    {" + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"id\\\": \\\"1\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"title\\\": \\\"Xenophon's imperial fiction : on the education of Cyrus\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"author\\\": \\\"James Tatum\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"isbn\\\": \\\"0691067570\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"publicationDate\\\": \\\"1989\\\"" + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "    }," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "    {" + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"id\\\": \\\"2\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"title\\\": \\\"You are here : personal geographies and other maps of the imagination\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"author\\\": \\\"Katharine A. Harmon\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"isbn\\\": \\\"1568984308\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"publicationDate\\\": \\\"2004\\\"" + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "    }," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "    {" + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"id\\\": \\\"3\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"title\\\": \\\"You just don't understand : women and men in conversation\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"author\\\": \\\"Deborah Tannen\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"isbn\\\": \\\"0345372050\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"publicationDate\\\": \\\"1990\\\"" + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "    }" + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "]\", Body.Type.STRING))," + Line.SEPARATOR +
                        "                Times.once()" + Line.SEPARATOR +
                        "        )" + Line.SEPARATOR +
                        "        .thenRespond(" + Line.SEPARATOR +
                        "                response()" + Line.SEPARATOR +
                        "                        .withStatusCode(304)" + Line.SEPARATOR +
                        "                        .withBody(\"[" + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "    {" + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"id\\\": \\\"1\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"title\\\": \\\"Xenophon's imperial fiction : on the education of Cyrus\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"author\\\": \\\"James Tatum\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"isbn\\\": \\\"0691067570\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"publicationDate\\\": \\\"1989\\\"" + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "    }," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "    {" + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"id\\\": \\\"2\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"title\\\": \\\"You are here : personal geographies and other maps of the imagination\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"author\\\": \\\"Katharine A. Harmon\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"isbn\\\": \\\"1568984308\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"publicationDate\\\": \\\"2004\\\"" + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "    }," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "    {" + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"id\\\": \\\"3\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"title\\\": \\\"You just don't understand : women and men in conversation\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"author\\\": \\\"Deborah Tannen\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"isbn\\\": \\\"0345372050\\\"," + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "        \\\"publicationDate\\\": \\\"1990\\\"" + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "    }" + StringEscapeUtils.escapeJava(Line.SEPARATOR) + "]\")" + Line.SEPARATOR +
                        "        );",
                expectationSerializer.serializeAsJava(
                        new Expectation(
                                new HttpRequest()
                                        .withPath("somePath")
                                        .withBody(new StringBody("[" + Line.SEPARATOR +
                                                "    {" + Line.SEPARATOR +
                                                "        \"id\": \"1\"," + Line.SEPARATOR +
                                                "        \"title\": \"Xenophon's imperial fiction : on the education of Cyrus\"," + Line.SEPARATOR +
                                                "        \"author\": \"James Tatum\"," + Line.SEPARATOR +
                                                "        \"isbn\": \"0691067570\"," + Line.SEPARATOR +
                                                "        \"publicationDate\": \"1989\"" + Line.SEPARATOR +
                                                "    }," + Line.SEPARATOR +
                                                "    {" + Line.SEPARATOR +
                                                "        \"id\": \"2\"," + Line.SEPARATOR +
                                                "        \"title\": \"You are here : personal geographies and other maps of the imagination\"," + Line.SEPARATOR +
                                                "        \"author\": \"Katharine A. Harmon\"," + Line.SEPARATOR +
                                                "        \"isbn\": \"1568984308\"," + Line.SEPARATOR +
                                                "        \"publicationDate\": \"2004\"" + Line.SEPARATOR +
                                                "    }," + Line.SEPARATOR +
                                                "    {" + Line.SEPARATOR +
                                                "        \"id\": \"3\"," + Line.SEPARATOR +
                                                "        \"title\": \"You just don't understand : women and men in conversation\"," + Line.SEPARATOR +
                                                "        \"author\": \"Deborah Tannen\"," + Line.SEPARATOR +
                                                "        \"isbn\": \"0345372050\"," + Line.SEPARATOR +
                                                "        \"publicationDate\": \"1990\"" + Line.SEPARATOR +
                                                "    }" + Line.SEPARATOR +
                                                "]", Body.Type.STRING)),
                                Times.once()
                        ).thenRespond(
                                new HttpResponse()
                                        .withStatusCode(304)
                                        .withBody("[" + Line.SEPARATOR +
                                                "    {" + Line.SEPARATOR +
                                                "        \"id\": \"1\"," + Line.SEPARATOR +
                                                "        \"title\": \"Xenophon's imperial fiction : on the education of Cyrus\"," + Line.SEPARATOR +
                                                "        \"author\": \"James Tatum\"," + Line.SEPARATOR +
                                                "        \"isbn\": \"0691067570\"," + Line.SEPARATOR +
                                                "        \"publicationDate\": \"1989\"" + Line.SEPARATOR +
                                                "    }," + Line.SEPARATOR +
                                                "    {" + Line.SEPARATOR +
                                                "        \"id\": \"2\"," + Line.SEPARATOR +
                                                "        \"title\": \"You are here : personal geographies and other maps of the imagination\"," + Line.SEPARATOR +
                                                "        \"author\": \"Katharine A. Harmon\"," + Line.SEPARATOR +
                                                "        \"isbn\": \"1568984308\"," + Line.SEPARATOR +
                                                "        \"publicationDate\": \"2004\"" + Line.SEPARATOR +
                                                "    }," + Line.SEPARATOR +
                                                "    {" + Line.SEPARATOR +
                                                "        \"id\": \"3\"," + Line.SEPARATOR +
                                                "        \"title\": \"You just don't understand : women and men in conversation\"," + Line.SEPARATOR +
                                                "        \"author\": \"Deborah Tannen\"," + Line.SEPARATOR +
                                                "        \"isbn\": \"0345372050\"," + Line.SEPARATOR +
                                                "        \"publicationDate\": \"1990\"" + Line.SEPARATOR +
                                                "    }" + Line.SEPARATOR +
                                                "]")
                        )
                )
        );
    }

    @Test
    public void shouldSerializeMinimalObjectAsJava() throws IOException {
        // when
        assertEquals(Line.SEPARATOR +
                        "new MockServerClient()" + Line.SEPARATOR +
                        "        .when(" + Line.SEPARATOR +
                        "                request()" + Line.SEPARATOR +
                        "                        .withPath(\"somePath\")" + Line.SEPARATOR +
                        "                        .withBody(new StringBody(\"responseBody\", Body.Type.STRING))," + Line.SEPARATOR +
                        "                Times.once()" + Line.SEPARATOR +
                        "        )" + Line.SEPARATOR +
                        "        .thenRespond(" + Line.SEPARATOR +
                        "                response()" + Line.SEPARATOR +
                        "                        .withStatusCode(304)" + Line.SEPARATOR +
                        "        );",
                expectationSerializer.serializeAsJava(
                        new Expectation(
                                new HttpRequest()
                                        .withPath("somePath")
                                        .withBody(new StringBody("responseBody", Body.Type.STRING)),
                                Times.once()
                        ).thenRespond(
                                new HttpResponse()
                                        .withStatusCode(304)
                        )
                )
        );
    }

    @Test
    public void shouldSerializeArray() throws IOException {
        // given
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(objectWriter);


        // when
        expectationSerializer.serialize(new Expectation[]{fullExpectation, fullExpectation});

        // then
        verify(objectMapper).writerWithDefaultPrettyPrinter();
        verify(objectWriter).writeValueAsString(new ExpectationDTO[]{fullExpectationDTO, fullExpectationDTO});
    }

    @Test
    public void shouldHandleExceptionWhileSerializingObject() throws IOException {
        // given
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Exception while serializing expectation to JSON with value Expectation[httpRequest=<null>,times=<null>,httpRequestMatcher=null,httpResponse=<null>,httpForward=<null>,httpCallback=<null>]");
        // and
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(objectWriter);
        when(objectWriter.writeValueAsString(any(ExpectationDTO.class))).thenThrow(new RuntimeException("TEST EXCEPTION"));

        // when
        expectationSerializer.serialize(new Expectation(null, null));
    }

    @Test
    public void shouldHandleExceptionWhileSerializingArray() throws IOException {
        // given
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Exception while serializing expectation to JSON with value [Expectation[httpRequest=<null>,times=<null>,httpRequestMatcher=null,httpResponse=<null>,httpForward=<null>,httpCallback=<null>]]");
        // and
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(objectWriter);
        when(objectWriter.writeValueAsString(any(ExpectationDTO[].class))).thenThrow(new RuntimeException("TEST EXCEPTION"));

        // when
        expectationSerializer.serialize(new Expectation[]{new Expectation(null, null)});
    }

    @Test
    public void shouldHandleNullAndEmptyWhileSerializingArray() throws IOException {
        // when
        assertEquals("", expectationSerializer.serialize(new Expectation[]{}));
        assertEquals("", expectationSerializer.serialize((Expectation[]) null));
    }

    @Test
    public void shouldDeserializeObject() throws IOException {
        // given
        when(objectMapper.readValue(eq("requestBytes"), same(ExpectationDTO.class))).thenReturn(fullExpectationDTO);

        // when
        Expectation expectation = expectationSerializer.deserialize("requestBytes");

        // then
        assertEquals(fullExpectation, expectation);
    }

    @Test
    public void shouldDeserializeArray() throws IOException {
        // given
        when(objectMapper.readValue(eq("requestBytes"), same(ExpectationDTO[].class))).thenReturn(new ExpectationDTO[]{fullExpectationDTO, fullExpectationDTO});

        // when
        Expectation[] expectations = expectationSerializer.deserializeArray("requestBytes");

        // then
        assertArrayEquals(new Expectation[]{fullExpectation, fullExpectation}, expectations);
    }

    @Test
    public void shouldHandleExceptionWhileDeserializingObject() throws IOException {
        // given
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Exception while parsing response [requestBytes] for http response expectation");
        // and
        when(objectMapper.readValue(eq("requestBytes"), same(ExpectationDTO.class))).thenThrow(new IOException("TEST EXCEPTION"));

        // when
        expectationSerializer.deserialize("requestBytes");
    }

    @Test
    public void shouldHandleExceptionWhileDeserializingArray() throws IOException {
        // given
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Exception while parsing response [requestBytes] for http response expectation array");
        // and
        when(objectMapper.readValue(eq("requestBytes"), same(ExpectationDTO[].class))).thenThrow(new IOException("TEST EXCEPTION"));

        // when
        expectationSerializer.deserializeArray("requestBytes");
    }

    @Test
    public void shouldValidateInputForObject() throws IOException {
        // given
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Expected an JSON expectation object but http body is empty");
        // when
        expectationSerializer.deserialize("");
    }

    @Test
    public void shouldValidateInputForArray() throws IOException {
        // when
        assertArrayEquals(new Expectation[]{}, expectationSerializer.deserializeArray(""));
    }
}
