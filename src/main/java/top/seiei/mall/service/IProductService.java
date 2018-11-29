package top.seiei.mall.service;

import com.github.pagehelper.PageInfo;
import top.seiei.mall.bean.Product;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.vo.ProductDetailVo;

public interface IProductService {

    ServerResponse<String> saveProduct(Product product);

    ServerResponse<String> setSaleStatus(Integer id, Integer status);

    ServerResponse<ProductDetailVo> getDetail(Integer id);

    ServerResponse<PageInfo> getList(Integer pageindex, Integer pagesize);

    ServerResponse<PageInfo> searchProduct(String productname, Integer productid, Integer pageindex, Integer pagesize);
}
