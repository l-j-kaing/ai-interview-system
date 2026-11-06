package known.my.pojo.entity;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库 users 表
 */
@Data
@Builder
public class User {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（BCrypt加密后的哈希值）
     */
    private String passwordHash;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 邮箱（选填）
     */
    private String email;

    /**
     * 头像URL（选填）
     */
    private String avatarUrl;

    /**
     * 用户角色：USER、ADMIN
     */
    private String role = "USER";

    /**
     * 用户状态：ENABLED、DISABLED
     */
    private String status = "ENABLED";

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
