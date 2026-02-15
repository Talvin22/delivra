package site.delivra.application.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import site.delivra.application.model.entities.DeliveryTask;

import java.util.Optional;

@Repository
public interface DeliveryTaskRepository extends JpaRepository<DeliveryTask, Integer>, JpaSpecificationExecutor<DeliveryTask> {

    Optional<DeliveryTask> findByIdAndDeletedFalse(Integer id);
    Page<DeliveryTask> findAllByDeletedFalse(Pageable pageable);
}
