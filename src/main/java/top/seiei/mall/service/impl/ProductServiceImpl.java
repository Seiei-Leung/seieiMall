package top.seiei.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import top.seiei.mall.bean.Category;
import top.seiei.mall.bean.Product;
import top.seiei.mall.common.Const;
import top.seiei.mall.common.ResponseCode;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.dao.CategoryMapper;
import top.seiei.mall.dao.ProductMapper;
import top.seiei.mall.service.ICategoryService;
import top.seiei.mall.service.IProductService;
import top.seiei.mall.util.PropertiesUtil;
import top.seiei.mall.vo.ProductDetailVo;
import top.seiei.mall.vo.ProductListVo;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private ICategoryService iCategoryService;

    /**
     * 后台新增或修改商品信息
     * @param product 商品信息
     * @return 响应对象
     */
    public ServerResponse<String> saveProduct(Product product) {
        if (product.getCategoryId() == null || product.getName() == null) {
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
     * 后台修改商品状态
     * @param productId 商品 ID
     * @param status 商品状态
     * @return 响应对象
     */
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return  ServerResponse.createdByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "商品参数错误");
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int resultCount = productMapper.updateByPrimaryKeySelective(product);
        if (resultCount > 0) {
            return ServerResponse.createdBySucessMessage("更新产品状态成功");
        }
        return ServerResponse.createdByErrorMessage("更新产品状态失败");
    }

    /**
     * 后台获取商品详情信息
     * @param productId 商品 ID
     * @return 响应对象
     */
    public ServerResponse<ProductDetailVo> getDetailByManage(Integer productId) {
        if (productId == null) {
            return  ServerResponse.createdByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "查询商品参数错误");
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return  ServerResponse.createdByErrorMessage("商品不存在，已下架或删除");
        }
        ProductDetailVo productDetailVo = productToProductDetailVo(product);
        return ServerResponse.createdBySuccess(productDetailVo);
    }

    /**
     * 后台获取商品列表（分页显示）
     * @param pageIndex 结果列表初始页
     * @param pageSize 结果列表一页的容量
     * @return 响应对象
     */
    public ServerResponse<PageInfo> getListByManage(Integer pageIndex, Integer pageSize) {
        // 使用 pageHepler 规则
        // 1、使用 pageStart
        PageHelper.startPage(pageIndex, pageSize);

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
     * 后台根据商品名称（模糊查询）或商品 ID，获取商品列表
     * @param productName 商品名称（模糊搜索）
     * @param categoryId 商品分类 ID
     * @param pageIndex 结果列表初始页
     * @param pageSize 结果列表一页的容量
     * @return 响应对象
     */
    public ServerResponse<PageInfo> searchProductByManage(String productName, Integer categoryId, Integer pageIndex, Integer pageSize) {
        // 获取父节点及其子节点的所有分类集合
        ServerResponse<Set<Integer>> categoryResponse = iCategoryService.getChildrenCategoryCodeByParentId(categoryId);
        Set<Integer> categoryList = null;
        if (categoryResponse.isSuccess()) {
            categoryList = categoryResponse.getData();
        }
        PageHelper.startPage(pageIndex, pageSize);
        List<Product> productList = productMapper.selectProductByNameAndCategoryId(productName, categoryList);
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
     * 前台获取商品详情
     * @param productId 商品 ID
     * @return 响应对象
     */
    public ServerResponse<ProductDetailVo> getDetailByPortal(Integer productId) {
        if (productId == null) {
            return  ServerResponse.createdByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "查询商品参数错误");
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null || product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return  ServerResponse.createdByErrorMessage("商品不存在，已下架或删除");
        }
        ProductDetailVo productDetailVo = productToProductDetailVo(product);
        return ServerResponse.createdBySuccess(productDetailVo);

    }

    /**
     * 前台根据商品名关键字或商品类获取商品列表
     * @param productName 商品名
     * @param categoryId 商品类
     * @param orderby 顺序
     * @param pageIndex 结果列表初始页
     * @param pageSize 结果列表一页的容量
     * @return 响应对象
     */
    public ServerResponse<PageInfo> searchProductByPortal(String productName, Integer categoryId, String orderby, Integer pageIndex, Integer pageSize) {
        // 这里不能直接使用后台那个搜索产品接口，因为在前台我们不应该把所有的产品都进行返回，这里应该返回一个空集
        if (StringUtils.isBlank(productName) && categoryId == null) {
            PageHelper.startPage(pageIndex, pageSize);
            List<ProductListVo> productListVoList = new ArrayList<>();
            PageInfo pageInfo = new PageInfo(productListVoList);
            return ServerResponse.createdBySuccess(pageInfo);
        }
        // 获取父节点及其子节点的所有分类集合 Set
        ServerResponse<Set<Integer>> categoryResponse = iCategoryService.getChildrenCategoryCodeByParentId(categoryId);
        Set<Integer> categoryList = null;
        if (categoryResponse.isSuccess()) {
            categoryList = categoryResponse.getData();
        }
        // 初始化 PageHelper
        PageHelper.startPage(pageIndex, pageSize);
        // 如果有排序条件，设置 PageHelper 排序条件
        // pageHelper 使用排序就是调用 orderBy 方法，传入的字符串，其格式如："price asc"，一个字符串为字段名称紧接空格后添加升序 "asc" 或 降序"desc"
        if (StringUtils.isNotBlank(orderby)) {
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderby)) {
                String[] orderByArray = orderby.split("_");
                PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
            }
        }
        // 这里使用的 sql 语句与后台使用的 sql 语句不同，这里的具有筛选商品状态是否在线功能
        List<Product> productList = productMapper.selectProductByNameAndCategoryIdForPortal(productName, categoryList);
        PageInfo pageInfo = new PageInfo(productList);
        // product 转为 ProductDetailVo
        List<ProductListVo> productListVoList = new ArrayList<>();
        for (Product item : productList) {
            productListVoList.add(productToProductListVo(item));
        }
        // 替换 pageinfo 中原先的 List
        pageInfo.setList(productListVoList);
        return ServerResponse.createdBySuccess(pageInfo);
    }

    /**
     * 数据库映射对象 product 转为 ProductDetailVo，用于显示，主要在其内部再添加了父节点以及图片url的前缀名称
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
