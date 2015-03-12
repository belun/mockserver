package org.mockserver.proxy.reverse;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import org.mockserver.proxy.unification.PortUnificationHandler;

/**
 * @author jamesdbloom
 */
@ChannelHandler.Sharable
public class ReverseProxyUnificationHandler extends PortUnificationHandler {

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, ChannelPipeline pipeline) {
        pipeline.addLast(new ReverseProxyUpstreamHandler());
    }
}
