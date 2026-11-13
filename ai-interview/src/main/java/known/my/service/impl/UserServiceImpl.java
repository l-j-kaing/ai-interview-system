package known.my.service.impl;

import known.my.common.exception.BaseException;
import known.my.common.result.ResultCode;
import known.my.mapper.UserMapper;
import known.my.pojo.dto.UserLoginDTO;
import known.my.pojo.dto.UserRegisterDTO;
import known.my.pojo.dto.UpdatePasswordDTO;
import known.my.pojo.dto.UpdateProfileDTO;
import known.my.pojo.entity.User;
import known.my.pojo.vo.UserInfoVO;
import known.my.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    /**
     * 用户登录
     */
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUsername();
        //进行md5加密
        String password = DigestUtils.md5DigestAsHex(userLoginDTO.getPassword().getBytes());
        //查询
        User user = userMapper.selectUserByUsername(username);

        if(user == null){
        //用户不存在
            throw new BaseException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if(!password.equals(user.getPasswordHash())){
            //密码错误
            throw new BaseException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if(user.getStatus().equals("DISABLED")){
            throw new BaseException(ResultCode.FORBIDDEN, "账号已禁用");
        }
        return user;
    }
    /**
     * 用户注册
     */
    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        // 创建用户对象
        User user = User.builder()
        .username(userRegisterDTO.getUsername())
        .passwordHash(DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes()))
        .nickname(userRegisterDTO.getNickname())
        .email(userRegisterDTO.getEmail())
        .createTime(LocalDateTime.now())
        .build();
        userMapper.insertNewUser(user);
    }

    /**
     * 获取当前登录用户信息
     */
    @Override
    public UserInfoVO getCurrentUser(Long userId) {
        return toUserInfoVO(getUserById(userId));
    }

    /**
     * 修改个人资料
     */
    @Override
    public UserInfoVO updateProfile(Long userId, UpdateProfileDTO updateProfileDTO) {
        if (!updateProfileDTO.isNicknameProvided()
                && !updateProfileDTO.isEmailProvided()
                && !updateProfileDTO.isAvatarUrlProvided()) {
            throw new BaseException(ResultCode.PARAM_ERROR, "请至少提供一个需要修改的资料字段");
        }

        getUserById(userId);
        userMapper.updateProfile(userId, updateProfileDTO);
        return toUserInfoVO(getUserById(userId));
    }

    /**
     * 修改密码
     */
    @Override
    public void updatePassword(Long userId, UpdatePasswordDTO updatePasswordDTO) {
        User user = getUserById(userId);
        String oldPasswordHash = DigestUtils.md5DigestAsHex(updatePasswordDTO.getOldPassword().getBytes());
        if (!oldPasswordHash.equals(user.getPasswordHash())) {
            throw new BaseException(ResultCode.PARAM_ERROR, "原密码错误");
        }

        user.setPasswordHash(DigestUtils.md5DigestAsHex(updatePasswordDTO.getNewPassword().getBytes()));
        userMapper.updatePassword(user);
    }

    private User getUserById(Long userId) {
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new BaseException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private UserInfoVO toUserInfoVO(User user) {
        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .createdAt(user.getCreateTime())
                .build();
    }
}
