package site.delivra.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.delivra.application.model.dto.chat.ChatMessageDTO;
import site.delivra.application.service.ChatService;
import site.delivra.application.utils.ApiUtils;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatFileController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApiUtils apiUtils;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/tasks/{taskId}/chat/files")
    public ResponseEntity<ChatMessageDTO> uploadFile(
            @PathVariable Integer taskId,
            @RequestParam("file") MultipartFile file) {

        Integer senderId = apiUtils.getUserIdFromAuthentication();
        ChatMessageDTO dto = chatService.uploadFile(taskId, senderId, file);
        messagingTemplate.convertAndSend("/topic/chat/" + taskId, dto);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/chat/files/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir, "chat").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = resolveContentType(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String resolveContentType(String filename) {

        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".pdf"))  return "application/pdf";
        return "application/octet-stream";
    }
}
