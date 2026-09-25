<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String ctx = request.getContextPath();
    String target = session.getAttribute("user") == null ? ctx + "/index.jsp" : ctx + "/dashboard";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Unavailable · InBank</title>
    <link rel="stylesheet" href="<%= ctx %>/css/bank.css">
</head>
<body class="auth-body">
<main class="auth-card">
    <a class="brand" href="<%= target %>" style="color:#14261f;"><span class="mark">IB</span> InBank</a>
    <h1 style="margin-top:18px;">That page is unavailable</h1>
    <p class="lede">Go back to InBank and try the action again.</p>
    <a class="btn" href="<%= target %>">Return to InBank</a>
</main>
</body>
</html>
