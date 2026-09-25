<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="dto.User" %>
<%@ page import="util.Money" %>
<%@ page import="util.TimeUtil" %>
<%@ include file="/WEB-INF/includes/top.jsp" %>
<%
    User user = (User) session.getAttribute("user");
%>
<section class="balance-card">
    <div class="label">Available balance</div>
    <strong><%= Money.inr(user.safeBalance()) %></strong>
    <p>Account <span class="account-no"><%= util.Html.esc(user.getAccountNumber()) %></span>
        · Member since <%= TimeUtil.format(user.getCreatedAt()) %></p>
</section>

<div class="grid-2">
    <section class="card">
        <h2>Deposit</h2>
        <form action="<%= ctx %>/deposit" method="post">
            <label for="depositAmount">Amount</label>
            <input id="depositAmount" type="number" name="amount" min="1" max="1000000" step="0.01" required>
            <button class="btn gold" type="submit">Deposit</button>
        </form>
    </section>
    <section class="card">
        <h2>Withdraw</h2>
        <p class="lede">This is refused when the amount is higher than your balance.</p>
        <form action="<%= ctx %>/withdraw" method="post">
            <label for="withdrawAmount">Amount</label>
            <input id="withdrawAmount" type="number" name="amount" min="1" max="1000000" step="0.01" required>
            <button class="btn" type="submit">Withdraw</button>
        </form>
    </section>
</div>

<div class="split" style="margin-top:28px;">
    <h2>Recent transactions</h2>
    <a href="<%= ctx %>/transactions">View all</a>
</div>
<%@ include file="/WEB-INF/includes/txn-table.jsp" %>
<%@ include file="/WEB-INF/includes/bottom.jsp" %>
