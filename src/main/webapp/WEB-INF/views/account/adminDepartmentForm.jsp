<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title>部门管理</title>
</head>

<body>
	<form id="inputForm" action="${ctx}/admin/department/${action}" method="post" class="form-horizontal">
		<input type="hidden" name="id" value="${department.id}"/>
		<fieldset>
			<legend><small>部门管理</small></legend>
			<div class="control-group">
				<label class="control-label">部门名:</label>
				<div class="controls">
					<input type="text" id="name" name="name" value="${department.name}" class="input-large required"/>
				</div>
			</div>
			<div class="control-group" <c:if test="${empty department.id}"> style="display:none"</c:if>>
				<label class="control-label">创建日期:</label>
				<div class="controls">
					<span class="help-inline" style="padding:5px 0px"><fmt:formatDate value="${department.createTime}" pattern="yyyy年MM月dd日  HH时mm分ss秒" /></span>
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
			<c:if test="${not empty department.id}"> $("#name").focus();</c:if>
			//为inputForm注册validate函数
			$("#inputForm").validate();
		});
	</script>
</body>
</html>
