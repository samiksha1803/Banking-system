<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="dto.User" %>
<%@ page import="util.Html" %>
<%@ page import="util.Money" %>
<%@ page import="util.TimeUtil" %>
<%@ include file="/WEB-INF/includes/top.jsp" %>
<%
    User user = (User) session.getAttribute("user");
%>
<h1>Profile</h1>
<p class="lede">Username and account number stay the same. You can update the contact details the bank uses for alerts.</p>
<section class="card" style="margin-bottom:16px;">
    <p><strong>Username</strong><br><%= Html.esc(user.getUsername()) %></p>
    <p><strong>Account number</strong><br><span class="account-no"><%= Html.esc(user.getAccountNumber()) %></span></p>
    <p><strong>Balance</strong><br><%= Money.inr(user.safeBalance()) %></p>
    <p><strong>Member since</strong><br><%= TimeUtil.format(user.getCreatedAt()) %></p>
    <a class="btn line" href="<%= ctx %>/password">Change password</a>
</section>
<section class="card" style="max-width:560px;">
    <h2>Edit details</h2>
    <form action="<%= ctx %>/profile" method="post">
        <label for="fullName">Full name</label>
        <input id="fullName" name="fullName" value="<%= Html.esc(user.getFullName()) %>" required>
        <label for="email">Email</label>
        <input id="email" type="email" name="email" value="<%= Html.esc(user.getEmail()) %>" required>
        <label for="phone">Mobile</label>
        <input id="phone" name="phone" value="<%= Html.esc(user.getPhone()) %>" placeholder="10-digit mobile, optional">
        <button class="btn" type="submit">Save profile</button>
    </form>
</section>
<%@ include file="/WEB-INF/includes/bottom.jsp" %>
