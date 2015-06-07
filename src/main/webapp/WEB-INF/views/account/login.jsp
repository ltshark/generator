<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.apache.shiro.web.filter.authc.FormAuthenticationFilter" %>
<%@ page import="org.apache.shiro.authc.ExcessiveAttemptsException" %>
<%@ page import="org.apache.shiro.authc.IncorrectCredentialsException" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<html>
<head>
    <title>登录页</title>
</head>

<body>
<form id="loginForm" action="${ctx}/login" method="post" class="form-horizontal">
    <%--<%--%>
    <%--String error = (String) request.getAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);--%>
    <%--if(error != null){--%>
    <%--%>--%>

    <c:choose>
        <c:when test="${empty shiroLoginFailure }"></c:when>
        <c:when test="${shiroLoginFailure eq 'cn.ltshark.shiro.CaptchaException'}">
            <div class="alert alert-error input-medium controls">
                <button class="close" data-dismiss="alert">×</button>
                验证码错误，请重新输入.
            </div>
        </c:when>
        <c:otherwise>
            <div class="alert alert-error input-medium controls">
                <button class="close" data-dismiss="alert">×</button>
                登录失败，请重试.
            </div>
        </c:otherwise>
    </c:choose>

    <%--<%--%>
    <%--}--%>
    <%--%>--%>
    <div class="control-group">
        <label for="username" class="control-label">名称:</label>

        <div class="controls">
            <input type="text" id="username" name="username" value="${username}" class="input-medium required"/>
        </div>
    </div>
    <div class="control-group">
        <label for="password" class="control-label">密码:</label>

        <div class="controls">
            <input type="password" id="password" name="password" class="input-medium required"/>
        </div>
    </div>

    <div class="control-group">
        <label for="password" class="control-label">验证码:</label>

        <div class="controls">
            <input id="veryCode" name="veryCode" type="text" class="input-medium required"/>
            <img id="imgObj" alt="" src="xuan/verifyCode"/>
            <a href="#" onclick="changeImg()">换一张</a>
            <img id="verifyImg" alt="" src=""/>
            <%--<input type="button" value="验证" onclick="isRightCode()"/>--%>
        </div>
        <div id="info"></div>
    </div>

    <div class="control-group">
        <div class="controls">
            <label class="checkbox" for="rememberMe"><input type="checkbox" id="rememberMe" name="rememberMe"/>
                记住我</label>
            <input id="submit_btn" class="btn btn-primary" type="submit" value="登录"/> <a class="btn"
                                                                                         href="${ctx}/register">注册</a>
            <span class="help-block">(管理员: <b>admin/admin</b>, 普通用户: <b>user/user</b>)</span>
        </div>
    </div>

</form>

<script>
    $(document).ready(function () {
        $("#loginForm").validate();
    });

    function changeImg() {
        var imgSrc = $("#imgObj");
        var src = imgSrc.attr("src");
        var t = chgUrl(src);
        imgSrc.attr("src", t);
    }
    //时间戳
    //为了使每次生成图片不一致，即不让浏览器读缓存，所以需要加上时间戳
    function chgUrl(url) {
        var timestamp = (new Date()).valueOf();
        urlurl = url.substring(0, 17);
        if ((url.indexOf("&") >= 0)) {
            urlurl = url + "×tamp=" + timestamp;
        } else {
            urlurl = url + "?timestamp=" + timestamp;
        }
        return urlurl;
    }
    //		function isRightCode(){
    //			var code = $("#veryCode").val();
    //			code = "c=" + code;
    //			$.ajax({
    //				type:"POST",
    //				url:"resultServlet/validateCode",
    //				data:code,
    //				success:callback
    //			});
    //		}
    //		function callback(data){
    //			var imgSrc = $("#verifyImg");
    //			alert(data);
    //			if(data == "true") {
    //				imgSrc.attr("src", "static/images/note_yes.gif");
    //			}else{
    //				imgSrc.attr("src", "static/images/note_no.gif");
    //			}
    ////			alert(data)
    ////			$("#info").html(data);
    //		}
</script>
</body>
</html>
