package book.standup.com.springboot.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository<Entity Type, PK Type>
public interface UserRepository extends JpaRepository<User, Long> {

    /* Spring Data JPA 사용 시 Repository에서 리턴 타입을 Optional 로 받을 수 있다
       : https://velog.io/@aidenshin/Optional-%EA%B4%80%EB%A0%A8.. */
    Optional<User> findByEmail(String email);
}
