package site.delivra.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.delivra.application.model.entities.User;
import site.delivra.application.model.entities.UserToken;
import site.delivra.application.model.enums.TokenType;

import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Integer> {

    Optional<UserToken> findByTokenAndType(String token, TokenType type);

    void deleteAllByUserAndType(User user, TokenType type);
}
