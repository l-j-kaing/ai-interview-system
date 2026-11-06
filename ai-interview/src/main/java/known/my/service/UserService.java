package known.my.service;

import known.my.pojo.dto.UpdatePasswordDTO;
import known.my.pojo.dto.UpdateProfileDTO;
import known.my.pojo.dto.UserLoginDTO;
import known.my.pojo.dto.UserRegisterDTO;
import known.my.pojo.entity.User;
import known.my.pojo.vo.UserInfoVO;

public interface UserService {
    //用户登录
    User login(UserLoginDTO userLoginDTO);
    //用户注册
    void register(UserRegisterDTO userRegisterDTO);
    //获取当前登录用户信息
    UserInfoVO getCurrentUser(Long userId);
    //修改个人资料
    UserInfoVO updateProfile(Long userId, UpdateProfileDTO updateProfileDTO);
    //修改密码
    void updatePassword(Long userId, UpdatePasswordDTO updatePasswordDTO);
}
