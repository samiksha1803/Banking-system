<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="dto.User" %>
<%@ page import="util.Html" %>
<%@ include file="/WEB-INF/includes/top.jsp" %>
<%
    String type = request.getParameter("type");
    if (type == null) {
        type = "";
    }
    User owner = (User) request.getAttribute("owner");
%>
<h1>Ledger</h1>
<p class="lede">
    <% if (owner != null) { %>
    Showing transactions for <%= Html.esc(owner.displayName()) %> (<%= Html.esc(owner.getAccountNumber()) %>).
    <% } else { %>
    Every transaction in the bank, with the same search used on a customer statement.
    <% } %>
</p>
<form class="filters" action="<%= ctx %>/admin/transactions" method="get">
    <% if (request.getParameter("userId") != null) { %>
    <input type="hidden" name="userId" value="<%= Html.esc(request.getParameter("userId")) %>">
    <% } %>
    <div>
        <label for="q">Search</label>
        <input id="q" name="q" value="<%= Html.esc(request.getParameter("q")) %>" placeholder="Reference, customer, or account">
    </div>
    <div>
        <label for="type">Type</label>
        <select id="type" name="type">
            <option value="">All</option>
            <option value="DEPOSIT" <%= "DEPOSIT".equals(type) ? "selected" : "" %>>Deposit</option>
            <option value="WITHDRAW" <%= "WITHDRAW".equals(type) ? "selected" : "" %>>Withdrawal</option>
            <option value="TRANSFER_IN" <%= "TRANSFER_IN".equals(type) ? "selected" : "" %>>Transfer received</option>
            <option value="TRANSFER_OUT" <%= "TRANSFER_OUT".equals(type) ? "selected" : "" %>>Transfer sent</option>
            <option value="ADJUSTMENT_CREDIT" <%= "ADJUSTMENT_CREDIT".equals(type) ? "selected" : "" %>>Credit adjustment</option>
            <option value="ADJUSTMENT_DEBIT" <%= "ADJUSTMENT_DEBIT".equals(type) ? "selected" : "" %>>Debit adjustment</option>
        </select>
    </div>
    <div>
        <label for="from">From</label>
        <input id="from" type="date" name="from" value="<%= Html.esc(request.getParameter("from")) %>">
    </div>
    <div>
        <label for="to">To</label>
        <input id="to" type="date" name="to" value="<%= Html.esc(request.getParameter("to")) %>">
    </div>
    <button class="btn" type="submit">Apply</button>
</form>
<p><a href="<%= ctx %>/admin/transactions">Clear filters</a></p>
<%@ include file="/WEB-INF/includes/txn-table.jsp" %>
<%@ include file="/WEB-INF/includes/bottom.jsp" %>
