package com.example.skillswap.repository;

import com.example.skillswap.entity.User;
import com.example.skillswap.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    @Query("""
            select u from User u
            where (:status is null or u.accountStatus = :status)
              and (
                :search is null
                or lower(u.firstName) like lower(concat('%', :search, '%'))
                or lower(u.lastName) like lower(concat('%', :search, '%'))
                or lower(u.email) like lower(concat('%', :search, '%'))
              )
            """)
    Page<User> searchUsers(
            @Param("search") String search,
            @Param("status") AccountStatus status,
            Pageable pageable
    );
}
