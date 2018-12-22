/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.common.close;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.ssl.SslCloseCompletionEvent;
import reactor.util.Logger;

import java.util.concurrent.atomic.AtomicReference;

public class CloseHandlerAdapter extends ChannelInboundHandlerAdapter {

    private final AtomicReference<CloseStatus> closeStatus;
    private final Logger log;

    public CloseHandlerAdapter(AtomicReference<CloseStatus> closeStatus, Logger log) {
        this.closeStatus = closeStatus;
        this.log = log;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof CloseWebSocketFrame && ((CloseWebSocketFrame) msg).isFinalFragment()) {
            CloseWebSocketFrame close = (CloseWebSocketFrame) msg;
            log.debug("Close status: {} {}", close.statusCode(), close.reasonText());
            closeStatus.set(new CloseStatus(close.statusCode(), close.reasonText()));
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof SslCloseCompletionEvent) {
            SslCloseCompletionEvent closeEvent = (SslCloseCompletionEvent) evt;
            if (!closeEvent.isSuccess()) {
                log.debug("Abnormal close status: {}", closeEvent.cause().toString());
                closeStatus.set(new CloseStatus(1006, closeEvent.cause().toString()));
            }
        }
        ctx.fireUserEventTriggered(evt);
    }
}
