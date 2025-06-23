<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<div class="col-lg-3 col-md-6 mb-4">
  <div class="card h-100 product-card">
    <a class="card-img-top-wrapper" href="${pageContext.request.contextPath}/product?id=${param.id}">
      <img class="card-img-top" alt="${param.name}"
           src="${pageContext.request.contextPath}/static/images/products/${param.image}">
    </a>
    <div class="card-body d-flex flex-column flex-column-reverse">
      <div class="mt-2 d-grid">
        <c:if test="${param.stock > 0}">
          <a class="btn btn-outline-primary btn-sm" href="${pageContext.request.contextPath}/order/create/${param.id}">
            <i class="fas fa-cart-arrow-down"></i> 立即购买
          </a>
          <c:if test="${not empty sessionScope.user}">
            <button class="btn btn-primary btn-sm mt-2"
                    onclick="addToCart(${param.id})
                        .then(msg => updateCartCount && updateCartCount(msg))
                        .catch(showError)">
              <i class="fas fa-shopping-cart"></i> 添加到购物车
            </button>
          </c:if>
        </c:if>
        <c:if test="${param.stock <= 0}">
          <button class="btn btn-secondary btn-sm text-dark" disabled>
            <i class="fas fa-times"></i> 暂时缺货
          </button>
        </c:if>
      </div>
      <div class="d-flex justify-content-between align-items-center">
          <span class="h5 text-danger mb-0">
              ¥<fmt:formatNumber value="${param.price}" pattern="#,##0.00"/>
          </span>
        <small class="text-muted">
          <c:choose>
            <c:when test="${param.showSales}">销量: ${param.sales}</c:when>
            <c:otherwise>库存: ${param.stock}</c:otherwise>
          </c:choose>
        </small>
      </div>
      <a href="${pageContext.request.contextPath}/product?id=${param.id}" class="h-100">
        <h5 class="card-title product-name">${param.name}</h5>
        <p class="card-text text-muted small product-description">
          <c:choose>
            <c:when test="${fn:length(param.description) > 50}">
              ${fn:substring(param.description, 0, 50)}...
            </c:when>
            <c:otherwise>
              ${param.description}
            </c:otherwise>
          </c:choose>
        </p>
      </a>
    </div>
  </div>
</div>