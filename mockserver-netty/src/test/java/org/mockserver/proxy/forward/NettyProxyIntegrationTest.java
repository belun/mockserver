package org.mockserver.proxy.forward;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockserver.client.proxy.ProxyClient;
import org.mockserver.echo.EchoServer;
import org.mockserver.integration.proxy.AbstractClientProxyIntegrationTest;
import org.mockserver.proxy.Proxy;
import org.mockserver.proxy.ProxyBuilder;
import org.mockserver.socket.PortFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jamesdbloom
 */
public class NettyProxyIntegrationTest extends AbstractClientProxyIntegrationTest {

    private final static Logger logger = LoggerFactory.getLogger(NettyProxyIntegrationTest.class);

    private final static Integer SERVER_HTTP_PORT = PortFactory.findFreePort();
    private final static Integer PROXY_HTTP_PORT = PortFactory.findFreePort();
    private static EchoServer echoServer;
    private static Proxy proxy;
    private static ProxyClient proxyClient;

    @BeforeClass
    public static void setupFixture() throws Exception {
        logger.debug("SERVER_HTTP_PORT = " + SERVER_HTTP_PORT);
        logger.debug("PROXY_HTTP_PORT = " + PROXY_HTTP_PORT);

        servletContext = "";

        // start server
        echoServer = new EchoServer(SERVER_HTTP_PORT);

        // start proxy
        proxy = new ProxyBuilder()
                .withLocalPort(PROXY_HTTP_PORT)
                .build();

        // start client
        proxyClient = new ProxyClient("localhost", PROXY_HTTP_PORT);
    }

    @AfterClass
    public static void shutdownFixture() {
        // stop server
        echoServer.stop();

        // stop proxy
        proxy.stop();
    }

    @Before
    public void resetProxy() {
        proxyClient.reset();
    }

    @Override
    public int getProxyPort() {
        return PROXY_HTTP_PORT;
    }

    @Override
    public ProxyClient getProxyClient() {
        return proxyClient;
    }

    @Override
    public int getServerPort() {
        return SERVER_HTTP_PORT;
    }
}
