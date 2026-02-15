package site.delivra.application.repository.criteria;


import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import site.delivra.application.model.entities.User;
import site.delivra.application.model.request.user.UserSearchRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class UserSearchCriteria implements Specification<User> {

    private final UserSearchRequest request;


    @Override
    public Predicate toPredicate(
            @NonNull Root<User> root,
            CriteriaQuery<?> query,
            @NonNull CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(request.getUsername())) {
            predicates.add(criteriaBuilder.like(root.get(User.USERNAME_NAME_FIELD), "%" + request.getUsername() + "%"));
        }

        if (Objects.nonNull(request.getEmail())) {
            predicates.add(criteriaBuilder.like(root.get(User.EMAIL_NAME_FIELD), "%" + request.getEmail() + "%"));
        }

        if (Objects.nonNull(request.getDeleted())) {
            predicates.add(criteriaBuilder.equal(root.get(User.DELETED_FIELD), request.getDeleted()));
        }

        if (Objects.nonNull(request.getKeyword())) {
            Predicate keywordPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(root.get(User.USERNAME_NAME_FIELD), "%" + request.getKeyword() + "%"),
                    criteriaBuilder.like(root.get(User.EMAIL_NAME_FIELD), "%" + request.getKeyword() + "%")
            );
            predicates.add(keywordPredicate);
        }

        sort(root, criteriaBuilder, query);
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));

    }

    private void sort(Root<User> root, CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query) {
        if (Objects.nonNull(request.getUserSortField())) {

            switch (request.getUserSortField()) {
                case USERNAME -> query.orderBy(criteriaBuilder.asc(root.get(User.USERNAME_NAME_FIELD)));
                case EMAIL -> query.orderBy(criteriaBuilder.asc(root.get(User.EMAIL_NAME_FIELD)));
                default -> query.orderBy(criteriaBuilder.desc(root.get(User.ID_FIELD)));
            }
        } else {
            query.orderBy(criteriaBuilder.desc(root.get(User.ID_FIELD)));
        }
    }
}
