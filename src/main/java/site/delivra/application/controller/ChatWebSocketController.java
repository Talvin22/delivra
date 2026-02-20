package site.delivra.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import site.delivra.application.model.dto.chat.ChatMessageDTO;
import site.delivra.application.model.request.chat.SendMessageRequest;
import site.delivra.application.service.ChatService;
import site.delivra.application.service.model.AuthenticationConstants;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * User sends a chat message.
     *
     * Client publishes to: /app/chat/{taskId}
     * Server broadcasts to: /topic/chat/{taskId}
     *
     * Authentication: Principal is set by WebSocketAuthInterceptor (details = userId).
     */
    @MessageMapping("/chat/{taskId}")
    public void handleChatMessage(
            @DestinationVariable Integer taskId,
            SendMessageRequest request,
            Principal principal) {

        Integer senderId = extractUserId(principal);
        log.debug("Chat message: taskId={}, senderId={}", taskId, senderId);

        ChatMessageDTO saved = chatService.sendMessage(taskId, senderId, request);
        messagingTemplate.convertAndSend("/topic/chat/" + taskId, saved);
    }

    private Integer extractUserId(Principal principal) {
        if (principal instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth) {
            return Integer.parseInt((String) auth.getDetails());
        }
        throw new IllegalStateException("Cannot extract userId from principal");
    }
}
