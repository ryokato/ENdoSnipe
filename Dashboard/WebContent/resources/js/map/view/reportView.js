ENS.reportView = wgp.AbstractView
		.extend({
			tableColNames : [ "Id", "Start Time", "Finish Time", "Download",
					"", "Status" ],
			initialize : function(argument, treeSettings) {
				var appView = new ENS.AppView();
				this.treeSettings = treeSettings;
				var treeId = treeSettings.treeId;
				appView.addView(this, treeId + ENS.URL.PERFDOCTOR_POSTFIX_ID);

				this.tableMargin = 50;
				this.tableWidth = parseInt($("#" + this.id).width()
						- this.tableMargin);

				// 空のテーブルを作成
				this.createTabelColModel();
				this.render();
				
				this.selectedRowId = 0;

				var reportName = treeSettings.parentTreeId + "/"
						+ treeSettings.data;

				this.getReportList(reportName);
			},
			render : function() {
				$("#" + this.id).append('<div id="reportDiv"></div>');
				$("#reportDiv").css({
					"margin-top" : 20,
					"margin-left" : 15
				});
				$("#reportDiv").append('<table id="reportTable"></table>');
				$("#reportDiv").append('<div id="reportPager"></table>');
				var height = "auto";

				$("#reportTable").jqGrid(
						{
							datatype : "local",
							data : "",
							colModel : this.tableColModel,
							colNames : this.tableColNames,
							caption : "Report List of "
									+ this.treeSettings.parentTreeId + "/"
									+ this.treeSettings.data,
							pager : "reportPager",
							rowList : [ 20, 50, 100 ],
							pgbuttons : true,
							pginput : true,
							height : height,
							width : this.tableWidth,
							sortname : "reportTermFrom",
							sortorder : "asc",
							viewrecords : true,
							rownumbers : true,
							shrinkToFit : false,
							cellEdit : true,
							cmTemplate : {
								title : false
							}
						});
				$("#reportTable").filterToolbar({
					defaultSearch : 'cn'
				});
				$("#reportDiv").css('font-size', '0.8em');
			},
			_parseModel : function(model) {
				var tableData = model.attributes;

				return tableData;
			},
			onAdd : function(element) {
				console.log('call onAdd');
			},
			onChange : function(element) {
				console.log('called changeModel');
			},
			onRemove : function(element) {
				console.log('called removeModel');
			},
			onComplete : function(element) {
				if (element == wgp.constants.syncType.SEARCH) {
					appView.syncData();
				}
				this.reloadTable();
			},
			createTabelColModel : function() {
				this.tableColModel = [ {
					name : "reportId",
					width : 0,
					hidden : true
				}, {
					name : "reportTermFrom",
					width : parseInt(this.tableWidth * 0.33)
				}, {
					name : "reportTermTo",
					width : parseInt(this.tableWidth * 0.33)
				}, {
					name : "download",
					width : parseInt(this.tableWidth * 0.098),
					formatter : ENS.Utility.makeAnchor,
					editoptions : {
						"onclick" : "ENS.report.download",
						"linkName" : "Download"
					}
				}, {
					name : "Delete",
					width : parseInt(this.tableWidth * 0.087),
					formatter : ENS.Utility.makeAnchor,
					editoptions : {
						"onclick" : "ENS.report.deleteNode",
						"linkName" : "Delete"
					}
				}, {
					name : "status",
					width : parseInt(this.tableWidth * 0.1)
				} ];
			},
			reloadTable : function() {
				var tableViewData = [];
				var instance = this;
				_.each(this.collection.models, function(model, index) {
					tableViewData.push(instance._parseModel(model));
				});

				$("#reportTable").clearGridData().setGridParam({
					data : tableViewData
				}).trigger("reloadGrid");
			},
			getReportList : function(reportName) {

				// レポート出力定義を取得する
				// Ajax通信用の設定
				var settings = {
					data : {
						reportName : reportName
					},
					url : ENS.tree.REPORT_SELECT_BY_REPORT_NAME_URL
				};

				// 非同期通信でデータを送信する
				var ajaxHandler = new wgp.AjaxHandler();
				settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
				settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = "callbackGetReportList";
				ajaxHandler.requestServerAsync(settings);
			},
			callbackGetReportList : function(reportList) {
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
					var fmTimeDate = new Date(report.reportTermFrom);
					var toTimeDate = new Date(report.reportTermTo);

					var fmTimeStr = instance.getDateString(fmTimeDate);
					var toTimeStr = instance.getDateString(toTimeDate);

					report.reportTermFrom = fmTimeStr;
					report.reportTermTo = toTimeStr;

					tableViewData.push(report);
				});

				$("#reportTable").clearGridData().setGridParam({
					data : tableViewData
				}).trigger("reloadGrid");
			},
			getDateString : function(date) {
				// 年月日の取得
				var month = date.getMonth() + 1;
				var day = date.getDate();
				var hour = date.getHours();
				var minute = date.getMinutes();
				var second = date.getSeconds();

				// 1桁を2桁に変換する
				if (month < 10) {
					month = "0" + month;
				}
				if (day < 10) {
					day = "0" + day;
				}
				if (hour < 10) {
					hour = "0" + hour;
				}
				if (minute < 10) {
					minute = "0" + minute;
				}
				if (second < 10) {
					second = "0" + second;
				}

				// 整形して返却
				return date.getFullYear() + "/" + month + "/" + day + " "
						+ hour + ":" + minute + ":" + second;
			}
		});

ENS.report.download = function(id) {
	
	var event = {
		"file_name" : "20130512_133200-20130514_133200.zip"
	};

	var rowData = $("#reportTable").getRowData(id);
	var reportId = rowData.reportId;
	var status= rowData.status;
	if(status!="completed")
		{
		alert("Cannot Download yet because it does not finish to create!");
		return;
		}
	$("input#reportId").val(reportId);
	$('#btn').click();
};

ENS.report.callbackDownload = function(response) {
	alert(response);
};
ENS.report.deleteNode = function(id) {
	var rowData = $("#reportTable").getRowData(id);
	var ids = rowData.reportId;
	var url = ENS.tree.REPORT_DELETE_BY_ID_URL;
	this.selectedRowId = id;

	var settings = {
		data : {
			reportId : ids
		},
		url : url
	};
	settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
	settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = "_callbackDeleteNode";
	var ajaxHandler = new wgp.AjaxHandler();
	ajaxHandler.requestServerAsync(settings);
};

ENS.report._callbackDeleteNode = function(data) {
	if(data.result === "failure" ){
		alert("Delete the report is failed.");
		return;
	}
	var grid = $("#reportTable");
	var id = this.selectedRowId;
	grid.delRowData(id);
};