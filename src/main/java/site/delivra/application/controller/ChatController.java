package site.delivra.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import site.delivra.application.model.constants.ApiLogMessage;
import site.delivra.application.model.dto.chat.ChatMessageDTO;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.model.response.PaginationResponse;
import site.delivra.application.service.ChatService;
import site.delivra.application.utils.ApiUtils;

import java.security.Principal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks/{taskId}/chat")
public class ChatController {

    private final ChatService chatService;

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
    public ResponseEntity<Void> markAsRead(@PathVariable Integer taskId, Principal principal) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        Integer userId = extractUserId(principal);
        chatService.markAsRead(taskId, userId);
        return ResponseEntity.ok().build();
    }

    private Integer extractUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            return Integer.parseInt((String) auth.getDetails());
        }
        throw new IllegalStateException("Cannot extract userId from principal");
    }
}
