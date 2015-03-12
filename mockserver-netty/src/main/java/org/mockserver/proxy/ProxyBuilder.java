package org.mockserver.proxy;

import org.mockserver.proxy.reverse.ReverseProxy;
import org.mockserver.proxy.forward.ForwardProxy;

/**
 * This class should be used to configure the Proxy, using this class is the simplest way to create an Proxy instance
 *
 * @author jamesdbloom
 */
public class ProxyBuilder {

    private Integer localPort;
    private String remoteHost;
    private Integer remotePort;

    /**
     * Configure the local port for the proxy, this will be the same port for all traffic including HTTP, SOCKS, CONNECT and SSL
     *
     * @param localPort the local port to use
     */
    public ProxyBuilder withLocalPort(Integer localPort) {
        this.localPort = localPort;
        return this;
    }

    /**
     * Configure a reverse proxy that forwards all requests from the localPort to the remoteHost and remotePort
     *
     * @param remoteHost the destination hostname for forwarding
     * @param remotePort the destination port for forwarding
     */
    public ProxyBuilder withRemote(String remoteHost, Integer remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        return this;
    }

    /**
     * Build an instance of the Proxy
     */
    public Proxy build() {
        if (localPort != null) {
            if (remoteHost != null && remotePort != null) {
                return new ReverseProxy(localPort, remoteHost, remotePort);
            } else {
                return new ForwardProxy(localPort);
            }
        } else {
            throw new IllegalArgumentException("LocalPort must be specified before the proxy is started");
        }
    }

}
