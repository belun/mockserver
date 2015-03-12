package org.mockserver.integration;

import org.mockserver.client.proxy.ProxyClient;
import org.mockserver.proxy.Proxy;
import org.mockserver.proxy.ProxyBuilder;

/**
 * @author jamesdbloom
 */
public class ClientAndProxy extends ProxyClient {

    private final Proxy proxy;

    public ClientAndProxy(Integer port) {
        super("localhost", port);
        proxy = new ProxyBuilder().withLocalPort(port).build();
    }

    public static ClientAndProxy startClientAndProxy(Integer port) {
        return new ClientAndProxy(port);
    }

    public boolean isRunning() {
        return proxy.isRunning();
    }
}
