package top.seiei.mall.controller.backend;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import top.seiei.mall.bean.Category;
import top.seiei.mall.bean.User;
import top.seiei.mall.common.Const;
import top.seiei.mall.common.ResponseCode;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.service.ICategoryService;
import top.seiei.mall.service.IUserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/manage/category/")
public class CategoryManagerController {

    @Resource
    private IUserService iUserService;

    @Resource
    private ICategoryService iCategoryService;

    /**
     * 获取当前父节点所有子节点（仅子类，不包括孙类）的信息
     * @param session session 对象
     * @param parentId 父节点
     * @return 响应对象
     */
    @RequestMapping("get_parallel_category.do")
    @ResponseBody
    public ServerResponse<List<Category>> getParallelCategoryByParentId(HttpSession session, @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iCategoryService.getParallelCategoryByParentId(parentId);
    }

    /**
     * 获取当前节点的所有子节点（包括孙类）的信息
     * @param session session 对象
     * @param parentId 父节点
     * @return 响应对象
     */
    @RequestMapping("get_allcategory.do")
    @ResponseBody
    public ServerResponse<List<Map<String, Object>>> getAllCategoryByParentId(HttpSession session, @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iCategoryService.getAllCategoryByParentId(parentId);
    }

    /**
     * 添加商品类节点
     * @param session session 对象
     * @param name 节点名
     * @param parentId 父节点
     * @return 响应对象
     */
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse<String> addCategory(HttpSession session, String name, @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iCategoryService.addCategory(name, parentId);
    }

    /**
     * 修改商品类名
     * @param session session 对象
     * @param name 商品类名
     * @param id 当前修改商品的节点ID
     * @return 响应对象
     */
    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse<String> setCategoryName(HttpSession session, String name, int id) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iCategoryService.setCategoryName(name, id);
    }
}
