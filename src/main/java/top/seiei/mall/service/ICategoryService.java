package top.seiei.mall.service;

import top.seiei.mall.bean.Category;
import top.seiei.mall.common.ServerResponse;

import java.util.List;
import java.util.Map;

public interface ICategoryService {

    ServerResponse<List<Category>> getParallelCategoryByParentId(int parentId);

    ServerResponse<List<Map<String, Object>>> getAllCategoryByParentId(int parentId);

    ServerResponse<String> addCategory(String name, int parentId);

    ServerResponse<String> setCategoryName(String name, Integer id);
}
