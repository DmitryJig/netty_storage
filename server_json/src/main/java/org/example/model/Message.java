package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // создает конструктор, геттеры, сеттеры, тустринг, иквалс и хэшкод
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // игнорировать если в модели нет полей а в сообщении есть
public class Message {

    private Command command;
    private String status;
    private String file;
    private long length;
    private byte[] data;
}
