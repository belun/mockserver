package org.mockserver.client.serialization;

import org.junit.Test;
import org.mockserver.Line;
import org.mockserver.client.serialization.model.*;
import org.mockserver.matchers.Times;
import org.mockserver.model.*;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author jamesdbloom
 */
public class ObjectMapperFactoryTest {


    @Test
    public void shouldDeserializeCompleteObject() throws IOException {
        // given
        String json = ("{" + Line.SEPARATOR +
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
        ExpectationDTO expectationDTO = ObjectMapperFactory.createObjectMapper().readValue(json, ExpectationDTO.class);

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
                .setTimes(new TimesDTO(Times.exactly(5))), expectationDTO);
    }

    @Test
    public void shouldParseJSONWithParametersBody() throws IOException {
        // given
        String json = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
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
                "    }" + Line.SEPARATOR +
                "}");

        // when
        ExpectationDTO expectationDTO = ObjectMapperFactory.createObjectMapper().readValue(json, ExpectationDTO.class);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setBody(new ParameterBodyDTO(new ParameterBody(
                                        new Parameter("parameterOneName", "parameterOneValueOne", "parameterOneValueTwo"),
                                        new Parameter("parameterTwoName", "parameterTwoValue")
                                )))
                ), expectationDTO);
    }

    @Test
    public void shouldParseJSONWithInvalidBody() throws IOException {
        // given
        String json = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"body\" : {" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        ExpectationDTO expectationDTO = ObjectMapperFactory.createObjectMapper().readValue(json, ExpectationDTO.class);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                ), expectationDTO);
    }

    @Test
    public void shouldParseJSONWithMissingTypeFromBody() throws IOException {
        // given
        String json = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"body\" : {" + Line.SEPARATOR +
                "            \"value\" : \"some_value\"" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        ExpectationDTO expectationDTO = ObjectMapperFactory.createObjectMapper().readValue(json, ExpectationDTO.class);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                ), expectationDTO);
    }

    @Test
    public void shouldParseJSONWithMissingValueFromBody() throws IOException {
        // given
        String json = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"body\" : {" + Line.SEPARATOR +
                "            \"type\" : \"STRING\"" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        ExpectationDTO expectationDTO = ObjectMapperFactory.createObjectMapper().readValue(json, ExpectationDTO.class);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                ), expectationDTO);
    }

    @Test
    public void shouldParseJSONWithWrongFieldInBody() throws IOException {
        // given
        String json = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"body\" : {" + Line.SEPARATOR +
                "            \"type\" : \"STRING\"," + Line.SEPARATOR +
                "            \"wrong_name\" : \"some_value\"" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        ExpectationDTO expectationDTO = ObjectMapperFactory.createObjectMapper().readValue(json, ExpectationDTO.class);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                ), expectationDTO);
    }

    @Test
    public void shouldParseJSONWithWrongValueFieldTypeInStringBody() throws IOException {
        // given
        String json = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"body\" : {" + Line.SEPARATOR +
                "            \"type\" : \"STRING\"," + Line.SEPARATOR +
                "            \"value\" : 1" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        ExpectationDTO expectationDTO = ObjectMapperFactory.createObjectMapper().readValue(json, ExpectationDTO.class);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                ), expectationDTO);
    }

    @Test
    public void shouldParseJSONWithWrongValueFieldTypeInBinaryBody() throws IOException {
        // given
        String json = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"body\" : {" + Line.SEPARATOR +
                "            \"type\" : \"BINARY\"," + Line.SEPARATOR +
                "            \"value\" : 1" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        ExpectationDTO expectationDTO = ObjectMapperFactory.createObjectMapper().readValue(json, ExpectationDTO.class);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                ), expectationDTO);
    }

    @Test
    public void shouldParseJSONWithWrongValueFieldTypeInParameterBody() throws IOException {
        // given
        String json = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"body\" : {" + Line.SEPARATOR +
                "            \"type\" : \"PARAMETERS\"," + Line.SEPARATOR +
                "            \"value\" : 1" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        ExpectationDTO expectationDTO = ObjectMapperFactory.createObjectMapper().readValue(json, ExpectationDTO.class);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                ), expectationDTO);
    }

    @Test
    public void shouldParseJSONWithWrongTypeFieldTypeInBody() throws IOException {
        // given
        String json = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"body\" : {" + Line.SEPARATOR +
                "            \"type\" : 1," + Line.SEPARATOR +
                "            \"value\" : \"some_value\"" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        ExpectationDTO expectationDTO = ObjectMapperFactory.createObjectMapper().readValue(json, ExpectationDTO.class);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                ), expectationDTO);
    }

    @Test
    public void shouldParseJSONWithExactStringBody() throws IOException {
        // given
        String json = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"body\" : {" + Line.SEPARATOR +
                "            \"type\" : \"STRING\"," + Line.SEPARATOR +
                "            \"value\" : \"some_value\"" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        ExpectationDTO expectationDTO = ObjectMapperFactory.createObjectMapper().readValue(json, ExpectationDTO.class);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setBody(new StringBodyDTO(new StringBody("some_value", Body.Type.STRING)))
                ), expectationDTO);
    }

    @Test
    public void shouldParseJSONWithRegexStringBody() throws IOException {
        // given
        String json = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"body\" : {" + Line.SEPARATOR +
                "            \"type\" : \"REGEX\"," + Line.SEPARATOR +
                "            \"value\" : \"some[a-zA-Z]*\"" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        ExpectationDTO expectationDTO = ObjectMapperFactory.createObjectMapper().readValue(json, ExpectationDTO.class);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setBody(new StringBodyDTO(new StringBody("some[a-zA-Z]*", Body.Type.REGEX)))
                ), expectationDTO);
    }

    @Test
    public void shouldParseJSONWithJsonStringBody() throws IOException {
        // given
        String json = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"body\" : {" + Line.SEPARATOR +
                "            \"type\" : \"JSON\"," + Line.SEPARATOR +
                "            \"value\" : \"{'employees':[{'firstName':'John', 'lastName':'Doe'}]}\"" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        ExpectationDTO expectationDTO = ObjectMapperFactory.createObjectMapper().readValue(json, ExpectationDTO.class);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setBody(new StringBodyDTO(new StringBody("{'employees':[{'firstName':'John', 'lastName':'Doe'}]}", Body.Type.JSON)))
                ), expectationDTO);
    }


    @Test
    public void shouldParseJSONWithXPathStringBody() throws IOException {
        // given
        String json = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"body\" : {" + Line.SEPARATOR +
                "            \"type\" : \"XPATH\"," + Line.SEPARATOR +
                "            \"value\" : \"\\\\some\\\\xpath\"" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        ExpectationDTO expectationDTO = ObjectMapperFactory.createObjectMapper().readValue(json, ExpectationDTO.class);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setBody(new StringBodyDTO(new StringBody("\\some\\xpath", Body.Type.XPATH)))
                ), expectationDTO);
    }

    @Test
    public void shouldParseJSONWithBinaryBody() throws IOException {
        // given
        String json = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"body\" : {" + Line.SEPARATOR +
                "            \"type\" : \"BINARY\"," + Line.SEPARATOR +
                "            \"value\" : \"" + DatatypeConverter.printBase64Binary("some_value".getBytes()) + "\"" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        ExpectationDTO expectationDTO = ObjectMapperFactory.createObjectMapper().readValue(json, ExpectationDTO.class);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setBody(new BinaryBodyDTO(new BinaryBody("some_value".getBytes())))
                ), expectationDTO);
    }

    @Test
    public void shouldParseJSONWithParameterBody() throws IOException {
        // given
        String json = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
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
                "    }" + Line.SEPARATOR +
                "}");

        // when
        ExpectationDTO expectationDTO = ObjectMapperFactory.createObjectMapper().readValue(json, ExpectationDTO.class);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                                .setBody(new ParameterBodyDTO(new ParameterBody(
                                        new Parameter("parameterOneName", "parameterOneValueOne", "parameterOneValueTwo"),
                                        new Parameter("parameterTwoName", "parameterTwoValue")
                                )))
                ), expectationDTO);
    }

    @Test
    public void shouldParseJSONWithInvalidArrayParameterBody() throws IOException {
        // given
        String json = ("{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"body\" : {" + Line.SEPARATOR +
                "            \"type\" : \"PARAMETERS\"," + Line.SEPARATOR +
                "            \"parameters\" : {" + Line.SEPARATOR +
                "                    \"name\" : \"parameterOneName\"," + Line.SEPARATOR +
                "                    \"values\" : [ \"parameterOneValueOne\", \"parameterOneValueTwo\" ]" + Line.SEPARATOR +
                "                }" + Line.SEPARATOR +
                "        }" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}");

        // when
        ExpectationDTO expectationDTO = ObjectMapperFactory.createObjectMapper().readValue(json, ExpectationDTO.class);

        // then
        assertEquals(new ExpectationDTO()
                .setHttpRequest(
                        new HttpRequestDTO()
                ), expectationDTO);
    }
}
