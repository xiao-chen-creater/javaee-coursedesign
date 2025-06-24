package com.shop.controller;

import com.shop.entity.Product;
import com.shop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 首页控制器
 */
@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    /**
     * 首页
     */
    @GetMapping("/")
    public String index(Model model) {
        // 获取热销商品
        List<Product> hotProducts = productService.findHotProducts(8);
        model.addAttribute("hotProducts", hotProducts);

        // 获取最新商品
        List<Product> latestProducts = productService.findLatestProducts(8);
        model.addAttribute("latestProducts", latestProducts);

        return "index";
    }

    /**
     * 商品列表页
     */
    @GetMapping("/products")
    public String products(@RequestParam(value = "page", defaultValue = "1") int page,
                           @RequestParam(value = "size", defaultValue = "12") int size,
                           @RequestParam(value = "keyword", required = false) String keyword,
                           @RequestParam(value = "sort", required = false) String sort,
                           Model model) {

        List<Product> products;
        int totalCount;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // 搜索商品
            products = productService.searchProducts(keyword, sort);
            totalCount = products.size();
            model.addAttribute("keyword", keyword);
        } else {
            // 分页查询所有商品
            products = productService.findByPage(page, size, sort);
            totalCount = productService.getTotalCount();
        }

        // 计算分页信息
        int totalPages = (int) Math.ceil((double) totalCount / size);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("sort", sort);

        return "products";
    }

    /**
     * 商品详情页
     */
    @RequestMapping("/product")
    public String productDetail(@RequestParam("id") int id, Model model) {
        Product product = productService.findById(id);
        if (product == null) {
            return "redirect:/products";
        }

        model.addAttribute("product", product);

        // 推荐相关商品
        List<Product> relatedProducts = productService.findLatestProducts(4);
        model.addAttribute("relatedProducts", relatedProducts);

        return "product-detail";
    }

    @RequestMapping("/error")
    public String error() {
    	return "error";
    }
}