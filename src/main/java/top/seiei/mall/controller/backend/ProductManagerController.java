package top.seiei.mall.controller.backend;

import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import top.seiei.mall.bean.Product;
import top.seiei.mall.bean.User;
import top.seiei.mall.common.Const;
import top.seiei.mall.common.ResponseCode;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.service.IFileService;
import top.seiei.mall.service.IProductService;
import top.seiei.mall.service.IUserService;
import top.seiei.mall.util.PropertiesUtil;
import top.seiei.mall.vo.ProductDetailVo;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/manage/product/")
public class ProductManagerController {

    @Resource
    private IUserService iUserService;

    @Resource
    private IProductService iProductService;

    @Resource
    private IFileService iFileService;

    /**
     * 新增或修改商品信息
     * @param session session 对象
     * @param product 商品信息
     * @return 响应对象
     */
    @RequestMapping("save_product.do")
    @ResponseBody
    public ServerResponse<String> saveProduct(HttpSession session, Product product) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iProductService.saveProduct(product);
    }

    /**
     * 修改商品状态
     * @param session session 对象
     * @param id 商品 ID
     * @param status 商品状态
     * @return 响应对象
     */
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse<String> setSaleStatus(HttpSession session, Integer id, Integer status) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iProductService.setSaleStatus(id, status);
    }

    /**
     * 获取商品信息
     * @param session session 对象
     * @param id 商品 ID
     * @return 响应对象
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> getDetail(HttpSession session, Integer id) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iProductService.getDetailByManage(id);
    }

    /**
     * 后台获取商品列表（分页显示）
     * @param session session 对象
     * @param pageindex 结果列表初始页
     * @param pagesize 结果列表一页的容量
     * @return 响应对象
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> getList(HttpSession session,
                                            @RequestParam(value="pageindex", defaultValue="1") Integer pageindex,
                                            @RequestParam(value="pagesize", defaultValue="10") Integer pagesize) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iProductService.getListByManage(pageindex, pagesize);
    }

    /**
     * 根据商品名称（模糊查询）或商品 ID，获取商品列表
     * @param session session 对象
     * @param productname 商品名称（模糊搜索）
     * @param productid 商品 ID
     * @param pageindex 结果列表初始页
     * @param pagesize 结果列表一页的容量
     * @return 响应对象
     */
    @RequestMapping("search_product.do")
    @ResponseBody
    public ServerResponse<PageInfo> searchProduct(HttpSession session,
                                                  String productname,
                                                  Integer productid,
                                                  @RequestParam(value="pageindex", defaultValue="1") Integer pageindex,
                                                  @RequestParam(value="pagesize", defaultValue="10") Integer pagesize) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iProductService.searchProductByManage(productname, productid, pageindex, pagesize);
    }

    /**
     * 后台图片上传
     * @param file 图片文件
     * @param resquest request 对象
     * @return 响应对象
     */
    @RequestMapping(value = "upload.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Map<String, String>> upload(MultipartFile file ,HttpServletRequest resquest) {
        // 首先检查是否为管理员
        User user = (User) resquest.getSession().getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }

        // 根据相对路径获取服务器上资源的绝对路径，现在指的是 webapp 目录下 upload 目录
        /* 如 F:\javacodeForIdea\seieiMall\src\main\webapp\upload */
        String path = resquest.getServletContext().getRealPath("upload");
        String fileName = iFileService.upload(file, path);
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + fileName;
        Map<String, String> result = new HashMap<>();
        result.put("fileName", fileName);
        result.put("url", url);
        return ServerResponse.createdBySuccess(result);
    }
}
