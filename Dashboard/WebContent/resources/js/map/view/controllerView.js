ENS.controllerView = wgp.AbstractView
		.extend({
			tableColNames : [ "Property", "Current Value", "Update Value",
					"Property Detail" ],
			initialize : function(argument, treeSettings) {
				var instance = this;
				this.tableMargin = 50;
				this.tableWidth = parseInt($("#persArea_drop_0_1").width()
						- this.tableMargin);
				this.tableColModel = this.createTableColModel();

				var appView = new ENS.AppView();
				this.treeSettings = treeSettings;
				appView.addView(this, treeSettings.treeId
						+ ENS.URL.CONTROLLER_POSTFIX_ID);

				// 空のテーブルを作成
				this.render();

				$("#controllerReloadButton").on("click", function() {
					instance._reload();
				});

				$("#controllerClearButton").on(
						"click",
						function() {
							var rowIDs = $("#controllerTable").getDataIDs();
							$.each(rowIDs, function(i, item) {
								$("#controllerTable").setCell(item,
										'updateValue', undefined, "", "");
							});
						});

				$("#controllerUpdateButton").on(
						"click",
						function() {
							var selRow;
							instance.collection.models = [];
							ENS.tree.agentName = instance.treeSettings.treeId;
							var changedCells = $("#controllerTable")
									.getChangedCells();
							var selrowId = $("#controllerTable").getGridParam(
									'selrow');
							if (selrowId != null) {
								selRow = $("#controllerTable").getRowData(
										selrowId);
								if (selRow.updateValue.indexOf("<input") == 0) {
									$selRow = $(selRow.updateValue);
									var id = $selRow.attr("id");
									selRow.updateValue = $("#" + id).val();
								}
								changedCells.push(selRow);
							}
							if (changedCells.length === 0) {
								return;
							}
							var settings = {
								data : {
									agentName : ENS.tree.agentName,
									propertyList : JSON.stringify(changedCells)
								},
								url : ENS.tree.CONTROLLER_UPDATE
							};
							var ajaxHandler = new wgp.AjaxHandler();
							ajaxHandler.requestServerAsync(settings);
						});

				this.id = argument.id;
				this._reload();
			},
			render : function() {
				$("#" + this.id).append('<div id="controllerDiv"></div>');
				$("#controllerDiv").css({
					"margin-left" : 5,
					"margin-top" : 5
				});
				$("#controllerDiv")
						.append(
								"<input type='button' class='default-btn' id='controllerReloadButton' value='reload'>");
				$("#controllerReloadButton").css({
					"margin-left" : this.tableWidth - 198 + "px"
				});

				$("#controllerDiv")
						.append(
								"<input type='button' class='default-btn' id='controllerClearButton' value='clear'>");
				$("#controllerDiv")
						.append(
								"<input type='button' class='default-btn' id='controllerUpdateButton' value='update'>");
				$("#controllerDiv").append(
						'<table id="controllerTable"></table>');
				$("#controllerDiv")
						.append('<div id="controllerPager"></table>');
				var height = "auto";

				$("#controllerTable").jqGrid({
					datatype : "local",
					data : "",
					colModel : this.tableColModel,
					colNames : this.tableColNames,
					caption : "Properties of " + this.treeSettings.id,
					pager : "controllerPager",
					rowNum : 10000,
					height : height,
					width : this.tableWidth,
					viewrecords : true,
					rownumbers : true,
					shrinkToFit : true,
					cellEdit : true,
					cmTemplate : {
						title : false
					},
					cellsubmit : 'clientArray'
				});
				$("#controllerTable").filterToolbar({
					defaultSearch : 'cn'
				});
				$("#controllerDiv").css('font-size', '0.8em');
			},
			_parseModel : function(model) {
				var tableData = model.attributes;

				return tableData;
			},
			_reload : function() {
				var instance = this;
				instance.collection.models = [];
				ENS.tree.agentName = instance.treeSettings.treeId;
				var settings = {
					data : {
						agentName : ENS.tree.agentName
					},
					url : ENS.tree.CONTROLLER_RELOAD
				};
				var ajaxHandler = new wgp.AjaxHandler();
				ajaxHandler.requestServerAsync(settings);
			},
			onAdd : function(element) {
			},
			onChange : function(element) {
			},
			onRemove : function(element) {
			},
			onComplete : function(element) {
				if (element == wgp.constants.syncType.SEARCH) {
					appView.syncData();
				}
				this.reloadTable();
			},
			reloadTable : function() {
				var tmpTableViewData = [];
				var instance = this;
				_.each(this.collection.models, function(model, index) {
					tmpTableViewData.push(instance._parseModel(model));
				});

				tmpTableViewData.sort(function(data1, data2) {
					var order1 = parseInt(data1.sortOrder, 10);
					var order2 = parseInt(data2.sortOrder, 10);
					return order1 - order2;
				});
				var cnt;
				var lastSortOrder = "0";
				var tableViewData = [];
				var propertyObj;
				for (cnt = 0; cnt < tmpTableViewData.length; cnt++) {
					propertyObj = tmpTableViewData[cnt];
					// 同一ソート番号を持つデータが存在する場合は古い要素を削除。
					if (lastSortOrder !== propertyObj.sortOrder) {
						tableViewData.push(propertyObj);
						lastSortOrder = propertyObj.sortOrder;
					}
				}
				$("#controllerTable").clearGridData().setGridParam({
					data : tableViewData
				}).trigger("reloadGrid");
			},
			createTableColModel : function() {
				var tableColModel = [ {
					name : "property",
					width : parseInt(this.tableWidth * 0.25),
					key : true
				}, {
					name : "currentValue",
					width : parseInt(this.tableWidth * 0.1)
				}, {
					name : "updateValue",
					width : parseInt(this.tableWidth * 0.1),
					editable : true,
					edittype : "text"
				}, {
					name : "propertyDetail",
					width : parseInt(this.tableWidth * 0.5)
				} ];
				return tableColModel;
			}
		});