<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/includes/top.jsp" %>
<h1>Change password</h1>
<p class="lede">Use 6 to 64 characters. You stay signed in after a successful change, and an alert is stored.</p>
<section class="card" style="max-width:560px;">
    <form action="<%= ctx %>/password" method="post">
        <label for="currentPassword">Current password</label>
        <input id="currentPassword" type="password" name="currentPassword" autocomplete="current-password" required>
        <label for="newPassword">New password</label>
        <input id="newPassword" type="password" name="newPassword" autocomplete="new-password" minlength="6" required>
        <label for="confirmPassword">Confirm new password</label>
        <input id="confirmPassword" type="password" name="confirmPassword" autocomplete="new-password" minlength="6" required>
        <button class="btn" type="submit">Update password</button>
    </form>
</section>
<%@ include file="/WEB-INF/includes/bottom.jsp" %>
