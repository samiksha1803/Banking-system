<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="util.Html" %>
<%
    String ctx = request.getContextPath();
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Create account · InBank</title>
    <link rel="stylesheet" href="<%= ctx %>/css/bank.css">
</head>
<body class="auth-body">
<main class="auth-card">
    <a class="brand" href="<%= ctx %>/index.jsp" style="color:#14261f;"><span class="mark">IB</span> InBank</a>
    <h1 style="margin-top:18px;">Create account</h1>
    <p class="lede">You will receive a 12-digit account number that starts with 6210.</p>
    <% if (error != null) { %><div class="banner bad"><%= Html.esc(error) %></div><% } %>
    <form action="<%= ctx %>/register" method="post">
        <label for="fullName">Full name</label>
        <input id="fullName" name="fullName" value="<%= Html.esc((String) request.getAttribute("fullName")) %>" required>
        <label for="username">Username</label>
        <input id="username" name="username" value="<%= Html.esc((String) request.getAttribute("username")) %>" required>
        <label for="email">Email</label>
        <input id="email" type="email" name="email" value="<%= Html.esc((String) request.getAttribute("email")) %>" required>
        <label for="phone">Mobile</label>
        <input id="phone" name="phone" value="<%= Html.esc((String) request.getAttribute("phone")) %>" placeholder="Optional">
        <label for="password">Password</label>
        <input id="password" type="password" name="password" minlength="6" required>
        <label for="confirmPassword">Confirm password</label>
        <input id="confirmPassword" type="password" name="confirmPassword" minlength="6" required>
        <button class="btn gold" type="submit">Create account</button>
    </form>
    <p><a href="<%= ctx %>/index.jsp">Back to sign in</a></p>
</main>
</body>
</html>
