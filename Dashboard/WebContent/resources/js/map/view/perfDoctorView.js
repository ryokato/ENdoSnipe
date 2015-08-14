ENS.perfDoctorView = wgp.AbstractView.extend({
	tableColNames : [ "Time", "Description", "Level", "Class Name",
			"Method Name", "Detail", "Download", "Logfile", "detailResult" ],
	initialize : function(argument, treeSettings) {

		this.tableMargin = 50;
		this.tableWidth = parseInt($("#persArea_drop_0_1").width() - this.tableMargin);
		this.tableColModel = this.createTableColModel();

		var appView = new ENS.AppView();
		this.treeSettings = treeSettings;
		appView.addView(this, treeSettings.treeId
				+ ENS.URL.PERFDOCTOR_POSTFIX_ID);

		var dualSliderId = this.id + "_dualSlider";
		// dual slider area (add div and css, and make slider)
		$("#" + this.id).append('<div id="' + dualSliderId + '"></div>');
		$('#' + dualSliderId).css(ENS.nodeinfo.parent.css.dualSliderArea);
		$('#' + dualSliderId).css(ENS.nodeinfo.parent.css.dualSliderArea);
		this.dualSliderView = new ENS.DualSliderView({
			id : dualSliderId,
			rootView : this
		});

		// 空のテーブルを作成
		this.render();

		var startTime = new Date(new Date().getTime() - argument.term * 1000);
		var endTime = new Date();
		appView.getTermPerfDoctorData([ treeSettings.treeId ], startTime,
				endTime, argument.maxLineNum);

		this.id = argument.id;

		this.dualSliderView.setScaleMovedEvent(function(from, to) {
			var appView = new ENS.AppView();
			var startTime = new Date(new Date().getTime() - from);
			var endTime = new Date(new Date().getTime() - to);
			appView.getTermPerfDoctorData([ treeSettings.treeId ], startTime,
					endTime, 100);
		});
	},
	render : function() {
		$("#" + this.id).append('<div id="journalDiv"></div>');
		$("#journalDiv").css({
			"margin-left" : 5
		});
		$("#journalDiv").append('<table id="journalTable"></table>');
		$("#journalDiv").append('<div id="journalPager"></table>');
		var height = "auto";

		$("#journalTable").jqGrid({
			datatype : "local",
			data : "",
			colModel : this.tableColModel,
			colNames : this.tableColNames,
			caption : "Diagnosis of " + this.treeSettings.id,
			pager : "journalPager",
			rowNum : 20,
			rowList : [ 20, 50, 100 ],
			pgbuttons : true,
			pginput : true,
			height : height,
			width : this.tableWidth,
			sortname : "date",
			sortorder : "desc",
			viewrecords : true,
			rownumbers : true,
			shrinkToFit : false,
			cellEdit : true,
			cmTemplate : {
				title : false
			}
		});
		$("#journalTable").filterToolbar({
			defaultSearch : 'cn'
		});
		$("#journalDiv").css('font-size', '0.8em');
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
	reloadTable : function() {
		var tableViewData = [];
		var instance = this;
		_.each(this.collection.models, function(model, index) {
			tableViewData.push(instance._parseModel(model));
		});

		$("#journalTable").clearGridData().setGridParam({
			data : tableViewData
		}).trigger("reloadGrid");
	},
	createTableColModel : function() {
		var tableColModel = [ {
			name : "date",
			width : parseInt(this.tableWidth * 0.09)
		}, {
			name : "description",
			width : parseInt(this.tableWidth * 0.38)
		}, {
			name : "level",
			width : parseInt(this.tableWidth * 0.06)
		}, {
			name : "className",
			width : parseInt(this.tableWidth * 0.12)
		}, {
			name : "methodName",
			width : parseInt(this.tableWidth * 0.12)
		}, {
			name : "detailInfo",
			// width : 140,
			width : parseInt(this.tableWidth * 0.07),
			formatter : ENS.Utility.makeAnchor2,
			editoptions : {
				"onclick" : "ENS.perfDoctor.dialog",
				"linkName" : "Detail"
			}
		}, {
			name : "detail",
			width : parseInt(this.tableWidth * 0.1),
			formatter : ENS.Utility.makeAnchor2,
			editoptions : {
				"onclick" : "ENS.perfDoctor.download",
				"linkName" : "Download"
			}
		}, {
			name : "logFileName",
			width : 0,
			hidden : true
		}, {
			name : "detailResult",
			width : 0,
			hidden : true
		} ];

		return tableColModel;
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

ENS.perfDoctor.download = function(id) {
	var rowData = $("#journalTable").getRowData(id);
	var fileName = rowData.logFileName;
	$("input#fileName").val(fileName);
	$('#jvnLogBtn').click();
};

ENS.perfDoctor.dialog = function(id) {
	var rowData = $("#journalTable").getRowData(id);

	$("#perDocTime").empty();
	$("#perDocTime").append(rowData.date);

	$("#perDocDescription").empty();
	$("#perDocDescription").append(rowData.description);

	$("#perDocLevel").empty();
	$("#perDocLevel").append(rowData.level);

	$("#perDocClassName").empty();
	$("#perDocClassName").append(rowData.className);

	$("#perDocMethodName").empty();
	$("#perDocMethodName").append(rowData.methodName);

	$("#perDocLogFileName").empty();
	$("#perDocLogFileName").append(rowData.logFileName);

	var changed = rowData.detailResult;
	changed = changed.replace(/>/g, "&gt;").replace(/</g, "&lt;");
	changed = changed.replace(/%/gi, "<br/>");
	$("#perDocDetail").empty();
	$("#perDocDetail").append(changed);
	$("#performanceDoctorDialog").dialog({
		modal : true,
		width : 1200,
		height : 800,
	});
};
