package site.delivra.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import site.delivra.application.model.constants.ApiLogMessage;
import site.delivra.application.model.dto.chat.ChatMessageDTO;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.model.response.PaginationResponse;
import site.delivra.application.service.ChatService;
import site.delivra.application.utils.ApiUtils;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks/{taskId}/chat")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApiUtils apiUtils;

    @GetMapping
    public ResponseEntity<DelivraResponse<PaginationResponse<ChatMessageDTO>>> getChatHistory(
            @PathVariable Integer taskId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        Pageable pageable = PageRequest.of(page, limit);
        DelivraResponse<PaginationResponse<ChatMessageDTO>> response =
                chatService.getChatHistory(taskId, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer taskId) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        Integer userId = apiUtils.getUserIdFromAuthentication();
        chatService.markAsRead(taskId, userId);
        messagingTemplate.convertAndSend(
                "/topic/chat/" + taskId + "/read",
                Map.of("readByUserId", userId)
        );
        return ResponseEntity.ok().build();
    }
}
