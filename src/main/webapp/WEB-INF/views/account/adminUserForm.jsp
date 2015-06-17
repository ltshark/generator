<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title>用户管理</title>
</head>

<body>
	<form id="inputForm" action="${ctx}/admin/user/${action}" method="post" class="form-horizontal">
		<input type="hidden" name="id" value="${user.id}"/>
		<fieldset>
			<legend><small>用户管理</small></legend>
			<div class="control-group">
				<label class="control-label">登录名:</label>
				<div class="controls">
					<input type="text" id="loginName" name="loginName" value="${user.loginName}" class="input-large required" <c:if test="${not empty user.id}"> disabled=""</c:if> />
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">用户名:</label>
				<div class="controls">
					<input type="text" id="name" name="name" value="${user.name}" class="input-large required"/>
				</div>
			</div>
			<div class="control-group">
				<label for="plainPassword" class="control-label">密码:</label>
				<div class="controls">
					<input type="password" id="plainPassword" name="plainPassword" class="input-large" placeholder="...Leave it blank if no change"/>
				</div>
			</div>
			<div class="control-group">
				<label for="confirmPassword" class="control-label">确认密码:</label>
				<div class="controls">
					<input type="password" id="confirmPassword" name="confirmPassword" class="input-large" equalTo="#plainPassword" />
				</div>
			</div>
			<div class="control-group" <c:if test="${empty user.id}"> style="display:none"</c:if>>
				<label class="control-label">注册日期:</label>
				<div class="controls">
					<span class="help-inline" style="padding:5px 0px"><fmt:formatDate value="${user.registerDate}" pattern="yyyy年MM月dd日  HH时mm分ss秒" /></span>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">角色:</label>
				<div class="controls">
					<%--<input type="text" id="roles" name="roles" class="input-large required" />--%>
					<select id="roles" name="roles" <shiro:hasRole name="admin"> disabled=""</shiro:hasRole>>
						<option value="user" selected>用户</option>
						<%--<option value="admin">超级管理员</option>--%>
						<option value="departmentAdmin" >部门管理员</option>
					</select>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">角色:</label>
				<div class="controls">
					<%--<input type="text" id="roles" name="roles" class="input-large required" />--%>
					<select id="roles" name="roles"  <shiro:hasRole name="admin"> disabled=""</shiro:hasRole>>
						<option value="user" selected>用户</option>
						<%--<option value="admin">超级管理员</option>--%>
						<option value="departmentAdmin" >部门管理员</option>
					</select>
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
			<c:if test="${empty user.id}"> $("#loginName").focus();</c:if>
			<c:if test="${not empty user.id}"> $("#name").focus();</c:if>
			//为inputForm注册validate函数
			$("#inputForm").validate();
		});
	</script>
</body>
</html>
