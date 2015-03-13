package org.mockserver.matchers;

import com.google.common.base.Charsets;
import org.mockserver.Line;
import org.mockserver.client.serialization.ObjectMapperFactory;
import org.mockserver.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jamesdbloom
 */
public class HttpRequestMatcher extends ObjectWithReflectiveEqualsHashCodeToString implements Matcher<HttpRequest> {

    private HttpRequest httpRequest;
    private RegexStringMatcher methodMatcher = null;
    private RegexStringMatcher pathMatcher = null;
    private MultiValueMapMatcher queryStringParameterMatcher = null;
    private BodyMatcher bodyMatcher = null;
    private MultiValueMapMatcher headerMatcher = null;
    private HashMapMatcher cookieMatcher = null;

    public HttpRequestMatcher(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
        if (httpRequest != null) {
            withMethod(httpRequest.getMethod());
            withPath(httpRequest.getPath());
            withQueryStringParameters(httpRequest.getQueryStringParameters());
            withBody(httpRequest.getBody());
            withHeaders(httpRequest.getHeaders());
            withCookies(httpRequest.getCookies());
        }
    }

    private HttpRequestMatcher withMethod(String method) {
        this.methodMatcher = new RegexStringMatcher(method);
        return this;
    }

    private HttpRequestMatcher withPath(String path) {
        this.pathMatcher = new RegexStringMatcher(path);
        return this;
    }

    private HttpRequestMatcher withQueryStringParameters(List<Parameter> parameters) {
        this.queryStringParameterMatcher = new MultiValueMapMatcher(KeyToMultiValue.toMultiMap(parameters));
        return this;
    }

    private HttpRequestMatcher withBody(Body body) {
        if (body != null) {
            switch (body.getType()) {
                case STRING:
                    this.bodyMatcher = new ExactStringMatcher(((StringBody) body).getValue());
                    break;
                case REGEX:
                    this.bodyMatcher = new RegexStringMatcher(((StringBody) body).getValue());
                    break;
                case PARAMETERS:
                    this.bodyMatcher = new ParameterStringMatcher(((ParameterBody) body).getValue());
                    break;
                case XPATH:
                    this.bodyMatcher = new XPathStringMatcher(((StringBody) body).getValue());
                    break;
                case JSON:
                    this.bodyMatcher = new JsonStringMatcher(((StringBody) body).getValue());
                    break;
                case BINARY:
                    this.bodyMatcher = new BinaryMatcher(((BinaryBody) body).getValue());
                    break;
            }
        }
        return this;
    }

    private HttpRequestMatcher withHeaders(Header... headers) {
        this.headerMatcher = new MultiValueMapMatcher(KeyToMultiValue.toMultiMap(headers));
        return this;
    }

    private HttpRequestMatcher withHeaders(List<Header> headers) {
        this.headerMatcher = new MultiValueMapMatcher(KeyToMultiValue.toMultiMap(headers));
        return this;
    }

    private HttpRequestMatcher withCookies(Cookie... cookies) {
        this.cookieMatcher = new HashMapMatcher(KeyAndValue.toHashMap(cookies));
        return this;
    }

    private HttpRequestMatcher withCookies(List<Cookie> cookies) {
        this.cookieMatcher = new HashMapMatcher(KeyAndValue.toHashMap(cookies));
        return this;
    }

    public boolean matches(HttpRequest httpRequest) {
        if (httpRequest == this.httpRequest) {
            return true;
        } else if (httpRequest != null) {
            boolean methodMatches = matches(methodMatcher, httpRequest.getMethod());
            boolean pathMatches = matches(pathMatcher, httpRequest.getPath());
            boolean queryStringParametersMatches = matches(queryStringParameterMatcher, (httpRequest.getQueryStringParameters() != null ? new ArrayList<KeyToMultiValue>(httpRequest.getQueryStringParameters()) : null));
            boolean bodyMatches;
            if (bodyMatcher instanceof BinaryMatcher) {
                bodyMatches = matches(bodyMatcher, httpRequest.getBodyAsRawBytes());
            } else {
                bodyMatches = matches(bodyMatcher, (httpRequest.getBody() != null ? new String(httpRequest.getBody().getRawBytes(), Charsets.UTF_8) : ""));
            }
            boolean headersMatch = matches(headerMatcher, (httpRequest.getHeaders() != null ? new ArrayList<KeyToMultiValue>(httpRequest.getHeaders()) : null));
            boolean cookiesMatch = matches(cookieMatcher, (httpRequest.getCookies() != null ? new ArrayList<KeyAndValue>(httpRequest.getCookies()) : null));
            boolean result = methodMatches && pathMatches && queryStringParametersMatches && bodyMatches && headersMatch && cookiesMatch;
            if (!result && logger.isDebugEnabled()) {
                logger.debug("\n\nMatcher:" + Line.SEPARATOR + Line.SEPARATOR +
                        "[" + this + "]" + Line.SEPARATOR + Line.SEPARATOR +
                        "did not match request:" + Line.SEPARATOR + Line.SEPARATOR +
                        "[" + httpRequest + "]" + Line.SEPARATOR + Line.SEPARATOR +
                        "because:" + Line.SEPARATOR + Line.SEPARATOR +
                        "methodMatches = " + methodMatches + "" + Line.SEPARATOR +
                        "pathMatches = " + pathMatches + "" + Line.SEPARATOR +
                        "queryStringParametersMatch = " + queryStringParametersMatches + "" + Line.SEPARATOR +
                        "bodyMatches = " + bodyMatches + "" + Line.SEPARATOR +
                        "headersMatch = " + headersMatch + "" + Line.SEPARATOR +
                        "cookiesMatch = " + cookiesMatch);
            }
            return result;
        } else {
            return false;
        }
    }

    private <T> boolean matches(Matcher<T> matcher, T t) {
        boolean result = false;

        if (matcher == null) {
            result = true;
        } else if (matcher.matches(t)) {
            result = true;
        }

        return result;
    }

    @Override
    public String toString() {
        try {
            return ObjectMapperFactory
                    .createObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(httpRequest);
        } catch (Exception e) {
            return super.toString();
        }
    }
}
