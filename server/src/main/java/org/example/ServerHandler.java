package org.example;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        System.out.println("msg = " + msg);
        if (msg.getCommand().equals("put")) {
            Path root = Path.of("server/user-dir");
            Files.createDirectories(root);
            Path file = root.resolve(msg.getFile().getPath());
            Files.createDirectories(file.getParent());
            try {
                Files.createFile(file);
            } catch (FileAlreadyExistsException ignored) {
                System.out.println("File already exists");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Files.write(file, msg.getData());
        }
        ChannelFuture future = ctx.writeAndFlush(String.format("File %s stored!\n", msg.getFile().getName()));
        future.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
