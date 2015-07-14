<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<html>
<head>
	<title>资料修改</title>
</head>

<body>
	<c:if test="${not empty error}">
		<div id="error" class="alert alert-error"><button data-dismiss="alert" class="close">×</button>${error}</div>
	</c:if>
<form id="inputForm" action="${ctx}/profile" method="post" class="form-horizontal">
		<input type="hidden" name="id" value="${user.id}"/>
		<fieldset>
			<legend><small>资料修改</small></legend>
			<div class="control-group">
				<label for="name" class="control-label">用户名:</label>
				<div class="controls">
					<input type="text" id="name" name="name" value="${user.name}" class="input-large required" disabled/>
				</div>
			</div>
			<div class="control-group">
				<label for="plainPassword" class="control-label">密码:</label>
				<div class="controls">
					<input type="password" id="plainPassword" name="plainPassword" class="input-large" placeholder="不修改留空"/>
				</div>
			</div>
			<div class="control-group">
				<label for="confirmPassword" class="control-label">确认密码:</label>
				<div class="controls">
					<input type="password" id="confirmPassword" name="confirmPassword" class="input-large" equalTo="#plainPassword" />
				</div>
			</div>
			<div class="control-group">
				<label for="phone" class="control-label">手机号码:</label>
				<div class="controls">
					<input type="text" id="phone" name="phone" value="${user.phone}" class="input-large number"/>
				</div>
			</div>
			<div class="control-group">
				<label for="email" class="control-label">Email:</label>
				<div class="controls">
					<input type="text" id="email" name="email" value="${user.email}" class="input-large email"/><form:errors path="email"></form:errors>
				</div>
			</div>
			<div class="control-group">
				<label for="description" class="control-label">备注:</label>
				<div class="controls">
					<input type="text" id="description" name="description" value="${user.description}" class="input-large"/>
				</div>
			</div>
			<div class="form-actions">
				<input id="submit_btn" class="btn btn-primary" type="submit" value="提交"/>&nbsp;
				<input id="cancel_btn" class="btn" type="button" value="返回" onclick="history.back()"/>
			</div>
		</fieldset>
	</form>

	<script>
		$(document).ready(function() {
			//聚焦第一个输入框
			$("#name").focus();
			//为inputForm注册validate函数
			$("#inputForm").validate();
		});
	</script>
</body>
</html>
