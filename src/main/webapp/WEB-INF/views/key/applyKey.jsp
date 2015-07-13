<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fun" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<html>
<head>
    <title>证书申请</title>
</head>
<body>
<fieldset>
    <legend>
        <small>证书申请</small>
    </legend>
    <div class="control-group">
        <%--<div class="controls">--%>
        <table id="contentTable" style="margin-left:100px;">
            <thead>
            </thead>
            <tr>
                <td width="200">姓名：<c:out value="${user.name}"/></td>
                <td width="200">电话：<c:out value="${user.phone}"/></td>
                <td>&nbsp;</td>
            </tr>
            <tr>
                <td>部门：<c:out value="${user.department}"/></td>
                <td>固话：</td>
                <td>email：<c:out value="${user.email}"/></td>
            </tr>
            <tr>
                <td>备注：<c:out value="${user.description}"/></td>
            </tr>
            <tr>
                <td>证书信息</td>
            </tr>
            <tr>
                <td>
                    &nbsp;
                </td>
            </tr>
            <%--<div class="control-group">--%>
            <table style="margin-left:100px;">
                <tr>
                    <td>
                        <a href="${ctx}/key/create?keyType=1">
                            <button type="button" name="hardware" class="btn" style="height:90px;width:110px;"
                                    <c:if test="${task != null }">disabled</c:if>>硬件证书申请
                            </button>
                        </a>
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        <a href="${ctx}/key/create?keyType=2">
                            <button type="button" name="software" class="btn" style="height:90px;width:110px;"
                                    <c:if test="${task != null}">disabled</c:if>>软件证书申请
                            </button>
                        </a>
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        <a href="${ctx}/key/create?keyType=3">
                            <button type="button" name="temp" class="btn" style="height:90px;width:110px;"
                                    <c:if test="${task != null}">disabled</c:if>>临时证书申请
                            </button>
                        </a>
                    </td>
                </tr>
                <tr>
                    <td>
                        &nbsp;
                    </td>
                </tr>

                <tr>
                    <td>
                        <button type="button" name="hardware" class="btn" style="height:35px;width:390px;"
                                <c:if test="${not canDownload}">disabled</c:if>>证书下载/证书写入KEY
                        </button>
                    </td>
                </tr>
            </table>
            <%--</div>--%>
            </tbody>
        </table>
        <%--</div>--%>
    </div>
</fieldset>
</body>
</html>
