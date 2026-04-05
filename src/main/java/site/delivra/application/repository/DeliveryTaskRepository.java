package site.delivra.application.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.delivra.application.model.entities.DeliveryTask;
import site.delivra.application.model.enums.DeliveryTaskStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryTaskRepository extends JpaRepository<DeliveryTask, Integer>, JpaSpecificationExecutor<DeliveryTask> {

    Optional<DeliveryTask> findByIdAndDeletedFalse(Integer id);

    Page<DeliveryTask> findAllByDeletedFalse(Pageable pageable);

    @Query("SELECT t.status, COUNT(t) FROM DeliveryTask t WHERE t.user.id = :driverId AND t.deleted = false GROUP BY t.status")
    List<Object[]> countTasksByStatusForDriver(@Param("driverId") Integer driverId);

    @Query("SELECT COUNT(t) FROM DeliveryTask t WHERE t.user.id = :driverId AND t.status = :status AND t.deleted = false")
    long countPendingTasksForDriver(@Param("driverId") Integer driverId, @Param("status") DeliveryTaskStatus status);

    @Query("SELECT COUNT(t) FROM DeliveryTask t WHERE t.user.id = :driverId AND t.status IN :statuses AND t.deleted = false")
    long countActiveTasksForDriver(@Param("driverId") Integer driverId, @Param("statuses") List<DeliveryTaskStatus> statuses);

    List<DeliveryTask> findAllByDeletedFalseAndCreatedAfter(LocalDateTime since, Sort sort);

    List<DeliveryTask> findAllByDeletedFalseAndCompany_IdAndCreatedAfter(Integer companyId, LocalDateTime since, Sort sort);

    Page<DeliveryTask> findAllByDeletedFalseAndCompany_Id(Integer companyId, Pageable pageable);

    long countByDeletedFalseAndCompany_Id(Integer companyId);

    long countByDeletedFalseAndCompany_IdAndStatus(Integer companyId, DeliveryTaskStatus status);

    long countByDeletedFalse();

    long countByDeletedFalseAndStatus(DeliveryTaskStatus status);
}
