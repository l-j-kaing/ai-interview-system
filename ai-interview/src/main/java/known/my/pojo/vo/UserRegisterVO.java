package known.my.pojo.vo;

import lombok.Data;

@Data
public class UserRegisterVO {

    private Integer userId;

    private String username;

    private String nickname = null;

    private String createdAt;
}
