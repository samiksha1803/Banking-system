<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="util.ChatTurn" %>
<%@ page import="util.Html" %>
<%@ include file="/WEB-INF/includes/top.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<ChatTurn> chat = (List<ChatTurn>) session.getAttribute("chat");
%>
<h1>Account assistant</h1>
<p class="lede">Ask about your balance, account number, or recent transactions. Answers come from your InBank records on this server. Nothing is sent to an outside AI service.</p>
<form class="suggestions" action="<%= ctx %>/assistant" method="get">
    <button class="btn line" type="submit" name="ask" value="What is my balance?">My balance</button>
    <button class="btn line" type="submit" name="ask" value="What is my account number?">Account number</button>
    <button class="btn line" type="submit" name="ask" value="Show my recent transactions">Recent transactions</button>
    <button class="btn line" type="submit" name="ask" value="How do I transfer money?">How to transfer</button>
</form>
<div class="chat">
    <% if (chat == null || chat.isEmpty()) { %>
    <div class="bubble assistant">Hello. I can look up your balance, account number, and latest transactions, and I can explain deposits, withdrawals, transfers, and password changes.</div>
    <% } else { for (ChatTurn turn : chat) { %>
    <div class="bubble <%= "user".equals(turn.getRole()) ? "user" : "assistant" %>"><%= Html.esc(turn.getText()) %></div>
    <% } } %>
</div>
<section class="card" style="max-width:720px;">
    <form action="<%= ctx %>/assistant" method="post">
        <label for="message">Your question</label>
        <textarea id="message" name="message" maxlength="300" required></textarea>
        <button class="btn" type="submit">Ask</button>
        <a class="btn line" href="<%= ctx %>/assistant?clear=1">Clear chat</a>
    </form>
</section>
<%@ include file="/WEB-INF/includes/bottom.jsp" %>
