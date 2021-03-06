import javax.net.ssl.SSLEngine;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpServer {
	public static void main(String[] args) {
		EventLoopGroup master = new NioEventLoopGroup();
		EventLoopGroup worker = new NioEventLoopGroup();
		try {
			ServerBootstrap sb = new ServerBootstrap();
			sb.group(master, worker)
			  .channel(NioServerSocketChannel.class)
			  .childHandler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel ch) throws Exception {
					SSLEngine sslEngine = SSLContextFactory.getSslContext().createSSLEngine();
                    sslEngine.setUseClientMode(false);
                    ch.pipeline().addLast(new SslHandler(sslEngine));
					ch.pipeline().addLast(new HttpRequestDecoder());//Decodes ByteBufs into HttpRequests and HttpContents.
					ch.pipeline().addLast(new HttpObjectAggregator(65536));//将HttpRequests和HttpContents整合成FullHttpRequest
					ch.pipeline().addLast(new HttpResponseEncoder());//Encodes an HttpResponse or an HttpContent into a ByteBuf.
					ch.pipeline().addLast(new ChunkedWriteHandler());//adds support for writing a large data stream asynchronously neither spending a lot of memory nor getting OutOfMemoryError
					ch.pipeline().addLast(new HttpFileServerHandler());
				}
			});
			ChannelFuture future = sb.bind(9999).sync();
			
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
			master.shutdownGracefully();
			worker.shutdownGracefully();
		}
	}
}
