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
import java.util.List;

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
        updateUserFileList();
        System.out.println("Список файлов в директории клиента обновлен");
    }

    public void onSendToServerButtonClick(ActionEvent actionEvent) {
        final String path = clientFileList.getSelectionModel().getSelectedItem();
        Path send = Path.of(path);
        String pathInServer = Path.of(Config.USER_DIRECTORY).relativize(send).toString();
        try {
            if (Files.size(send) < Config.MAX_OBJECT_SIZE) {
                new Thread(() -> {
                    try {
                        Message message = Message.builder()
                                .command(Command.PUT)
                                .file(pathInServer)
                                .length(Files.size(send))
                                .data(Files.readAllBytes(send))
                                .build();
                        client.send(message, response -> {
                            updateServerFileList(response.getFiles());
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

        new Thread(() -> {
            Message message = Message.builder()
                    .command(Command.FILES)
                    .build();
            client.send(message, response -> {
                updateServerFileList(response.getFiles());
                System.out.println("Список файлов на сервере обновлен");
            });
        }).start();

    }

    public void onSendToClientButtonClick(ActionEvent actionEvent) {
        final String path = serverFileList.getSelectionModel().getSelectedItem();

        new Thread(() -> {

            Message message = Message.builder()
                    .command(Command.GET)
                    .file(path)
                    .build();
            client.send(message, response -> {
                Path file = Path.of(Config.USER_DIRECTORY, response.getFile());
                try {
                    Files.createDirectories(file.getParent());
                    Files.createFile(file);
                } catch (FileAlreadyExistsException ignore) {
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try (FileOutputStream output = new FileOutputStream(file.toFile())) {
                    output.write(response.getData());
                    updateUserFileList();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }).start();
    }

    public void onDeleteInServerButtonClick(ActionEvent actionEvent) {
        serverFileList.getSelectionModel().selectFirst();
        final String fileForDelete = serverFileList.getSelectionModel().getSelectedItem();
        new Thread(() -> {
            Message message = Message.builder().command(Command.DELETE).file(fileForDelete).build();
            client.send(message, response -> {
                updateServerFileList(response.getFiles());
            });
        }).start();
    }

    public void updateUserFileList() {
        clientFileList.getItems().clear();
        try {
            clientFileList.getItems().addAll(client.getFileList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateServerFileList(List<String> files) {
        serverFileList.getItems().clear();
        try {
            serverFileList.getItems().addAll(files);
        } catch (Exception e) {
            System.out.println("Ошибка обновления списка файлов на сервере" + e);
        }
    }
}
