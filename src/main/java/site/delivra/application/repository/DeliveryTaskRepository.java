package site.delivra.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.delivra.application.model.entities.DeliveryTask;

@Repository
public interface DeliveryTaskRepository extends JpaRepository<DeliveryTask, Integer> {
}
