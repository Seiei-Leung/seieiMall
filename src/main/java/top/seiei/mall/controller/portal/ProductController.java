package top.seiei.mall.controller.portal;

import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.service.IProductService;
import top.seiei.mall.vo.ProductDetailVo;

import javax.annotation.Resource;


/**
 * 前台商品管理
 */
@Controller
@RequestMapping("/product/")
public class ProductController {

    @Resource
    private IProductService iProductService;

    /**
     * 前台获取商品详情
     * @param productid 商品 ID
     * @return 响应对象
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> getDetail(Integer productid) {
        return iProductService.getDetailByPortal(productid);
    }

    /**
     * 前台根据商品名称关键字或商品分类获取商品列表
     * @param productname 商品名
     * @param categoryid 商品分类
     * @param oderby 排序
     * @param pageindex 结果列表初始页
     * @param pagesize 结果列表一页的容量
     * @return 响应对象
     */
    @RequestMapping("search_product.do")
    @ResponseBody
    public ServerResponse<PageInfo> searchProduct(@RequestParam(value = "productname", required = false) String productname,
                                                  @RequestParam(value = "categoryid", required = false)Integer categoryid,
                                                  @RequestParam(value = "oderby", required = false)String oderby,
                                                  @RequestParam(value="pageindex", defaultValue="1") Integer pageindex,
                                                  @RequestParam(value="pagesize", defaultValue="10") Integer pagesize) {
        return iProductService.searchProductByPortal(productname, categoryid, oderby, pageindex, pagesize);
    }

}
