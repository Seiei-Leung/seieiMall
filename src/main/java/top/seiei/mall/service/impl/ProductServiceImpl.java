package top.seiei.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import top.seiei.mall.bean.Category;
import top.seiei.mall.bean.Product;
import top.seiei.mall.common.ResponseCode;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.dao.CategoryMapper;
import top.seiei.mall.dao.ProductMapper;
import top.seiei.mall.service.IProductService;
import top.seiei.mall.util.PropertiesUtil;
import top.seiei.mall.vo.ProductDetailVo;
import top.seiei.mall.vo.ProductListVo;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private CategoryMapper categoryMapper;

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

    /**
     * 修改商品状态
     * @param id 商品 ID
     * @param status 商品状态
     * @return 响应对象
     */
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

    /**
     * 获取商品信息
     * @param id 商品 ID
     * @return 响应对象
     */
    public ServerResponse<ProductDetailVo> getDetail(Integer id) {
        if (id == null) {
            return  ServerResponse.createdByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "查询商品参数错误");
        }
        Product product = productMapper.selectByPrimaryKey(id);
        if (product == null) {
            return  ServerResponse.createdByErrorMessage("商品不存在，已下架或删除");
        }
        ProductDetailVo productDetailVo = productToProductDetailVo(product);
        return ServerResponse.createdBySuccess(productDetailVo);
    }

    /**
     * 后台获取商品列表（分页显示）
     * @param pageindex 结果列表初始页
     * @param pagesize 结果列表一页的容量
     * @return 响应对象
     */
    public ServerResponse<PageInfo> getList(Integer pageindex, Integer pagesize) {
        // 使用 pageHepler 规则
        // 1、使用 pageStart
        PageHelper.startPage(pageindex, pagesize);

        // 2、填充 sql 语句
        // 3、初始化 PageInfo
        List<Product> productList = productMapper.getProductList();
        PageInfo pageResult = new PageInfo(productList);

        // productListVoList 取代 product
        List<ProductListVo> productListVoList = new ArrayList<>();
        for (Product item : productList) {
            ProductListVo productListVo = productToProductListVo(item);
            productListVoList.add(productListVo);
        }
        pageResult.setList(productListVoList);
        return ServerResponse.createdBySuccess(pageResult);
    }
    /**
     * 根据商品名称（模糊查询）或商品 ID，获取商品列表
     * @param productname 商品名称（模糊搜索）
     * @param productid 商品 ID
     * @param pageindex 结果列表初始页
     * @param pagesize 结果列表一页的容量
     * @return 响应对象
     */
    public ServerResponse<PageInfo> searchProduct(String productname, Integer productid, Integer pageindex, Integer pagesize) {
        PageHelper.startPage(pageindex, pagesize);
        List<Product> productList = productMapper.selectProductByNameAndId(productname, productid);
        PageInfo pageResult = new PageInfo(productList);
        List<ProductListVo> productListVoList = new ArrayList<>();
        for (Product item : productList) {
            ProductListVo productListVo = productToProductListVo(item);
            productListVoList.add(productListVo);
        }
        pageResult.setList(productListVoList);
        return ServerResponse.createdBySuccess(pageResult);
    }





    /**
     * 数据库映射对象 product 转为 ProductDetailVo，用于显示
     * @param product product 对象
     * @return ProductDetailVo 对象
     */
    private ProductDetailVo productToProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setCreateTime(product.getCreateTime());
        productDetailVo.setUpdateTime(product.getUpdateTime());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setSubImags(product.getSubImags());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setSubtitle(product.getSubtitle());
        // 设置父节点
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null) {
            productDetailVo.setParentCategoryId(0);
        } else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        // 从配置文件中获取 ftp 的 URL
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        return productDetailVo;
    }

    /**
     * Product 转化为 ProductListVo
     * @param product product 对象
     * @return ProductListVo
     */
    public ProductListVo productToProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setName(product.getName());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setStatus(product.getStatus());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setPrice(product.getPrice());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        return  productListVo;
    }

}
