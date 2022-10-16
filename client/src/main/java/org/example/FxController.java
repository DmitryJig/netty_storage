package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
        File file = new File(path);
        try {
            Message message = new Message("put", file, Files.readAllBytes(file.toPath()));
            System.out.println("Start send file from client to server: " + path);
            client.send(message, (response) -> {
                System.out.println("response = " + response);
            });
        } catch (IOException e) {
            System.out.println("Error send file " + e);
        } catch (InterruptedException e) {
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
