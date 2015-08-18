<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet"
	href="<%=request.getContextPath()%>/resources/css/dialog/SignalDialog.css"
	type="text/css" media="all">

</head>
<body>
	<div id="persArea"></div>
	<input id="treeData" type="hidden" value='${treeData}' />
	<div id="signalDialog" title="Signal Definition" style="display:none">
		<div id="signalNameArea" class="dialogContentArea" style="display:none">
			<div id="signalIdValue" class="dialogValue">
				<input type="text" id="signalId" class="signalValue" value="" />
			</div>
		</div>
		<div id="signalNameArea" class="dialogContentArea">
			<div id="signalNameItem" class="dialogItem">Signal Name : </div>
			<div id="signalNameValue" class="dialogValue">
				<input type="text" id="signalName" class="signalValue" value="new signal" />
				<input type="hidden" id="beforeSignalName" class="signalValue" value="" />
			</div>
		</div>
		<div id="signalMatchingPatternArea" class="dialogContentArea">
			<div id="signalMatchingPatternItem" class="dialogItem">Matching Pattern : </div>
			<div id="signalMatchingPatternValue" class="dialogValue">
				<input type="text" id="matchingPattern" class="signalValue" value="" />
			</div>
		</div>
		<div id="signalPatternArea" class="dialogContentArea">
			<div id="signalPatternItem" class="dialogItem">Signal Pattern : </div>
			<div id="signalPatternValue" class="dialogValue">
				<select>
					<option value="3">3 Level</option>
					<option value="5">5 Level</option>
				</select>
			</div>
		</div>
		<div id="signalPatternValueArea" class="dialogContentArea">
			<div id="signalPatternValueItem" class="dialogItem">Threshold Value of each signal levels : </div>
			<div id="signalPatternValueValue" class="dialogValue">
				<div id="signalPatternValue_1" class="dialogPatternValue">
					<div class="levelLabel">INFO : </div>
					<input type="text" id="patternValue_1" class="signalValue" value="" pattern="^([+-]?)([1-9]\d*|0)(\.\d+)?$" />
				</div>
				<div id="signalPatternValue_2" class="dialogPatternValue">
					<div class="levelLabel">WARNING : </div>
					<input type="text" id="patternValue_2" class="signalValue" value="" pattern="^([+-]?)([1-9]\d*|0)(\.\d+)?$" />
				</div>
				<div id="signalPatternValue_3" class="dialogPatternValue">
					<div class="levelLabel">ERROR : </div>
					<input type="text" id="patternValue_3" class="signalValue" value="" pattern="^([+-]?)([1-9]\d*|0)(\.\d+)?$" />
				</div>
				<div id="signalPatternValue_4" class="dialogPatternValue">
					<div class="levelLabel">CRITICAL : </div>
					<input type="text" id="patternValue_4" class="signalValue" value="" pattern="^([+-]?)([1-9]\d*|0)(\.\d+)?$" />
				</div>
			</div>
		</div>
		<div class="clearFloat"></div>
		<div id="signalEscalationPeriodArea" class="dialogContentArea">
			<div id="signalEscalationPeriodItem" class="dialogItem">Escalation Period : </div>
			<div id="signalEscalationPeriodValue" class="dialogValue">
				<input type="text" id="escalationPeriod" class="signalValue" value="15" pattern="^([1-9]\d*|0)(\.\d+)?$" />
			</div>
			<div id="signalEscalationPeriodUnit">s</div>
		</div>
		<div id="signalAlertMailArea" class="dialogContentArea">
		<input type="checkbox" name="signalAlertMail" id="signalAlertMail" value="true"><label for="signalAlertMail">Send Mail</label></input>
		</div>
	</div>
</body>
</html>