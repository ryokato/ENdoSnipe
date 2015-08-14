ENS.schedulingReportDialog.rowId = {};

ENS.schedulingReportView = wgp.AbstractView
		.extend({
			tableColNames : [ "Id", "Report Name", "Report Target", "Schedule",
					"Time", "Day", "Date", "" ],
			initialize : function(argument, treeSettings) {
			
				var appView = new ENS.AppView();
				this.treeSettings = treeSettings;
				this.tableMargin = 50;
				this.tableWidth = parseInt($("#persArea_drop_0_1").width()
						- this.tableMargin);

				// 空のテーブルを作成
				this.createTabelColModel();
				this.render();
				var nodeName = treeSettings.treeId;
				this.getAllScheReportList_(nodeName);
			},
			render : function() {
				$("#" + this.id).append('<div id="scheduleReportDiv"></div>');
				$("#scheduleReportDiv").css({
					"margin-left" : 5
				});
				$("#scheduleReportDiv").append('<table id="scheduleReportTable"></table>');
				$("#scheduleReportDiv").append('<div id="reportPager"></table>');
				var height = "auto";

				$("#scheduleReportTable").jqGrid(
						{
							datatype : "local",
							data : "",
							colModel : this.tableColModel,
							colNames : this.tableColNames,
							caption : "Report List of "
									+ this.treeSettings.parentTreeId + "/"
									+ this.treeSettings.data,
							pager : "reportPager",
							rowNum : 20,
							rowList : [ 20, 50, 100 ],
							pgbuttons : true,
							pginput : true,
							height : height,
							width : this.tableWidth,
							viewrecords : true,
							rownumbers : true,
							shrinkToFit : false,
							cellEdit : true,
							cmTemplate: { title: false }
						});
				$("#scheduleReportTable").filterToolbar({
					defaultSearch : 'cn'
				});
				$("#scheduleReportDiv").css('font-size', '0.8em');
			},
			_parseModel : function(model) {
				var tableData = model.attributes;

				return tableData;
			},
			createTabelColModel : function() {
				this.tableColModel = [ {
					name : "reportId",
					width : 0,
					hidden : true
				}, {
					name : "reportName",
					width : this.tableWidth * 0.25
				}, {
					name : "targetMeasurementName",
					width : this.tableWidth * 0.34
				}, {
					name : "term",
					width : this.tableWidth * 0.08
				}, {
					name : "time",
					width : this.tableWidth * 0.068
				}, {
					name : "day",
					width : this.tableWidth * 0.06
				}, {
					name : "date",
					width : this.tableWidth * 0.06
				}, {
					name : "",
					width : this.tableWidth * 0.075,
					formatter : ENS.Utility.makeAnchor1,
					editoptions : {
						"onclick1" : "ENS.report.updateSchedulingReport",
						"updateLink" : "Update",
						"onclick2" : "ENS.report.deleteSchedulingReport",
						"deleteLink" : "Delete"
					}

				} ];

			},
			getAllScheReportList_ : function(nodeName) {

				// レポート出力定義を取得する
				// Ajax通信用の設定
				var settings = {
						data : {
							nodeName : nodeName
						},
					url : ENS.tree.SCHEDULING_REPORT_SELECT_ALL_URL_BY_AGENT
				};

				// 非同期通信でデータを送信する
				var ajaxHandler = new wgp.AjaxHandler();
				settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
				settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = "callbackGetAllScheReportList_";
				ajaxHandler.requestServerAsync(settings);
			},
			callbackGetAllScheReportList_ : function(reportList) {
				var tableViewData = [];
				var instance = this;
				_.each(reportList, function(report, index) {
					// レポート名についている余計なパスを除外する
					var reportPath = report.reportName;
					var reportPathSplitList = reportPath.split("/");
					var reportPathLength = reportPathSplitList.length;
					var reportName = reportPathSplitList[reportPathLength - 1];
					report.reportName = reportName;

					// 開始日と終了日のフォーマットを変更する
					var timeDate = new Date(report.time);
					var timeStr = instance.getDateString(timeDate);
					report.time = timeStr;

					tableViewData.push(report);
				});

				$("#scheduleReportTable").clearGridData().setGridParam({
					data : tableViewData
				}).trigger("reloadGrid");
			},
			getDateString : function(time) {
				var hour = time.getHours();
				var minute = time.getMinutes();

				if (hour < 10) {
					hour = "0" + hour;
				}
				if (minute < 10) {
					minute = "0" + minute;
				}

				return hour + ":" + minute;
			}

		});

ENS.report.updateSchedulingReport = function(rowId) {

	var rowData = $("#scheduleReportTable").jqGrid('getRowData', rowId);
	ENS.schedulingReportDialog.rowId = rowId;
	var term = rowData.term;
	var timeSplit = rowData.time.split(":");

	$("#schedulingReportId").val(rowData.reportId);
	$("#beforeSchedulingReportName").val(rowData.reportName);
	$("#schedulingReportName").val(rowData.reportName);
	$("#schedulingReportTargetName").val(rowData.targetMeasurementName);
	// シグナル名の表示名称は自身より親のツリー構造を除外した値を指定する。

	var viewClassName = new ENS.SchedulingReportDialogView({
		dialogId : ENS.tree.REPORT_SCHEDULE_DIALOG,
		signalType : ENS.tree.EDIT_SCHEDULE_TYPE
	});

	if (term == "WEEKLY") {

		$("#schedulingReportWeekly").attr('checked', 'checked');
		$("#schedulingReportWeekly").change();
		$('#schedulingReportSelectedWeeklyId option').each(function() {
			if (this.value == rowData.day) {
				$(this).prop("selected", "selected");
				return;
			}
		});

	} else if (term == "MONTHLY") {

		$("#schedulingReportMonthly").attr('checked', 'checked');
		$("#schedulingReportMonthly").change();
		$('#schedulingReportSelectedMonthlyId option').each(function() {
			if (this.value == rowData.date) {
				$(this).prop("selected", "selected");
				return;
			}
		});
	}
	$('#schedulingReportSelectedTimeId option').each(function() {
		if (this.value == timeSplit[0]) {
			$(this).prop("selected", "selected");
			return;
		}
	});

	$('#schedulingReportSelectedTimeMinuteId option').each(function() {
		if (this.value == timeSplit[1]) {
			$(this).prop("selected", "selected");
			return;
		}
	});

};
ENS.report.deleteSchedulingReport = function(rowId) {
	var grid = $("#scheduleReportTable");
	var rowData = $("#scheduleReportTable").jqGrid('getRowData', rowId);
	grid.delRowData(rowId);
	var settings = {
		
		data : {
			reportId : rowData.reportId
		},
		url : ENS.tree.REPORT_SCHEDULE_DELETE_BY_ID_URL
	};
	var ajaxHandler = new wgp.AjaxHandler();
	settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
	ajaxHandler.requestServerAsync(settings);
};
ENS.report.callbackDownload = function(response) {
	alert(response);
};