package known.my.controller;

import jakarta.validation.Valid;
import known.my.common.constant.JwtClaimsConstant;
import known.my.common.context.BaseContext;
import known.my.common.properties.JwtProperties;
import known.my.common.result.Result;
import known.my.common.utils.JwtUtil;
import known.my.pojo.dto.UpdatePasswordDTO;
import known.my.pojo.dto.UpdateProfileDTO;
import known.my.pojo.dto.UserLoginDTO;
import known.my.pojo.dto.UserRegisterDTO;
import known.my.pojo.entity.User;
import known.my.pojo.vo.UserInfoVO;
import known.my.pojo.vo.UserLoginVO;
import known.my.pojo.vo.UserRegisterVO;
import known.my.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Slf4j

public class UserController {
    private static final String TOKEN_TYPE = "Bearer";
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserRegisterVO> register(@RequestBody @Valid UserRegisterDTO userRegisterDTO) {
        log.info("用户注册：{}", userRegisterDTO.getUsername());
        userService.register(userRegisterDTO);
        return Result.success();
    }
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody @Valid UserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO.getUsername());
        User user =  userService.login(userLoginDTO);
        //创建token
        Map<String, Object> claim = new HashMap<>();
        claim.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claim);
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .token(token)
                .userId(user.getId().intValue())
                .username(user.getUsername())
                .expiresIn(String.valueOf(jwtProperties.getUserTtl()))
                .tokenType(TOKEN_TYPE)
                .nickname(user.getNickname())
                .build();
        return Result.success(userLoginVO);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public Result<UserInfoVO> getCurrentUser() {
        log.info("获取当前登录用户信息：{}", BaseContext.getCurrentId());
        return Result.success(userService.getCurrentUser(BaseContext.getCurrentId()));
    }

    /**
     * 修改个人资料
     */
    @PutMapping("/profile")
    public Result<UserInfoVO> updateProfile(@RequestBody @Valid UpdateProfileDTO updateProfileDTO) {
        log.info("修改个人资料：{}", BaseContext.getCurrentId());
        return Result.success(userService.updateProfile(BaseContext.getCurrentId(), updateProfileDTO));
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody @Valid UpdatePasswordDTO updatePasswordDTO) {
        log.info("修改密码：{}", BaseContext.getCurrentId());
        userService.updatePassword(BaseContext.getCurrentId(), updatePasswordDTO);
        return Result.success();
    }
}
