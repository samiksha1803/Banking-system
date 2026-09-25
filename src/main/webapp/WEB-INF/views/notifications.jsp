<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="dto.Notification" %>
<%@ page import="util.Html" %>
<%@ page import="util.TimeUtil" %>
<%@ include file="/WEB-INF/includes/top.jsp" %>
<%
    @SuppressWarnings("unchecked")
    List<Notification> notes = (List<Notification>) request.getAttribute("notes");
    boolean mailOn = Boolean.TRUE.equals(request.getAttribute("mailOn"));
%>
<h1>Alerts</h1>
<p class="lede">
    <% if (mailOn) { %>
    Email delivery is turned on. The same message is saved here.
    <% } else { %>
    Email delivery is off, so alerts are saved in the app. To also send email, set enabled=true in src/main/resources/mail.properties and add your SMTP login.
    <% } %>
</p>
<% if (notes == null || notes.isEmpty()) { %>
<section class="card"><p>No alerts yet. Deposits, withdrawals, transfers, and password changes will show up here.</p></section>
<% } else { for (Notification note : notes) { %>
<article class="card note <%= note.isOpened() ? "" : "unread" %>" style="margin-bottom:12px;">
    <div class="split">
        <h2><%= Html.esc(note.getSubject()) %></h2>
        <span class="muted"><%= TimeUtil.format(note.getCreatedAt()) %></span>
    </div>
    <p><%= Html.esc(note.getMessage()) %></p>
</article>
<% } } %>
<%@ include file="/WEB-INF/includes/bottom.jsp" %>
