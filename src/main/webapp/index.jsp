<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="util.Html" %>
<%
    String ctx = request.getContextPath();
    String flashOk = (String) session.getAttribute("flashSuccess");
    String flashBad = (String) session.getAttribute("flashError");
    if (flashOk != null) {
        session.removeAttribute("flashSuccess");
    }
    if (flashBad != null) {
        session.removeAttribute("flashError");
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sign in · InBank</title>
    <link rel="stylesheet" href="<%= ctx %>/css/bank.css">
</head>
<body class="auth-body">
<main class="auth-card">
    <a class="brand" href="<%= ctx %>/index.jsp" style="color:#14261f;"><span class="mark">IB</span> InBank</a>
    <h1 style="margin-top:18px;">Sign in</h1>
    <p class="lede">Personal banking for deposits, withdrawals, transfers, and statements.</p>
    <% if ("1".equals(request.getParameter("signin"))) { %>
    <div class="banner bad">Please sign in to continue.</div>
    <% } %>
    <% if ("transfer".equals(request.getParameter("expired"))) { %>
    <div class="banner bad">Your session ended before the transfer was sent. Sign in, open <strong>Transfer</strong>, and submit the form again. Money is not moved until you see a success message.</div>
    <% } %>
    <% if (flashOk != null) { %><div class="banner ok"><%= Html.esc(flashOk) %></div><% } %>
    <% if (flashBad != null) { %><div class="banner bad"><%= Html.esc(flashBad) %></div><% } %>
    <form action="<%= ctx %>/login" method="post">
        <label for="username">Username</label>
        <input id="username" name="username" autocomplete="username" required>
        <label for="password">Password</label>
        <input id="password" type="password" name="password" autocomplete="current-password" required>
        <button class="btn" type="submit">Sign in</button>
    </form>
    <p><a href="<%= ctx %>/register.jsp">Create an account</a></p>
    <div class="demo">
        <strong>Staff demo</strong><br>
        Username <span class="mono">admin</span> · Password <span class="mono">admin123</span>
    </div>
</main>
</body>
</html>
