package top.seiei.mall.dao;

import org.apache.ibatis.annotations.Param;
import top.seiei.mall.bean.Shipping;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    int deleteByPrimaryKeyAndUserId(@Param("userId") Integer userId, @Param("id") Integer id);

    int updateByShipping(Shipping shipping);

    Shipping selectByUseIdAndShippingId(@Param("userId") Integer userId, @Param("id") Integer id);

    List<Shipping> selectAllByUserId(Integer userId);

}