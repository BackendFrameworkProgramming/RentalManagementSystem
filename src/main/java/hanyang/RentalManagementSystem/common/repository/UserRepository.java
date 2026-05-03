package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findAllByIsDeletedFalse(Pageable pageable);
    Optional<User> findByIdAndIsDeletedFalse(Long id);
}
