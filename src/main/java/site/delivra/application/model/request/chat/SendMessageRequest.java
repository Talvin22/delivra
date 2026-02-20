package site.delivra.application.model.request.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotBlank(message = "messageText cannot be blank")
    private String messageText;
}
