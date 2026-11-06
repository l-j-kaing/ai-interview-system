package known.my.pojo.dto;

import jakarta.validation.constraints.Email;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class UpdateProfileDTO {
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatarUrl;

    @JsonIgnore
    private boolean nicknameProvided;

    @JsonIgnore
    private boolean emailProvided;

    @JsonIgnore
    private boolean avatarUrlProvided;

    public void setNickname(String nickname) {
        this.nickname = nickname;
        this.nicknameProvided = true;
    }

    public void setEmail(String email) {
        this.email = email;
        this.emailProvided = true;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        this.avatarUrlProvided = true;
    }
}
