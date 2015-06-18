<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags" %>

<html>
<head>
    <title>用户管理</title>
</head>

<body>
<form id="inputForm" action="${ctx}/admin/user/${action}" method="post" class="form-horizontal">
    <input type="hidden" name="id" value="${user.id}"/>
    <fieldset>
        <legend>
            <small>用户管理</small>
        </legend>
        <div class="control-group">
            <label class="control-label">登录名:</label>

            <div class="controls">
                <input type="text" id="loginName" name="loginName" value="${user.loginName}"
                       class="input-large required" <c:if test="${not empty user.id}"> disabled=""</c:if> />
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
                <input type="password" id="plainPassword" name="plainPassword"
                       <c:choose>
                       <c:when test="${empty user.id}">class="input-large required" </c:when>
                           <c:otherwise>class="input-large" placeholder="...如果没有变化则留空"</c:otherwise>
                </c:choose>/>
            </div>
        </div>
        <div class="control-group">
            <label for="confirmPassword" class="control-label">确认密码:</label>

            <div class="controls">
                <input type="password" id="confirmPassword" name="confirmPassword" class="input-large"
                       equalTo="#plainPassword"/>
            </div>
        </div>
        <div class="control-group" <c:if test="${empty user.id}"> style="display:none"</c:if>>
            <label class="control-label">注册日期:</label>

            <div class="controls">
                <span class="help-inline" style="padding:5px 0px"><fmt:formatDate value="${user.registerDate}"
                                                                                  pattern="yyyy年MM月dd日  HH时mm分ss秒"/></span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">部门:</label>

            <div class="controls">
                <%--<input type="text" id="roles" name="roles" class="input-large required" />--%>
                <select id="department_id" name="department_id" <c:if test="${not empty user.id}"> <shiro:hasRole
                        name="departmentAdmin"> disabled=""</shiro:hasRole></c:if>>
                    <c:forEach items="${departments}" var="department">
                        <option value="${department.id}" <c:if
                                test="${user.department.id==department.id}"> selected </c:if> >${department.name}</option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">角色:</label>

            <div class="controls">
                <%--<input type="text" id="roles" name="roles" class="input-large required" />--%>
                <select id="roles" name="roles">
                    <option value="user" <c:if test="${user.roles=='user'}"> selected </c:if>>用户</option>
                    <%--<option value="admin">超级管理员</option>--%>
                    <option value="departmentAdmin" <c:if test="${user.roles=='departmentAdmin'}"> selected </c:if>>
                        部门管理员
                    </option>
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
    $(document).ready(function () {
        //聚焦第一个输入框
        <c:if test="${empty user.id}">
        $("#loginName").focus();
        </c:if>
        <c:if test="${not empty user.id}">
        $("#name").focus();
        </c:if>
        //为inputForm注册validate函数
        $("#inputForm").validate();
    });
</script>
</body>
</html>
