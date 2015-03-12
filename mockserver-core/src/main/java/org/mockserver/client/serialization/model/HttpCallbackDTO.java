package org.mockserver.client.serialization.model;

import org.mockserver.model.HttpCallback;
import org.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

/**
 * @author jamesdbloom
 */
public class HttpCallbackDTO extends ObjectWithReflectiveEqualsHashCodeToString {

    private String callbackClass;

    public HttpCallbackDTO(HttpCallback httpCallback) {
        callbackClass = httpCallback.getCallbackClass();
    }

    public HttpCallbackDTO() {
    }

    public HttpCallback buildObject() {
        return new HttpCallback()
                .withCallbackClass(callbackClass);
    }

    public String getCallbackClass() {
        return callbackClass;
    }

    public HttpCallbackDTO setCallbackClass(String callbackClass) {
        this.callbackClass = callbackClass;
        return this;
    }
}

