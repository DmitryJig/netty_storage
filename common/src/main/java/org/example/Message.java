package org.example;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

public class Message implements Serializable {
    private String command;
    private File file;
    private byte[] data;

    public Message(String command, File file, byte[] data) {
        this.command = command;
        this.file = file;
        this.data = data;
    }

    public String getCommand() {
        return command;
    }

    public File getFile() {
        return file;
    }

    public byte[] getData() {
        return data;
    }



    @Override
    public String toString() {
        return "Message{" +
                "command='" + command + '\'' +
                ", file=" + file +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
