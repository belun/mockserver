package org.mockserver.client.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.mockserver.Line;
import org.mockserver.client.serialization.model.ExpectationDTO;
import org.mockserver.mock.Expectation;
import org.mockserver.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jamesdbloom
 */
public class ExpectationSerializer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();

    public String serialize(Expectation expectation) {
        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(new ExpectationDTO(expectation));
        } catch (Exception e) {
            logger.error(String.format("Exception while serializing expectation to JSON with value %s", expectation), e);
            throw new RuntimeException(String.format("Exception while serializing expectation to JSON with value %s", expectation), e);
        }
    }

    public String serializeAsJava(Expectation expectation) {
        StringBuffer output = new StringBuffer();
        if (expectation != null) {
            HttpRequest httpRequest = expectation.getHttpRequest();
            HttpResponse httpResponse = expectation.getHttpResponse(false);
            HttpForward httpForward = expectation.getHttpForward();
            HttpCallback httpCallback = expectation.getHttpCallback();
            output.append(Line.SEPARATOR +
                    "new MockServerClient()" + Line.SEPARATOR +
                    "        .when(" + Line.SEPARATOR +
                    "                request()");
            if (StringUtils.isNotEmpty(httpRequest.getMethod())) {
                output.append(Line.SEPARATOR + "                        .withMethod(\"").append(httpRequest.getMethod()).append("\")");
            }
            if (StringUtils.isNotEmpty(httpRequest.getPath())) {
                output.append(Line.SEPARATOR + "                        .withPath(\"").append(httpRequest.getPath()).append("\")");
            }
            if (httpRequest.getHeaders().size() > 0) {
                serializeAsJavaKeyToMultiValue(output, "Header", new ArrayList<KeyToMultiValue>(httpRequest.getHeaders()));
            }
            if (httpRequest.getCookies().size() > 0) {
                serializeAsJavaKeyAndValue(output, "Cookie", new ArrayList<KeyAndValue>(httpRequest.getCookies()));
            }
            if (httpRequest.getQueryStringParameters().size() > 0) {
                serializeAsJavaKeyToMultiValue(output, "QueryStringParameter", new ArrayList<KeyToMultiValue>(httpRequest.getQueryStringParameters()));
            }
            if (httpRequest.getBody() != null) {
                if (httpRequest.getBody() instanceof StringBody) {
                    output.append(Line.SEPARATOR + "                        .withBody(new StringBody(\"").append(StringEscapeUtils.escapeJava(((StringBody) httpRequest.getBody()).getValue())).append("\", Body.Type.").append(httpRequest.getBody().getType()).append("))");
                } else if (httpRequest.getBody() instanceof ParameterBody) {
                    output.append(Line.SEPARATOR + "                        .withBody(");
                    output.append(Line.SEPARATOR + "                                new ParameterBody(" + Line.SEPARATOR);
                    serializeAsJavaKeyToMultiValueList(output, "Parameter", new ArrayList<KeyToMultiValue>(((ParameterBody) httpRequest.getBody()).getValue()), 40);
                    output.append("                                )");
                    output.append(Line.SEPARATOR + "                        )");
                } else if (httpRequest.getBody() instanceof BinaryBody) {
                    output.append(Line.SEPARATOR + "                        .withBody(new byte[0]) /* note: not possible to generate code for binary data */");
                }
            }
            output.append("," + Line.SEPARATOR +
                    "                Times.once()" + Line.SEPARATOR +
                    "        )" + Line.SEPARATOR);
            if (httpResponse != null) {
                output.append("        .thenRespond(" + Line.SEPARATOR +
                        "                response()" + Line.SEPARATOR);
                if (httpResponse.getStatusCode() != null) {
                    output.append("                        .withStatusCode(").append(httpResponse.getStatusCode()).append(")");
                }
                if (httpResponse.getHeaders().size() > 0) {
                    serializeAsJavaKeyToMultiValue(output, "Header", new ArrayList<KeyToMultiValue>(httpResponse.getHeaders()));
                }
                if (httpResponse.getCookies().size() > 0) {
                    serializeAsJavaKeyAndValue(output, "Cookie", new ArrayList<KeyAndValue>(httpResponse.getCookies()));
                }
                if (httpResponse.getBodyAsString() != null && httpResponse.getBodyAsString().length() > 0) {
                    output.append(Line.SEPARATOR + "                        .withBody(\"").append(StringEscapeUtils.escapeJava(httpResponse.getBodyAsString())).append("\")");
                }
                output.append(Line.SEPARATOR + "        );");
            }
            if (httpForward != null) {
                output.append("        .thenForward(" + Line.SEPARATOR +
                        "                forward()" + Line.SEPARATOR);
                if (httpForward.getHost() != null) {
                    output.append("                        .withHost(\"").append(httpForward.getHost()).append("\")" + Line.SEPARATOR);
                }
                if (httpForward.getPort() != null) {
                    output.append("                        .withPort(").append(httpForward.getPort()).append(")" + Line.SEPARATOR);
                }
                if (httpForward.getScheme() != null) {
                    output.append("                        .withScheme(HttpForward.Scheme.").append(httpForward.getScheme()).append(")" + Line.SEPARATOR);
                }
                output.append("        );");
            }
            if (httpCallback != null) {
                output.append("        .thenCallback(" + Line.SEPARATOR +
                        "                callback()" + Line.SEPARATOR);
                if (httpCallback.getCallbackClass() != null) {
                    output.append("                        .withCallbackClass(\"").append(httpCallback.getCallbackClass()).append("\")" + Line.SEPARATOR);
                }
                output.append("        );");
            }
        }

        return output.toString();
    }

    private void serializeAsJavaKeyToMultiValue(StringBuffer output, String name, List<KeyToMultiValue> keyToMultiValues) {
        output.append(Line.SEPARATOR + "                        .with").append(name).append("s(" + Line.SEPARATOR);
        serializeAsJavaKeyToMultiValueList(output, name, keyToMultiValues, 32);
        output.append("                        )");
    }

    private void serializeAsJavaKeyToMultiValueList(StringBuffer output, String name, List<KeyToMultiValue> keyToMultiValues, int indent) {
        for (int i = 0; i < keyToMultiValues.size(); i++) {
            KeyToMultiValue keyToMultiValue = keyToMultiValues.get(i);
            output.append(Strings.padStart("", indent, ' '));
            output.append("new ").append(name).append("(\"").append(keyToMultiValue.getName()).append("\"");
            for (String value : keyToMultiValue.getValues()) {
                output.append(", \"").append(value).append("\"");
            }
            output.append(")");
            if (i < (keyToMultiValues.size() - 1)) {
                output.append(",");
            }
            output.append(Line.SEPARATOR);
        }
    }

    private void serializeAsJavaKeyAndValue(StringBuffer output, String name, List<KeyAndValue> keyAndValues) {
        output.append(Line.SEPARATOR + "                        .with").append(name).append("s(" + Line.SEPARATOR);
        serializeAsJavaKeyAndValueList(output, name, keyAndValues, 32);
        output.append("                        )");
    }

    private void serializeAsJavaKeyAndValueList(StringBuffer output, String name, List<KeyAndValue> keyAndValues, int indent) {
        for (int i = 0; i < keyAndValues.size(); i++) {
            KeyAndValue keyAndValue = keyAndValues.get(i);
            output.append(Strings.padStart("", indent, ' '));
            output.append("new ").append(name).append("(\"").append(keyAndValue.getName()).append("\"");
            output.append(", \"").append(keyAndValue.getValue()).append("\"");
            output.append(")");
            if (i < (keyAndValues.size() - 1)) {
                output.append(",");
            }
            output.append(Line.SEPARATOR);
        }
    }

    public String serialize(Expectation[] expectation) {
        try {
            if (expectation != null && expectation.length > 0) {
                ExpectationDTO[] expectationDTOs = new ExpectationDTO[expectation.length];
                for (int i = 0; i < expectation.length; i++) {
                    expectationDTOs[i] = new ExpectationDTO(expectation[i]);
                }
                return objectMapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(expectationDTOs);
            }
            return "";
        } catch (Exception e) {
            logger.error("Exception while serializing expectation to JSON with value " + Arrays.asList(expectation), e);
            throw new RuntimeException("Exception while serializing expectation to JSON with value " + Arrays.asList(expectation), e);
        }
    }

    public Expectation deserialize(String jsonExpectation) {
        if (jsonExpectation == null || jsonExpectation.isEmpty()) {
            throw new IllegalArgumentException("Expected an JSON expectation object but http body is empty");
        }
        Expectation expectation = null;
        try {
            ExpectationDTO expectationDTO = objectMapper.readValue(jsonExpectation, ExpectationDTO.class);
            if (expectationDTO != null) {
                expectation = expectationDTO.buildObject();
            }
        } catch (Exception e) {
            logger.error("Exception while parsing response [" + jsonExpectation + "] for http response expectation", e);
            throw new RuntimeException("Exception while parsing response [" + jsonExpectation + "] for http response expectation", e);
        }
        return expectation;
    }

    public Expectation[] deserializeArray(String jsonExpectations) {
        Expectation[] expectations = new Expectation[]{};
        if (jsonExpectations != null && !jsonExpectations.isEmpty()) {
            try {
                ExpectationDTO[] expectationDTOs = objectMapper.readValue(jsonExpectations, ExpectationDTO[].class);
                if (expectationDTOs != null && expectationDTOs.length > 0) {
                    expectations = new Expectation[expectationDTOs.length];
                    for (int i = 0; i < expectationDTOs.length; i++) {
                        expectations[i] = expectationDTOs[i].buildObject();
                    }
                }
            } catch (Exception e) {
                logger.error("Exception while parsing response [" + jsonExpectations + "] for http response expectation array", e);
                throw new RuntimeException("Exception while parsing response [" + jsonExpectations + "] for http response expectation array", e);
            }
        }
        return expectations;
    }
}
