<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<html>
<head>
    <title>证书申请</title>
</head>

<body>
<form id="inputForm" action="${ctx}/task/${action}" method="post" class="form-horizontal">
    <input type="hidden" name="keyType" value="${keyType}"/>
    <input type="hidden" name="userId" value="${user.id}"/>
    <fieldset>
        <legend>
            <small>证书申请</small>
        </legend>
        <div class="control-group">
            <table>
                <tr><td>姓名&nbsp;:&nbsp;${user.name}</td></tr>
                <tr><td>部门&nbsp;:&nbsp;${user.name}</td></tr>
                <tr><td>邮件&nbsp;:&nbsp;${user.name}</td></tr>
                <tr><td>手机&nbsp;:&nbsp;${user.name}</td></tr>
                <tr><td>固话&nbsp;:&nbsp;${user.name}</td></tr>
                <tr><td><i>您正在申请硬件/软件证书文件</i></td></tr>
            </table>
        </div>
        <div class="form-actions">
            <input id="submit_btn" class="btn btn-primary" type="submit" value="提交"/>&nbsp;
            <input id="cancel_btn" class="btn" type="button" value="返回" onclick="history.back()"/>
        </div>
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
