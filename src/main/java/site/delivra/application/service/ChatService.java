package site.delivra.application.service;

import org.springframework.data.domain.Pageable;
import site.delivra.application.model.dto.chat.ChatMessageDTO;
import site.delivra.application.model.request.chat.SendMessageRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.model.response.PaginationResponse;

public interface ChatService {

    ChatMessageDTO sendMessage(Integer taskId, Integer senderId, SendMessageRequest request);

    DelivraResponse<PaginationResponse<ChatMessageDTO>> getChatHistory(Integer taskId, Pageable pageable);

    void markAsRead(Integer taskId, Integer userId);
}
