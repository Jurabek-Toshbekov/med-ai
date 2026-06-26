package uz.sdg.sos.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.sdg.sos.base.BaseRepository;

import uz.sdg.sos.entity.UserEntity;
import uz.sdg.sos.entity.enums.AccountTypeEnum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsAllByIdAndAccountType(Long id, AccountTypeEnum accountType);
    Optional<UserEntity> findByPhoneNumber(String phone);
    Page<UserEntity>findAllByPhoneNumberContainingIgnoreCase(String phoneNumber, Pageable pageable);
    Page<UserEntity>findAllByLastNameContainingIgnoreCase(String lastName, Pageable pageable);
    Page<UserEntity>findAllByFirstNameContainingIgnoreCase(String lastName, Pageable pageable);
    Page<UserEntity>findAllByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(String firstName, String lastName, Pageable pageable);
    List<UserEntity> findAllByAccountType(AccountTypeEnum accountType);


    int countAllByAccountType(AccountTypeEnum accountType);
    @Query("SELECT COUNT(u) FROM users u WHERE YEAR(u.createdAt) = :year AND MONTH(u.createdAt) = :month")
    int countUsersByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT u.id FROM users u WHERE u.accountType = :accountType")
    List<Long> findUserIdsByAccountType(@Param("accountType") AccountTypeEnum accountType);


}
