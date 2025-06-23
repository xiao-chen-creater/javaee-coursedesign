<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>错误 - 网上商城</title>
  <link href="${pageContext.request.contextPath}/static/css/bootstrap.min.css" rel="stylesheet">
  <link href="${pageContext.request.contextPath}/static/css/all.min.css" rel="stylesheet">
  <link href="${pageContext.request.contextPath}/static/css/style.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="common/toast.jsp"/>
<jsp:include page="common/header.jsp"/>

<div class="container">
  <div class="row justify-content-center mt-5">
    <div class="col-md-6 col-lg-4">
      <div class="card shadow">
        <div class="card-header bg-danger text-white text-center">
          <h4><i class="fas fa-triangle-exclamation text-danger"></i> 错误</h4>
        </div>
        <div class="card-body">
          <h5 class="card-title">${param.message}</h5>
        </div>
      </div>
    </div>
  </div>
</div>

<jsp:include page="common/dependency_js.jsp"/>
</body>
</html>