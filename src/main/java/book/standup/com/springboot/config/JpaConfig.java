package book.standup.com.springboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/* @EnableJpaAuditing : 메임 함수에 있을 경우, @WebMvcTest 에 의해 스캔된다.
 * 스캔되면 안되는 이유 : @EnableJpaAuditing 를 사용하기 위해서는 최소 하나의 @Entity 클래스가 필요하지만, test 환경에서는 설정해줄 이유가 없기 때문 */
@Configuration
@EnableJpaAuditing // JPA Auditing 활성화
public class JpaConfig {
}
