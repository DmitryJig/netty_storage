package org.example.logic;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.example.config.Config;
import org.example.model.Command;
import org.example.model.Message;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StorageLogic {

    public static void process(Message message, Channel channel){
        if (message.getCommand().equals(Command.PUT)){
            Path file = Path.of(Config.USER_DIRECTORY, message.getFile());
            try {
                Files.createDirectories(Path.of(Config.USER_DIRECTORY));
                Files.createFile(file);


            } catch (FileAlreadyExistsException ignored){
            }catch (IOException e) {
                ChannelFuture future = channel.writeAndFlush(
                        Message.builder().command(message.getCommand()).status("FILE ERROR").build()
                );
                future.addListener(ChannelFutureListener.CLOSE);
                return;
            }
            try(FileOutputStream output = new FileOutputStream(file.toFile())) {

                output.write(message.getData());
            } catch (IOException e) {
                ChannelFuture future = channel.writeAndFlush(
                        Message.builder().command(message.getCommand()).status("FILE ERROR").build()
                );
                future.addListener(ChannelFutureListener.CLOSE);
                return;
            } finally {
                channel.close();
            }
        }

        if (message.getCommand().equals("GET")){
            Path file = Path.of(Config.USER_DIRECTORY, message.getFile());
            try{
                if (Files.exists(file) && Files.size(file) < 10_000){
                    Message message1 = Message.builder()
                            .command(message.getCommand())
                            .file(file.getFileName().toString())
                            .status("OK")
                            .length(Files.size(file))
                            .data(Files.readAllBytes(file))
                            .build();
                    channel.writeAndFlush(message1);
                }
            } catch (IOException e) {
                ChannelFuture future = channel.writeAndFlush(
                        Message.builder().command(message.getCommand()).status("FILE ERROR").build()
                );
                future.addListener(ChannelFutureListener.CLOSE);
            } finally {
                channel.close();
            }
        }
    }
}
