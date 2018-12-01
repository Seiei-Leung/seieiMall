package top.seiei.mall.service;

import com.github.pagehelper.PageInfo;
import top.seiei.mall.bean.Product;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.vo.ProductDetailVo;

public interface IProductService {

    ServerResponse<String> saveProduct(Product product);

    ServerResponse<String> setSaleStatus(Integer productId, Integer status);

    ServerResponse<ProductDetailVo> getDetailByManage(Integer productId);

    ServerResponse<PageInfo> getListByManage(Integer pageIndex, Integer pageSize);

    ServerResponse<PageInfo> searchProductByManage(String productName, Integer categoryId, Integer pageIndex, Integer pageSize);

    ServerResponse<ProductDetailVo> getDetailByPortal(Integer productId);

    ServerResponse<PageInfo> searchProductByPortal(String productName, Integer categoryId, String orderby, Integer pageIndex, Integer pageSize);
}
