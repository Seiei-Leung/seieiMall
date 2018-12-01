package top.seiei.mall.service.impl;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import top.seiei.mall.bean.Category;
import top.seiei.mall.common.ResponseCode;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.dao.CategoryMapper;
import top.seiei.mall.service.ICategoryService;

import javax.annotation.Resource;
import java.util.*;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    /**
     * 获取当前父节点所有子节点（仅子类，不包括孙类）的信息
     * @param parentId 父节点
     * @return 响应对象
     */
    public ServerResponse<List<Category>> getParallelCategoryByParentId(int parentId) {
        List<Category> categoryList = categoryMapper.getCategoryByParentId(parentId);
        if (CollectionUtils.isEmpty(categoryList)) {
            return ServerResponse.createdByErrorMessage("未找到该品类下的子品类");
        }
        return ServerResponse.createdBySuccess(categoryList);
    }

    /**
     * 获取当前节点的所有子节点（包括孙类）的信息
     * @param parentId 父节点
     * @return 响应对象
     */
    public ServerResponse<List<Map<String, Object>>> getAllCategoryByParentId(int parentId) {
        ServerResponse<List<Category>> serverResponse = getParallelCategoryByParentId(parentId);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("未找到该品类下的子品类");
        }
        List<Map<String, Object>> resultList = getChildrenCategory(serverResponse.getData(), 0);
        if (CollectionUtils.isEmpty(resultList)) {
            return ServerResponse.createdByErrorMessage("未找到该品类下的子品类");
        }
        return ServerResponse.createdBySuccess(resultList);
    }

    /**
     * 用于迭代获取当前节点的所有子节点（包括孙类）的信息
     * 重点是找到一个“储存变量”可以储存在迭代间储存信息
     * @param categoryList 当前节点的所有的子类（仅子类，不包括孙类）信息列表
     * @param lev 表示当前层数，这里并没有设置到响应对象中返回
     * @return 当前节点的所有子节点（包括孙类）的信息
     */
    private List<Map<String, Object>> getChildrenCategory(List<Category> categoryList, int lev) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Category item : categoryList) {
            int nextId = item.getId();
            int level = lev + 1;
            Map<String, Object> categoryItem = new HashMap<>();
            categoryItem.put("id", item.getId());
            categoryItem.put("parentId", item.getParentId());
            categoryItem.put("data", item);
            List<Category> categoryListTemp = categoryMapper.getCategoryByParentId(nextId);
            categoryItem.put("children", getChildrenCategory(categoryListTemp, level));
            list.add(categoryItem);
        }
        return list;
    }

    /**
     * 获取当前节点的所有子节点（包括孙类）的节点 ID，以数组的形式表示
     * @param parentId 父节点索引
     * @return 由节点 ID 组成的数组构成的响应对象
     */
    public ServerResponse<Set<Integer>> getChildrenCategoryCodeByParentId(Integer parentId) {
        ServerResponse<List<Category>> serverResponse = getParallelCategoryByParentId(parentId);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("未找到该品类下的子品类");
        }
        Set<Integer> resultList = getChildrenCategoryCode(serverResponse.getData());
        resultList.add(parentId);
        if (CollectionUtils.isEmpty(resultList)) {
            return ServerResponse.createdByErrorMessage("未找到该品类下的子品类");
        }
        return ServerResponse.createdBySuccess(resultList);
    }

    /**
     * 迭代获取当前节点的所有子节点（包括孙类）的节点 ID
     * @param categoryList 当前节点的所有的子类（仅子类，不包括孙类）信息列表
     * @return 由节点 ID 组成的数组
     */
    private Set<Integer> getChildrenCategoryCode(List<Category> categoryList) {
        Set<Integer> resultList = new HashSet<>();
        for (Category categoryItem : categoryList) {
            resultList.add(categoryItem.getId());
            Set<Integer> categoryListTemp = getChildrenCategoryCode(categoryMapper.getCategoryByParentId(categoryItem.getId()));
            for (Integer item : categoryListTemp) {
                resultList.add(item);
            }
        }
        return resultList;
    }



    /**
     * 添加商品类节点
     * @param name 节点名
     * @param parentId 父节点
     * @return 响应对象
     */
    public ServerResponse<String> addCategory(String name, int parentId) {
        if (StringUtils.isBlank(name)) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "添加品类参数错误");
        }
        Category category = new Category();
        category.setName(name);
        category.setStatus(true);
        category.setParentId(parentId);
        int resultCount = categoryMapper.insert(category);
        if (resultCount > 0) {
            return ServerResponse.createdBySucessMessage("添加品类成功");
        }
        return ServerResponse.createdByErrorMessage("添加品类失败");
    }

    /**
     * 修改商品类名
     * @param name 商品类名
     * @param id 当前修改商品的节点ID
     * @return 响应对象
     */
    public ServerResponse<String> setCategoryName(String name, Integer id) {
        if (id == null || StringUtils.isBlank(name)) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "更新品类参数错误");
        }
        Category category = new Category();
        category.setName(name);
        category.setId(id);
        int resultCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (resultCount > 0) {
            return ServerResponse.createdBySucessMessage("更新品类成功");
        }
        return ServerResponse.createdByErrorMessage("更新品类失败");
    }
}
