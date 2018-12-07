package top.seiei.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import top.seiei.mall.bean.Shipping;
import top.seiei.mall.common.ResponseCode;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.dao.ShippingMapper;
import top.seiei.mall.service.IShippingService;

import javax.annotation.Resource;
import java.util.List;

@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Resource
    private ShippingMapper shippingMapper;

    /**
     * 新增收货地址
     * @param userId 用户 ID
     * @param shipping 地址信息
     * @return 新增地址信息后返回的主键
     */
    public ServerResponse<Integer> add(Integer userId, Shipping shipping) {
        if (shipping == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        }
        shipping.setUserId(userId);
        // 此时 xml 文件中使用了 `useGeneratedKeys="true" keyProperty="id"` 两个属性，用于获取插入后生成的主键
        // 成功插入后，该主键值会注入到 shipping 对象中
        int result = shippingMapper.insert(shipping);
        if (result == 0) {
            return ServerResponse.createdByErrorMessage("新增地址失败");
        }
        return ServerResponse.createdBySuccess(shipping.getId());
    }

    /**
     * 删除收货地址
     * @param userId 用户 ID
     * @param shippingId 地址信息 ID
     * @return 是否删除成功
     */
    public ServerResponse delete(Integer userId, Integer shippingId) {
        if (shippingId == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        }
        int result = shippingMapper.deleteByPrimaryKeyAndUserId(userId, shippingId);
        if (result == 0) {
            return ServerResponse.createdByErrorMessage("删除地址失败");
        }
        return ServerResponse.createdBySucessMessage("删除地址成功");
    }

    /**
     * 更新地址信息
     * @param userId 用户 ID
     * @param shipping 地址信息
     * @return 是否更新成功
     */
    public ServerResponse update(Integer userId, Shipping shipping) {
        if (shipping == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        }
        shipping.setUserId(userId);
        int result = shippingMapper.updateByShipping(shipping);
        if (result == 0) {
            return ServerResponse.createdByErrorMessage("更新地址失败");
        }
        return ServerResponse.createdBySucessMessage("更新地址成功");
    }

    /**
     * 获取一条收货地址详细信息
     * @param userId 用户 ID
     * @param shippingId 收货地址 ID
     * @return 收货地址详细信息
     */
    public ServerResponse<Shipping> selectOne(Integer userId, Integer shippingId) {
        if (shippingId == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        }
        Shipping shipping = shippingMapper.selectByUseIdAndShippingId(userId, shippingId);
        if (shipping == null) {
            return ServerResponse.createdByErrorMessage("查无此地址详细信息");
        }
        return ServerResponse.createdBySuccess(shipping);
    }

    /**
     * 根据用户 ID 获取所有收货地址列表
     * @param userId 用户 ID
     * @param pageIndex 结果列表初始页
     * @param pageSize 结果列表一页的容量
     * @return 所有收货地址列表
     */
    public ServerResponse<PageInfo> selectAll(Integer userId, Integer pageIndex, Integer pageSize) {
        PageHelper.startPage(pageIndex, pageSize);
        List<Shipping> result = shippingMapper.selectAllByUserId(userId);
        PageInfo pageInfo = new PageInfo(result);
        return ServerResponse.createdBySuccess(pageInfo);
    }
}
