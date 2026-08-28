<%@ include file="../../include.jsp"%>

<!DOCTYPE html>
<html dir="${textDirection}">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
<%@ include file="../../favicon.jsp"%>

<link href="${contextPath}/<spring:theme code="globalstyles"/>" media="screen" rel="stylesheet" type="text/css" />
<link href="${contextPath}/<spring:theme code="stylesheet"/>" media="screen" rel="stylesheet" type="text/css" />
<link href="${contextPath}/<spring:theme code="jquerystylesheet"/>" media="screen" rel="stylesheet" type="text/css" />
<c:if test="${textDirection == 'rtl' }">
    <link href="${contextPath}/<spring:theme code="rtlstylesheet"/>" rel="stylesheet" type="text/css" >
</c:if>

<script type="text/javascript" src="${contextPath}/<spring:theme code="jquerysource"/>"></script>
<script type="text/javascript" src="${contextPath}/<spring:theme code="jqueryuisource"/>"></script>
<script src="${contextPath}/<spring:theme code="generalsource" />" type="text/javascript"></script>

<script type="text/javascript">
$(document).ready(function() {
	$("#tabs").tabs();
});
</script>

<sec:authorize access="hasRole('ROLE_ADMINISTRATOR')">
<script type="text/javascript">
function toggleUserAccountStatus(username, isCurrentlyEnabled) {
	var action = isCurrentlyEnabled ? "disable" : "enable";
	var doEnable = !isCurrentlyEnabled;
	if (confirm("Are you sure you want to " + action + " user '" + username + "'?")) {
		$.ajax({
			url: "${contextPath}/admin/account/enabledisableuser",
			type: "POST",
			data: {
				"doEnable": doEnable,
				"username": username
			},
			success: function(data, textStatus, jqXHR) {
				if (jqXHR.responseText === "success" || data === "success") {
					alert("User '" + username + "' was successfully " + (doEnable ? "enabled" : "disabled") + ".");
					location.reload();
				} else {
					alert(jqXHR.responseText || data);
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				alert("An error occurred: " + (jqXHR.responseText || errorThrown));
			}
		});
	}
}
</script>
</sec:authorize>

</head>
<body style="background: #FFFFFF;">
	<div class="dialogContent">
		<div class="dialogSection sectionContent">

	<div id="tabs">
		<ul>
			<li><a href="#infoTab"><spring:message code="teacher.teacherinfo.teacherInformation" /></a></li>
			<li><a href="#runsTab"><spring:message code="student.studentinfo.runList" /></a></li>
			<li><a href="#unitsTab"><spring:message code="teacher.teacherinfo.unitList" /></a></li>
		</ul>
		<div id="infoTab">
			<table>
				<tr>
					<th><spring:message code="teacher.teacherinfo.id" /></th>
					<td><c:out value="${userInfoMap['ID']}" /></td>
				</tr>
				<tr>
					<th><spring:message code="teacher.teacherinfo.name" /></th>
					<td><c:out value="${userInfoMap['First Name']}" />&nbsp;<c:out
							value="${userInfoMap['Last Name']}" /></td>
				</tr>
				<tr>
					<th><spring:message code="teacher.teacherinfo.username" /></th>
					<td><c:out value="${userInfoMap['Username']}" /></td>
				</tr>
				<tr>
					<th><spring:message code="teacher.teacherinfo.displayName" /></th>
					<td><c:out value="${userInfoMap['Display Name']}" /></td>
				</tr>
				<tr>
					<th><spring:message code="teacher.teacherinfo.emailAddress" /></th>
					<td><c:out value="${userInfoMap['Email']}" /></td>
				</tr>
				<tr>
					<th><spring:message code="teacher.teacherinfo.signupDate" /></th>
					<td><fmt:formatDate value="${userInfoMap['Sign Up Date']}"
							type="both" dateStyle="short" timeStyle="short" /></td>
				</tr>
				<tr>
					<th><spring:message code="teacher.teacherinfo.city" /></th>
					<td><c:out value="${userInfoMap['City']}" /></td>
				</tr>
				<tr>
					<th><spring:message code="teacher.teacherinfo.state" /></th>
					<td><c:out value="${userInfoMap['State']}" /></td>
				</tr>
				<tr>
					<th><spring:message code="teacher.teacherinfo.country" /></th>
					<td><c:out value="${userInfoMap['Country']}" /></td>
				</tr>
				<tr>
					<th><spring:message code="teacher.teacherinfo.schoolName" /></th>
					<td><c:out value="${userInfoMap['School Name']}" /></td>
				</tr>
				<tr>
					<th><spring:message code="teacher.teacherinfo.schoolLevel" /></th>
					<td><span style="text-transform: lowercase;"><c:out
								value="${userInfoMap['School Level']}" /></td>
				</tr>
				<tr>
					<th><spring:message
							code="teacher.teacherinfo.curriculumSubjects" /></th>
					<td><span style="text-transform: lowercase;"><c:out
								value="${userInfoMap['Curriculum Subjects']}" /></td>
				</tr>
				<tr>
					<th><spring:message code="teacher.registerteacher.language" /></th>
					<td><span style="text-transform: lowercase;"><c:out
								value="${userInfoMap['Language']}" /></td>
				</tr>
				<tr>
					<th><spring:message code="teacher.teacherinfo.howDidYouHear" /></th>
					<td><span style="text-transform: lowercase;"><c:out
								value="${userInfoMap['How did you hear about us']}" /></td>
				</tr>
				<tr>
					<th><spring:message code="teacher.teacherinfo.numberOfLogins" /></th>
					<td><c:out value="${userInfoMap['Number of Logins']}" /></td>
				</tr>
				<tr>
					<th><spring:message code="teacher.teacherinfo.lastLogin" /></th>
					<td><fmt:formatDate value="${userInfoMap['Last Login']}"
							type="both" dateStyle="short" timeStyle="short" /></td>
				</tr>
				<sec:authorize access="hasRole('ROLE_ADMINISTRATOR')">
					<tr>
						<th><spring:message code="teacher.teacherinfo.accountEnabled" /></th>
						<td>
							<c:out value="${userInfoMap['Account Enabled']}" />
							<button type="button" style="margin-left: 8px;" onclick="toggleUserAccountStatus('${userInfoMap['Username']}', ${userInfoMap['Account Enabled']})">
								<c:choose>
									<c:when test="${userInfoMap['Account Enabled']}">disable user</c:when>
									<c:otherwise>enable user</c:otherwise>
								</c:choose>
							</button>
						</td>
					</tr>
				</sec:authorize>
			</table>
		</div>
		<div id="runsTab">
			<table>
				<c:forEach var="run" items="${runList}">
					<tr>
						<th><spring:message code="student.studentinfo.runId" /></th>
						<td><c:out value="${run.id}"></c:out></td>
					</tr>

					<tr>
						<th><spring:message code="student.studentinfo.runName" /></th>
						<td><c:out value="${run.name}"></c:out></td>
					</tr>

					<tr>
						<th><spring:message code="student.studentinfo.runStartTime" /></th>
						<td><c:out value="${run.starttime}"></c:out></td>
					</tr>

					<tr>
						<th colspan="2"><hr></hr></th>
					</tr>
				</c:forEach>
			</table>
		</div>
		<div id="unitsTab">
			<table>
				<c:forEach var="project" items="${projectList}">
					<tr>
						<th><spring:message code="teacher.teacherinfo.unitId" /></th>
						<td><a target="_blank" href="${contextPath}/previewproject.html?projectId=${project.id}"><c:out value="${project.id}"></c:out></a></td>
					</tr>

					<tr>
						<th><spring:message code="teacher.teacherinfo.unitName" /></th>
						<td><c:out value="${project.name}"></c:out></td>
					</tr>

					<tr>
						<th colspan="2"><hr></hr></th>
					</tr>
				</c:forEach>
			</table>
		</div>
	</div>

	</div>
</body>
</html>
