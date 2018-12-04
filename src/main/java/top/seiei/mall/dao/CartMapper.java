package top.seiei.mall.dao;

import org.apache.ibatis.annotations.Param;
import top.seiei.mall.bean.Cart;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

    int deleteByUserIdAndProductId(@Param("userId") Integer userId, @Param("productIdList") List<Integer> productIdList);

    List<Cart> selectByUserId(Integer userId);
}