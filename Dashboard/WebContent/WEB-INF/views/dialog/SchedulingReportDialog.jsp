<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet"
	href="<%=request.getContextPath()%>/resources/css/dialog/SchedulingReportDialog.css"
	type="text/css" media="all">
</head>
<body>
	<div id="persArea"></div>
	<input id="treeData" type="hidden" value='${treeData}' />
	<div id="schedulingReportDialog" title="Periodic Report" style="display: none">
		<div id="schedulingReportNameArea" class="dialogContentArea"
			style="display: none">
			<div id="reportIdValue" class="dialogValue">
				<input type="text" style="width:410px" id="schedulingReportId" class="schedulingReportValue" value="" />
				<input type="text" style="width:410px" id="beforeSchedulingReportName" class="beforeSchedulingReportValue"
					value="" />
			</div>
			
		</div>
		<div id="schedulingReportNameArea" class="dialogContentArea">
			<div id="schedulingReportNameItem" class="dialogItem">Periodic Report Name :</div>
			<div id="schedulingReportNameValue" class="dialogValue">
				<input type="text" style="width:410px" id="schedulingReportName" class="schedulingReportValue"
					value="" />
			</div>
		</div>
		<div id="schedulingReportTargetNameArea" class="dialogContentArea">
			<div id="schedulingReportTargetNameItem" class="dialogItem">Target
				Measurement Name :</div>
			<div id="schedulingReportTargetNameValue" class="dialogValue">
					<input type="text" style="width:410px" id="schedulingReportTargetName" class="reportValue" value=""
					readonly="readonly" disabled="disabled" /><!-- /default/127.0.0.1/agent_000/common/fundamental -->
			</div>
		</div>
		<div id="schedulingReportSetTermArea" class="dialogContentArea">
			<div id="schedulingReportSetTermItem" class="dialogItem">Periodic Report Term :</div>
			
			<div id="schedulingReportSetTermValue" class="dialogValue">
				<input type="radio" name="scheduling_report_type" id="schedulingReportDaily" value="DAILY" checked />DAILY&nbsp; <!-- checked  -->
				<input type="radio" name="scheduling_report_type" id="schedulingReportWeekly" value="WEEKLY" />WEEKLY&nbsp;
				<input type="radio" name="scheduling_report_type" id="schedulingReportMonthly" value="MONTHLY" />MONTHLY&nbsp;
			</div>
			<div id="schedulingReportSetTermChoosingItem" >
			<div id="schedulingReportDayAndDate"></div>
			<div id="schedulingReportTime"></div>
			
			</div>
		</div>
			</div>
</body>
</html>