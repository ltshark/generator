<%@ page import="cn.ltshark.service.account.ShiroDbRealm" %>
<%@ page import="org.apache.shiro.SecurityUtils" %>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div id="header">
    <div id="title">
        <h1><a href="${ctx}">证书申请管理系统</a>
            <small></small>
            <shiro:user>
                <div class="btn-group pull-right">
                    <a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="icon-user"></i> <shiro:principal property="name"/>
                        <span class="caret"></span>
                    </a>

                    <ul class="dropdown-menu">
                        <shiro:hasRole name="admin">
                            <li><a href="${ctx}/admin/user">用户管理</a></li>
                            <li><a href="${ctx}/admin/department">部门管理</a></li>
                            <li class="divider"></li>
                            <li><a href="${ctx}/admin/key/listUserKeyTask">批量申请证书</a></li>
                            <li><a href="${ctx}/admin/key/listKeyTask?taskStatus=1">待审批申请</a></li>
                            <li><a href="${ctx}/admin/key/listKeyTask?taskStatus=2">已审批申请</a></li>
                            <li><a href="${ctx}/admin/key/listKeyTask?taskStatus=3">已拒绝申请</a></li>
                            <li class="divider"></li>
                        </shiro:hasRole>
                        <shiro:hasRole name="departmentAdmin">
                            <li><a href="${ctx}/admin/user">用户管理</a></li>
                            <li class="divider"></li>
                            <li><a href="${ctx}/admin/key/listUserKeyTask">批量申请证书</a></li>
                            <% if (((ShiroDbRealm.ShiroUser) SecurityUtils.getSubject().getPrincipal()).department == "Network") {%>
                            <li><a href="${ctx}/admin/key/listKeyTask?taskStatus=1">待审批申请</a></li>
                            <li><a href="${ctx}/admin/key/listKeyTask?taskStatus=2">已审批申请</a></li>
                            <li><a href="${ctx}/admin/key/listKeyTask?taskStatus=3">已拒绝申请</a></li>
                            <li class="divider"></li>
                            <%}%>
                        </shiro:hasRole>

                            <%--<li><a href="${ctx}/api">APIs</a></li>--%>
                        <li><a href="${ctx}/profile">密码修改</a></li>
                        <li><a href="${ctx}/logout">登出</a></li>
                    </ul>
                </div>
            </shiro:user>
        </h1>
    </div>
</div>