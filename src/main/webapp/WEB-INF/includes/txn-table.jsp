<%@ page import="java.util.List" %>
<%@ page import="dto.Transaction" %>
<%@ page import="util.Html" %>
<%@ page import="util.Money" %>
<%@ page import="util.TimeUtil" %>
<%@ page import="util.Txn" %>
<%
    @SuppressWarnings("unchecked")
    List<Transaction> rows = (List<Transaction>) request.getAttribute("transactions");
    boolean showUser = Boolean.TRUE.equals(request.getAttribute("showUser"));
%>
<div class="table-wrap card">
<table>
    <thead>
    <tr>
        <th>Date and time</th>
        <th>ID</th>
        <th>Reference</th>
        <% if (showUser) { %><th>Customer</th><% } %>
        <th>Type</th>
        <th>Details</th>
        <th>Amount</th>
        <th>Balance after</th>
    </tr>
    </thead>
    <tbody>
    <% if (rows == null || rows.isEmpty()) { %>
    <tr><td colspan="<%= showUser ? 8 : 7 %>">No transactions match this view yet.</td></tr>
    <% } else { for (Transaction txn : rows) { %>
    <tr>
        <td><%= TimeUtil.format(txn.getCreatedAt()) %></td>
        <td>#<%= txn.getId() %></td>
        <td class="mono"><%= Html.text(txn.getReferenceNumber()) %></td>
        <% if (showUser) { %>
        <td><%= Html.text(txn.getUser() == null ? null : txn.getUser().displayName()) %></td>
        <% } %>
        <td><span class="pill pill-<%= Html.esc(txn.getType()) %>"><%= Html.esc(Txn.label(txn.getType())) %></span></td>
        <td><%= Html.text(txn.getDetails()) %></td>
        <td class="<%= txn.isCredit() ? "amt in" : "amt out" %>"><%= txn.isCredit() ? "+" : "-" %><%= Money.inr(txn.getAmount()) %></td>
        <td><%= Money.inr(txn.getBalanceAfter()) %></td>
    </tr>
    <% } } %>
    </tbody>
</table>
</div>
