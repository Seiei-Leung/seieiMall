package top.seiei.mall.dao;

import org.apache.ibatis.annotations.Param;
import top.seiei.mall.bean.Product;

import java.util.List;
import java.util.Set;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKeyWithBLOBs(Product record);

    int updateByPrimaryKey(Product record);

    List<Product> getProductList();

    List<Product> selectProductByNameAndCategoryId(@Param("productName") String productName, @Param("categoryIdList") Set<Integer> categoryIdList);

    List<Product> selectProductByNameAndCategoryIdForPortal(@Param("productName") String productName, @Param("categoryIdList") Set<Integer> categoryIdList);
}