package known.my.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserInfoVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatarUrl;
    private String role;
    private LocalDateTime createdAt;
}
