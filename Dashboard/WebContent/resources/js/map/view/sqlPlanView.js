ENS.sqlPlanView = wgp.AbstractView
		.extend({
			tableColNamesList : {
				"sqlStatement" : [ "Query" ],
				"sqlExecutePlan" : [ "Get Time", "Query Plan" ],
				"stackTrace" : [ "Stack Trace" ]
			},
			initialize : function(argument, treeSettings) {

				var appView = new ENS.AppView();
				this.treeSettings = treeSettings;
				var treeId = treeSettings.treeId;
				appView.addView(this, treeId);

				this.tableMargin = 20;
				this.tableWidth = $("#" + this.id).width() - this.tableMargin
						* 3;

				this.createTableColModelsList();

				// 空のテーブルを作成
				this.render();

				this.getSqlPlan(treeId);
			},
			render : function() {
				var divId = "sqlPlanDiv";
				$("#" + this.id).append('<div id="' + divId + '"></div>');
				$("#" + divId).css({
					"margin-top" : 20,
					"margin-left" : 15
				});

				// 各テーブルを作成する
				this.createSqlStatementTabel(divId);
				this.createSqlExecutePlanTable(divId);
				this.createStackTraceTable(divId);

				$(".ui-jqgrid-sortable").css("font-size", "13px");
			},
			onAdd : function(element) {
				// Do nothing
			},
			onChange : function(element) {
				// Do nothing
			},
			onRemove : function(element) {
				// Do nothing
			},
			onComplete : function(element) {
				// Do nothing
			},
			createTableColModelsList : function() {

				this.tableColModelsList = {
					"sqlStatement" : [ {
						name : "sqlStatement",
						width : this.tableWidth - 5
					} ],
					"sqlExecutePlan" : [ {
						name : "gettingPlanTime",
						width : 150
					}, {
						name : "executePlan",
						width : this.tableWidth - 160
					} ],
					"stackTrace" : [ {
						name : "stackTrace",
						width : this.tableWidth - 35
					} ]
				};
			},
			createSqlStatementTabel : function(divId) {
				$("#" + divId).append('<table id="sqlStatementTable"></table>');

				$("#sqlStatementTable").jqGrid({
					datatype : "local",
					data : "",
					colModel : this.tableColModelsList["sqlStatement"],
					colNames : this.tableColNamesList["sqlStatement"],
					caption : "Query",
					height : "auto",
					width : this.tableWidth,
					shrinkToFit : false,
					cellEdit : true,
					cmTemplate: { title: false }
				});

				$("#gbox_sqlStatementTable").css({
					"margin-bottom" : this.tableMargin,
				});

				$("#sqlStatementTable").css('font-size', '13px');
				$("#sqlStatementTable").css('word-break', 'break-all');
			},
			createSqlExecutePlanTable : function(divId) {
				$("#" + divId).append(
						'<table id="sqlExecutePlanTable"></table>');

				$("#sqlExecutePlanTable").jqGrid({
					datatype : "local",
					data : "",
					colModel : this.tableColModelsList["sqlExecutePlan"],
					colNames : this.tableColNamesList["sqlExecutePlan"],
					caption : "Query Plan",
					rowNum : 5,
					rowList : [ 5, 3, 1 ],
					pgbuttons : true,
					pginput : true,
					height : "auto",
					width : this.tableWidth,
					sortname : "stackTrace",
					sortorder : "desc",
					viewrecords : true,
					rownumbers : false,
					shrinkToFit : false,
					cellEdit : true,
					cmTemplate: { title: false }
				});

				$("#gbox_sqlExecutePlanTable").css({
					"margin-bottom" : this.tableMargin
				});

				$("#sqlExecutePlanTable").css('font-size', '13px');
				$("#sqlExecutePlanTable").css('word-break', 'break-all');
			},
			createStackTraceTable : function(divId) {
				$("#" + divId).append('<table id="stackTraceTable"></table>');
				$("#" + divId).append('<div id="stackTracePager"></table>');

				$("#stackTraceTable").jqGrid({
					datatype : "local",
					data : "",
					colModel : this.tableColModelsList["stackTrace"],
					colNames : this.tableColNamesList["stackTrace"],
					caption : "Stack Trace",
					pager : "stackTracePager",
					rowList : [ 5, 3, 1 ],
					pgbuttons : true,
					pginput : true,
					height : "auto",
					width : this.tableWidth,
					sortname : "gettingPlanTime",
					sortorder : "desc",
					viewrecords : true,
					rownumbers : true,
					shrinkToFit : false,
					cellEdit : true,
					cmTemplate: { title: false }
				});

				$("#gbox_stackTraceTable").css({
					"margin-bottom" : this.tableMargin
				});

				$("#stackTraceTable").css('font-size', '13px');
				$("#stackTraceTable").css('word-break', 'break-all');
				$("#stackTracePager").css('font-size', '13px');
			},
			getSqlPlan : function(itemName) {
				// SQL文、SQL実行計画、スタックトレースを取得する
				// Ajax通信用の設定
				var settings = {
					data : {
						itemName : itemName
					},
					url : ENS.tree.GET_SQL_PLAN
				};

				// 非同期通信でデータを送信する
				var ajaxHandler = new wgp.AjaxHandler();
				settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
				settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = "callbackGetSqlPlan";
				ajaxHandler.requestServerAsync(settings);
			},
			callbackGetSqlPlan : function(sqlPlan) {
				var sqlStatement = sqlPlan.sqlStatement;
				var executionPlanList = sqlPlan.executionPlanList;
				var gettingPlanTimeList = sqlPlan.gettingPlanTimeList;
				var stackTraceList = sqlPlan.stackTraceList;

				if (sqlStatement) {
					// 各テーブル用データを作成し、テーブルごとにリロードする
					var sqlStatementTableData = [ {
						"sqlStatement" : sqlStatement
					} ];

					$("#sqlStatementTable").clearGridData().setGridParam({
						data : sqlStatementTableData
					}).trigger("reloadGrid");
				}

				if (executionPlanList && gettingPlanTimeList) {
					var executionPlanTableData = [];
					_
							.each(
									executionPlanList,
									function(executionPlan, index) {

										if (executionPlan) {
											executionPlan = executionPlan
													.replace(/\n/g, "<br>");
											executionPlan = executionPlan
													.replace(/\s\s/g, "　");
										}

										var data = {
											"gettingPlanTime" : gettingPlanTimeList[index],
											"executePlan" : executionPlan
										};
										executionPlanTableData.push(data);
									});

					$("#sqlExecutePlanTable").clearGridData().setGridParam({
						data : executionPlanTableData
					}).trigger("reloadGrid");
				}

				if (stackTraceList) {
					var stackTraceTableData = [];

					_
							.each(
									stackTraceList,
									function(stackTrace, index) {
										var data = {
											"stackTrace" : stackTrace
										};
										stackTraceTableData.push(data);
									});

					$("#stackTraceTable").clearGridData().setGridParam({
						data : stackTraceTableData
					}).trigger("reloadGrid");
				}
			}
		});