package org.mockserver.proxy;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.proxy.reverse.ReverseProxy;
import org.mockserver.proxy.forward.ForwardProxy;
import org.mockserver.socket.PortFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * @author jamesdbloom
 */
public class ProxyBuilderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldConfigureForwardProxy() {
        // given
        // - some ports
        Integer port = PortFactory.findFreePort();

        // when
        Proxy proxy = new ProxyBuilder()
                .withLocalPort(port)
                .build();

        try {
            // then
            assertThat(proxy, is(instanceOf(ForwardProxy.class)));
            ForwardProxy unificationProxy = (ForwardProxy)proxy;
            assertThat(unificationProxy.getPort(), is(port));
        } finally {
            proxy.stop();
        }
    }

    @Test
    public void shouldConfigureReverseProxy() {
        // given
        // - some ports
        Integer port = PortFactory.findFreePort();
        String remoteHost = "random.host";
        Integer remotePort = PortFactory.findFreePort();

        // when
        Proxy proxy = new ProxyBuilder()
                .withLocalPort(port)
                .withRemote(remoteHost, remotePort)
                .build();

        try {
            // then
            assertThat(proxy, is(instanceOf(ReverseProxy.class)));
            ReverseProxy reverseProxy = (ReverseProxy)proxy;
            assertThat(reverseProxy.getLocalPort(), is(port));
            assertThat(reverseProxy.getRemoteHost(), is(remoteHost));
            assertThat(reverseProxy.getRemotePort(), is(remotePort));
        } finally {
            proxy.stop();
        }
    }


    @Test
    public void shouldThrowExceptionWhenNoLocalPort() {
        // then
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("LocalPort must be specified before the proxy is started"));

        // when
        new ProxyBuilder().build();
    }
}
