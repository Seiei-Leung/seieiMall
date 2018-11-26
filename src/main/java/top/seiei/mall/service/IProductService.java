package top.seiei.mall.service;

import top.seiei.mall.bean.Product;
import top.seiei.mall.common.ServerResponse;

public interface IProductService {

    ServerResponse<String> saveProduct(Product product);

    ServerResponse<String> setSaleStatus(Integer id, Integer status);
}
