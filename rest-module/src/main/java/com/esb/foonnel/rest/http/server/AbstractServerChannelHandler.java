package com.esb.foonnel.rest.http.server;

import com.esb.foonnel.rest.RestListenerCom;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.channel.ChannelHandler.Sharable;

@Sharable
public abstract class AbstractServerChannelHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(RestListenerCom.class);

    @Override
    protected void channelRead0(ChannelHandlerContext context, FullHttpRequest request) {
        try {
            FullHttpResponse response = handle(request);
            context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            logger.error("Read message: ", e);
            // The request must always be released.
            ReferenceCountUtil.release(request);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        ctx.close();
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    protected abstract FullHttpResponse handle(FullHttpRequest request);


}
