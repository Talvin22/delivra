package site.delivra.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.delivra.application.model.entities.NavigationSession;
import site.delivra.application.model.enums.NavigationSessionStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface NavigationSessionRepository extends JpaRepository<NavigationSession, Integer> {

    Optional<NavigationSession> findByDeliveryTaskIdAndStatus(Integer taskId, NavigationSessionStatus status);

    boolean existsByDeliveryTaskIdAndStatus(Integer taskId, NavigationSessionStatus status);

    @Query("""
            SELECT n FROM NavigationSession n
            WHERE n.deliveryTask.user.id = :driverId
              AND n.status = 'COMPLETED'
            ORDER BY n.endedAt DESC
            """)
    List<NavigationSession> findLastCompletedSessionsForDriver(@Param("driverId") Integer driverId,
                                                               org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COUNT(ns) FROM NavigationSession ns WHERE ns.deliveryTask.company.id = :companyId")
    long countByCompanyId(@Param("companyId") Integer companyId);

    long count();
}
