package site.delivra.application.model.request.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotBlank(message = "messageText cannot be blank")
    @Size(max = 2000, message = "message too long")
    private String messageText;
}
