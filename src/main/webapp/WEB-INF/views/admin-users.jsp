<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="dto.User" %>
<%@ page import="util.Html" %>
<%@ page import="util.Money" %>
<%@ include file="/WEB-INF/includes/top.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<User> people = (List<User>) request.getAttribute("users");
    String q = request.getParameter("q");
%>
<h1>Customers</h1>
<p class="lede">Search accounts, open a customer's ledger, activate or deactivate access, or post a balance adjustment.</p>
<form class="filters" action="<%= ctx %>/admin/users" method="get">
    <div>
        <label for="q">Search</label>
        <input id="q" name="q" value="<%= Html.esc(q) %>" placeholder="Name, username, email, phone, or account">
    </div>
    <button class="btn" type="submit">Search</button>
</form>

<section class="card" style="margin-bottom:18px;">
    <h2>Balance adjustment</h2>
    <form action="<%= ctx %>/admin/users" method="post">
        <input type="hidden" name="action" value="adjust">
        <input type="hidden" name="q" value="<%= Html.esc(q) %>">
        <label for="userId">Customer id</label>
        <input id="userId" name="userId" required>
        <label for="direction">Direction</label>
        <select id="direction" name="direction">
            <option value="CREDIT">Credit</option>
            <option value="DEBIT">Debit</option>
        </select>
        <label for="amount">Amount</label>
        <input id="amount" type="number" name="amount" min="1" max="1000000" step="0.01" required>
        <label for="reason">Reason</label>
        <input id="reason" name="reason" maxlength="120" required>
        <button class="btn gold" type="submit">Save adjustment</button>
    </form>
</section>

<div class="table-wrap card">
<table>
    <thead>
    <tr><th>ID</th><th>Customer</th><th>Account</th><th>Email</th><th>Balance</th><th>Status</th><th></th></tr>
    </thead>
    <tbody>
    <% if (people == null || people.isEmpty()) { %>
    <tr><td colspan="7">No customers match that search.</td></tr>
    <% } else { for (User person : people) { %>
    <tr>
        <td><%= person.getId() %></td>
        <td><%= Html.esc(person.displayName()) %><br><span class="muted">@<%= Html.esc(person.getUsername()) %></span></td>
        <td class="mono"><%= Html.text(person.getAccountNumber()) %></td>
        <td><%= Html.text(person.getEmail()) %></td>
        <td><%= Money.inr(person.safeBalance()) %></td>
        <td><%= person.isEnabled() ? "Active" : "Inactive" %><%= person.isAdmin() ? " · Admin" : "" %></td>
        <td class="actions">
            <a class="btn small line" href="<%= ctx %>/admin/transactions?userId=<%= person.getId() %>">Ledger</a>
            <% if (!person.isAdmin()) { %>
            <form class="inline" action="<%= ctx %>/admin/users" method="post">
                <input type="hidden" name="userId" value="<%= person.getId() %>">
                <input type="hidden" name="q" value="<%= Html.esc(q) %>">
                <% if (person.isEnabled()) { %>
                <input type="hidden" name="action" value="deactivate">
                <button class="btn small danger" type="submit">Deactivate</button>
                <% } else { %>
                <input type="hidden" name="action" value="activate">
                <button class="btn small" type="submit">Activate</button>
                <% } %>
            </form>
            <% } %>
        </td>
    </tr>
    <% } } %>
    </tbody>
</table>
</div>
<%@ include file="/WEB-INF/includes/bottom.jsp" %>
