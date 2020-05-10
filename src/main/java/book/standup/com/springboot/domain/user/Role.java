package book.standup.com.springboot.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// enum 사용법 : https://mine-it-record.tistory.com/204 && https://alklid.tistory.com/1003
@Getter
@RequiredArgsConstructor
public enum Role {

    GUEST("ROLE_GUEST", "손님"),
    USER("ROLE_USER", "일반 사용자");

    private final String key;
    private final String title;

}
