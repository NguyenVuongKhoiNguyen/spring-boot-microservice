package com.poly.user.repositories;

import com.poly.user.models.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
  List<UserRole> findByUserIdAndDelIfFalse(Long userId);
}
