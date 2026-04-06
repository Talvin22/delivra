package site.delivra.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
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

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "gif", "webp", "pdf", "doc", "docx", "xls", "xlsx");

    private final ChatMessageRepository chatMessageRepository;
    private final DeliveryTaskRepository taskRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

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
    @Transactional
    public ChatMessageDTO uploadFile(Integer taskId, Integer senderId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new site.delivra.application.exception.InvalidDataException("File is empty");
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) ext = originalName.substring(dot + 1).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new site.delivra.application.exception.InvalidDataException(
                    "File type not allowed. Allowed: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        DeliveryTask task = taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new site.delivra.application.exception.NotFoundException(
                        site.delivra.application.model.constants.ApiErrorMessage.DELIVERY_NOT_FOUND_BY_ID.getMessage(taskId)));

        User sender = userRepository.findByIdAndDeletedFalse(senderId)
                .orElseThrow(() -> new site.delivra.application.exception.NotFoundException(
                        site.delivra.application.model.constants.ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(senderId)));

        String storedName = UUID.randomUUID() + "." + ext;
        Path dir = Paths.get(uploadDir, "chat");
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(storedName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }

        ChatMessage message = new ChatMessage();
        message.setDeliveryTask(task);
        message.setSender(sender);
        message.setFileUrl("/chat/files/" + storedName);
        message.setFileName(originalName);

        ChatMessage saved = chatMessageRepository.save(message);
        log.debug("File message saved: id={}, taskId={}, file={}", saved.getId(), taskId, storedName);

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
                .fileUrl(message.getFileUrl())
                .fileName(message.getFileName())
                .isRead(message.getIsRead())
                .created(message.getCreated())
                .build();
    }
}
