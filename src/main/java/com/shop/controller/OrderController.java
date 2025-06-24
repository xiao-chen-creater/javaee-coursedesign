package com.shop.controller;

import com.shop.entity.Cart;
import com.shop.entity.Order;
import com.shop.entity.User;
import com.shop.service.CartService;
import com.shop.service.OrderService;
import com.shop.util.Either;
import com.shop.util.JsonResult;
import com.shop.util.OrderStatusRequest;
import com.shop.util.StringUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.List;
import java.util.Random;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private CartService cartService;

    private final Random random = new Random(System.currentTimeMillis());

    /**
     * 我的订单页面
     */
    @GetMapping("/orders")
    public String orders(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        List<Order> orders = orderService.findByUserId(user.getId());
        model.addAttribute("orders", orders);
        return "orders";
    }

    /**
     * 订单详情页面
     */
    @GetMapping("/detail/{id}")
    public String orderDetail(@PathVariable("id") int id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        Order order = orderService.findById(id);
        if (order == null || order.getUserId() != user.getId()) {
            return "redirect:/order/orders";
        }

        model.addAttribute("order", order);
        return "order-detail";
    }

    /**
     * 结算页面
     */
    @GetMapping("/checkout")
    public String checkout(@RequestParam("productIds") List<Integer> productIds, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        if (productIds == null || productIds.isEmpty()) {
            return "redirect:/cart";
        }

        List<Cart> carts = cartService.findByUserId(user.getId()).stream()
                .filter(cart -> productIds.contains(cart.getProductId()))
                .toList();
        model.addAttribute("carts", carts);
        model.addAttribute("subtotal", carts.stream()
                .map(cart -> cart.getProduct().getPrice().multiply(new BigDecimal(cart.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP)
                .toString());
        return "checkout";
    }

    /**
     * 创建订单
     */
    @ResponseBody
    @PostMapping("/create")
    public JsonResult<Order> create(@RequestBody List<Integer> productIds, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return JsonResult.error("请先登录");
        }

        if (productIds == null || productIds.isEmpty()) {
            return JsonResult.error("请选择要购买的商品");
        }

        String address = user.getAddress();
        if (StringUtils.isEmpty(address)) {
            return JsonResult.error("请选择地址");
        }

        // 创建订单
        Either<Order> orderResult = orderService.createOrderFromCart(user, productIds);
        if (!orderResult.isSuccess()) {
            return JsonResult.error(orderResult.error());
        }

        return simulateOrderPayment(orderResult.result());
    }

    /**
     * 直接创建订单，不删除购物车
     */
    @GetMapping("/create/{productId}")
    public String createDirectly(@PathVariable("productId") int productId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login?redirect=/order/create/" + productId;
        }

        if (!StringUtils.isPhone(user.getPhone())) {
            return "redirect:/error?message=" + URLEncoder.encode("请先设置手机号");
        }

        if (StringUtils.isEmpty(user.getAddress())) {
            return "redirect:/error?message=" + URLEncoder.encode("请先设置地址");
        }

        // 创建订单
        Either<Order> orderResult = orderService.createOrderFromProduct(user, productId);
        if (!orderResult.isSuccess()) {
            return "redirect:/error?message=" + URLEncoder.encode(orderResult.error());
        }

        return "redirect:/order/detail/" + orderResult.result().getId();
    }

    /**
     * 模拟支付
     */
    private JsonResult<Order> simulateOrderPayment(Order order) {
        try {
            Thread.sleep(1000 + random.nextInt(2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean paymentSuccess = random.nextInt(100) < 80;

        if (paymentSuccess) {
            if (orderService.updateStatus(order.getId(), Order.STATUS_PAID)) {
                return JsonResult.success(order);
            } else {
                return JsonResult.error("支付失败");
            }
        } else {
            return JsonResult.error("支付失败");
        }
    }

    /**
     * 更新订单状态
     */
    @PostMapping("/status")
    @ResponseBody
    public JsonResult<Void> updateStatus(@RequestBody OrderStatusRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return JsonResult.error("请先登录");
        }

        int id = request.getId();
        String status = request.getStatus();

        // 验证订单是否属于当前用户
        Order order = orderService.findById(id);
        if (order == null || order.getUserId() != user.getId()) {
            return JsonResult.error("订单不存在");
        }

        if (orderService.updateStatus(id, status)) {
            return JsonResult.success();
        } else {
            return JsonResult.error("更新失败");
        }
    }

    /**
     * 根据状态筛选订单
     */
    @GetMapping("/status/{status}")
    @ResponseBody
    public JsonResult<List<Order>> filterOrders(@PathVariable("status") String status, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return JsonResult.error("请先登录");
        }

        try {
            return JsonResult.success(orderService.findByUserIdAndStatus(user.getId(), status));
        } catch (Exception e) {
            return JsonResult.error("获取失败", List.of());
        }
    }
}
