package org.mockserver.proxy.forward;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import org.mockserver.codec.MockServerServerCodec;
import org.mockserver.proxy.Proxy;
import org.mockserver.proxy.unification.PortUnificationHandler;

/**
 * @author jamesdbloom
 */
@ChannelHandler.Sharable
class ForwardProxyUnificationHandler extends PortUnificationHandler {

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, ChannelPipeline pipeline) {
        pipeline.addLast(new MockServerServerCodec(isSslEnabled(ctx)));
        pipeline.addLast(new ForwardProxyHandler(ctx.channel().attr(Proxy.HTTP_PROXY).get(), ctx.channel().attr(Proxy.LOG_FILTER).get()));
    }

}
