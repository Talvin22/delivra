package site.delivra.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.delivra.application.exception.NotFoundException;
import site.delivra.application.model.constants.ApiErrorMessage;
import site.delivra.application.model.dto.chat.ChatMessageDTO;
import site.delivra.application.model.entities.ChatMessage;
import site.delivra.application.model.entities.DeliveryTask;
import site.delivra.application.model.entities.User;
import site.delivra.application.model.request.chat.SendMessageRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.model.response.PaginationResponse;
import site.delivra.application.repository.ChatMessageRepository;
import site.delivra.application.repository.DeliveryTaskRepository;
import site.delivra.application.repository.UserRepository;
import site.delivra.application.service.ChatService;
import site.delivra.application.service.EmailService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final DeliveryTaskRepository taskRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public ChatMessageDTO sendMessage(Integer taskId, Integer senderId, SendMessageRequest request) {
        DeliveryTask task = taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.DELIVERY_NOT_FOUND_BY_ID.getMessage(taskId)));

        User sender = userRepository.findByIdAndDeletedFalse(senderId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(senderId)));

        ChatMessage message = new ChatMessage();
        message.setDeliveryTask(task);
        message.setSender(sender);
        message.setMessageText(request.getMessageText());

        ChatMessage saved = chatMessageRepository.save(message);
        log.debug("Chat message saved: id={}, taskId={}, senderId={}", saved.getId(), taskId, senderId);

        if (task.getUser() != null) {
            task.getUser().getEmail(); // force-init lazy proxy within transaction
        }
        emailService.sendChatNotification(task, sender, request.getMessageText());

        return toDto(saved);
    }

    @Override
    public DelivraResponse<PaginationResponse<ChatMessageDTO>> getChatHistory(Integer taskId, Pageable pageable) {
        if (!taskRepository.existsById(taskId)) {
            throw new NotFoundException(ApiErrorMessage.DELIVERY_NOT_FOUND_BY_ID.getMessage(taskId));
        }

        Page<ChatMessageDTO> page = chatMessageRepository
                .findByDeliveryTaskIdAndDeletedFalseOrderByCreatedAsc(taskId, pageable)
                .map(this::toDto);

        return DelivraResponse.createSuccessful(PaginationResponse.<ChatMessageDTO>builder()
                .content(page.getContent())
                .pagination(PaginationResponse.Pagination.builder()
                        .total(page.getTotalElements())
                        .limit(page.getSize())
                        .page(page.getNumber() + 1)
                        .pages(page.getTotalPages())
                        .build())
                .build());
    }

    @Override
    @Transactional
    public void markAsRead(Integer taskId, Integer userId) {
        chatMessageRepository.markAllAsRead(taskId, userId);
    }

    private ChatMessageDTO toDto(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .taskId(message.getDeliveryTask().getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .messageText(message.getMessageText())
                .isRead(message.getIsRead())
                .created(message.getCreated())
                .build();
    }
}
