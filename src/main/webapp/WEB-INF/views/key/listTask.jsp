<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<html>
<head>
    <title>审批申请管理</title>
</head>

<body>
<form id="inputForm" name="inputForm" action="${ctx}/key/batchHandle" method="post" class="form-horizontal">
    <fieldset>
        <legend>
            <small>
                <c:choose>
                    <c:when test="${taskStatus=='1'}">待审批</c:when>
                    <c:when test="${taskStatus=='2'}">已审批</c:when>
                    <c:otherwise>已拒绝</c:otherwise>
                </c:choose>
            </small>
        </legend>
        <c:if test="${not empty message}">
            <div id="message" class="alert alert-success">
                <button data-dismiss="alert" class="close">×</button>
                    ${message}</div>
        </c:if>
        <%--<div class="row">--%>
        <%--<div class="span4 offset7">--%>
        <%--<form class="form-search" action="#">--%>
        <%--<label>名称：</label> <input type="text" name="search_LIKE_name" class="input-medium" value="${param.search_LIKE_name}">--%>
        <%--<button type="submit" class="btn" id="search_btn">Search</button>--%>
        <%--</form>--%>
        <%--</div>--%>
        <%--<tags:sort/>--%>
        <%--</div>--%>

        <table id="contentTable" class="table table-striped table-bordered table-condensed">
            <thead>
            <tr>
                <th><input type="checkbox" onclick="checkAllSubpolicies(this.checked);"></th>
                <th>用户名</th>
                <th>申请证书类型</th>
                <th>申请时间</th>
                <c:if test="${taskStatus=='1'}">
                    <th>管理</th>
                </c:if>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${tasks.content}" var="task">
                <tr>
                    <td><input id="taskId" name="taskId" type="checkbox" value="${task.id}"></td>
                    <td>${task.user.name}</td>
                    <td>
                        <c:choose>
                            <c:when test="${task.type=='1'}">硬件证书</c:when>
                            <c:when test="${task.type=='2'}">软件证书</c:when>
                            <c:otherwise>临时证书</c:otherwise>
                        </c:choose>
                    </td>
                    <td><fmt:formatDate value="${task.applyDate}" pattern="yyyy年MM月dd日  HH时mm分ss秒"/></td>
                    <c:if test="${taskStatus=='1'}">
                        <td><a href="${ctx}/key/approval/${task.id}">批准</a> &nbsp;&nbsp;<a
                                href="${ctx}/key/refuse/${task.id}">拒绝</a></td>
                    </c:if>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        <tags:pagination page="${tasks}" paginationSize="10"/>
        <div class="form-actions">
            <select id="actionType" name="actionType">
                <option value="2">批准</option>
                <option value="3">拒绝</option>
            </select>
            <input id="submit_btn" class="btn btn-primary" type="button" value="审批" onclick="batchHandle();"/>&nbsp;
            <input id="cancel_btn" class="btn" type="button" value="返回" onclick="history.back()"/>
        </div>
    </fieldset>
</form>
<script>
    function isChecked(form, name) {
        var checked = false;
        var element = form[name];
        if (element) {
            if (element.length) {
                for (var i = 0; i < element.length; i++) {
                    if (element[i].checked) {
                        checked = true;
                        break;
                    }
                }
            } else {
                checked = element.checked;
            }
        }
        return checked;
    }

    function batchHandle() {
        if (isChecked(document.inputForm, "taskId")) {
            $("#inputForm").submit()
            return true;
        } else {
            alert("请选择一个申请");
            return false;
        }
    }

    function checkAllSubpolicies(checked) {
        checkAll(document.inputForm, "taskId", checked);
    }

    function checkAll(form, name, checked) {
        if (form) {
            var element = form[name];
            if (element) {
                if (element.length) {
                    for (var i = 0; i < element.length; i++) {
                        if (!element[i].disabled) {
                            element[i].checked = checked;
                        }
                    }
                } else {
                    if (!element.disabled) {
                        element.checked = checked;
                    }
                }
            }
        }
    }
</script>
</body>
</html>
