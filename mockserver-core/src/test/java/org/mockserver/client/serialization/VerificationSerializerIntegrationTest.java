package org.mockserver.client.serialization;

import org.junit.Test;
import org.mockserver.Line;
import org.mockserver.client.serialization.model.HttpRequestDTO;
import org.mockserver.client.serialization.model.VerificationDTO;
import org.mockserver.client.serialization.model.VerificationTimesDTO;
import org.mockserver.verify.Verification;
import org.mockserver.verify.VerificationTimes;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;

/**
 * @author jamesdbloom
 */
public class VerificationSerializerIntegrationTest {

    @Test
    public void shouldIgnoreExtraFields() throws IOException {
        // given
        String requestBytes = "{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"path\" : \"somepath\"," + Line.SEPARATOR +
                "    \"random_field\" : \"random_value\"" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}";

        // when
        Verification verification = new VerificationSerializer().deserialize(requestBytes);

        // then
        assertEquals(new VerificationDTO()
                .setHttpRequest(new HttpRequestDTO(request().withPath("somepath")))
                .buildObject(), verification);
    }

    @Test
    public void shouldDeserializeCompleteObject() throws IOException {
        // given
        String requestBytes = "{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"method\" : \"GET\"," + Line.SEPARATOR +
                "    \"path\" : \"somepath\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"count\" : 2," + Line.SEPARATOR +
                "    \"exact\" : true" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}";

        // when
        Verification verification = new VerificationSerializer().deserialize(requestBytes);

        // then
        assertEquals(new VerificationDTO()
                .setHttpRequest(new HttpRequestDTO(request().withMethod("GET").withPath("somepath")))
                .setTimes(new VerificationTimesDTO(VerificationTimes.exactly(2)))
                .buildObject(), verification);
    }

    @Test
    public void shouldDeserializePartialObject() throws IOException {
        // given
        String requestBytes = "{" + Line.SEPARATOR +
                "    \"path\": \"somePath\"" + Line.SEPARATOR +
                "}";

        // when
        Verification verification = new VerificationSerializer().deserialize(requestBytes);

        // then
        assertEquals(new VerificationDTO()
                .setHttpRequest(new HttpRequestDTO(request()))
                .buildObject(), verification);
    }

    @Test
    public void shouldSerializeCompleteObject() throws IOException {
        // when
        String jsonExpectation = new VerificationSerializer().serialize(
                new VerificationDTO()
                        .setHttpRequest(new HttpRequestDTO(request().withMethod("GET").withPath("somepath")))
                        .setTimes(new VerificationTimesDTO(VerificationTimes.exactly(2)))
                        .buildObject()
        );

        // then
        assertEquals("{" + Line.SEPARATOR +
                "  \"httpRequest\" : {" + Line.SEPARATOR +
                "    \"method\" : \"GET\"," + Line.SEPARATOR +
                "    \"path\" : \"somepath\"" + Line.SEPARATOR +
                "  }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"count\" : 2," + Line.SEPARATOR +
                "    \"exact\" : true" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}", jsonExpectation);
    }

    @Test
    public void shouldSerializePartialObject() throws IOException {
        // when
        String jsonExpectation = new VerificationSerializer().serialize(
                new VerificationDTO()
                        .setHttpRequest(new HttpRequestDTO(request()))
                        .buildObject()
        );

        // then
        assertEquals("{" + Line.SEPARATOR +
                "  \"httpRequest\" : { }," + Line.SEPARATOR +
                "  \"times\" : {" + Line.SEPARATOR +
                "    \"count\" : 1," + Line.SEPARATOR +
                "    \"exact\" : true" + Line.SEPARATOR +
                "  }" + Line.SEPARATOR +
                "}", jsonExpectation);
    }
}
