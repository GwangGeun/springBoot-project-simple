package book.standup.com.springboot.config.auth;

import book.standup.com.springboot.domain.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@RequiredArgsConstructor
// Spring Security 설정들을 활성화 시켜준다.
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomOauth2UserService customOauth2UserService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.
                // h2-console 화면을 사용하기 위해 해당 옵션들을 disable
                csrf().disable().
                headers().frameOptions().disable()
                .and()
                /* URL 별 권한 관리를 설정하는 옵션의 시작점
                 * authorizeRequests 가 선언되어야만 antMatchers 옵션을 사용할 수 있다. */
                .authorizeRequests()
                /*  antMatchers
                 *  - 권한 관리 대상을 지정하는 옵션
                 *  - URL, HTTP 메소드별로 관리가 가능하다.
                 *  - "/" 등 지정된 URL 들은 permitAll() 옵션을 통해 전체 열람 권한을 부여
                 *  - "/api/v1/**" 주소를 가진 API 는 USER 권한을 가진 사람만 접근 가능하도록 설정
                 *  */
                .antMatchers("/", "/css/**", "/images/**", "/js/**", "/h2-console/**").permitAll()
                // enum 의 name method : https://www.tutorialspoint.com/java/lang/enum_name.htm
                .antMatchers("/api/v1/**").hasRole(Role.USER.name())
                /* anyRequest
                 * - 설정된 값들 이외 나머지 URL 들을 의미 */
                .anyRequest().authenticated()
                .and()
                /* 로그아웃 기능에 대한 여러 설정의 진입점
                 * 로그아웃 성공 시 / 주소로 이동한다. */
                .logout()
                .logoutSuccessUrl("/")
                .permitAll()
                .and()
                // OAuth2 로그인 기능에 대한 여러 설정의 진입점
                .oauth2Login()
                // OAuth2 로그인 성공 이후 사용자 정보를 가져올 때의 설정들을 담당
                .userInfoEndpoint()
                /* 소셜 로그인 성공 시 후속 조치를 진행할 UserSevice 인터페이스의 구현체를 등록
                 * 리소스 서버(즉, 소셜 서비스들)에서 사용자 정보를 가져온 상태에서 추가로 진행하고자 하는 기능을 명시할 수 있다. */
                .userService(customOauth2UserService);

    }
}
