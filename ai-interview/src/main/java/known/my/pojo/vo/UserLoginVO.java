package known.my.pojo.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLoginVO {
    private String token;

    private String tokenType;

    private String expiresIn;

    private Integer userId;

    private String username;

    private String nickname;
}
