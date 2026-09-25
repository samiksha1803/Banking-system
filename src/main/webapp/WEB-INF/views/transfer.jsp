<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="dto.User" %>
<%@ page import="util.Html" %>
<%@ include file="/WEB-INF/includes/top.jsp" %>
<%
    User user = (User) session.getAttribute("user");
    String accountValue = (String) request.getAttribute("accountNumber");
    String amountValue = (String) request.getAttribute("amount");
    String noteValue = (String) request.getAttribute("note");
%>
<h1>Transfer money</h1>
<p class="lede">Available balance: <strong><%= util.Money.inr(user.safeBalance()) %></strong>.
    Your account number is <strong class="account-no"><%= Html.esc(user.getAccountNumber()) %></strong>.</p>
<p class="muted">Ask the recipient for their 12-digit number from <strong>Profile</strong> (digits only, no spaces).
    You cannot transfer to your own account.</p>
<section class="card" style="max-width:560px;">
    <form action="<%= ctx %>/transfer" method="post">
        <label for="accountNumber">Recipient account number (12 digits)</label>
        <input id="accountNumber" name="accountNumber" inputmode="numeric" pattern="[0-9]{12}" maxlength="12"
               placeholder="621050503228" value="<%= Html.esc(accountValue) %>" required>
        <label for="amount">Amount</label>
        <input id="amount" type="number" name="amount" min="1" max="1000000" step="0.01"
               value="<%= Html.esc(amountValue) %>" required>
        <label for="note">Note</label>
        <input id="note" name="note" maxlength="120" value="<%= Html.esc(noteValue) %>" placeholder="Optional">
        <button class="btn gold" type="submit">Send transfer</button>
    </form>
</section>
<%@ include file="/WEB-INF/includes/bottom.jsp" %>
