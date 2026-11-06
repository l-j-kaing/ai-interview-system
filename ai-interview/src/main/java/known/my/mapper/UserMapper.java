package known.my.mapper;

import known.my.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import known.my.pojo.dto.UpdateProfileDTO;

@Mapper
public interface UserMapper {
    /**
     * 根据用户名查询用户
     */
    User selectUserByUsername(String username);

    /**
     * 根据用户ID查询用户
     */
    User selectUserById(Long id);

    /**
     * 插入新用户
     */
    void insertNewUser(User user);

    /**
     * 更新用户资料
     */
    int updateProfile(@Param("id") Long id, @Param("profile") UpdateProfileDTO updateProfileDTO);

    /**
     * 更新用户密码
     */
    int updatePassword(User user);
}
