package top.seiei.mall.service;

import com.github.pagehelper.PageInfo;
import top.seiei.mall.bean.Shipping;
import top.seiei.mall.common.ServerResponse;

public interface IShippingService {

    ServerResponse<Integer> add(Integer userId, Shipping shipping);

    ServerResponse delete(Integer userId, Integer shippingId);

    ServerResponse update(Integer userId, Shipping shipping);

    ServerResponse<Shipping> selectOne(Integer userId, Integer shippingId);

    ServerResponse<PageInfo> selectAll(Integer userId, Integer pageIndex, Integer pageSize);
}
