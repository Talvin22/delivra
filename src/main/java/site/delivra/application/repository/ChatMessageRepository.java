package site.delivra.application.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.delivra.application.model.entities.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    Page<ChatMessage> findByDeliveryTaskIdAndDeletedFalseOrderByCreatedAsc(Integer taskId, Pageable pageable);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.deliveryTask.id = :taskId AND m.sender.id != :userId AND m.isRead = false AND m.deleted = false")
    void markAllAsRead(Integer taskId, Integer userId);
}
