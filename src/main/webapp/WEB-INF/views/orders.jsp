<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>我的订单 - 网上商城</title>
  <link href="${pageContext.request.contextPath}/static/css/bootstrap.min.css" rel="stylesheet">
  <link href="${pageContext.request.contextPath}/static/css/all.min.css" rel="stylesheet">
  <link href="${pageContext.request.contextPath}/static/css/style.css" rel="stylesheet">
</head>
<body>
<!-- 导航栏 -->
<jsp:include page="common/header.jsp"/>
<jsp:include page="common/toast.jsp"/>
<jsp:include page="common/profile-nav.jsp">
  <jsp:param name="selected_item" value="1"/>
</jsp:include>

<div class="container mt-4">
  <div class="row">
    <div class="col-md-10">
      <div class="card">
        <div class="card-header d-flex justify-content-between align-items-center">
          <h5 class="mb-0"><i class="fas fa-shopping-bag me-2"></i>我的订单</h5>
          <form class="d-flex">
            <div class="input-group input-group-sm">
              <input id="startDate" class="form-control" type="date">
              <span class="input-group-text"> - </span>
              <input id="endDate" class="form-control" type="date">
            </div>
          </form>
          <div class="btn-group" role="group">
            <input type="radio" class="btn-check" name="orderStatus" id="all" value="" checked>
            <label class="btn btn-outline-primary" for="all">全部</label>

            <input type="radio" class="btn-check" name="orderStatus" id="pending" value="pending">
            <label class="btn btn-outline-warning" for="pending">待付款</label>

            <input type="radio" class="btn-check" name="orderStatus" id="paid" value="paid">
            <label class="btn btn-outline-success" for="paid">已付款</label>

            <input type="radio" class="btn-check" name="orderStatus" id="shipped" value="shipped">
            <label class="btn btn-outline-info" for="shipped">已发货</label>

            <input type="radio" class="btn-check" name="orderStatus" id="completed" value="completed">
            <label class="btn btn-outline-secondary" for="completed">已完成</label>
          </div>
        </div>
        <div class="card-body">
          <!-- 订单列表 -->
          <div id="orderList">
            <c:forEach var="order" items="${orders}">
              <div class="card order-card mb-3"
                   data-status="${order.status}"
                   data-create-time="<fmt:formatDate value="${order.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/>">
                <div class="card-header">
                  <div class="row align-items-center">
                    <div class="col-md-3">
                      <strong>订单号：</strong>${order.id}
                    </div>
                    <div class="col-md-3">
                      <strong>下单时间：</strong>
                      <fmt:formatDate value="${order.createTime}" pattern="yyyy-MM-dd HH:mm"/>
                    </div>
                    <div class="col-md-3">
                      <strong>订单金额：</strong>
                      <fmt:formatNumber value="${order.totalAmount}" pattern="¥#,##0.00"/>
                    </div>
                    <div class="col-md-3 text-end">
                      <c:choose>
                        <c:when test="${order.status == 'pending'}">
                          <span class="fw-bold text-warning">待支付</span>
                        </c:when>
                        <c:when test="${order.status == 'paid'}">
                          <span class="fw-bold text-success">已支付</span>
                        </c:when>
                        <c:when test="${order.status == 'shipped'}">
                          <span class="fw-bold text-info">已发货</span>
                        </c:when>
                        <c:when test="${order.status == 'completed'}">
                          <span class="fw-bold text-secondary">已完成</span>
                        </c:when>
                        <c:when test="${order.status == 'cancelled'}">
                          <span class="fw-bold text-dark">已取消</span>
                        </c:when>
                      </c:choose>
                    </div>
                  </div>
                </div>
                <div class="card-body">
                  <c:forEach var="item" items="${order.orderItems}" varStatus="status">
                    <jsp:include page="common/order-product-item.jsp">
                      <jsp:param name="name" value="${item.productName}"/>
                      <jsp:param name="image" value="${item.productImage}"/>
                      <jsp:param name="description" value="${item.productDescription}"/>
                      <jsp:param name="quantity" value="${item.quantity}"/>
                      <jsp:param name="price" value="${item.price}"/>
                      <jsp:param name="index" value="${status.index}"/>
                      <jsp:param name="order_id" value="${item.oid}"/>
                      <jsp:param name="status" value="${order.status}"/>
                    </jsp:include>
                  </c:forEach>
                </div>
              </div>
            </c:forEach>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<jsp:include page="common/dependency_js.jsp"/>
<script>
    document.addEventListener('DOMContentLoaded', () => {
        let filterStatus = '';
        let filterStart = undefined;
        let filterEnd = undefined;

        document.querySelectorAll('input[name="orderStatus"]').forEach(radio => {
            radio.addEventListener('change', () => {
                filterStatus = radio.value;
                updateOrders();
            });
        });
        document.querySelector('#startDate').addEventListener('change', () => {
            const startDate = document.querySelector('#startDate').value;
            filterStart = startDate ? new Date(startDate) : undefined;
            document.querySelector('#endDate').min = startDate;
            updateOrders();
        });
        document.querySelector('#endDate').addEventListener('change', () => {
            const endDate = document.querySelector('#endDate').value;
            filterEnd = endDate ? new Date(endDate) : undefined;
            document.querySelector('#startDate').max = endDate;
            updateOrders();
        });

        function updateOrders() {
            document.querySelectorAll('.order-card').forEach(card => {
                const status = card.dataset.status;
                const createTime = new Date(card.dataset.createTime);
                const display =
                    // 状态
                    (filterStatus === '' || status === filterStatus) &&
                    // 起始时间
                    (!filterStart || createTime >= filterStart) &&
                    // 结束时间
                    (!filterEnd || createTime <= filterEnd);
                card.style.display = display ? 'block' : 'none';
            })
        }
    });
</script>
</body>
</html>