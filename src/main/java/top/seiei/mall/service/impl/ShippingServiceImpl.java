package top.seiei.mall.service.impl;

import net.sf.jsqlparser.schema.Server;
import org.springframework.stereotype.Service;
import top.seiei.mall.bean.Shipping;
import top.seiei.mall.common.ResponseCode;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.dao.ShippingMapper;
import top.seiei.mall.service.IShippingService;

import javax.annotation.Resource;

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
}
