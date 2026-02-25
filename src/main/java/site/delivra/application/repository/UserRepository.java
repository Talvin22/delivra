package site.delivra.application.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import site.delivra.application.model.entities.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> , JpaSpecificationExecutor<User> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Integer id);

    Page<User> findByDeletedFalse(Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Integer id);

    Optional<User> findByIdAndDeletedFalse(Integer id);

    Optional<User> findByEmailAndDeletedFalse(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
}
