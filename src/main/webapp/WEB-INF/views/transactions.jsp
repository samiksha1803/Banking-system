<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="util.Html" %>
<%@ include file="/WEB-INF/includes/top.jsp" %>
<%
    String type = request.getParameter("type");
    if (type == null) {
        type = "";
    }
    String q = request.getParameter("q");
    String from = request.getParameter("from");
    String to = request.getParameter("to");
%>
<h1>Transactions</h1>
<p class="lede">Search by reference, note, name, or account. Filter by type and date.</p>
<form class="filters" action="<%= ctx %>/transactions" method="get">
    <div>
        <label for="q">Search</label>
        <input id="q" name="q" value="<%= Html.esc(q) %>" placeholder="Reference, note, or account">
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
        <input id="from" type="date" name="from" value="<%= Html.esc(from) %>">
    </div>
    <div>
        <label for="to">To</label>
        <input id="to" type="date" name="to" value="<%= Html.esc(to) %>">
    </div>
    <button class="btn" type="submit">Apply</button>
</form>
<p><a href="<%= ctx %>/transactions">Clear filters</a></p>
<%@ include file="/WEB-INF/includes/txn-table.jsp" %>
<%@ include file="/WEB-INF/includes/bottom.jsp" %>
