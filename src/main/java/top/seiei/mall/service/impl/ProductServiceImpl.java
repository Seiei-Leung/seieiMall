package top.seiei.mall.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import top.seiei.mall.bean.Product;
import top.seiei.mall.common.ResponseCode;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.dao.ProductMapper;
import top.seiei.mall.service.IProductService;

import javax.annotation.Resource;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Resource
    private ProductMapper productMapper;
    /**
     * 新增或修改商品信息
     * @param product 商品信息
     * @return 响应对象
     */
    public ServerResponse<String> saveProduct(Product product) {
        if (product == null) {
            return  ServerResponse.createdByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "商品参数错误");
        }
        // 如果主图信息为空，默认子图的第一张为主图
        if (StringUtils.isBlank(product.getMainImage()) && StringUtils.isNotBlank(product.getSubImags())) {
            String[] subImagesList = product.getSubImags().split(",");
            if (subImagesList.length > 0) {
                product.setMainImage(subImagesList[0]);
            }
        }
        if (product.getId() != null) {
            int resultCount = productMapper.updateByPrimaryKeySelective(product);
            if (resultCount > 0) {
                return ServerResponse.createdBySucessMessage("更新产品成功");
            }
            return ServerResponse.createdByErrorMessage("更新产品失败");
        } else {
            int resultCount = productMapper.insert(product);
            if (resultCount > 0) {
                return ServerResponse.createdBySucessMessage("新增产品成功");
            }
            return ServerResponse.createdByErrorMessage("新增产品失败");
        }
    }

    public ServerResponse<String> setSaleStatus(Integer id, Integer status) {
        if (id == null || status == null) {
            return  ServerResponse.createdByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "商品参数错误");
        }
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        int resultCount = productMapper.updateByPrimaryKeySelective(product);
        if (resultCount > 0) {
            return ServerResponse.createdBySucessMessage("更新产品状态成功");
        }
        return ServerResponse.createdByErrorMessage("更新产品状态失败");
    }
}
