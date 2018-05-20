import java.io.File;
import java.io.RandomAccessFile;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

//SimpleChannelInboundHandler, which allows to explicit only handle a specific type of messages
public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	
	//指定路径
	final static String PATH = "/test";
//	final static String FILE_PATH = "G:\\test\\";
	final static String FILE_PATH = "/apps/svr/test/";
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		String uri = request.uri();
		System.out.println("method:" + request.method());
		System.out.println("request:"+uri);
		System.out.flush();
		//检查路径是否合法
		if(!uri.startsWith(PATH)){
			sendSimpleResponse(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}
		//如果是目录，展示目录
		if (uri.equals(PATH)) {
			sendFileList(ctx);
			return;
		}
		//如果是文件，检查文件是否存在
		String fileName = FILE_PATH + uri.replace(PATH + "/", "");
		File file = new File(fileName);
		if(!file.exists()){
			sendSimpleResponse(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}
		//展示文件
		RandomAccessFile raf = null;
		raf = new RandomAccessFile(file, "r");
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/html;charset=UTF-8");
		response.headers().set(HttpHeaderNames.CONTENT_LENGTH,file.length());
		response.headers().set(HttpHeaderNames.TRANSFER_ENCODING,"chunked");
		ctx.write(response);
		ctx.writeAndFlush(new ChunkedFile(raf,120)).addListener(ChannelFutureListener.CLOSE);
	}
	
	public void sendSimpleResponse(ChannelHandlerContext ctx, HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
		response.headers().set(HttpHeaderNames.CONTENT_LENGTH,0);
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);;
	}
	
	public void sendFileList(ChannelHandlerContext ctx) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>");
		File file = new File(FILE_PATH);
		File[] fileList = file.listFiles();
		for (int i = 0; i < fileList.length; i++) {
			sb.append("<li><a href='" + PATH + "/" + fileList[i].getName() + "'>" + fileList[i].getName() + "</a></li>");
		}
		sb.append("</body></html>");
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/html;charset=UTF-8");
		response.headers().set(HttpHeaderNames.CONTENT_LENGTH,sb.length());
		ByteBuf buffer = Unpooled.copiedBuffer(sb,CharsetUtil.UTF_8);
		response.content().writeBytes(buffer);
		buffer.release();
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		
	}

}
