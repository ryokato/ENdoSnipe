ENS.profilerView = wgp.AbstractView
		.extend({
			tableColNames : [ "classMethodId", "Response", "Target", "Class Name",
					"Method Name", "TAT Threshold", "CPU Threshold",
					"Call Count", "Total Time", "Average Time", "Maximum Time",
					"Minimum Time", "Total CPU Time", "Average CPU Time",
					"Maximum CPU Time", "Minimum CPU Time", "Total User Time",
					"Average User Time", "Maximum User Time",
					"Minimum User Time", "Throwable Count",
					"profileValueChanged" ],
			initialize : function(argument, treeSettings) {
				var instance = this;
				this.tableMargin = 50;
				this.tableWidth = parseInt($("#persArea_drop_0_1").width()
						- this.tableMargin);
				this.tableColModel = this.createTableColModel();

				var appView = new ENS.AppView();
				this.treeSettings = treeSettings;
				appView.addView(this, treeSettings.treeId
						+ ENS.URL.PROFILER_POSTFIX_ID);

				// 空のテーブルを作成
				this.render();

				$("#reloadButton").on("click", function() {
					instance.collection.models = [];
					ENS.tree.agentName = instance.treeSettings.treeId;
					var settings = {
						data : {
							agentName : ENS.tree.agentName
						},
						url : ENS.tree.PROFILER_RELOAD
					};
					var ajaxHandler = new wgp.AjaxHandler();
					ajaxHandler.requestServerAsync(settings);
				});

				$("#resetButton").on("click", function() {
					ENS.tree.agentName = instance.treeSettings.treeId;
					var settings = {
						data : {
							agentName : ENS.tree.agentName
						},
						url : ENS.tree.PROFILER_RESET
					};
					var ajaxHandler = new wgp.AjaxHandler();
					ajaxHandler.requestServerAsync(settings);
				});

				$("#updateButton").on("click", function() {
					var rowIDs = $("#profilerTable").getDataIDs();
					$.each(rowIDs, function(i, item) {
						var itemEscaped = ENS.Utility.createFilterParameter(item);
						$("#profilerTable").setCell(item, 'profileValueChanged', 'false', "", "");
						if ($("#profilerTable").getCell(item, "target") == "false") {
							$('#' + itemEscaped).removeClass('ui-widget-content');
							$('#' + itemEscaped).css('background-color', 'gray');
						} else {
							$('#' + itemEscaped).addClass('ui-widget-content');
						}
					});
					ENS.tree.agentName = instance.treeSettings.treeId;
					var changedCells = JSON.stringify($("#profilerTable").getChangedCells());
					var settings = {
							data : {
								agentName : ENS.tree.agentName,
								invocations : changedCells
							},
							url : ENS.tree.PROFILER_UPDATE
					};
					var ajaxHandler = new wgp.AjaxHandler();
					ajaxHandler.requestServerAsync(settings);
				});

				$("#downloadButton").on("click", function() {
					var iframe =  $('iframe[id="profilerIframe"]')[0].contentDocument;
					var $iframeBody = $(iframe.documentElement).children("body");
					
					var $oldForm = $iframeBody.filter("form");
					if ($oldForm.size() > 0) {
						$oldForm.remove();
					}

					ENS.tree.agentName = instance.treeSettings.treeId;
					var $form = $('<form name="profileForm" method="POST"/>')
					$form.attr('action', ENS.tree.PROFILER_DOWNLOAD);
					$form.append($('<input type="hidden" name="agentName" value="' + ENS.tree.agentName +'"/>'));
					$iframeBody.append($form);
					$form.submit();
				});
				this.id = argument.id;
			},
			render : function() {
				$("#" + this.id).append('<div id="profilerDiv"></div>');
				$("#profilerDiv").css({
					"margin-left" : 5,
					"margin-top" : 5
				});
				$("#profilerDiv")
						.append(
								"<input type='button' class='default-btn' id='reloadButton' value='reload'>");
				
				$("#reloadButton").css({
					"margin-left": this.tableWidth - 290 + "px"
				});
				
				$("#profilerDiv").append(
						"<input type='button' class='default-btn' id='resetButton' value='reset'>");
				$("#profilerDiv")
						.append(
								"<input type='button' class='default-btn' id='updateButton' value='update'>");
				$("#profilerDiv")
				.append(
						"<input type='button' class='default-btn' id='downloadButton' value='download'>");
				$("#profilerDiv").append('<table id="profilerTable"></table>');
				$("#profilerDiv").append('<div id="profilerPager"></table>');
				var height = "auto";

				$("#profilerTable").jqGrid(
						{
							datatype : "local",
							data : "",
							colModel : this.tableColModel,
							colNames : this.tableColNames,
							caption : "Profile of " + this.treeSettings.id,
							pager : "profilerPager",
							rowNum : 10000,
							pgbuttons : true,
							pginput : true,
							height : height,
							width : this.tableWidth,
							// sortname : "date",
							sortorder : "desc",
							viewrecords : true,
							rownumbers : true,
							shrinkToFit : false,
							cellEdit : true,
							cmTemplate : {
								title : false
							},
							cellsubmit : 'clientArray',
							loadComplete : function() {
								var rowIDs = $("#profilerTable").getDataIDs();
								$.each(rowIDs, function(i, item) {
									if ($("#profilerTable").getCell(item,
											"profileValueChanged") == "true") {
										item = ENS.Utility.createFilterParameter(item);
										$('#' + item).removeClass(
												'ui-widget-content');
										$('#' + item).css('background-color',
												'#FF8080');
									} else if ($("#profilerTable").getCell(
											item, "target") == "false") {
										item = ENS.Utility.createFilterParameter(item);
										$('#' + item).removeClass(
												'ui-widget-content');
										$('#' + item).css('background-color',
												'gray');
									}
								});
							}
						});
				$("#profilerTable").filterToolbar({
					defaultSearch : 'cn'
				});
				$("#profilerDiv").css('font-size', '0.8em');
				
				// Add iframe to download.
				var $iframe= $("<iframe id='profilerIframe'><body /></iframe>");
				$iframe.hide();
				$("#" + this.id).append($iframe);
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

				$("#profilerTable").clearGridData().setGridParam({
					data : tableViewData
				}).trigger("reloadGrid");
			},
			createTableColModel : function() {
				var cellValueBefore;
				var onEditFocusFunc = function(e) {
					cellValueBefore = e.target.value;
				};
				var onEditKeyDown = function(e) {
					if (cellValueBefore != e.target.value) {
						var key = e.charCode || e.keyCode;
						if (key == 13)// enter
						{ 
							var rowId = e.target.id.split("_")[0];
							rowId = $('#profilerTable').getDataIDs()[parseInt(rowId) -1];
							var rowIdEscaped = ENS.Utility.createFilterParameter(rowId);
							$('#' + rowIdEscaped).removeClass(
							'ui-widget-content');
							$('#' + rowIdEscaped).css('background-color',
							'#FF8080');
							$("#profilerTable").setCell(rowId,
									'profileValueChanged', 'true',
									"", "");
						}
					}
				};
				var tableColModel = [
						{
							name : "classMethodId",
							width : 0,
							hidden : true,
							key : true
						},
						{
							name : "transactionGraph",
							width : parseInt(this.tableWidth * 0.05),
							editable : true,
							edittype : "select",
							editoptions : {
								value : "true:true;false:false",
								dataEvents : [ {
									type : 'change',
									fn : function(e) {
										var rowId = e.target.id.split("_")[0];
										rowId = $('#profilerTable').getDataIDs()[parseInt(rowId) -1];
										var rowIdEscaped = ENS.Utility.createFilterParameter(rowId);
										$('#' + rowIdEscaped).removeClass(
												'ui-widget-content');
										$('#' + rowIdEscaped).css('background-color',
												'#FF8080');
										$("#profilerTable").setCell(rowId,
												'profileValueChanged', 'true',
												"", "");
									}
								} ]
							}
						},
						{
							name : "target",
							width : parseInt(this.tableWidth * 0.05),
							editable : true,
							edittype : "select",
							editoptions : {
								value : "true:true;false:false",
								dataEvents : [ {
									type : 'change',
									fn : function(e) {
										var rowId = e.target.id.split("_")[0];
										rowId = $('#profilerTable').getDataIDs()[parseInt(rowId) -1];
										if (e.target.value == "false") {
											$("#profilerTable").setCell(rowId,
													'transactionGraph',
													'false', "", "");
										}
										$("#profilerTable").setCell(rowId,
												'profileValueChanged', 'true',
												"", "");
										var rowIdEscaped = ENS.Utility.createFilterParameter(rowId);
										$('#' + rowIdEscaped).removeClass(
												'ui-widget-content');
										$('#' + rowIdEscaped).css('background-color',
												'#FF8080');
									}
								} ]
							}
						},
						{
							name : "className",
							width : parseInt(this.tableWidth * 0.2)
						},
						{
							name : "methodName",
							width : parseInt(this.tableWidth * 0.2)
						},
						{
							name : "alarmThreshold",
							width : parseInt(this.tableWidth * 0.05),
							editable : true,
							edittype : "text",
							editrules : {
								number : true
							},
							editoptions : {
								dataEvents : [ {
									type : 'change',
									fn : function(e) {
										var rowId = e.target.id.split("_")[0];
										rowId = $('#profilerTable').getDataIDs()[parseInt(rowId) -1];
										var rowIdEscaped = ENS.Utility.createFilterParameter(rowId);
										$('#' + rowIdEscaped).removeClass(
												'ui-widget-content');
										$('#' + rowIdEscaped).css('background-color',
												'#FF8080');
										$("#profilerTable").setCell(rowId,
												'profileValueChanged', 'true',
												"", "");
									}
								}, 
								{
									type : 'keydown', 
									fn : onEditKeyDown
								},
								{
									type : 'focus',
									fn : onEditFocusFunc
								}]
							}
						},
						{
							name : "alarmCpuThreshold",
							width : parseInt(this.tableWidth * 0.05),
							editable : true,
							edittype : "text",
							editrules : {
								number : true
							},
							editoptions : {
								dataEvents : [ {
									type : 'change',
									fn : function(e) {
										var rowId = e.target.id.split("_")[0];
										rowId = $('#profilerTable').getDataIDs()[parseInt(rowId) -1];
										var rowIdEscaped = ENS.Utility.createFilterParameter(rowId);
										$('#' + rowIdEscaped).removeClass(
												'ui-widget-content');
										$('#' + rowIdEscaped).css('background-color',
												'#FF8080');
										$("#profilerTable").setCell(rowId,
												'profileValueChanged', 'true',
												"", "");
									}
								}, 
								{
									type : 'keydown', 
									fn : onEditKeyDown
								},
								{
									type : 'focus',
									fn : onEditFocusFunc
								}]
							}
						}, {
							name : "callCount",
							width : parseInt(this.tableWidth * 0.05),
							sorttype : "float"
						}, {
							name : "total",
							width : parseInt(this.tableWidth * 0.05),
							sorttype : "float"
						}, {
							name : "average",
							width : parseInt(this.tableWidth * 0.05),
							sorttype : "float"
						}, {
							name : "maximum",
							width : parseInt(this.tableWidth * 0.05),
							sorttype : "float"
						}, {
							name : "minimum",
							width : parseInt(this.tableWidth * 0.05),
							sorttype : "float"
						}, {
							name : "cpuTotal",
							width : parseInt(this.tableWidth * 0.05),
							sorttype : "float"
						}, {
							name : "cpuAverage",
							width : parseInt(this.tableWidth * 0.05),
							sorttype : "float"
						}, {
							name : "cpuMaximum",
							width : parseInt(this.tableWidth * 0.05),
							sorttype : "float"
						}, {
							name : "cpuMinimum",
							width : parseInt(this.tableWidth * 0.05),
							sorttype : "float"
						}, {
							name : "userTotal",
							width : parseInt(this.tableWidth * 0.05),
							sorttype : "float"
						}, {
							name : "userAverage",
							width : parseInt(this.tableWidth * 0.05),
							sorttype : "float"
						}, {
							name : "userMaximum",
							width : parseInt(this.tableWidth * 0.05),
							sorttype : "float"
						}, {
							name : "userMinimum",
							width : parseInt(this.tableWidth * 0.05),
							sorttype : "float"
						}, {
							name : "throwableCount",
							width : parseInt(this.tableWidth * 0.05),
							sorttype : "float"
						}, {
							name : "profileValueChanged",
							width : 0,
							hidden : true,
							editable : true,
							edittype : "text"
						} ];

				return tableColModel;
			}
		});