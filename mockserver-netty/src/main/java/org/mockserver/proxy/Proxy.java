package org.mockserver.proxy;

import io.netty.util.AttributeKey;
import org.mockserver.filters.LogFilter;

import java.net.InetSocketAddress;

/**
 * This class should not be constructed directly instead use ProxyBuilder to build and configure this class
 *
 * @see ProxyBuilder
 *
 * @author jamesdbloom
 */
public interface Proxy {

    public static final AttributeKey<Proxy> HTTP_PROXY = AttributeKey.valueOf("HTTP_PROXY");
    public static final AttributeKey<LogFilter> LOG_FILTER = AttributeKey.valueOf("PROXY_LOG_FILTER");
    public static final AttributeKey<InetSocketAddress> REMOTE_SOCKET = AttributeKey.valueOf("REMOTE_SOCKET");

    public void stop();

    public boolean isRunning();
}
