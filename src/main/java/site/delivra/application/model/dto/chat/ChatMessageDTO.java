package site.delivra.application.model.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO implements Serializable {

    private Integer id;
    private Integer taskId;
    private Integer senderId;
    private String senderUsername;
    private String messageText;
    private Boolean isRead;
    private LocalDateTime created;
}
