package top.seiei.mall.dao;

import org.apache.ibatis.annotations.Param;
import top.seiei.mall.bean.User;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkByUserName(String username);

    int checkByEmail(String email);

    // mybatis 只能传入一个参数，所以当要传入两个参数时就要封装成一个对象作为参数
    // 这里使用 @Param 注释，将参数转化为 Map 类型，所以在 mapper.xml 中的 ParameterType 需要传入 Map
    User selectForLogin(@Param("username") String username,@Param("password") String password);

    String selectQuestionByUserName(String username);

    int checkByQuestion(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);

    int updatePassword(String username, String newpassword);
}