<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<html>
<head>
    <title>证书申请</title>
</head>

<body>
<form id="inputForm" action="${ctx}/key/${action}" method="post" class="form-horizontal">
    <input type="hidden" name="keyType" value="${keyType}"/>
    <%--<input type="hidden" name="userId" value="${user.id}"/>--%>
    <fieldset>
        <legend>
            <small>软件/硬件证书申请</small>
        </legend>
        <div class="control-group">
            <label for="name" class="control-label">姓名:</label>
            <div class="controls">
                <label id="name" class="control-label-text-left">${user.name}</label>
            </div>
        </div>
        <div class="control-group">
            <label for="department" class="control-label">部门:</label>
            <div class="controls">
                <label id="department" class="control-label-text-left">${user.name}</label>
            </div>
        </div>
        <div class="control-group">
            <label for="email" class="control-label">邮件:</label>
            <div class="controls">
                <label id="email" class="control-label-text-left">${user.name}</label>
            </div>
        </div>
        <div class="control-group">
            <label for="mobile" class="control-label">手机:</label>
            <div class="controls">
                <label id="mobile" class="control-label-text-left">${user.name}</label>
            </div>
        </div>
        <div class="control-group">
            <label for="telephone" class="control-label">固话:</label>
            <div class="controls">
                <label id="telephone" class="control-label-text-left">${user.name}</label>
            </div>
        </div>
        <div class="controls">
            <label><i>您正在申请硬件/软件证书文件</i></label>
        </div>
        <div class="form-actions">
            <input id="submit_btn" class="btn btn-primary" type="submit" value="提交"/>&nbsp;
            <input id="cancel_btn" class="btn" type="button" value="返回" onclick="history.back()"/>
        </div>

        <%--<div class="control-group">--%>
            <%--<table>--%>
                <%--<tr><td>姓名&nbsp;:&nbsp;${user.name}</td></tr>--%>
                <%--<tr><td>部门&nbsp;:&nbsp;${user.name}</td></tr>--%>
                <%--<tr><td>邮件&nbsp;:&nbsp;${user.name}</td></tr>--%>
                <%--<tr><td>手机&nbsp;:&nbsp;${user.name}</td></tr>--%>
                <%--<tr><td>固话&nbsp;:&nbsp;${user.name}</td></tr>--%>
                <%--<tr><td><i>您正在申请硬件/软件证书文件</i></td></tr>--%>
            <%--</table>--%>
        <%--</div>--%>
        <%--<div class="form-actions">--%>
            <%--<input id="submit_btn" class="btn btn-primary" type="submit" value="提交"/>&nbsp;--%>
            <%--<input id="cancel_btn" class="btn" type="button" value="返回" onclick="history.back()"/>--%>
        <%--</div>--%>
    </fieldset>
</form>
<script>
    //    $(document).ready(function () {
    //        //聚焦第一个输入框
    //        $("#task_title").focus();
    //        //为inputForm注册validate函数
    //        $("#inputForm").validate();
    //    });
</script>
</body>
</html>
