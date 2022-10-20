package org.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class Client {
    public final static int MAX_OBJECT_SIZE = 1024 * 1024 * 100;
    private final Path USER_DIR = Paths.get("client/dir");
    private final String HOST = "localhost";
    private final int PORT = 9000;
    private FxController controller;

    public Client(FxController controller) {
        this.controller = controller;
    }

    public List<String> getFileList() throws IOException {

        List<String> fileList = new ArrayList();
        Files.walkFileTree(USER_DIR, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!Files.isDirectory(file)) {
                    fileList.add(file.getParent().toString() + File.separator + file.getFileName().toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return fileList;
    }

    public void deleteFile(String pathForDelete) {
        try {
            Files.delete(Paths.get(pathForDelete));
        } catch (IOException e) {
            System.out.println("Exception delete file: " + e);
        }
    }

    void send(Message message, Consumer<String> callback) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap client = new Bootstrap();
            client.group(workerGroup);
            client.channel(NioSocketChannel.class);
            client.option(ChannelOption.SO_KEEPALIVE, true);
            client.handler(new ChannelInitializer<>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(
                            new ObjectEncoder(),
                            new LineBasedFrameDecoder(80),
                            new StringDecoder(StandardCharsets.UTF_8),
                            new ClientHandler(message, callback)
                    );
                }
            });
            ChannelFuture future = client.connect(HOST, PORT).sync();
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
