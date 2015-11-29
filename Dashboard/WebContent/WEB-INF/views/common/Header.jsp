<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</head>
<body>
	<table class="headerTable" width="100%">
		<tr>
			<td style="float: left;"><img
				src="<%=request.getContextPath()%>/resources/images/ENdoSnipe_logo.png" />
			</td>
			<td style="float: right;"><a class="top-btn"
				id="header_button_explorer" href="<%=request.getContextPath()%>/">Explorer</a>
				<a class="top-btn" id="header_button_dashboard"
				href="<%=request.getContextPath()%>/dashboard/dashboardList">Dashboard</a>
				<a class="top-btn" id="header_button_systemMap"
				href="<%=request.getContextPath()%>/systemMap/mapView">Map</a>
			</td>
		</tr>
	</table>
</body>
<script>
	var href = window.location.href;
	var exp = false, dash = false, map = false ;
	
	if (href.match(/ENdoSnipe[\/|\/\?.*]$/) != null) {
		exp = true;
	}else if (href.match(/dashboardList\?*.*$/) != null) {
		dash = true;
	}else if (href.match(/systemMap\?*.*$/) != null) {
		map = true;
	}
	$("#header_button_explorer").button({
		disabled : exp
	});
	$("#header_button_dashboard").button({
		disabled : dash
	});
	$("#header_button_systemMap").button({
		disabled : map
	});
	$(".ui-button-text").click(function() {
		if ($("#dashboardMode").val() == ENS.dashboard.mode.EDIT) {
			window.resourceDashboardListView.saveOperation();
		}
	});
</script>
</html>