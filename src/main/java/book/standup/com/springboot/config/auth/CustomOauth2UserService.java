package book.standup.com.springboot.config.auth;

import book.standup.com.springboot.config.auth.dto.OAuthAttributes;
import book.standup.com.springboot.config.auth.dto.SessionUser;
import book.standup.com.springboot.domain.user.User;
import book.standup.com.springboot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Collections;

// 본 클래스 : 구글로그인 이후 가져온 사용자의 정보(email, name, picture 등)들을 기반으로 가입 및 정보수정, 세션 저장 등의 기능을 지원
@RequiredArgsConstructor
@Service
public class CustomOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        /* 현재 로그인 진행 중인 서비스를 구분하는 코드
         * ex) 구글 로그인인지 네이버 로그인인지 구분 */
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        /* OAuth2 로그인 진행 시 키가 되는 필드 값. PK 와 같은 의미.
         * 구글의 경우 기본적으로 코드를 지원하지만, 네이버 카카오 등은 기본지원하지 않는다. 구글의 기본 코드는 "sub" */
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        // OAuth2UserService 를 통해 가져온 OAuth2User 의 attribute 를 담은 클래스 ( DTO )
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
        System.out.println("registrationId >>>"+registrationId+" userNameAttributeName >>>"+userNameAttributeName+" oAuth2User.getAttributes() >>> "+ oAuth2User.getAttributes());

        // User 를 DB 에 저장
        User user = saveOrUpdate(attributes);
        /* session 에 저장할 때, user 객체를 저장하지 않고 SessionUser 클래스를 만들어서 저장한 이유
         * : 직렬화가 필요 & Entity 자체를 직렬화 하지 않기 위한 목적 */
        httpSession.setAttribute("user", new SessionUser(user));

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())), attributes.getAttributes(), attributes.getNameAttributeKey());

    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
                // 기존에 유저가 등록되어 있는 경우 : 이름과 이미지가 변경되었던 되지 않았던, 최신 정보로 수정해준다.
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                /* 기존에 유저가 등록되어 있지 않은 경우 : 새로운 정보들이 셋업 된 User Entity 를 리턴
                 * orElse 참고 : https://itstory.tk/entry/Java-8-OptionalorElse-vs-OptionalorElseGet */
                .orElse(attributes.toEntity());
        return userRepository.save(user);
    }

}
