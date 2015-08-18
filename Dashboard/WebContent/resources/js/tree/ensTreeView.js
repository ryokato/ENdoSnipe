// シグナルダイアログの設定項目を格納しておくためのリスト
ENS.tree.signalDefinitionList = [];
ENS.tree.doRender = true;
ENS.tree.addedOtherNodes = [];
ENS.tree.doMessage = true;

ENS.treeView = wgp.TreeView
		.extend({
			/**
			 * ツリー要素をクリックした際の、別ペインとの 連携処理を行う。 targetId {String} 別ペインのID
			 */
			setClickEvent : function(targetId) {
				var instance = this;
				this.treeCollection = {};
				this.maxId = 0;
				this.targetId = targetId;
				$("#" + this.$el.attr("id")).mousedown(function(event) {
					var whichValue = event.which;
					// event.whichがundefindになるのは、mausedownを直接呼び出した場合なので、通す。
					if (whichValue != 1 && whichValue != undefined) {
						return;
					}
					var target = event.target;
					if ("A" == target.tagName) {
						var treeId = $(target).attr("id");
						var treeModel = instance.collection.get(treeId);
						instance.clickModel(treeModel);
					}
					if ("a" == target.parentElement.localName) {
						var parentPath = target.parentElement;
						var iconId = $(parentPath).attr("id");
						treeModel = instance.collection.get(iconId);
						instance.clickModel(treeModel);
					}
				});
			},
			/**
			 * クリックされたツリー要素のモデルに応じて処理を行う。 treeModel {Backbone.Model}
			 */
			clickModel : function(treeModel) {

				// モデルタイプが「シグナル」の場合は何もしない。
				if (ENS.tree.type.SIGNAL == treeModel.get("type")) {
					return;
				}
				if (ENS.tree.type.SUMMARYSIGNAL == treeModel.get("type")) {
					return;
				}
				if (this.childView) {
					var tmpAppView = new wgp.AppView();
					tmpAppView.removeView(this.childView);
					this.childView = null;
				}
				$("#" + this.targetId).children().remove();
				var targetContentId = this.targetId + "_content";
				$("#" + this.targetId).append(
						"<div id ='" + targetContentId + "'></div>");

				var dataId = treeModel.get("id");

				var viewSettings = null;
				$.each(wgp.constants.VIEW_SETTINGS, function(index, value) {
					if (dataId.match(index)) {
						viewSettings = value;
						return false;
					}
				});
				if (viewSettings == null) {
					viewSettings = wgp.constants.VIEW_SETTINGS["default"];
					if (viewSettings == null) {
						return;
					}
				}
				var viewClassName = viewSettings.viewClassName;
				$.extend(true, viewSettings, {
					id : targetContentId
				});

				var treeSettings = treeModel.attributes;
				// 動的に生成するオブジェクトを切り替える必要があるため、やむを得ずeval()を使う
				this.childView = eval("new " + viewClassName
						+ "(viewSettings, treeSettings)");
			},
			/**
			 * 複数ノード更新時に、rollbackとclean_nodeを一時的にOFFにする。
			 */
			start_batch : function() {
				var tree = jQuery.jstree
						._reference($("#" + this.$el.attr("id")));
				var rollback = tree.get_rollback;
				tree.get_rollback = function() {
				};
				var clean_node = tree.clean_node;
				tree.clean_node = function() {
				};

				return [ tree, rollback, clean_node ];
			},
			/**
			 * 複数ノード更新時に、rollbackとclean_nodeを一時的にOFFにする処理の後処理としてONにする。
			 */
			end_batch : function(args) {
				var tree = args[0];
				tree.get_rollback = args[1];
				tree.clean_node = args[2];
			},
			render : function(renderType, treeModel) {
				// renderを行わない場合はreturnする
				if (ENS.tree.doRender === false) {
					return;
				}
				if (renderType == wgp.constants.RENDER_TYPE.ADD) {
					var parentTreeId = treeModel.get("parentTreeId");
					var idAttribute = treeModel.idAttribute;
					var targetTag;
					var addPlace = "";

					if (parentTreeId !== null && parentTreeId !== undefined) {
						var topNodeNum = $("#tree_area > ul > li").length;

						// Topノードがすでにあるときに、Topノードを追加する時は、他のTopノードの後に追加する。
						// そうでない場合は、親ノードの下にノードを作成する。
						if (topNodeNum !== 0 && parentTreeId === "") {
							addPlace = "after";
							targetTag = $("#tree_area > ul > li").last();
						} else {
							addPlace = "last";
							var nodes = this.collection.where({
								treeId : parentTreeId
							});
							if (nodes.length > 0) {
								targetTag = this.getTreeNode(nodes[0].id,
										idAttribute);
							} else {
								targetTag = this.getTreeNode(parentTreeId,
										idAttribute);
							}
						}
					}

					var instance = this;
					var treeData = this.createTreeData(treeModel);

					$("#" + this.$el.attr("id")).jstree("create_node",
							$(targetTag), addPlace, treeData);
				} else {
					wgp.TreeView.prototype.render.call(this, renderType,
							treeModel);
				}
			},
			renderAll : function() {
				var instance = this;
				// View jsTree
				var settings = this.treeOption;
				settings = $.extend(true, settings, {
					json_data : {
						data : this.createJSONData()
					}
				});

				$("#" + this.$el.attr("id")).jstree(settings).bind(
						"loaded.jstree", function(event, data) {
							instance.setOpenCloseIcon();
						});
				$("#" + this.$el.attr("id"))
						.bind(
								"create_node.jstree",
								function(event, data) {
									var childModel = data.args[2].data[0];
									var icon = childModel.icon;
									var id = childModel.attr.Id;
									var elem = document.getElementById(id);

									if (icon == "center") {
										var parentLiTag = $(elem).parent("li");
										if (parentLiTag) {
											parentLiTag
													.attr("class",
															"jstree-last jstree-closed");
										}
									}

									if (icon == "center" || icon == "leaf") {

										if (ENS.tree.addedOtherNodes
												.indexOf(id) == -1) {
											// シグナルやレポートアイコンなど、その他ノードをコレクションから検索し追加する
											instance.addOtherNodes(id);
											ENS.tree.addedOtherNodes.push(id);
										}
									}
								});

				this.getAllReport_();
				this.getAllScheduleReport_();
				this.getAllSignal_();
				this.getAllSummarySignal_();
				this.getAllMulResGraph_();
				this.clearSignalDialog_();
			},
			/**
			 * ツリーを開け閉めするためのアイコンを設定する。
			 */
			setOpenCloseIcon : function() {
				_.each(this.collection.models, function(model, index) {
					var type = model.attributes.type;
					if (type == ENS.tree.type.GROUP) {
						var id = model.attributes.id;
						var elem = document.getElementById(id);
						var parentLiTag = $(elem).parent("li");
						if (parentLiTag) {
							parentLiTag.attr("class",
									"jstree-last jstree-closed");
						}
					}
				});
			},
			createTreeData : function(treeModel) {
				var returnData = wgp.TreeView.prototype.createTreeData.call(
						this, treeModel);
				var titleData = returnData.data;
				if (titleData.title.indexOf("&#47;") >= 0) {
					titleData.title = titleData.title.split("&#47;").join("/");
				}
				if (titleData.title.indexOf("\n") >= 0) {
					titleData.title = titleData.title.split("\n").join(" ");
				}

				var type = treeModel.get("type");
				if (treeModel.get("icon")) {
					returnData.data.icon = treeModel.get("icon");
				} else if (type == ENS.tree.type.GROUP) {
					returnData.data.icon = "center";
				} else if (type == ENS.tree.type.TARGET) {
					returnData.data.icon = "leaf";
				} else if (type == ENS.tree.type.MULTIPLERESOURCEGRAPH) {
					returnData.data.icon = "graph";
				}
				return returnData;
			},
			addOtherNodes : function(childNodeId) {

				var otherNodes = this.collection.where({
					parentTreeId : childNodeId
				});

				var idAttribute = "id";
				var targetTag = this.getTreeNode(childNodeId, idAttribute);

				var instance = this;
				_.each(otherNodes, function(otherNode, index) {
					var treeData = instance.createTreeData(otherNode);
					$("#" + instance.$el.attr("id")).jstree("create_node",
							$(targetTag), "last", treeData);
				});

			},
			/**
			 * ツリーにコンテキストメニューの表示設定を行う。
			 */
			addContextMenu : function(contextOption) {
				// contextOptionの中身
				// "menu_id" : 表示するコンテキストメニュータグのID名,
				// "menu_name" : 表示するメニュー名
				// "showParam" : コンテキストメニュー表示制限,
				// "executeClass" : メニュー選択時の実行クラス。
				// "children" : 子要素のコンテキストオプション
				var instance = this;
				this.contextCollection = new Backbone.Collection();
				this.addContextCollection_(contextOption);

				// コンテキストメニュー用のタグを生成する。
				var menuId = this.$el.attr("id") + "_contextManu";
				var tmpClickTarget = null;

				var settingOptions = {
					onShow : function(event, target) {
						var clickTarget = event.target;
						var tagName = clickTarget.tagName;
						if (tagName != "A") {
							return false;
						}
						var treeId = $(clickTarget).attr("id");
						var treeModel = instance.collection.get(treeId);

						// 各コンテキストメニューの表示可否設定を行う。
						instance.checkShowContext_(treeModel);
						tmpClickTarget = clickTarget;

					},
					onSelect : function(event, target) {
						var clickTarget = event.target;
						var id = $(clickTarget).attr("id");
						var targetOption = instance.getContextOption_(id);
						if (targetOption == null) {
							return;
						}
						var dialogId = targetOption.get("executeOption").dialogId;

						if (dialogId == ENS.tree.SIGNAL_DIALOG) {
							instance.signalOnSelect_(event, target, menuId,
									tmpClickTarget);
						} else if (dialogId == ENS.tree.REPORT_DIALOG) {
							instance.reportOnSelect_(event, target, menuId,
									tmpClickTarget);
						} else if (dialogId == ENS.tree.MULTIPLE_RESOURCE_GRAPH_DIALOG) {
							instance.multipleResourceGraphOnSelect_(event,
									target, menuId, tmpClickTarget);
						} else if (dialogId == ENS.tree.SUMMARYSIGNAL_DIALOG) {
							instance.summarySignalOnSelect_(event, target,
									menuId, tmpClickTarget);
						} else if (dialogId == ENS.tree.REPORT_SCHEDULE_DIALOG) {
							instance.reportScheduleOnSelect_(event, target,
									menuId, tmpClickTarget);
						}
					}
				};
				contextMenuCreator.initializeContextMenu(menuId, contextOption);
				contextMenuCreator.createContextMenu(this.$el.attr("id"),
						menuId, settingOptions);
			},
			/**
			 * シグナルに関連するコンテキストメニュー選択時の処理を行う。
			 */
			signalOnSelect_ : function(event, target, menuId, tmpClickTarget) {
				var clickTarget = event.target;
				var id = $(clickTarget).attr("id");
				var targetOption = this.getContextOption_(id);

				var executeOption = targetOption.get("executeOption");
				executeOption.treeId = $(tmpClickTarget).attr("id");
				executeOption.displayName = $(tmpClickTarget).text();

				// 削除が選択された場合はそのノードを削除する
				if (id == ENS.tree.DELETE_SIGNAL_TYPE) {
					if (confirm("Are you sure you want to delete it?")) {
						this.deleteSignal_(executeOption);
					}
					return;
				}

				// 開くViewのクラス
				var executeClass = targetOption.get("executeClass");
				if (executeClass) {
					if (id == ENS.tree.ADD_SIGNAL_TYPE) {
						// デフォルトのレポート名にレポート出力対象のノード名を入れる(空白を削除する処理を入れている)
						var targetNodeName = executeOption.displayName.replace(
								/\s+/g, "");
						$("#signalName").val(targetNodeName + " signal");
						// Matching Patternにデフォルトのツリー階層を入力する
						$("#matchingPattern").val(executeOption.treeId);

						// 編集の場合は事前に値を設定する。
					} else if (id == ENS.tree.EDIT_SIGNAL_TYPE) {
						var treeModel = this.collection
								.get(executeOption.treeId);
						this.inputSignalDialog_(treeModel);

						// 追加時と同様に扱うため、親ツリーのIDを設定する。
						executeOption.treeId = this
								.getSignalParentTreeId_(executeOption.treeId);
					}

					// set execute class and function, if push ok
					// button.
					executeOption.okObject = this;
					executeOption.okFunctionName = "signalPushOkFunction";
					executeOption.cancelObject = this;
					executeOption.cancelFunctionName = "signalPushCancelFunction";
					eval("new " + executeClass + "(executeOption)");
				}
			},
			/**
			 * レポートに関するコンテキストメニュー選択時の処理を行う。
			 */
			reportOnSelect_ : function(event, target, menuId, tmpClickTarget) {
				var clickTarget = event.target;
				var id = $(clickTarget).attr("id");
				var targetOption = this.getContextOption_(id);
				if (targetOption == null) {
					return;
				}

				var executeOption = targetOption.get("executeOption");
				executeOption.treeId = $(tmpClickTarget).attr("id");
				executeOption.displayName = $(tmpClickTarget).text();

				// 削除が選択された場合はそのノードを削除する
				if (id == ENS.tree.DELETE_REPORT_TYPE) {
					if (confirm("Are you sure you want to delete it?")) {
						this.deleteReport_(executeOption);
					}
					return;
				}

				// 開くViewのクラス
				var executeClass = targetOption.get("executeClass");
				if (executeClass) {
					if (id == ENS.tree.OUTPUT_REPORT_TYPE) {
						// デフォルトのレポート名にレポート出力対象のノード名を入れる(空白を削除する処理を入れている)
						var targetNodeName = executeOption.displayName.replace(
								/\s+/g, "");
						$("#reportName").val(targetNodeName + " report");
						// Target Measurement Nameにツリー階層を入力する
						$("#targetName").val(executeOption.treeId);
					}

					// set execute class and function, if push ok
					// button.
					executeOption.okObject = this;
					executeOption.okFunctionName = "reportPushOkFunction";
					executeOption.cancelObject = this;
					executeOption.cancelFunctionName = "reportPushCancelFunction";
					eval("new " + executeClass + "(executeOption)");
				}
			},
			multipleResourceGraphOnSelect_ : function(event, target, menuId,
					tmpClickTarget) {

				var clickTarget = event.target;
				var id = $(clickTarget).attr("id");
				var targetOption = this.getContextOption_(id);

				var executeOption = targetOption.get("executeOption");
				executeOption.treeId = $(tmpClickTarget).attr("id");
				executeOption.displayName = $(tmpClickTarget).text();

				// 削除が選択された場合はそのノードを削除する
				if (id == ENS.tree.DELETE_MULTIPLE_RESOURCE_GRAPH_TYPE) {
					if (confirm("Are you sure you want to delete it?")) {
						this.deleteMulResGraph_(executeOption);
					}
					return;
				}

				// 開くViewのクラス
				var executeClass = targetOption.get("executeClass");
				if (executeClass) {

					if (id == ENS.tree.ADD_MULTIPLE_RESOURCE_GRAPH_TYPE) {

						$('#multipleResourceGraphName').removeAttr("disabled");
						// デフォルトのレポート名にレポート出力対象のノード名を入れる(空白を削除する処理を入れている)
						var targetNodeName = executeOption.displayName.replace(
								/\s+/g, "");
						$("#multipleResourceGraphName").val(
								targetNodeName + " Graph");

					} else if (id == ENS.tree.EDIT_MULTIPLE_RESOURCE_GRAPH_TYPE) {
						$('#multipleResourceGraphName').attr("disabled", true);
						$('#multipleResourceGraphLstBox2').empty();
						var treeModel = this.collection
								.get(executeOption.treeId);
						this.inputMulResGraphDialog_(treeModel);

						// 追加時と同様に扱うため、親ツリーのIDを設定する。
						executeOption.treeId = this
								.getSignalParentTreeId_(executeOption.treeId);
					}

					// set execute class and function, if push ok
					// button.
					executeOption.okObject = this;
					executeOption.okFunctionName = "mulResGraphPushOkFunction";
					executeOption.cancelObject = this;
					executeOption.cancelFunctionName = "mulResGraphPushCancelFunction";
					eval("new " + executeClass + "(executeOption)");
				}
			},
			summarySignalOnSelect_ : function(event, target, menuId,
					tmpClickTarget) {
				var clickTarget = event.target;
				var id = $(clickTarget).attr("id");
				var targetOption = this.getContextOption_(id);

				var executeOption = targetOption.get("executeOption");
				executeOption.treeId = $(tmpClickTarget).attr("id");
				executeOption.displayName = $(tmpClickTarget).text();

				// 削除が選択された場合はそのノードを削除する
				if (id == ENS.tree.DELETE_SUMMARYSIGNAL_TYPE) {
					if (confirm("Are you sure you want to delete it?")) {
						this.deleteSummarySignal_(executeOption);
					}
					return;
				}

				// 開くViewのクラス
				var executeClass = targetOption.get("executeClass");
				if (executeClass) {
					if (id == ENS.tree.ADD_SUMMARYSIGNAL_TYPE) {
						var targetNodeName = executeOption.displayName.replace(
								/\s+/g, "");
						$("#summarySignalName").val(
								targetNodeName + " summary signal");

					} else if (id == ENS.tree.EDIT_SUMMARYSIGNAL_TYPE) {
						ENS.tree.doMessage = true;
						$('#summarySignalName').attr("disabled", true);
						$('#summarySignalList2Box').empty();
						var treeModel = this.collection
								.get(executeOption.treeId);
						this.inputSummarySignalDialog_(treeModel);

						// 追加時と同様に扱うため、親ツリーのIDを設定する。
						executeOption.treeId = this
								.getSignalParentTreeId_(executeOption.treeId);
					}

					// set execute class and function, if push ok
					// button.
					executeOption.okObject = this;
					executeOption.okFunctionName = "summarySignalPushOkFunction";
					executeOption.cancelObject = this;
					executeOption.cancelFunctionName = "summarySignalPushCancelFunction";
					eval("new " + executeClass + "(executeOption)");
				}
			},

			/** 閾値判定の定義を入力した後に実行するメソッド。 */
			signalPushOkFunction : function(event, option) {

				// add tree data for signal
				var treeId = option.treeId;
				var signalDispalyName = $("#signalName").val();
				var signalName = treeId + "/" + signalDispalyName;

				// Ajax通信のコールバック関数名
				var callbackFunction = "";
				// Ajax通信の送信先URL
				var url = "";
				// サーバに送信するデータ
				var sendData;
				// Ajax通信のコールバック関数と送信先URLを追加か編集かによって決める
				// シグナル追加時
				if (option.signalType == ENS.tree.ADD_SIGNAL_TYPE) {
					sendData = this.createSendAddSignalData_(signalName);
					callbackFunction = "callbackAddSignal_";
					url = ENS.tree.SIGNAL_ADD_URL;

					// シグナル編集時
				} else if (option.signalType == ENS.tree.EDIT_SIGNAL_TYPE) {
					sendData = this.createSendEditSignalData_(signalName);
					callbackFunction = "callbackEditSignal_";
					url = ENS.tree.SIGNAL_EDIT_URL;
				}

				// Ajax通信用の設定
				var settings = {
					data : sendData,
					url : url
				};

				// 非同期通信でデータを送信する
				var ajaxHandler = new wgp.AjaxHandler();
				settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
				settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = callbackFunction;
				ajaxHandler.requestServerAsync(settings);

				// 閾値判定定義の入力内容をクリアする。
				this.clearSignalDialog_();
			},
			/** 閾値判定の定義を入力をキャンセルした場合に実行するメソッド */
			signalPushCancelFunction : function(event, option) {

				// 閾値判定定義の入力内容をクリアする。
				this.clearSignalDialog_();
			},
			summarySignalPushCancelFunction : function(event, option) {

				// 閾値判定定義の入力内容をクリアする。
				this.clearSummarySignalDialog_();
			},
			/**
			 * シグナル新規追加時の送信データを生成する。
			 */
			createSendAddSignalData_ : function(signalName) {
				// 入力された秒をミリ秒に変換する
				var escalationPeriod = $("#escalationPeriod").val() * 1000;
				// シグナル定義を作成する
				var signalDefinition = {
					signalId : $("#signalId").val(),
					signalName : signalName,
					matchingPattern : $("#matchingPattern").val(),
					level : $("#signalPatternValue select").val(),
					patternValue : this.createPatternValue_(),
					escalationPeriod : escalationPeriod,
					sendMail : $("#signalAlertMail").prop("checked")
				};

				var sendData = {
					signalDefinition : JSON.stringify(signalDefinition)
				};

				return sendData;
			},
			/**
			 * シグナル編集時の送信データを生成する。
			 */
			createSendEditSignalData_ : function(signalName) {
				// 入力された秒をミリ秒に変換する
				var escalationPeriod = $("#escalationPeriod").val() * 1000;
				// シグナル定義を作成する
				var signalDefinition = {
					signalId : $("#signalId").val(),
					signalName : signalName,
					matchingPattern : $("#matchingPattern").val(),
					level : $("#signalPatternValue select").val(),
					patternValue : this.createPatternValue_(),
					escalationPeriod : escalationPeriod,
					sendMail : $("#signalAlertMail").prop("checked")
				};

				var sendData = {
					signalDefinition : JSON.stringify(signalDefinition)
				};

				return sendData;
			},
			createPatternValue_ : function() {
				var level = $("#signalPatternValue select").val();

				// 各レベルの閾値をカンマ区切りの文字列としてサーバに送る
				var patternValue = "";
				if (level == 3) {
					patternValue += $("#patternValue_2").val();
					patternValue += ",";
					patternValue += $("#patternValue_4").val();
				} else if (level == 5) {
					patternValue += $("#patternValue_1").val();
					patternValue += ",";
					patternValue += $("#patternValue_2").val();
					patternValue += ",";
					patternValue += $("#patternValue_3").val();
					patternValue += ",";
					patternValue += $("#patternValue_4").val();
				}

				return patternValue;
			},
			/** レポート出力の定義を入力した後に実行するメソッド。 */
			reportPushOkFunction : function(event, option) {
				// Ajax通信のコールバック関数名
				var callbackFunction = "";
				// Ajax通信の送信先URL
				var url = "";
				// レポート出力定義
				var reportDefinition;
				// サーバに送信するデータ
				var sendData;
				// Ajax通信のコールバック関数と送信先URLをシグナルタイプによって決める
				if (option.signalType == ENS.tree.OUTPUT_REPORT_TYPE) {
					reportDefinition = this.createReportDefinition_(option);
					sendData = {
						reportDefinition : JSON.stringify(reportDefinition)
					};
					callbackFunction = "callbackAddReport_";
					url = ENS.tree.REPORT_ADD_URL;
				}

				// Ajax通信用の設定
				var settings = {
					data : sendData,
					url : url
				};

				// 非同期通信でデータを送信する
				var ajaxHandler = new wgp.AjaxHandler();
				settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
				settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = callbackFunction;
				ajaxHandler.requestServerAsync(settings);

				// レポート出力ダイアログの入力内容をクリアする。
				this.clearReportDialog_();

				// ツリーノードを追加する
				var treeOption = this.createReportTreeOption_(reportDefinition);
				this.collection.add([ treeOption ]);
			},
			/** レポート出力をキャンセルした場合に実行するメソッド */
			reportPushCancelFunction : function(event, option) {

				// レポート出力ダイアログの入力内容をクリアする。
				this.clearReportDialog_();
			},
			createReportDefinition_ : function(option) {
				var treeId = option.treeId;
				var reportName = $("#reportName").val();
				var reportFullName = treeId + "/" + reportName;

				// レポート出力定義を作成する
				var reportDefinition = {
					reportId : $("#reportId").val(),
					reportName : reportFullName,
					targetMeasurementName : $("#targetName").val(),
					reportTermFrom : $("#jquery-ui-datepicker-from").val(),
					reportTermTo : $("#jquery-ui-datepicker-to").val()
				};

				return reportDefinition;
			},
			mulResGraphPushOkFunction : function(event, option) {

				// add tree data for multipleResourceGraph
				var treeId = option.treeId;
				var mulResGraphDispalyName = $("#multipleResourceGraphName")
						.val();
				var mulResGraphName = treeId + "/" + mulResGraphDispalyName;

				// Ajax通信のコールバック関数名
				var callbackFunction = "";
				// Ajax通信の送信先URL
				var url = "";
				// サーバに送信するデータ
				var sendData = this.createSendMulResGraphData_(mulResGraphName);
				// Ajax通信のコールバック関数と送信先URLを追加か編集かによって決める
				if (option.signalType == ENS.tree.ADD_MULTIPLE_RESOURCE_GRAPH_TYPE) {
					callbackFunction = "callbackAddMulResGraph_";
					url = ENS.tree.MULTIPLE_RESOURCE_GRAPH_ADD_URL;

				} else if (option.signalType == ENS.tree.EDIT_MULTIPLE_RESOURCE_GRAPH_TYPE) {
					callbackFunction = "callbackEditMulResGraph_";
					url = ENS.tree.MULTIPLE_RESOURCE_GRAPH_EDIT_URL;

				}

				// Ajax通信用の設定
				var settings = {
					data : sendData,
					url : url
				};

				// 非同期通信でデータを送信する
				var ajaxHandler = new wgp.AjaxHandler();
				settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
				settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = callbackFunction;
				ajaxHandler.requestServerAsync(settings);

				// 複数グラフ定義の入力内容をクリアする。
				this.clearMulResGraphDialog_();
			},
			/** 閾値判定の定義を入力をキャンセルした場合に実行するメソッド */
			mulResGraphPushCancelFunction : function(event, option) {

				// 閾値判定定義の入力内容をクリアする。
				this.clearMulResGraphDialog_();
			},
			/**
			 * 複数グラフ追加/編集時の送信データを生成する。
			 */
			createSendMulResGraphData_ : function(mulResGraphName) {

				// ラジオボタンで「Graph Selection」を指定した場合
				var measurementItem = "";
				if ($('#multipleResourceGraphSelection').attr("checked")) {
					var measurementList = $(
							"#multipleResourceGraphLstBox2>option").map(
							function() {
								return $(this).val();
							});

					measurementItem = measurementList.get(0);
					for ( var i = 1; i < measurementList.length; i++) {
						measurementItem = measurementItem + ","
								+ measurementList.get(i);
					}
				}

				var measurementItemPattern = "";
				if ($('#multipleResourceGraphRegExpression').attr("checked")) {
					measurementItemPattern = $("#multipleResourceGraphItems")
							.val();
				}

				// 複数グラフ定義を作成する
				var mulResGraphDefinition = {
					multipleResourceGraphId : $("#mulResGraphId").val(),
					multipleResourceGraphName : mulResGraphName,
					measurementItemIdList : measurementItem,
					measurementItemPattern : measurementItemPattern
				};

				var sendData = {
					mulResGraphDefinition : JSON
							.stringify(mulResGraphDefinition)
				};

				return sendData;
			},
			extractNameSignal_ : function(signalNames) {
				// Ajax通信用の送信先URL
				var settings = {
					url : ENS.tree.SUMMARYSIGNAL_ADD_URL,
					data : {
						signalName : signalNames
					}
				};
				var ajaxHandler = new wgp.AjaxHandler();
				var result = ajaxHandler.requestServerSync(settings);
			},
			checkShowContext_ : function(treeModel) {
				$.each(this.contextCollection.models, function(index, value) {
					var menuId = value.get("menu_id");
					$("#" + menuId).show();
					var showTreeTypes = value.get("showTreeTypes");
					if (!showTreeTypes) {
						return true;
					}

					if (_.contains(showTreeTypes, treeModel.get("type"))) {
						return true;
					} else {
						$("#" + menuId).hide();
					}
				});
			},
			addContextCollection_ : function(contextOption) {
				var instance = this;
				jQuery.each(contextOption, function(index, target) {
					instance.contextCollection.push(target);
					var children = target.children;
					if (children != null && children.length !== 0) {
						instance.addContextCollection_(children);
					}
				});
			},
			getContextOption_ : function(id) {
				var optionList = this.contextCollection.where({
					menu_id : id
				});
				if (optionList == null) {
					return;
				}
				if (optionList.length === 0) {
					return;
				}
				return optionList[0];
			},
			deleteSignal_ : function(executeOption) {
				// リアルタイム通信を止める
				appView.stopSyncData([ executeOption.treeId ]);

				// Ajax通信の送信先URL
				var callbackFunction = "callbackDeleteSignal_";
				var url = ENS.tree.SIGNAL_DELETE_URL;

				// Ajax通信用の設定
				var settings = {
					data : {
						signalName : executeOption.treeId
					},
					url : url
				};

				// 非同期通信でシグナル削除依頼電文を送信する。
				var ajaxHandler = new wgp.AjaxHandler();
				settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
				settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = callbackFunction;
				ajaxHandler.requestServerAsync(settings);
			},
			deleteReport_ : function(executeOption) {
				// Ajax通信の送信先URL
				var callbackFunction = "callbackDeleteReport_";
				var url = ENS.tree.REPORT_DELETE_BY_NAME_URL;

				// ツリー
				var treeId = executeOption.treeId;
				var treeIdSplitList = treeId.split("/");
				var splitLength = treeIdSplitList.length;

				var reportName = "/";
				for ( var index = 1; index < splitLength - 1; index++) {
					reportName += treeIdSplitList[index];
					reportName += "/";
				}
				var treeModel = this.collection.get(treeId);
				reportName += treeModel.get("data");

				// Ajax通信用の設定
				var settings = {
					data : {
						reportName : reportName
					},
					url : url
				};

				// 非同期通信でシグナル削除依頼電文を送信する。
				var ajaxHandler = new wgp.AjaxHandler();
				settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
				settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = callbackFunction;
				ajaxHandler.requestServerAsync(settings);
			},
			getAllSignal_ : function() {
				// シグナル定義を取得する
				// Ajax通信用の設定
				var settings = {
					url : ENS.tree.SIGNAL_SELECT_ALL_URL
				};

				// 非同期通信でデータを送信する
				var ajaxHandler = new wgp.AjaxHandler();
				settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
				settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = "callbackGetAllSignal_";
				ajaxHandler.requestServerAsync(settings);
			},
			getAllReport_ : function() {
				// レポート出力定義を取得する
				// Ajax通信用の設定
				var settings = {
					url : ENS.tree.REPORT_SELECT_ALL_URL
				};

				// 非同期通信でデータを送信する
				var ajaxHandler = new wgp.AjaxHandler();
				settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
				settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = "callbackGetAllReport_";
				ajaxHandler.requestServerAsync(settings);
			},
			getAllScheduleReport_ : function() {
				// レポート出力定義を取得する
				// Ajax通信用の設定
				var settings = {
					url : ENS.tree.SCHEDULING_REPORT_SELECT_ALL_URL
				};

				// 非同期通信でデータを送信する
				var ajaxHandler = new wgp.AjaxHandler();
				settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
				settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = "callbackGetAllReport_";
				ajaxHandler.requestServerAsync(settings);

			},
			getAllMulResGraph_ : function() {
				// multipleResourceGraph定義を取得する
				// Ajax通信用の設定

				var settings = {
					url : ENS.tree.MULTIPLE_RESOURCE_GRAPH_SELECT_ALL_URL
				};

				// 非同期通信でデータを送信する
				var ajaxHandler = new wgp.AjaxHandler();
				settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
				settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = "callbackGetAllMulResGraph_";
				ajaxHandler.requestServerAsync(settings);
			},
			getAllSummarySignal_ : function() {
				// Summaryシグナル定義を取得する
				// Ajax通信用の設定
				var settings = {
					url : ENS.tree.SUMMARY_SIGNAL_SELECT_ALL_URL
				};

				// 非同期通信でデータを送信する
				var ajaxHandler = new wgp.AjaxHandler();
				settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
				settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = "callbackGetAllSummarySignal_";
				ajaxHandler.requestServerAsync(settings);
			},
			callbackGetAllSummarySignal_ : function(summarySignalDefinitionList) {
				var instance = this;
				var addOptionList = [];
				var addTopOptionList = [];
				_
						.each(
								summarySignalDefinitionList,
								function(sumamrySignalDefinition, index) {
									var treeOption = instance
											.createSummarySignalTreeOption_(sumamrySignalDefinition);
									if (treeOption == null) {
										return;
									}
									if (treeOption.id.split("/").length <= 3) {
										addTopOptionList.push(treeOption);
									} else {
										addOptionList.push(treeOption);
									}
								});

				// renderのADDを実行する権限を無くす
				ENS.tree.doRender = false;
				// ツリーノードに追加する
				this.collection.add(addOptionList);
				// renderのADDを実行する権限を与える
				ENS.tree.doRender = true;
				this.collection.add(addTopOptionList);
				var settings = {
					url : ENS.tree.SUMMARY_SIGNAL_CHANGE_STATE_URL
				};

				// 非同期通信でデータを送信する
				var ajaxHandler = new wgp.AjaxHandler();
				ajaxHandler.requestServerAsync(settings);

			},
			callbackGetAllSignal_ : function(signalDefinitionList) {
				var instance = this;
				var addOptionList = [];
				_.each(signalDefinitionList, function(signalDefinition, index) {
					var treeOption = instance
							.createSignalTreeOption_(signalDefinition);
					addOptionList.push(treeOption);
				});

				// renderのADDを実行する権限を無くす
				ENS.tree.doRender = false;
				// ツリーノードに追加する
				this.collection.add(addOptionList);
				// renderのADDを実行する権限を与える
				ENS.tree.doRender = true;
			},
			callbackGetAllReport_ : function(reportDefinitionList) {
				var instance = this;
				var addOptionList = [];
				var addOptionListSecond = [];

				// 追加するツリーノードのオプションを作成する
				_.each(reportDefinitionList, function(reportDefinition,
						reportIndex) {
					var treeOption = instance
							.createReportTreeOption_(reportDefinition);
					if (treeOption.id.split("/").length <= 3) {
						addOptionListSecond.push(treeOption);
					} else {
						addOptionList.push(treeOption);
					}
				});

				// renderのADDを実行する権限を無くす
				ENS.tree.doRender = false;
				// ツリーノードに追加する
				this.collection.add(addOptionList);
				// renderのADDを実行する権限を与える
				ENS.tree.doRender = true;
				this.collection.add(addOptionListSecond);
			},
			callbackAddMulResGraph_ : function(responseDto) {
				// Do nothing
				var result = responseDto.result;

				// 編集操作に失敗した場合はメッセージを表示する。
				if (result === "failure") {
					var message = responseDto.message;
					alert(message);
					return;
				}
			},
			callbackGetAllMulResGraph_ : function(mulResGraphDefinitionList) {
				var instance = this;
				var addOptionList = [];
				var addTopOptionList = [];
				_
						.each(
								mulResGraphDefinitionList,
								function(mulResGraphDefinition, index) {
									var treeOption = instance
											.createMulResGraphSignalTreeOption_(mulResGraphDefinition);

									if (treeOption.id.split("/").length <= 3) {
										addTopOptionList.push(treeOption);
									} else {
										addOptionList.push(treeOption);
									}

								});

				// renderのADDを実行する権限を無くす
				ENS.tree.doRender = false;
				// ツリーノードに追加する
				this.collection.add(addOptionList);
				// renderのADDを実行する権限を与える
				ENS.tree.doRender = true;

				this.collection.add(addTopOptionList);
			},
			deleteMulResGraph_ : function(executeOption) {
				// リアルタイム通信を止める
				appView.stopSyncData([ executeOption.treeId ]);

				// Ajax通信の送信先URL
				var callbackFunction = "callbackDeleteMulResGraph_";
				var url = ENS.tree.MULTIPLE_RESOURCE_GRAPH_DELETE_URL;

				// Ajax通信用の設定
				var settings = {
					data : {
						multipleResourceGraphName : executeOption.treeId
					},
					url : url
				};

				// 非同期通信でシグナル削除依頼電文を送信する。
				var ajaxHandler = new wgp.AjaxHandler();
				settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
				settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = callbackFunction;
				ajaxHandler.requestServerAsync(settings);
			},
			/**
			 * シグナル追加操作の結果を表示する。
			 */
			callbackAddSignal_ : function(responseDto) {
				var result = responseDto.result;

				// 追加操作に失敗した場合はメッセージを表示する。
				if (result === "fail") {
					var message = responseDto.message;
					alert(message);
					return;
				}
				var signalDefinition = responseDto.data;
				var signalId = signalDefinition.signalId;
				if (signalId === 0) {
					alert("This signalName already exists."
							+ "\nPlease change signal name and try again.");
					return;
				}
			},
			/**
			 * シグナル編集操作の結果を表示する。
			 */
			callbackEditSignal_ : function(responseDto) {
				var result = responseDto.result;

				// 編集操作に失敗した場合はメッセージを表示する。
				if (result === "fail") {
					var message = responseDto.message;
					alert(message);
					return;
				}
			},
			// シグナル削除処理応答受信後にツリーからシグナルを削除する。
			callbackDeleteSignal_ : function(responseDto) {
				var result = responseDto.result;

				// 削除操作に失敗した場合はメッセージを表示する。
				if (result === "fail") {
					var message = responseDto.message;
					alert(message);
					return;
				}
			},
			// レポート削除処理応答受信後にツリーからシグナルを削除する。
			callbackDeleteReport_ : function(data) {
				var reportName = data.reportName;
				if (reportName === undefined || reportName === null) {
					alert("Failed to delete report.");
					return;
				}

				var reportNameSplitList = reportName.split("/");
				var splitLength = reportNameSplitList.length;

				var treeId = "";
				// レポート名からツリーIDを作成する
				for ( var index = 1; index < splitLength; index++) {
					if (index == splitLength - 1) {
						treeId += ENS.tree.REPORT_PREFIX_ID;
					} else {
						treeId += "/";
					}
					treeId += reportNameSplitList[index];
				}

				this.collection.remove({
					id : treeId
				});
			},
			callbackAddReport_ : function(reportDefinition) {
				if (reportDefinition.message_ === "duplicate") {
					$('<div></div>')
							.appendTo('body')
							.html(
									'<div><h6>This report is already exsit. Do you override it?</h6></div>')
							.dialog(
									{
										modal : true,
										title : 'message',
										zIndex : 10000,
										autoOpen : true,
										width : 'auto',
										resizable : true,
										buttons : {
											Yes : function() {
												var url = ENS.tree.REPORT_ADD_DUPLICATE_URL;
												var settings = {
													data : {
														reportDefinitionDto : JSON
																.stringify(reportDefinition)
													},
													url : url
												};
												var ajaxHandler = new wgp.AjaxHandler();
												ajaxHandler
														.requestServerAsync(settings);
												$(this).dialog("close");
											},
											No : function() {
												$(this).dialog("close");
											}
										},
										close : function(event, ui) {
											$(this).remove();
										}
									});
				}
			},
			callbackEditMulResGraph_ : function(responseDto) {
				var result = responseDto.result;

				// 編集操作に失敗した場合はメッセージを表示する。
				if (result === "fail") {
					var message = responseDto.message;
					alert(message);
					return;
				}
			},
			// シグナル削除処理応答受信後にツリーからシグナルを削除する。
			callbackDeleteMulResGraph_ : function(responseDto) {
				var result = responseDto.result;

				// 削除操作に失敗した場合はメッセージを表示する。
				if (result === "fail") {
					var message = responseDto.message;
					alert(message);
					return;
				}
			},
			createSummarySignalTreeOption_ : function(summarySignalDefinition) {
				var summarySignalName = summarySignalDefinition.summarySignalName;
				if (summarySignalName == null) {
					return;
				}
				var nameSplitList = summarySignalName.split("/");
				var nameSplitListLength = nameSplitList.length;

				var showName = nameSplitList[nameSplitListLength - 1];
				// 親ノードのパス
				var targetTreeId = "";
				// 新規ノードのパス
				var reportTreeId = "";
				// 親ノードへのパスと、新規ノードのパスを作成する
				for ( var index = 1; index < nameSplitListLength; index++) {
					var nameSplit = nameSplitList[index];

					if (index == nameSplitListLength - 1) {
						reportTreeId += ENS.tree.REPORT_PREFIX_ID;
					} else {
						targetTreeId += "/";
						targetTreeId += nameSplit;

						reportTreeId += "/";
					}
					reportTreeId += nameSplit;
				}

				var summaryIcon = ENS.tree.SIGNAL_ICON_0;
				if (summarySignalDefinition.summarySignalStatus == 1) {
					summaryIcon = "signal_4";
				}
				var treeOption = {
					id : summarySignalName,
					treeId : summarySignalName,
					data : showName,
					parentTreeId : targetTreeId,
					icon : summaryIcon,
					type : ENS.tree.type.SUMMARYSIGNAL,
					message : summarySignalDefinition.message
				};

				return treeOption;
			},
			createSignalTreeOption_ : function(signalDefinition) {
				var signalName = signalDefinition.signalName;

				var nameSplitList = signalName.split("/");
				var nameSplitListLength = nameSplitList.length;

				var showName = nameSplitList[nameSplitListLength - 1];
				// 親ノードのパス
				var targetTreeId = "";
				// 新規ノードのパス
				var reportTreeId = "";
				// 親ノードへのパスと、新規ノードのパスを作成する
				for ( var index = 1; index < nameSplitListLength; index++) {
					var nameSplit = nameSplitList[index];

					if (index == nameSplitListLength - 1) {
						reportTreeId += ENS.tree.REPORT_PREFIX_ID;
					} else {
						targetTreeId += "/";
						targetTreeId += nameSplit;

						reportTreeId += "/";
					}
					reportTreeId += nameSplit;
				}

				var treeOption = {
					id : signalName,
					treeId : signalName,
					data : showName,
					parentTreeId : targetTreeId,
					icon : ENS.tree.SIGNAL_ICON_0,
					type : ENS.tree.type.SIGNAL,
					matchingPattern : signalDefinition.matchingPattern,
					signalDashboard : signalDefinition.signalDashboard,
					sendMail : signalDefinition.sendMail
				};

				return treeOption;
			},
			createReportTreeOption_ : function(reportDefinition) {
				var reportId = reportDefinition.reportId;

				var reportName = reportDefinition.reportName;

				var nameSplitList = reportName.split("/");
				var nameSplitListLength = nameSplitList.length;

				var showName = nameSplitList[nameSplitListLength - 1];
				// 親ノードのパス
				var targetTreeId = "";
				// 新規ノードのパス
				var reportTreeId = "";
				// 親ノードへのパスと、新規ノードのパスを作成する
				for ( var index = 1; index < nameSplitListLength; index++) {
					var nameSplit = nameSplitList[index];

					if (index == nameSplitListLength - 1) {
						reportTreeId += ENS.tree.REPORT_PREFIX_ID;
					} else {
						targetTreeId += "/";
						targetTreeId += nameSplit;

						reportTreeId += "/";
					}
					reportTreeId += nameSplit;
				}

				var treeOption = {
					id : reportTreeId,
					treeId : reportTreeId,
					data : showName,
					parentTreeId : targetTreeId,
					icon : ENS.tree.REPORT_ICON,
					type : ENS.tree.type.REPORT
				};

				return treeOption;
			},
			createMulResGraphSignalTreeOption_ : function(mulResGraphDefinition) {
				var multipleResourceGraphName = mulResGraphDefinition.multipleResourceGraphName;

				var nameSplitList = multipleResourceGraphName.split("/");
				var nameSplitListLength = nameSplitList.length;

				var showName = nameSplitList[nameSplitListLength - 1];
				// 親ノードのパス
				var targetTreeId = "";
				// 新規ノードのパス
				var reportTreeId = "";
				// 親ノードへのパスと、新規ノードのパスを作成する
				for ( var index = 1; index < nameSplitListLength; index++) {
					var nameSplit = nameSplitList[index];

					if (index == nameSplitListLength - 1) {
						reportTreeId += ENS.tree.REPORT_PREFIX_ID;
					} else {
						targetTreeId += "/";
						targetTreeId += nameSplit;

						reportTreeId += "/";
					}
					reportTreeId += nameSplit;
				}

				var treeOption = {
					id : multipleResourceGraphName,
					treeId : multipleResourceGraphName,
					data : showName,
					parentTreeId : targetTreeId,
					icon : ENS.tree.MULRECGRAPH_ICON,
					type : ENS.tree.type.MULTIPLERESOURCEGRAPH
				};

				return treeOption;
			},
			/**
			 * シグナルダイアログをクリアする。
			 */
			clearSignalDialog_ : function() {
				$("#signalId").val("");
				$("#signalName").val("");
				$("#matchingPattern").val("");
				$("#signalPatternValue select").val("");
				$("#patternValue_1").val("");
				$("#patternValue_2").val("");
				$("#patternValue_3").val("");
				$("#patternValue_4").val("");
				$("#patternValue_5").val("");
				$("#escalationPeriod").val(ENS.tree.DEFAULT_ESCALATION_PERIOD);
				$signalAlert = $("#signalAlertMailArea input[name='signalAlertMail']");
				$.each($signalAlert, function(index, data) {
					data.checked = false;
				});
			},
			/**
			 * レポートダイアログをクリアする。
			 */
			clearReportDialog_ : function() {
				$("#reportId").val("");
				$("#reportName").val("");
				$("#targetName").val("");
				$("#jquery-ui-datepicker-from").val("");
				$("#jquery-ui-datepicker-to").val("");
			},
			/**
			 * シグナルダイアログをクリアする。
			 */
			clearMulResGraphDialog_ : function() {
				$("#multipleResourceGraphId").val("");
				$("#multipleResourceGraphName").val("");
				$("#multipleResourceGraphLstBox2").empty();
				$("#multipleResourceGraphItems").val("");
			},

			clearSummarySignalDialog_ : function() {
				$("#summarySignalId").val("");
				$("#summarySignalName").val("");
				$("#summarySignalList2Box").empty();
				$("#and").attr('checked', 'checked');

			},
			inputMulResGraphDialog_ : function(treeModel) {
				// Ajax通信用の送信先URL
				var settings = {
					url : ENS.tree.MULTIPLE_RESOURCE_GRAPH_GET_URL,
					data : {
						multipleResourceGraphName : treeModel.get("id")
					}
				};

				var ajaxHandler = new wgp.AjaxHandler();
				var result = ajaxHandler.requestServerSync(settings);
				var multipleResourceGraphDefinition = JSON.parse(result);

				// 各入力項目を入力する
				$("#multipleResourceGraphId")
						.val(
								multipleResourceGraphDefinition.multipleResourceGraphId);

				// シグナル名の表示名称は自身より親のツリー構造を除外した値を指定する。
				$("#multipleResourceGraphName")
						.val(
								this
										.getSignalDisplayName_(multipleResourceGraphDefinition.multipleResourceGraphName));
				$("#beforemultipleResourceGraphName")
						.val(
								multipleResourceGraphDefinition.multipleResourceGraphName);

				// 正規表現パターンが指定されていない場合は系列直接指定であるため、
				// 被選択内容を生成する。
				if (multipleResourceGraphDefinition.measurementItemPattern.length === 0) {

					var measurementList = multipleResourceGraphDefinition.measurementItemIdList
							.split(",");
					for ( var i = 0; i < measurementList.length; i++) {
						$('#multipleResourceGraphLstBox2').append(
								"<option value='" + measurementList[i] + "'>"
										+ measurementList[i] + "</option>");
					}
				}

				$("#multipleResourceGraphItems").val(
						multipleResourceGraphDefinition.measurementItemPattern);

			},

			inputSummarySignalDialog_ : function(treeModel) {
				var instance = this;
				// Ajax通信用の送信先URL
				var settings = {
					url : ENS.tree.SUMMARYSIGNAL_GET_URL,
					data : {
						summarySignalName : treeModel.get("id")
					}
				};

				var ajaxHandler = new wgp.AjaxHandler();
				var result = ajaxHandler.requestServerSync(settings);
				var summarySignalDefinition = JSON.parse(result);

				$("#summarySignalId").val(
						summarySignalDefinition.summarySignalId);

				$("#summarySignalName")
						.val(
								this
										.getSignalDisplayName_(summarySignalDefinition.summarySignalName));
				var type = summarySignalDefinition.summarySignalType;

				if (type === 0) {
					$("#and").attr('checked', 'checked');
				} else {
					$("#or").attr('checked', 'checked');
				}

				$("#beforesummarySignalName").val(
						summarySignalDefinition.summarySignalName);
				var measurementList = summarySignalDefinition.signalList;
				for ( var i = 0; i < measurementList.length; i++) {
					if (measurementList[i] !== "") {
						$('#summarySignalList2Box')
								.append(
										"<option value='"
												+ measurementList[i]
												+ "'>"
												+ instance
														.getSignalDisplayName_(measurementList[i])
												+ "</option>");
					}
				}
			},
			inputSignalDialog_ : function(treeModel) {
				// Ajax通信用の送信先URL
				var settings = {
					url : ENS.tree.SIGNAL_GET_URL,
					data : {
						signalName : treeModel.get("id")
					}
				};

				var ajaxHandler = new wgp.AjaxHandler();
				var result = ajaxHandler.requestServerSync(settings);
				var signalDefinition = JSON.parse(result);
				var patternValueSplit = signalDefinition.patternValue
						.split(",");

				var level = signalDefinition.level;

				// レベル別の閾値を入力する
				if (level == 3) {
					$("#patternValue_2").val(patternValueSplit[0]);
					$("#patternValue_4").val(patternValueSplit[1]);
				} else if (level == 5) {
					_.each(patternValueSplit, function(data, index) {
						var patternValue = patternValueSplit[index];
						$("#patternValue_" + (index + 1)).val(patternValue);
					});
				}

				// 各入力項目を入力する
				$("#signalId").val(signalDefinition.signalId);

				// シグナル名の表示名称は自身より親のツリー構造を除外した値を指定する。
				$("#signalName")
						.val(
								this
										.getSignalDisplayName_(signalDefinition.signalName));
				$("#beforeSignalName").val(signalDefinition.signalName);

				$("#matchingPattern").val(signalDefinition.matchingPattern);
				$("#signalPatternValue select").val(level);

				var escalationPeriod = $("#escalationPeriod").val(
						(signalDefinition.escalationPeriod - 0) / 1000);
				var sendMail = signalDefinition.sendMail;
				if (sendMail === true) {
					$('#signalAlertMail').prop('checked', true);
				} else {
					$('#signalAlertMail').prop('checked', false);
				}
			},
			getIcon : function(signalDefinition) {
				var icon = "";
				var level = signalDefinition.level;
				var signalValue = signalDefinition.signalValue;

				if (signalValue === undefined) {
					return ENS.tree.SIGNAL_ICON_0;
				}
				var signalLevel = 0;
				if (level == 3) {
					signalLevel = parseInt(signalValue, 10);
					if (0 <= signalLevel && signalLevel < 3) {
						icon = "signal_" + 2 * signalLevel;
					} else {
						icon = ENS.tree.SIGNAL_ICON_STOP;
					}
				} else if (level == 5) {
					signalLevel = parseInt(signalValue, 10);
					if (0 <= signalLevel && signalLevel < 5) {
						icon = "signal_" + signalLevel;
					} else {
						icon = ENS.tree.SIGNAL_ICON_STOP;
					}
				} else {
					icon = ENS.tree.SIGNAL_ICON_STOP;
				}

				return icon;
			},
			/**
			 * ツリーの表示状態を復元する。 selectTreeId {String} 選択しているツリーのID openNodes
			 * {String[]} 展開しているツリーのID配列
			 */
			restoreDisplayState : function(selectTreeId, openNodes) {

				// ツリーの展開状態を復元する。
				if (openNodes && openNodes.length > 0) {
					this.openNodes(openNodes, "id");
				}

				// ツリーの選択状態を復元する。
				if (selectTreeId) {
					this.selectNode(selectTreeId, "id");
				}
			},
			/**
			 * ツリー要素の基となるコレクションに要素が追加された場合に、変更内容をツリーに反映する。
			 */
			onAdd : function(treeModel) {

				var treeType = treeModel.get("type");
				if (ENS.tree.type.SUMMARYSIGNAL == treeType) {
					var message = treeModel.get("message");
					if (message !== "" && message != undefined) {
						alert(message);
						return;
					}
					appView.syncData([ treeModel.get("id") ]);
				}

				// 画面に反映する。(WGP内の実装では、初期化が正しく判定されないのでここで実装する)
				this.render(wgp.constants.RENDER_TYPE.ADD, treeModel);

				// シグナルの場合はリアルタイム更新開始処理を行う。
				if (ENS.tree.type.SIGNAL == treeType) {
					appView.syncData([ treeModel.get("id") ]);
				}

			},
			/**
			 * ツリー要素の基となるコレクションが変更された場合に、 変更内容をツリーに反映する。
			 */
			onChange : function(treeModel) {

				// 継承元の変更処理を実行して画面に反映する。
				ENS.ResourceTreeView.prototype.onChange.call(this, treeModel);

				var treeId = treeModel.get("id");
				var treeType = treeModel.get("type");
				var message = treeModel.get("message");
				if (ENS.tree.type.SUMMARYSIGNAL == treeType) {

					if (ENS.tree.doMessage && message !== ""
							&& message != undefined) {
						alert(message);
						ENS.tree.doMessage = false;
						return;
					} else if (message == "" || message == undefined) {
						// 継承元の変更処理を実行して画面に反映する。
						wgp.TreeView.prototype.onChange.call(this, treeModel);

						var previousTreeId = treeModel.previousAttributes()["id"];
						var treeTag = this.getTreeNode(previousTreeId, "id");
						treeTag.attr("id", treeId);
						var treeIcon = treeModel.get("icon");
						var iconTag = treeTag.find("ins");

						// 状態を一度クリアする。
						iconTag.removeClass(ENS.tree.SIGNAL_ICON_0);
						iconTag.removeClass(ENS.tree.SIGNAL_ICON_1);
						iconTag.removeClass(ENS.tree.SIGNAL_ICON_2);
						iconTag.removeClass(ENS.tree.SIGNAL_ICON_3);
						iconTag.removeClass(ENS.tree.SIGNAL_ICON_4);
						iconTag.removeClass(ENS.tree.SIGNAL_ICON_5);

						// 状態を再度設定する。
						iconTag.addClass(treeIcon);

						// 前回のシグナルIDによるリアルタイム更新を停止
						// 新しいシグナルIDによるリアルタイム更新を開始
						appView.stopSyncData([ previousTreeId ]);
						appView.syncData([ treeId ]);
					}

				} else {

					// 継承元の変更処理を実行して画面に反映する。
					wgp.TreeView.prototype.onChange.call(this, treeModel);

					var previousTreeId = treeModel.previousAttributes()["id"];
					var treeTag = this.getTreeNode(previousTreeId, "id");
					treeTag.attr("id", treeId);
				}
				// シグナルの場合は状態を変更する。
				if (ENS.tree.type.SIGNAL == treeType) {
					var treeIcon = treeModel.get("icon");
					var iconTag = treeTag.find("ins");

					// 状態を一度クリアする。
					iconTag.removeClass(ENS.tree.SIGNAL_ICON_0);
					iconTag.removeClass(ENS.tree.SIGNAL_ICON_1);
					iconTag.removeClass(ENS.tree.SIGNAL_ICON_2);
					iconTag.removeClass(ENS.tree.SIGNAL_ICON_3);
					iconTag.removeClass(ENS.tree.SIGNAL_ICON_4);
					iconTag.removeClass(ENS.tree.SIGNAL_ICON_5);

					// 状態を再度設定する。
					iconTag.addClass(treeIcon);

					// 前回のシグナルIDによるリアルタイム更新を停止
					// 新しいシグナルIDによるリアルタイム更新を開始
					appView.stopSyncData([ previousTreeId ]);
					appView.syncData([ treeId ]);
				}

			},
			/**
			 * ツリー要素の基となるコレクションの要素が削除された場合に、削除内容をツリーに反映する。
			 */
			onRemove : function(treeModel) {

				// 継承元の削除処理を実行して画面に反映する。
				wgp.TreeView.prototype.onRemove.call(this, treeModel);

				var treeType = treeModel.get("type");

				// シグナルの場合はリアルタイム更新開始処理を行う。
				if (ENS.tree.type.SIGNAL == treeType
						|| ENS.tree.type.SUMMARYSIGNAL == treeType) {
					appView.stopSyncData([ treeModel.get("id") ]);
				}

			},
			// シグナル名を基にシグナルの親階層のツリーIDを取得する。
			getSignalParentTreeId_ : function(signalName) {
				var lastIndexOf = signalName.lastIndexOf("/");
				return signalName.substr(0, lastIndexOf);
			},
			// シグナル名を基にシグナルの表示名称を取得する。
			getSignalDisplayName_ : function(signalName) {
				var lastIndexOf = signalName.lastIndexOf("/");
				return signalName.substr(lastIndexOf + 1);
			},
			onComplete : function(type) {

				if (type == wgp.constants.syncType.SEARCH) {
					this.renderAll();
				}
			},
			summarySignalPushOkFunction : function(event, option) {

				// add tree data for SummarySignal
				var treeId = option.treeId;
				var summarySignalDispalyName = $("#summarySignalName").val();
				var summarySignalName = treeId + "/" + summarySignalDispalyName;

				// Ajax通信のコールバック関数名
				var callbackFunction = "";
				// Ajax通信の送信先URL
				var url = "";
				// サーバに送信するデータ
				var sendData;
				// Ajax通信のコールバック関数と送信先URLを追加か編集かによって決める
				// SummarySignal追加時
				if (option.summarySignalType == ENS.tree.ADD_SUMMARYSIGNAL_TYPE) {

					sendData = this
							.createSendSummarySignalData_(summarySignalName);
					callbackFunction = "callbackSummarySignal_";
					url = ENS.tree.SUMMARYSIGNAL_ADD_URL;

					// SummarySignal編集時
				} else if (option.summarySignalType == ENS.tree.EDIT_SUMMARYSIGNAL_TYPE) {
					sendData = this
							.createSendSummarySignalData_(summarySignalName);
					callbackFunction = "callbackSummarySignal_";
					url = ENS.tree.SUMMARYSIGNAL_EDIT_URL;
				}

				// Ajax通信用の設定
				var settings = {
					data : sendData,
					url : url
				};

				// 非同期通信でデータを送信する
				var ajaxHandler = new wgp.AjaxHandler();
				settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
				settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = callbackFunction;
				ajaxHandler.requestServerAsync(settings);

				// 閾値判定定義の入力内容をクリアする。
				this.clearSummarySignalDialog_();
			},
			createSendSummarySignalData_ : function(summarySignal) {
				var summarySignalList;
				var summarySignalItem;
				summarySignalList = $("#summarySignalList2Box>option").map(
						function() {
							return $(this).val();

						});
				summarySignalItem = summarySignalList.get(0);
				for ( var i = 1; i < summarySignalList.length; i++) {
					summarySignalItem = summarySignalItem + ","
							+ summarySignalList.get(i);
				}
				var summarySignalType = $(
						"input[name='summarySignal_type']:checked").val();
				if (summarySignalType == "and") {
					summarySignalType = 0;
				} else {
					summarySignalType = 1;
				}

				var summarySignalDefinition = {
					summarySignalId : $("#summarySignalId").val(),
					summarySignalName : summarySignal,
					summarySignalType : summarySignalType,
					signalList : summarySignalItem
				};
				var sendData = {
					summarySignalDefinition : JSON
							.stringify(summarySignalDefinition)
				};
				return sendData;

			},
			callbackSummarySignal_ : function(responseDto) {
				var result = responseDto.result;

				// 追加操作に失敗した場合はメッセージを表示する。
				if (result === "failure") {
					var message = responseDto.message;
					alert(message);
					return;
				}
			},
			deleteSummarySignal_ : function(executeOption) {

				// リアルタイム通信を止める
				appView.stopSyncData([ executeOption.treeId ]);

				// Ajax通信の送信先URL
				var callbackFunction = "callbackSummarySignal_";
				var url = ENS.tree.SUMMARYSIGNAL_DELETE_URL;

				// Ajax通信用の設定
				var settings = {
					data : {
						summarySignalName : executeOption.treeId
					},
					url : url
				};

				// 非同期通信でシグナル削除依頼電文を送信する。
				var ajaxHandler = new wgp.AjaxHandler();
				settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
				settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = callbackFunction;
				ajaxHandler.requestServerAsync(settings);
			},
			editSummarySignalFunction_ : function(event, target, menuId,
					tmpClickTarget, summarySignalNames) {

				var clickTarget = event.target;
				var id = $(clickTarget).attr("id");
				var targetOption = this.getContextOption_(id);

				var executeOption = targetOption.get("executeOption");
				executeOption.treeId = $(tmpClickTarget).attr("id");
				executeOption.displayName = $(tmpClickTarget).text();
				var targetNodeName = executeOption.displayName.replace(/\s+/g,
						"");
				var url = ENS.tree.SUMMARYSIGNAL_EDIT_URL;

				var settings = {
					data : {
						summarySignalName : summarySignalNames
					},
					url : url
				};
				var ajaxHandler = new wgp.AjaxHandler();
				ajaxHandler.requestServerAsync(settings);

			},
			schedulingReportPushOkFunction : function(event, option) {
				// Ajax通信のコールバック関数名
				var callbackFunction = "";
				// Ajax通信の送信先URL
				var url = "";
				// レポート出力定義
				var schedulingReportDefinition;
				// サーバに送信するデータ
				var sendData;
				// Ajax通信のコールバック関数と送信先URLをシグナルタイプによって決める
				if (option.signalType == ENS.tree.OUTPUT_REPORT_SCHEDULE_TYPE) {
					schedulingReportDefinition = this
							.createSchedulingReportDefinition_(option);
					sendData = {
						schedulingReportDefinition : JSON
								.stringify(schedulingReportDefinition)
					};
					callbackFunction = "callbackAddSchedulingReport_";
					url = ENS.tree.SCHEDULING_REPORT_ADD_URL;

				}
				// Ajax通信用の設定
				var settings = {
					data : sendData,
					url : url
				};
				// 非同期通信でデータを送信する
				var ajaxHandler = new wgp.AjaxHandler();
				settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
				settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = callbackFunction;
				ajaxHandler.requestServerAsync(settings);

				// レポート出力ダイアログの入力内容をクリアする。
				this.clearReportDialog_();

				// ツリーノードを追加する
				var treeOption = this
						.createReportTreeOption_(schedulingReportDefinition);
				this.collection.add([ treeOption ]);
			},
			schedulingReportPushCancelFunction : function(event, option) {

				// レポート出力ダイアログの入力内容をクリアする。
				this.clearReportDialog_();
			},
			createSchedulingReportDefinition_ : function(option) {
				var treeId = option.treeId;
				var schedulingReportName = $("#schedulingReportName").val();
				var reportFullName = treeId + "/" + schedulingReportName;

				// レポート出力定義を作成する
				var schedulingReportDefinition = {
					reportId : $("#reportId").val(),
					reportName : reportFullName,
					targetMeasurementName : $("#schedulingReportTargetName")
							.val(),
					term : $('input[name=scheduling_report_type]:checked')
							.val(),
					time : $("#schedulingReportSelectedTimeId").val() + ":"
							+ $("#schedulingReportSelectedTimeMinuteId").val(),
					day : $("#schedulingReportSelectedWeeklyId").val(),
					date : $("#schedulingReportSelectedMonthlyId").val()
				};

				return schedulingReportDefinition;
			},
			callbackAddSchedulingReport_ : function(responseDto) {
				var result = responseDto.result;

				// 追加操作に失敗した場合はメッセージを表示する。
				if (result === "fail") {
					var message = responseDto.message;
					alert(message);
					return;
				}
				var schedulingReportDefinition = responseDto.data;
				var reportId = schedulingReportDefinition.reportId;
			},

			clearSchedulingReportDialog_ : function() {
				$("#schedulingReportId").val("");
				$("#schedulingReportName").val("");
				$("#beforeSchedulingReportName").val("");
				$("#schedulingReportTargetName").val("");
				$('input[name=scheduling_report_type]:checked').val("");
				$("#schedulingReportSelectedTimeId").val("") + ":"
						+ $("#schedulingReportSelectedTimeMinuteId").val("");
				$("#schedulingReportSelectedWeeklyId").val("");
				$("#schedulingReportSelectedMonthlyId").val("");
			},
			getSchedulingParentId_ : function(reportName) {
				var lastIndexOf = reportName.lastIndexOf("/");
				return reportName.substr(0, lastIndexOf);
			},
			reportScheduleOnSelect_ : function(event, target, menuId,
					tmpClickTarget) {
				var clickTarget = event.target;
				var id = $(clickTarget).attr("id");

				var targetOption = this.getContextOption_(id);
				if (targetOption == null) {
					return;
				}

				var executeOption = targetOption.get("executeOption");
				executeOption.treeId = $(tmpClickTarget).attr("id");
				executeOption.displayName = $(tmpClickTarget).text();

				// 削除が選択された場合はそのノードを削除する
				if (id == ENS.tree.DELETE_REPORT_TYPE) {
					if (confirm("Are you sure you want to delete it?")) {
						this.deleteReport_(executeOption);
					}
					return;
				}

				// 開くViewのクラス
				var executeClass = targetOption.get("executeClass");
				if (executeClass) {
					if (id == ENS.tree.OUTPUT_REPORT_SCHEDULE_TYPE) {
						// デフォルトのレポート名にレポート出力対象のノード名を入れる(空白を削除する処理を入れている)
						var targetNodeName = executeOption.displayName.replace(
								/\s+/g, "");
						$("#schedulingReportName").val(
								targetNodeName + " scheduling report");
						// Target Measurement Nameにツリー階層を入力する
						$("#schedulingReportTargetName").val(
								executeOption.treeId);
					}

					// set execute class and function, if push ok
					// button.
					executeOption.okObject = this;
					executeOption.okFunctionName = "schedulingReportPushOkFunction";
					executeOption.cancelObject = this;
					executeOption.cancelFunctionName = "schedulingReportPushCancelFunction";
					eval("new " + executeClass + "(executeOption)");
				}
			}

		});
