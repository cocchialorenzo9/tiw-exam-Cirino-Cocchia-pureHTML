<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Payment confirmed</title>
</head>
<body>
	<h3>Your payment has been confirmed!</h3>
	<c:url value="GetCurrentAccountList?iduser=${user.iduser}" var="homeUrl"/>
	<c:url value="GetCurrentAccount?CAid=${CA.idcurrentAccount}" var="CAUrl"/>
	<p><a href="${homeUrl}">Cilck here</a> to go to your homepage
	<p><a href="${CAUrl}">Click here</a> to go back to the selected account state
</body>
</html>