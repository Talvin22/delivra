package site.delivra.application.repository.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import site.delivra.application.model.entities.User;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import site.delivra.application.model.entities.DeliveryTask;
import site.delivra.application.model.enums.DeliveryTaskStatus;
import site.delivra.application.model.request.task.SearchDeliveryTaskRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class DeliveryTaskSearchCriteria implements Specification<DeliveryTask> {

    private final SearchDeliveryTaskRequest request;

    @Override
    public Predicate toPredicate(
            @NonNull Root<DeliveryTask> root,
            CriteriaQuery<?> query,
            @NonNull CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(request.getAddress())) {
            predicates.add(criteriaBuilder.like(root.get(DeliveryTask.ADDRESS_FIELD), "%" + request.getAddress() + "%"));
        }

        if (Objects.nonNull(request.getStatus())) {
            predicates.add(criteriaBuilder.equal(root.get(DeliveryTask.STATUS_FIELD), DeliveryTaskStatus.valueOf(request.getStatus())));
        }

        if (Objects.nonNull(request.getCreatedBy())) {
            predicates.add(criteriaBuilder.like(root.get(DeliveryTask.CREATED_BY_FIELD), "%" + request.getCreatedBy() + "%"));
        }

        if (Objects.nonNull(request.getDeleted())) {
            predicates.add(criteriaBuilder.equal(root.get(DeliveryTask.DELETED_FIELD), request.getDeleted()));
        }

        if (Objects.nonNull(request.getKeyword())) {
            Predicate keywordPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(root.get(DeliveryTask.ADDRESS_FIELD), "%" + request.getKeyword() + "%"),
                    criteriaBuilder.like(root.get(DeliveryTask.CREATED_BY_FIELD), "%" + request.getKeyword() + "%")
            );
            predicates.add(keywordPredicate);
        }

        sort(root, criteriaBuilder, query);
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private void sort(Root<DeliveryTask> root, CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query) {
        if (Objects.nonNull(request.getSortField())) {
            switch (request.getSortField()) {
                case ADDRESS -> query.orderBy(criteriaBuilder.asc(root.get(DeliveryTask.ADDRESS_FIELD)));
                case STATUS -> query.orderBy(criteriaBuilder.asc(root.get(DeliveryTask.STATUS_FIELD)));
                case CREATED_BY -> query.orderBy(criteriaBuilder.asc(root.get(DeliveryTask.CREATED_BY_FIELD)));
                case DRIVER -> query.orderBy(criteriaBuilder.asc(root.join(DeliveryTask.USER_FIELD).get(User.USERNAME_NAME_FIELD)));
                default -> query.orderBy(criteriaBuilder.desc(root.get(DeliveryTask.ID_FIELD)));
            }
        } else {
            query.orderBy(criteriaBuilder.desc(root.get(DeliveryTask.ID_FIELD)));
        }
    }
}