<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<html>
<head>
    <title>证书申请</title>
</head>
<body>
<table id="contentTable">
    <thead>
    </thead>
    <tr>
        <td width="200">姓名：</td>
        <td width="200">电话：</td>
        <td>&nbsp;</td>
    </tr>
    <tr>
        <td>部门：</td>
        <td>固话：</td>
        <td>email：</td>
    </tr>
    <tr>
        <td>备注：</td>
    </tr>
    <tr>
        <td>证书信息</td>
    </tr>
    <tr>
        <td>
            &nbsp;
        </td>
    </tr>
    <div>
        <table>
            <tr>
                <td>

                    <button type="button" name="hardware" class="btn" style="height:90px;width:100px;">硬件证书申请</button>
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <button type="button" name="software" class="btn" style="height:90px;width:100px;">软件证书申请</button>
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <button type="button" name="temp" class="btn" style="height:90px;width:100px;">临时证书申请</button>
                </td>
            </tr>
            <tr>
                <td>
                    &nbsp;
                </td>
            </tr>

            <tr>
                <td>
                    <button type="button" name="hardware" class="btn" style="height:35px;width:360px;">证书下载/证书写入KEY
                    </button>
                </td>
            </tr>
        </table>
    </div>
    </tbody>
</table>
</body>
</html>
