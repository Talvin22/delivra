package site.delivra.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import site.delivra.application.exception.InvalidDataException;
import site.delivra.application.exception.NotFoundException;
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
import site.delivra.application.service.impl.ChatServiceImpl;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private DeliveryTaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private ChatServiceImpl service;

    private DeliveryTask task;
    private User sender;
    private User receiver;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());

        receiver = new User();
        receiver.setId(20);
        receiver.setUsername("receiver");
        receiver.setEmail("rec@example.com");
        receiver.setDeleted(false);

        sender = new User();
        sender.setId(10);
        sender.setUsername("sender");
        sender.setEmail("snd@example.com");
        sender.setDeleted(false);

        task = new DeliveryTask();
        task.setId(1);
        task.setAddress("Addr");
        task.setUser(receiver);
        task.setDeleted(false);
    }

    @Test
    void sendMessage_taskNotFound_throwsNotFoundException() {
        when(taskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.empty());
        SendMessageRequest req = new SendMessageRequest();
        req.setMessageText("hi");

        assertThrows(NotFoundException.class, () -> service.sendMessage(1, 10, req));
    }

    @Test
    void sendMessage_senderNotFound_throwsNotFoundException() {
        when(taskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.empty());
        SendMessageRequest req = new SendMessageRequest();
        req.setMessageText("hi");

        assertThrows(NotFoundException.class, () -> service.sendMessage(1, 10, req));
    }

    @Test
    void sendMessage_valid_savesAndNotifies() {
        when(taskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(sender));
        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenAnswer(inv -> {
                    ChatMessage m = inv.getArgument(0);
                    m.setId(500);
                    m.setCreated(LocalDateTime.now());
                    m.setIsRead(false);
                    return m;
                });

        SendMessageRequest req = new SendMessageRequest();
        req.setMessageText("hello");

        ChatMessageDTO dto = service.sendMessage(1, 10, req);

        assertEquals(500, dto.getId());
        assertEquals("hello", dto.getMessageText());
        assertEquals(10, dto.getSenderId());
        assertEquals(1, dto.getTaskId());
        verify(emailService).sendChatNotification(task, sender, "hello");
    }

    @Test
    void uploadFile_emptyFile_throwsInvalidDataException() {
        MockMultipartFile empty = new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[0]);

        assertThrows(InvalidDataException.class, () -> service.uploadFile(1, 10, empty));
    }

    @Test
    void uploadFile_forbiddenExtension_throwsInvalidDataException() {
        MockMultipartFile bad = new MockMultipartFile("file", "malware.exe", "application/octet-stream",
                new byte[]{1, 2, 3});

        assertThrows(InvalidDataException.class, () -> service.uploadFile(1, 10, bad));
    }

    @Test
    void uploadFile_noExtension_throwsInvalidDataException() {
        MockMultipartFile noExt = new MockMultipartFile("file", "README", "text/plain",
                new byte[]{1, 2});

        assertThrows(InvalidDataException.class, () -> service.uploadFile(1, 10, noExt));
    }

    @Test
    void uploadFile_taskNotFound_throwsNotFoundException() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf",
                new byte[]{1, 2, 3});
        when(taskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.uploadFile(1, 10, file));
    }

    @Test
    void uploadFile_senderNotFound_throwsNotFoundException() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf",
                new byte[]{1, 2, 3});
        when(taskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.uploadFile(1, 10, file));
    }

    @Test
    void uploadFile_validFile_savesMessageWithFileUrl() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg",
                new byte[]{1, 2, 3});
        when(taskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(sender));
        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenAnswer(inv -> {
                    ChatMessage m = inv.getArgument(0);
                    m.setId(600);
                    m.setCreated(LocalDateTime.now());
                    m.setIsRead(false);
                    return m;
                });

        ChatMessageDTO dto = service.uploadFile(1, 10, file);

        assertEquals(600, dto.getId());
        assertEquals("photo.jpg", dto.getFileName());
        assertNotNull(dto.getFileUrl());
        assertTrue(dto.getFileUrl().startsWith("/chat/files/"));
        assertTrue(dto.getFileUrl().endsWith(".jpg"));
    }

    @Test
    void getChatHistory_taskDoesNotExist_throwsNotFoundException() {
        when(taskRepository.existsById(1)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> service.getChatHistory(1, PageRequest.of(0, 10)));
    }

    @Test
    void getChatHistory_validPaging_returnsPaginatedDtos() {
        ChatMessage message = new ChatMessage();
        message.setId(1);
        message.setDeliveryTask(task);
        message.setSender(sender);
        message.setMessageText("msg");
        message.setIsRead(false);
        message.setCreated(LocalDateTime.now());

        Pageable pageable = PageRequest.of(0, 10);
        Page<ChatMessage> page = new PageImpl<>(List.of(message), pageable, 1);

        when(taskRepository.existsById(1)).thenReturn(true);
        when(chatMessageRepository.findByDeliveryTaskIdAndDeletedFalseOrderByCreatedAsc(1, pageable))
                .thenReturn(page);

        DelivraResponse<PaginationResponse<ChatMessageDTO>> response = service.getChatHistory(1, pageable);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getPayload().getContent().size());
        assertEquals("msg", response.getPayload().getContent().get(0).getMessageText());
        assertEquals(1L, response.getPayload().getPagination().getTotal());
        assertEquals(1, response.getPayload().getPagination().getPage());
    }

    @Test
    void markAsRead_delegatesToRepository() {
        service.markAsRead(1, 10);
        verify(chatMessageRepository).markAllAsRead(1, 10);
    }

    @Test
    void uploadFile_caseInsensitiveExtension_allowed() {
        MockMultipartFile file = new MockMultipartFile("file", "PHOTO.JPG", "image/jpeg",
                new byte[]{1, 2, 3});
        when(taskRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndDeletedFalse(10)).thenReturn(Optional.of(sender));
        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenAnswer(inv -> {
                    ChatMessage m = inv.getArgument(0);
                    m.setId(700);
                    return m;
                });

        ChatMessageDTO dto = service.uploadFile(1, 10, file);

        assertEquals("PHOTO.JPG", dto.getFileName());
        assertTrue(dto.getFileUrl().endsWith(".jpg"));
    }

    @Test
    void uploadFile_emptyFileSkipsRepositoryLookups() {
        MockMultipartFile empty = new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[0]);

        assertThrows(InvalidDataException.class, () -> service.uploadFile(1, 10, empty));
        verify(taskRepository, never()).findByIdAndDeletedFalse(any());
        assertFalse(empty.getOriginalFilename() == null);
    }
}