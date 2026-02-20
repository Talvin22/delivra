package site.delivra.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.delivra.application.model.entities.NavigationSession;
import site.delivra.application.model.enums.NavigationSessionStatus;

import java.util.Optional;

@Repository
public interface NavigationSessionRepository extends JpaRepository<NavigationSession, Integer> {

    Optional<NavigationSession> findByDeliveryTaskIdAndStatus(Integer taskId, NavigationSessionStatus status);

    boolean existsByDeliveryTaskIdAndStatus(Integer taskId, NavigationSessionStatus status);
}
