<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet"
	href="<%=request.getContextPath()%>/resources/css/common/common.css"
	type="text/css" media="all">
<%@ include file="../include/ext/javaScriptInclude.jsp"%>
<%@ include file="../include/SystemMapInclude.jsp"%>
<%@ include file="common/Header.jsp"%>
<%@ include file="dialog/SignalDialog.jsp"%>
<%@ include file="dialog/MultipleResourceGraphDialog.jsp"%>
<%@ include file="dialog/SchedulingReportDialog.jsp"%>
<%@ include file="dialog/ReportDialog.jsp"%>
<%@ include file="dialog/PerformanceDoctorDialog.jsp"%>
<%@ include file="dialog/threadDumpDialog.jsp"%>
<%@ include file="dialog/SummarySignalDialog.jsp"%>
<title>ENdoSnipe SystemMap</title>
</head>
<body id="main" oncontextmenu="return false;" onload="self.focus();">

	<script type="text/javascript">
		var viewArea1 = {};
		var viewArea2 = {};

		viewArea1.width = 300;
		viewArea1.height = 800;
		viewArea1.rowspan = 1;
		viewArea1.colspan = 1;

		viewArea2.width = 900;
		viewArea2.height = 800;
		viewArea2.rowspan = 1;
		viewArea2.colspan = 1;

		var table = [ [ new wgp.PerspectiveModel(viewArea1),
				new wgp.PerspectiveModel(viewArea2) ] ];
		var perspectiveView = new wgp.PerspectiveView({
			id : "persArea",
			collection : table,
			minimum : false,
			close : false
		});
		perspectiveView.dropView("persArea_drop_0_0", "tree_area");
		perspectiveView.dropView("persArea_drop_0_1", "contents_area", "Map");
		$("#persArea_drop_0_1").addClass("ui-resizable-disabled");

		var appView = new ENS.AppView();
		
		var systemMapView = new ENS.SystemMapView({
			systemMapId : "contents_area"
		});
	</script>

	<%@ include file="../include/pluginsInclude.jsp"%>
	<script src="<%=request.getContextPath()%>/resources/js/common/user.js"
		type="text/javaScript"></script>

	<script>
		var treeView = new ENS.treeManager();
	</script>
	<input type="hidden" id="context" value="<%=request.getContextPath()%>" />

	<script>
		if ($("#connect").text() == "-1") {
			alert("DataCollector cannot be connected");
		}
	</script>
</body>
</html>
