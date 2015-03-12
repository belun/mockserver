package org.mockserver.client.serialization;

import org.junit.Test;
import org.mockserver.Line;
import org.mockserver.client.serialization.model.*;
import org.mockserver.model.*;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.ParameterBody.params;
import static org.mockserver.model.StringBody.*;

/**
 * @author jamesdbloom
 */
public class HttpRequestSerializerIntegrationTest {


    @Test
    public void shouldIgnoreExtraFields() throws IOException {
        // given
        String requestBytes = "{" + Line.SEPARATOR +
                "    \"path\": \"somePath\"," + Line.SEPARATOR +
                "    \"extra_field\": \"extra_value\"" + Line.SEPARATOR +
                "}";

        // when
        HttpRequest expectation = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setPath("somePath")
                .buildObject(), expectation);
    }

    @Test
    public void shouldDeserializeCompleteObject() throws IOException {
        // given
        String requestBytes = "{" + Line.SEPARATOR +
                "  \"method\" : \"someMethod\"," + Line.SEPARATOR +
                "  \"path\" : \"somePath\"," + Line.SEPARATOR +
                "  \"queryStringParameters\" : [ {" + Line.SEPARATOR +
                "    \"name\" : \"queryParameterName\"," + Line.SEPARATOR +
                "    \"values\" : [ \"queryParameterValue\" ]" + Line.SEPARATOR +
                "  } ]," + Line.SEPARATOR +
                "  \"body\" : {" + Line.SEPARATOR +
                "    \"type\" : \"STRING\"," + Line.SEPARATOR +
                "    \"value\" : \"somebody\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"cookies\" : [ {" + Line.SEPARATOR +
                "    \"name\" : \"someCookieName\"," + Line.SEPARATOR +
                "    \"value\" : \"someCookieValue\"" + Line.SEPARATOR +
                "  } ]," + Line.SEPARATOR +
                "  \"headers\" : [ {" + Line.SEPARATOR +
                "    \"name\" : \"someHeaderName\"," + Line.SEPARATOR +
                "    \"values\" : [ \"someHeaderValue\" ]" + Line.SEPARATOR +
                "  } ]" + Line.SEPARATOR +
                "}";

        // when
        HttpRequest expectation = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setMethod("someMethod")
                .setPath("somePath")
                .setQueryStringParameters(Arrays.<ParameterDTO>asList((ParameterDTO) new ParameterDTO(new Parameter("queryParameterName", Arrays.asList("queryParameterValue")))))
                .setBody(BodyDTO.createDTO(new StringBody("somebody", Body.Type.STRING)))
                .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                .buildObject(), expectation);
    }

    @Test
    public void shouldDeserializeStringBodyShorthand() throws IOException {
        // given
        String requestBytes = "{" + Line.SEPARATOR +
                "  \"body\" : \"somebody\"" + Line.SEPARATOR +
                "}";

        // when
        HttpRequest expectation = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setBody(BodyDTO.createDTO(exact("somebody")))
                .buildObject(), expectation);
    }

    @Test
    public void shouldDeserializeStringBodyWithType() throws IOException {
        // given
        String requestBytes = "{" + Line.SEPARATOR +
                "  \"body\" : {" + Line.SEPARATOR +
                "    \"type\" : \"STRING\"," + Line.SEPARATOR +
                "    \"value\" : \"somebody\"" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}";

        // when
        HttpRequest expectation = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setBody(BodyDTO.createDTO(exact("somebody")))
                .buildObject(), expectation);
    }

    @Test
    public void shouldDeserializeJsonBody() throws IOException {
        // given
        String requestBytes = "{" + Line.SEPARATOR +
                "  \"body\" : {" + Line.SEPARATOR +
                "    \"type\" : \"JSON\"," + Line.SEPARATOR +
                "    \"value\" : \"{ \\\"key\\\": \\\"value\\\" }\"" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}";

        // when
        HttpRequest expectation = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setBody(BodyDTO.createDTO(json("{ \"key\": \"value\" }")))
                .buildObject(), expectation);
    }

    @Test
    public void shouldDeserializeRegexBody() throws IOException {
        // given
        String requestBytes = "{" + Line.SEPARATOR +
                "  \"body\" : {" + Line.SEPARATOR +
                "    \"type\" : \"REGEX\"," + Line.SEPARATOR +
                "    \"value\" : \"some[a-z]{3}\"" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}";

        // when
        HttpRequest expectation = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setBody(BodyDTO.createDTO(regex("some[a-z]{3}")))
                .buildObject(), expectation);
    }

    @Test
    public void shouldDeserializeXpathBody() throws IOException {
        // given
        String requestBytes = "{" + Line.SEPARATOR +
                "  \"body\" : {" + Line.SEPARATOR +
                "    \"type\" : \"XPATH\"," + Line.SEPARATOR +
                "    \"value\" : \"/element[key = 'some_key' and value = 'some_value']\"" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}";

        // when
        HttpRequest expectation = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setBody(BodyDTO.createDTO(xpath("/element[key = 'some_key' and value = 'some_value']")))
                .buildObject(), expectation);
    }

    @Test
    public void shouldDeserializeParameterBody() throws IOException {
        // given
        String requestBytes = "{" + Line.SEPARATOR +
                "  \"body\" : {" + Line.SEPARATOR +
                "    \"type\" : \"PARAMETERS\"," + Line.SEPARATOR +
                "    \"parameters\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"nameOne\"," + Line.SEPARATOR +
                "      \"values\" : [ \"valueOne\" ]" + Line.SEPARATOR +
                "    }, {" + Line.SEPARATOR +
                "      \"name\" : \"nameTwo\"," + Line.SEPARATOR +
                "      \"values\" : [ \"valueTwo_One\", \"valueTwo_Two\" ]" + Line.SEPARATOR +
                "    } ]" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}";

        // when
        HttpRequest expectation = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setBody(BodyDTO.createDTO(params(
                        new Parameter("nameOne", "valueOne"),
                        new Parameter("nameTwo", "valueTwo_One", "valueTwo_Two")
                )))
                .buildObject(), expectation);
    }

    @Test
    public void shouldDeserializePartialObject() throws IOException {
        // given
        String requestBytes = "{" + Line.SEPARATOR +
                "    \"path\": \"somePath\"" + Line.SEPARATOR +
                "}";

        // when
        HttpRequest expectation = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setPath("somePath")
                .buildObject(), expectation);
    }

    @Test
    public void shouldDeserializeAsHttpRequestField() throws IOException {
        // given
        String requestBytes = "{" + Line.SEPARATOR +
                "    \"httpRequest\": {" + Line.SEPARATOR +
                "        \"path\": \"somePath\"," + Line.SEPARATOR +
                "        \"queryStringParameters\" : [ {" + Line.SEPARATOR +
                "            \"name\" : \"queryParameterName\"," + Line.SEPARATOR +
                "            \"values\" : [ \"queryParameterValue\" ]" + Line.SEPARATOR +
                "        } ]" + Line.SEPARATOR +
                "    }" + Line.SEPARATOR +
                "}";

        // when
        HttpRequest expectation = new HttpRequestSerializer().deserialize(requestBytes);

        // then
        assertEquals(new HttpRequestDTO()
                .setPath("somePath")
                .setQueryStringParameters(Arrays.<ParameterDTO>asList((ParameterDTO) new ParameterDTO(new Parameter("queryParameterName", Arrays.asList("queryParameterValue")))))
                .buildObject(), expectation);
    }

    @Test
    public void shouldSerializeCompleteObject() throws IOException {
        // when
        String jsonExpectation = new HttpRequestSerializer().serialize(
                new HttpRequestDTO()
                        .setMethod("someMethod")
                        .setPath("somePath")
                        .setQueryStringParameters(Arrays.<ParameterDTO>asList((ParameterDTO) new ParameterDTO(new Parameter("queryParameterName", Arrays.asList("queryParameterValue")))))
                        .setBody(BodyDTO.createDTO(new StringBody("somebody", Body.Type.STRING)))
                        .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("someHeaderName", Arrays.asList("someHeaderValue")))))
                        .setCookies(Arrays.<CookieDTO>asList(new CookieDTO(new Cookie("someCookieName", "someCookieValue"))))
                        .buildObject()
        );

        // then
        assertEquals("{" + Line.SEPARATOR +
                "  \"method\" : \"someMethod\"," + Line.SEPARATOR +
                "  \"path\" : \"somePath\"," + Line.SEPARATOR +
                "  \"queryStringParameters\" : [ {" + Line.SEPARATOR +
                "    \"name\" : \"queryParameterName\"," + Line.SEPARATOR +
                "    \"values\" : [ \"queryParameterValue\" ]" + Line.SEPARATOR +
                "  } ]," + Line.SEPARATOR +
                "  \"body\" : \"somebody\"," + Line.SEPARATOR +
                "  \"cookies\" : [ {" + Line.SEPARATOR +
                "    \"name\" : \"someCookieName\"," + Line.SEPARATOR +
                "    \"value\" : \"someCookieValue\"" + Line.SEPARATOR +
                "  } ]," + Line.SEPARATOR +
                "  \"headers\" : [ {" + Line.SEPARATOR +
                "    \"name\" : \"someHeaderName\"," + Line.SEPARATOR +
                "    \"values\" : [ \"someHeaderValue\" ]" + Line.SEPARATOR +
                "  } ]" + Line.SEPARATOR +
                "}", jsonExpectation);
    }

    @Test
    public void shouldSerializeArray() throws IOException {
        // when
        String jsonExpectation = new HttpRequestSerializer().serialize(
                new HttpRequest[]{
                        new HttpRequestDTO()
                                .setMethod("some_method_one")
                                .setPath("some_path_one")
                                .setBody(BodyDTO.createDTO(new StringBody("some_body_one", Body.Type.STRING)))
                                .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("some_header_name_one", Arrays.asList("some_header_value_one")))))
                                .buildObject(),
                        new HttpRequestDTO()
                                .setMethod("some_method_two")
                                .setPath("some_path_two")
                                .setBody(BodyDTO.createDTO(new StringBody("some_body_two", Body.Type.STRING)))
                                .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("some_header_name_two", Arrays.asList("some_header_value_two")))))
                                .buildObject()
                }
        );

        // then
        assertEquals("[ {" + Line.SEPARATOR +
                "  \"method\" : \"some_method_one\"," + Line.SEPARATOR +
                "  \"path\" : \"some_path_one\"," + Line.SEPARATOR +
                "  \"body\" : \"some_body_one\"," + Line.SEPARATOR +
                "  \"headers\" : [ {" + Line.SEPARATOR +
                "    \"name\" : \"some_header_name_one\"," + Line.SEPARATOR +
                "    \"values\" : [ \"some_header_value_one\" ]" + Line.SEPARATOR +
                "  } ]" + Line.SEPARATOR +
                "}, {" + Line.SEPARATOR +
                "  \"method\" : \"some_method_two\"," + Line.SEPARATOR +
                "  \"path\" : \"some_path_two\"," + Line.SEPARATOR +
                "  \"body\" : \"some_body_two\"," + Line.SEPARATOR +
                "  \"headers\" : [ {" + Line.SEPARATOR +
                "    \"name\" : \"some_header_name_two\"," + Line.SEPARATOR +
                "    \"values\" : [ \"some_header_value_two\" ]" + Line.SEPARATOR +
                "  } ]" + Line.SEPARATOR +
                "} ]", jsonExpectation);
    }

    @Test
    public void shouldSerializeList() throws IOException {
        // when
        String jsonExpectation = new HttpRequestSerializer().serialize(
                Arrays.asList(
                        new HttpRequestDTO()
                                .setMethod("some_method_one")
                                .setPath("some_path_one")
                                .setBody(BodyDTO.createDTO(new StringBody("some_body_one", Body.Type.STRING)))
                                .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("some_header_name_one", Arrays.asList("some_header_value_one")))))
                                .buildObject(),
                        new HttpRequestDTO()
                                .setMethod("some_method_two")
                                .setPath("some_path_two")
                                .setBody(BodyDTO.createDTO(new StringBody("some_body_two", Body.Type.STRING)))
                                .setHeaders(Arrays.<HeaderDTO>asList(new HeaderDTO(new Header("some_header_name_two", Arrays.asList("some_header_value_two")))))
                                .buildObject()
                )
        );

        // then
        assertEquals("[ {" + Line.SEPARATOR +
                "  \"method\" : \"some_method_one\"," + Line.SEPARATOR +
                "  \"path\" : \"some_path_one\"," + Line.SEPARATOR +
                "  \"body\" : \"some_body_one\"," + Line.SEPARATOR +
                "  \"headers\" : [ {" + Line.SEPARATOR +
                "    \"name\" : \"some_header_name_one\"," + Line.SEPARATOR +
                "    \"values\" : [ \"some_header_value_one\" ]" + Line.SEPARATOR +
                "  } ]" + Line.SEPARATOR +
                "}, {" + Line.SEPARATOR +
                "  \"method\" : \"some_method_two\"," + Line.SEPARATOR +
                "  \"path\" : \"some_path_two\"," + Line.SEPARATOR +
                "  \"body\" : \"some_body_two\"," + Line.SEPARATOR +
                "  \"headers\" : [ {" + Line.SEPARATOR +
                "    \"name\" : \"some_header_name_two\"," + Line.SEPARATOR +
                "    \"values\" : [ \"some_header_value_two\" ]" + Line.SEPARATOR +
                "  } ]" + Line.SEPARATOR +
                "} ]", jsonExpectation);
    }

    @Test
    public void shouldSerializeStringBody() throws IOException {
        // when
        String jsonExpectation = new HttpRequestSerializer().serialize(
                new HttpRequestDTO()
                        .setBody(BodyDTO.createDTO(exact("somebody")))
                        .buildObject()
        );

        // then
        assertEquals("{" + Line.SEPARATOR +
                "  \"body\" : \"somebody\"" + Line.SEPARATOR +
                "}", jsonExpectation);
    }

    @Test
    public void shouldSerializeJsonBody() throws IOException {
        // when
        String jsonExpectation = new HttpRequestSerializer().serialize(
                new HttpRequestDTO()
                        .setBody(BodyDTO.createDTO(json("{ \"key\": \"value\" }")))
                        .buildObject()
        );

        // then
        assertEquals("{" + Line.SEPARATOR +
                "  \"body\" : {" + Line.SEPARATOR +
                "    \"type\" : \"JSON\"," + Line.SEPARATOR +
                "    \"value\" : \"{ \\\"key\\\": \\\"value\\\" }\"" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}", jsonExpectation);
    }

    @Test
    public void shouldSerializeRegexBody() throws IOException {
        // when
        String jsonExpectation = new HttpRequestSerializer().serialize(
                new HttpRequestDTO()
                        .setBody(BodyDTO.createDTO(regex("some[a-z]{3}")))
                        .buildObject()
        );

        // then
        assertEquals("{" + Line.SEPARATOR +
                "  \"body\" : {" + Line.SEPARATOR +
                "    \"type\" : \"REGEX\"," + Line.SEPARATOR +
                "    \"value\" : \"some[a-z]{3}\"" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}", jsonExpectation);
    }

    @Test
    public void shouldSerializeXpathBody() throws IOException {
        // when
        String jsonExpectation = new HttpRequestSerializer().serialize(
                new HttpRequestDTO()
                        .setBody(BodyDTO.createDTO(xpath("/element[key = 'some_key' and value = 'some_value']")))
                        .buildObject()
        );

        // then
        assertEquals("{" + Line.SEPARATOR +
                "  \"body\" : {" + Line.SEPARATOR +
                "    \"type\" : \"XPATH\"," + Line.SEPARATOR +
                "    \"value\" : \"/element[key = 'some_key' and value = 'some_value']\"" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}", jsonExpectation);
    }

    @Test
    public void shouldSerializeParameterBody() throws IOException {
        // when
        String jsonExpectation = new HttpRequestSerializer().serialize(
                new HttpRequestDTO()
                        .setBody(BodyDTO.createDTO(params(
                                new Parameter("nameOne", "valueOne"),
                                new Parameter("nameTwo", "valueTwo_One", "valueTwo_Two")
                        )))
                        .buildObject()
        );

        // then
        assertEquals("{" + Line.SEPARATOR +
                "  \"body\" : {" + Line.SEPARATOR +
                "    \"type\" : \"PARAMETERS\"," + Line.SEPARATOR +
                "    \"parameters\" : [ {" + Line.SEPARATOR +
                "      \"name\" : \"nameOne\"," + Line.SEPARATOR +
                "      \"values\" : [ \"valueOne\" ]" + Line.SEPARATOR +
                "    }, {" + Line.SEPARATOR +
                "      \"name\" : \"nameTwo\"," + Line.SEPARATOR +
                "      \"values\" : [ \"valueTwo_One\", \"valueTwo_Two\" ]" + Line.SEPARATOR +
                "    } ]" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}", jsonExpectation);
    }

    @Test
    public void shouldSerializePartialRequestAndResponse() throws IOException {
        // when
        String jsonExpectation = new HttpRequestSerializer().serialize(new HttpRequestDTO()
                        .setPath("somePath")
                        .buildObject()
        );

        // then
        assertEquals("{" + Line.SEPARATOR +
                "  \"path\" : \"somePath\"" + Line.SEPARATOR +
                "}", jsonExpectation);
    }

    @Test
    public void shouldSerializePartialExpectation() throws IOException {
        // when
        String jsonExpectation = new HttpRequestSerializer().serialize(new HttpRequestDTO()
                        .setPath("somePath")
                        .buildObject()
        );

        // then
        assertEquals("{" + Line.SEPARATOR +
                "  \"path\" : \"somePath\"" + Line.SEPARATOR +
                "}", jsonExpectation);
    }
}
