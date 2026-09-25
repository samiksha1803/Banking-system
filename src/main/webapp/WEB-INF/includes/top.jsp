<%@ page import="dto.User" %>
<%@ page import="util.Html" %>
<%
    String ctx = request.getContextPath();
    User currentUser = (User) session.getAttribute("user");
    String nav = (String) request.getAttribute("nav");
    if (nav == null) {
        nav = "";
    }
    String title = (String) request.getAttribute("pageTitle");
    if (title == null) {
        title = "InBank";
    }
    Integer unread = (Integer) request.getAttribute("unreadCount");
    if (unread == null) {
        unread = 0;
    }
    String flashOk = (String) session.getAttribute("flashSuccess");
    String flashBad = (String) session.getAttribute("flashError");
    if (flashOk != null) {
        session.removeAttribute("flashSuccess");
    }
    if (flashBad != null) {
        session.removeAttribute("flashError");
    }
    String requestError = (String) request.getAttribute("error");
    String home = nav.startsWith("admin") ? ctx + "/admin" : ctx + "/dashboard";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><%= Html.esc(title) %> · InBank</title>
    <link rel="stylesheet" href="<%= ctx %>/css/bank.css">
</head>
<body>
<a class="skip" href="#content">Skip to content</a>
<header class="topbar">
    <a class="brand" href="<%= home %>"><span class="mark">IB</span> InBank</a>
    <% if (currentUser != null) { %>
    <div class="who">
        <strong><%= Html.esc(currentUser.displayName()) %></strong>
        <span class="account-no"><%= Html.esc(currentUser.getAccountNumber()) %></span>
    </div>
    <% } %>
</header>
<nav class="nav">
    <a class="<%= "dashboard".equals(nav) ? "active" : "" %>" href="<%= ctx %>/dashboard">Dashboard</a>
    <a class="<%= "transactions".equals(nav) ? "active" : "" %>" href="<%= ctx %>/transactions">Transactions</a>
    <a class="<%= "transfer".equals(nav) ? "active" : "" %>" href="<%= ctx %>/transfer">Transfer</a>
    <a class="<%= "alerts".equals(nav) ? "active" : "" %>" href="<%= ctx %>/notifications">Alerts<% if (unread > 0) { %><span class="badge"><%= unread %></span><% } %></a>
    <a class="<%= "assistant".equals(nav) ? "active" : "" %>" href="<%= ctx %>/assistant">Assistant</a>
    <a class="<%= "profile".equals(nav) ? "active" : "" %>" href="<%= ctx %>/profile">Profile</a>
    <% if (currentUser != null && currentUser.isAdmin()) { %>
    <a class="<%= "admin".equals(nav) ? "active" : "" %>" href="<%= ctx %>/admin">Admin</a>
    <a class="<%= "admin-users".equals(nav) ? "active" : "" %>" href="<%= ctx %>/admin/users">Customers</a>
    <a class="<%= "admin-txns".equals(nav) ? "active" : "" %>" href="<%= ctx %>/admin/transactions">Ledger</a>
    <% } %>
    <a href="<%= ctx %>/logout.jsp">Logout</a>
</nav>
<main class="wrap" id="content">
<% if (flashOk != null) { %><div class="banner ok"><%= Html.esc(flashOk) %></div><% } %>
<% if (flashBad != null) { %><div class="banner bad"><%= Html.esc(flashBad) %></div><% } %>
<% if (requestError != null) { %><div class="banner bad"><%= Html.esc(requestError) %></div><% } %>
