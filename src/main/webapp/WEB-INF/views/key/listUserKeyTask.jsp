<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<html>
<head>
    <title>批量申请证书管理</title>
</head>

<body>

<fieldset>
    <legend>
        <small>批量申请证书管理</small>
    </legend>
    <c:if test="${not empty message}">
        <div id="message" class="alert alert-success">
            <button data-dismiss="alert" class="close">×</button>
                ${message}</div>
    </c:if>
    <div class="row">
        <div class="span4 offset7">
            <form class="form-search" action="#">
                <label>用户名：</label><input type="text" name="search_LIKE_name" class="input-medium"
                                          value="${param.search_LIKE_name}">
                <button type="submit" class="btn" id="search_btn">Search</button>
            </form>
        </div>
        <tags:sort/>
    </div>
    <form id="inputForm" name="inputForm" action="${ctx}/admin/key/batchApply" method="post" class="form-horizontal">
        <table id="contentTable" class="table table-striped table-bordered table-condensed">
            <thead>
            <tr>
                <th><input type="checkbox" onclick="checkAllSubpolicies(this.checked);"></th>
                <th>用户名</th>
                <th>部门</th>
                <th>证书状态</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${users.content}" var="user">
                <tr>
                    <td><input id="userId" name="userId" type="checkbox" value="${user.id}"
                               <c:if test="${user.keyTask!=null}">disabled</c:if>></td>
                    <td>${user.name}</td>
                    <td>${user.department.name}</td>
                    <td><c:choose>
                        <c:when test="${user.keyTask.status==1}">正在审批</c:when>
                        <c:when test="${user.keyTask.status==2}">已生成证书</c:when>
                        <c:when test="${user.keyTask.status==3}">证书申请被拒绝</c:when>
                        <c:otherwise>未申请证书</c:otherwise>
                    </c:choose></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        <tags:pagination page="${users}" paginationSize="5"/>
        <div class="form-actions">
            <select id="keyType" name="keyType">
                <option value="1">软件证书</option>
                <option value="2">硬件证书</option>
                <option value="3">临时证书</option>
            </select>
            <input id="submit_btn" class="btn btn-primary" type="button" value="申请证书" onclick="batchHandle();"/>&nbsp;
            <input id="cancel_btn" class="btn" type="button" value="返回" onclick="history.back()"/>
        </div>
    </form>
</fieldset>

<script>
    function batchHandle() {
        if (isChecked(document.inputForm, "userId")) {
            $("#inputForm").submit()
            return true;
        } else {
            alert("请选择一个申请");
            return false;
        }
    }

    function checkAllSubpolicies(checked) {
        checkAll(document.inputForm, "userId", checked);
    }
</script>
</body>
</html>
