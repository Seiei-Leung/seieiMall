package top.seiei.mall.dao;

import org.apache.ibatis.annotations.Param;
import top.seiei.mall.bean.OrderItem;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    List<OrderItem> selectByOrderNo(Long orderNo);

    int batchInsert(@Param("orderItemList") List<OrderItem> orderItemList);

    int deleteByOrderNo(Long orderNo);
}