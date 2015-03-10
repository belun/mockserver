package org.mockserver.proxy;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.proxy.direct.DirectProxy;
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
    public void shouldConfigureDirectProxy() {
        // given
        // - some ports
        Integer port = PortFactory.findFreePort();
        String directRemoteHost = "random.host";
        Integer directRemotePort = PortFactory.findFreePort();

        // when
        Proxy proxy = new ProxyBuilder()
                .withLocalPort(port)
                .withDirect(directRemoteHost, directRemotePort)
                .build();

        try {
            // then
            assertThat(proxy, is(instanceOf(DirectProxy.class)));
            DirectProxy directProxy = (DirectProxy)proxy;
            assertThat(directProxy.getLocalPort(), is(port));
            assertThat(directProxy.getRemoteHost(), is(directRemoteHost));
            assertThat(directProxy.getRemotePort(), is(directRemotePort));
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
