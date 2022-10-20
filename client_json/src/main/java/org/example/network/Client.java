package org.example.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import org.example.FxController;
import org.example.config.Config;
import org.example.model.Command;
import org.example.model.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Client {

    private FxController controller;

    public Client(FxController controller) {
        this.controller = controller;
    }

    public void send(Message message, Consumer<Message> callback) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap client = new Bootstrap();
            client.group(workerGroup);
            client.channel(NioSocketChannel.class);
            client.option(ChannelOption.SO_KEEPALIVE, true);
            client.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel sh) throws Exception {
                    sh.pipeline().addLast(
                            new JsonObjectDecoder(), // проверяет все входящие данные и ищет последнюю закрытую скобку,
                            // только потом передает следующему обработчику данные как объект
                            new JacksonDecoder(), // этот превратит json в объект
                            new JacksonEncoder(),
                            new ClientHandler(message, callback)
                    );
                }
            });
            ChannelFuture future = client.connect(Config.HOST, Config.PORT).sync();
            future.addListener(f -> {
                if (!f.isSuccess()) {
                    callback.accept(Message.builder().command(message.getCommand()).status("CONNECTION FAILED").build());
                }
            });
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public List<String> getFileList() throws IOException {

        List<String> fileList = new ArrayList();
        Files.walkFileTree(Path.of(Config.USER_DIRECTORY), new SimpleFileVisitor<>() {
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
}
