package site.delivra.application.service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import site.delivra.application.model.dto.chat.ChatMessageDTO;
import site.delivra.application.model.request.chat.SendMessageRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.model.response.PaginationResponse;

public interface ChatService {

    ChatMessageDTO sendMessage(Integer taskId, Integer senderId, SendMessageRequest request);

    ChatMessageDTO uploadFile(Integer taskId, Integer senderId, MultipartFile file);

    DelivraResponse<PaginationResponse<ChatMessageDTO>> getChatHistory(Integer taskId, Pageable pageable);

    void markAsRead(Integer taskId, Integer userId);
}
