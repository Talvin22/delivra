package site.delivra.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.delivra.application.model.entities.Company;
import site.delivra.application.model.enums.CompanyStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {

    Optional<Company> findByIdAndDeletedFalse(Integer id);

    List<Company> findAllByDeletedFalse();

    boolean existsByName(String name);

    long countByDeletedFalseAndStatus(CompanyStatus status);

    long countByDeletedFalse();
}
