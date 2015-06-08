<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<html>
<head>
    <title>证书申请</title>
</head>

<body>
<form id="inputForm" action="${ctx}/key/${action}" method="get" class="form-horizontal">
    <input type="hidden" name="keyType" value="${keyType}"/>
    <%--<input type="hidden" name="userId" value="${user.id}"/>--%>
    <fieldset>
        <legend>
            <small>证书申请</small>
        </legend>
        <div class="control-group">
            <label id="info">申请证书需求已经提交，管理员将对您的申请进行审核，审核通过后，您可以通过“证书下载/写入KEY”完成证书申请后续动作，如有疑问电话咨询021-99999999</label>
        </div>
        <div class="form-actions">
            <input id="done_btn" class="btn btn-primary" type="submit" value="完成"/></a>&nbsp;
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
