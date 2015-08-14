ENS.threadDump.isTrue = false;

ENS.threadDumpView = wgp.AbstractView
		.extend({
			tableColNames : [ "Time", "Detail", "Download", "logFileName",
					"threadDumpInfo" ],
			initialize : function(argument, treeSettings) {
				var instance = this;
				this.tableMargin = 50;
				this.tableWidth = parseInt($("#persArea_drop_0_1").width()
						- this.tableMargin);
				this.tableColModel = this.createTableColModel();
				var appView = new ENS.AppView();
				this.appView = appView;
				this.treeSettings = treeSettings;
				this.argument = argument;
				appView.addView(this, "JvnLog_Notify");
				appView.addView(this, treeSettings.treeId
						+ ENS.URL.JVN_LOG_NOTIFY_POSTFIX_ID);
				var dualSliderId = this.id + "_dualSlider";
				// dual slider area (add div and css, and make slider)
				$("#" + this.id)
						.append('<div id="' + dualSliderId + '"></div>');
				$('#' + dualSliderId).css(
						ENS.nodeinfo.parent.css.dualSliderArea);
				$('#' + dualSliderId).css(
						ENS.nodeinfo.parent.css.dualSliderArea);
				this.dualSliderView = new ENS.DualSliderView({
					id : dualSliderId,
					rootView : this
				});
				// 空のテーブルを作成
				this.render();
				this.id = argument.id;
				this.argument.term = argument.term;
				var startTime = new Date(new Date().getTime() - argument.term
						* 1000);
				var endTime = new Date();
				appView.getTermThreadDumpData([ treeSettings.treeId ],
						startTime, endTime, argument.maxLineNum);
				this.id = argument.id;
				this.dualSliderView.setScaleMovedEvent(function(from, to) {
					var appView = new ENS.AppView();
					var startTime = new Date(new Date().getTime() - from);
					var endTime = new Date(new Date().getTime() - to);
					appView.getTermThreadDumpData([ treeSettings.treeId ],
							startTime, endTime, 100);
				});
				$("#button").on("click", function() {
					$("#button").attr("disabled", true);
					ENS.threadDump.isTrue = true;
					ENS.tree.agentName = instance.treeSettings.treeId;
					var settings = {
						data : {
							threadDump : ENS.tree.agentName
						},
						url : ENS.tree.THREAD_DUMP_CLICK
					};
					var ajaxHandler = new wgp.AjaxHandler();
					ajaxHandler.requestServerAsync(settings);
				});
			},
			render : function() {
				var id = this.id;
				$("#" + id).append('<div id="threadDumpDiv"></div>');
				$("#threadDumpDiv").css({
					"margin-left" : 5
				});
				$("#threadDumpDiv")
						.append(
								"<input type='button' class='default-btn' id='button' value='get Thread Dump'>");
				$("#threadDumpDiv")
						.append("<div class='clear-float'></div>");
				$("#button").css(
						{
							"margin-left" : this.tableWidth - 136 + "px",
						});
				$("#threadDumpDiv").append(
						'<table id="threadDumpTable"></table>');
				$("#threadDumpDiv")
						.append('<div id="threadDumpPager"></table>');
				$("#threadDumpTable").jqGrid({
					datatype : "local",
					data : "",
					colModel : this.tableColModel,
					colNames : this.tableColNames,
					caption : "ThreadDump of " + this.treeSettings.id,
					pager : "threadDumpPager",
					rowNum : 20,
					rowList : [ 20, 50, 100 ],
					pgbuttons : true,
					pginput : true,
					height : "auto",
					width : this.tableWidth,
					sortname : "date",
					sortorder : "desc",
					viewrecords : true,
					rownumbers : true,
					shrinkToFit : false,
					cellEdit : true,
					scrollerbar : true,
					cmTemplate : {
						title : false
					}
				});
				$("#threadDumpTable").filterToolbar({
					defaultSearch : 'cn'
				});
				$("#threadDumpDiv").css('font-size', '0.8em');
			},
			_parseModel : function(model) {
				var instance = this;
				var threadInfoData = instance
						.changedData(model.attributes.threadDumpInfo);
				model.attributes.threadDumpInfo = threadInfoData;
				var tableData = model.attributes;
				return tableData;
			},
			onAdd : function(element) {
				if (ENS.threadDump.isTrue) {
					var agentName = ENS.tree.agentName;
					var instance = this;
					var settings = {
						data : {
							threadDump : agentName
						},
						url : ENS.tree.THREADDUMP_AGENT_SELECT_ALL_URL
					};
					var ajaxHandler = new wgp.AjaxHandler();
					settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
					settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = "callbackThreadDumpList";
					ajaxHandler.requestServerAsync(settings);
					ENS.threadDump.isTrue = false;
				}
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
				$("#button").attr("disabled", false);
			},
			reloadTable : function() {
				var tableViewData = [];
				var instance = this;
				_.each(this.collection.models, function(model, index) {
					tableViewData.push(instance._parseModel(model));
				});
				$("#threadDumpTable").clearGridData().setGridParam({
					data : tableViewData
				}).trigger("reloadGrid");
			},
			createTableColModel : function() {
				var tableColModel = [ {
					name : "date",
					width : parseInt(0.32 * this.tableWidth)
				}, {
					name : "detail",
					width : parseInt(0.32 * this.tableWidth),
					formatter : ENS.Utility.makeAnchor,
					editoptions : {
						"onclick" : "ENS.threadDump.dialog",
						"linkName" : "Detail"
					}
				}, {
					name : "download",
					width : parseInt(0.315 * this.tableWidth),
					formatter : ENS.Utility.makeAnchor,
					editoptions : {
						"onclick" : "ENS.threadDump.download",
						"linkName" : "Download"
					}
				}, {
					name : "logFileName",
					hidden : true
				}, {
					name : "threadDumpInfo",
					hidden : true
				} ];
				return tableColModel;
			},
			changedData : function(threadDumpInfo) {
				var changed = threadDumpInfo;
				if (changed != null) {
					changed = changed.replace(/>/g, "&gt;").replace(/</g,
							"&lt;");
					changed = changed.replace(/%/gi, "<br/>");
				}
				return changed;
			},
			removeTag : function(threadDump) {
				var instance = this;
				var threadInfoData = instance
						.changedData(threadDump.threadDumpInfo);
				threadDump.threadDumpInfo = threadInfoData;
				var tableData = threadDump;
				return tableData;
			},
			callbackThreadDumpList : function(threadDumpList) {
				var tableViewData = [];
				var instance = this;
				_.each(threadDumpList, function(threadDump, index) {
					tableViewData.push(instance.removeTag(threadDump));
				});
				$("#threadDumpTable").clearGridData().setGridParam({
					data : tableViewData
				}).trigger("reloadGrid");
			},
			makeAnchor : function(cellValue, options, rowObject) {
				var selectValueList = options.colModel.editoptions;
				var val = rowObject.value;
				var rowId = options.rowId;
				var onclick = selectValueList.onclick;
				return '<a href="javascript:void(0)" onclick="'
						+ selectValueList.onclick + ';">' + 'DL' + '</a>';
			}
		});

ENS.threadDump.download = function(id) {
	var threadDumpData = $("#threadDumpTable").getRowData(id);
	var fileName = threadDumpData.logFileName;
	$("input#fileName").val(fileName);
	$('#jvnLogBtn').click();
};

ENS.threadDump.dialog = function(id) {
	var threadDumpData = $("#threadDumpTable").getRowData(id);

	$("#threadDumpTime").empty();
	$("#threadDumpTime").append(threadDumpData.date);

	var changed = threadDumpData.threadDumpInfo;
	$("#threadDump").empty();
	$("#threadDump").append('<pre>' + changed + '</pre>');
	$("#threadDumpDialog").dialog({
		modal : true,
		width : 1200,
		height : 800,
	});
};
