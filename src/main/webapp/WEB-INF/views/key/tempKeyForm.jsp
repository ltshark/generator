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
            <small>临时证书申请</small>
        </legend>
        <div class="control-group">
            <label for="phone_number" class="control-label">手机号码:</label>
            <div class="controls">
                <input type="text" id="phone_number" name="phoneNumber" value="" class="input-large required"
                       minlength="3"/>
            </div>
        </div>
        <div class="control-group">
            <label for="code" class="control-label">验证码:</label>

            <div class="controls">
                <input type="text" id="code" name="code" value="" class="input-large required" minlength="3"/>
                <input id="code_btn" class="btn btn-primary" type="button" value="获取验证码"/>&nbsp;
            </div>
        </div>
        <div class="controls">
            <label><i>您正在申请临时证书文件，需要对您的手机进行验证，请完成手机验证</i></label>
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
