package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.example.config.Config;
import org.example.model.Command;
import org.example.model.Message;
import org.example.network.Client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FxController {
    @FXML
    public ListView<String> serverFileList;
    @FXML
    public ListView<String> clientFileList;
    @FXML
    private Label upClientText;
    @FXML
    private Label upServerText;
    private Client client;

    public FxController() {
        this.client = new Client(this);
    }

    public void updateUserFileList(ActionEvent actionEvent) {
        clientFileList.getItems().clear();
        try {
            clientFileList.getItems().addAll(client.getFileList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onSendToServerButtonClick(ActionEvent actionEvent) {
        final String path = clientFileList.getSelectionModel().getSelectedItem();
        Path send = Path.of(path);
        try {
            if (Files.size(send) < Config.MAX_OBJECT_SIZE) {
                new Thread(() -> {
                    try {
                        Message message = Message.builder()
                                .command(Command.PUT)
                                .file(send.getFileName().toString())
                                .length(Files.size(send))
                                .data(Files.readAllBytes(send))
                                .build();
                        client.send(message, response -> {
                            System.out.printf("File %s %s", response.getFile(), response.getStatus());
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            } else {
                System.out.println("Too long file size");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onDeleteInClientButtonClick(ActionEvent actionEvent) {
        final String fileForDelete = clientFileList.getSelectionModel().getSelectedItem();
        client.deleteFile(fileForDelete);
        updateUserFileList();
    }

    public void updateServerFileList(ActionEvent actionEvent) {
    }

    public void onSendToClientButtonClick(ActionEvent actionEvent) {
        final String path = serverFileList.getSelectionModel().getSelectedItem();
        Path send = Path.of(path);
        new Thread(() -> {

            Message message = Message.builder()
                    .command(Command.GET)
                    .file(path)
                    .build();
            client.send(message, response -> {
                Path file = Path.of("client", response.getFile());
                try{
                    Files.createFile(file);
                } catch (FileAlreadyExistsException ignore){
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try (FileOutputStream output = new FileOutputStream(file.toFile())) {
                    output.write(response.getData());

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }).start();
    }

    public void onDeleteInServerButtonClick(ActionEvent actionEvent) {
    }

    public void updateUserFileList() {
        clientFileList.getItems().clear();
        try {
            clientFileList.getItems().addAll(client.getFileList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
