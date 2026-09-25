<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.util.List" %>
<%@ page import="dto.User" %>
<%@ page import="util.Html" %>
<%@ page import="util.Money" %>
<%@ page import="util.TimeUtil" %>
<%@ include file="/WEB-INF/includes/top.jsp" %>
<%
    long customerCount = (Long) request.getAttribute("customerCount");
    long todayCount = (Long) request.getAttribute("todayCount");
    long allCount = (Long) request.getAttribute("allCount");
    BigDecimal totalBalance = (BigDecimal) request.getAttribute("totalBalance");
    @SuppressWarnings("unchecked")
    List<User> recentUsers = (List<User>) request.getAttribute("recentUsers");
%>
<h1>Admin desk</h1>
<p class="lede">Customer totals, money held, and the latest ledger activity.</p>
<section class="stats">
    <div class="stat"><span>Customers</span><strong><%= customerCount %></strong></div>
    <div class="stat"><span>Money held</span><strong><%= Money.inr(totalBalance) %></strong></div>
    <div class="stat"><span>Transactions today</span><strong><%= todayCount %></strong></div>
    <div class="stat"><span>All transactions</span><strong><%= allCount %></strong></div>
</section>
<h2>Newest accounts</h2>
<div class="table-wrap card" style="margin-bottom:24px;">
<table>
    <thead><tr><th>Name</th><th>Username</th><th>Account</th><th>Balance</th><th>Joined</th></tr></thead>
    <tbody>
    <% if (recentUsers == null || recentUsers.isEmpty()) { %>
    <tr><td colspan="5">No accounts yet.</td></tr>
    <% } else { for (User person : recentUsers) { %>
    <tr>
        <td><%= Html.esc(person.displayName()) %></td>
        <td><%= Html.esc(person.getUsername()) %></td>
        <td class="mono"><%= Html.text(person.getAccountNumber()) %></td>
        <td><%= Money.inr(person.safeBalance()) %></td>
        <td><%= TimeUtil.format(person.getCreatedAt()) %></td>
    </tr>
    <% } } %>
    </tbody>
</table>
</div>
<div class="split">
    <h2>Latest transactions</h2>
    <a href="<%= ctx %>/admin/transactions">Open the full ledger</a>
</div>
<%@ include file="/WEB-INF/includes/txn-table.jsp" %>
<%@ include file="/WEB-INF/includes/bottom.jsp" %>
