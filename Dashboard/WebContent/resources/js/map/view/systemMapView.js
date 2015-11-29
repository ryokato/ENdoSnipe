ENS.SystemMapView = wgp.AbstractView.extend({
	tagName : "div",
	initialize : function(argument) {
		// 定数の定義
		this.DIV_ID_CONTROLLER = "range_controller";
		this.ID_CLUSTER_NAME = "cluster_name";

		_.bindAll();

		var width = $("#" + this.$el.attr("id")).width();
		var height = $("#" + this.$el.attr("id")).height() - 300;
		_.extend(argument, {
			width : width,
			height : height
		});
		this.collection = new MapElementList();

		var ajaxHandler = new wgp.AjaxHandler();
		this.ajaxHandler = ajaxHandler;

		var contextMenuId = this.cid + "_contextMenu";
		this.contextMenuId = contextMenuId;

		this.systemMapId = argument["systemMapId"];

		// 本クラスのrenderメソッド実行
		this.renderExtend();

		// イベント実行可設定
		this.isExecuteEvent_ = true;
	},
	renderExtend : function() {

		// システムマップを読み込む。
		this.onLoad();
		return this;
	},
	destroy : function() {
		// TODO 未実装。
	},
	onSave : function(Model) {
		// TODO 未実装。
	},
	onLoad : function() {
		// Clusterのドロップダウンリストを描画する
		this._renderClusterList();

		// スクロール位置をリセット
		$("#" + this.$el.attr("id")).scrollTop(0);
		$("#" + this.$el.attr("id")).scrollLeft(0);

		// コレクションをリセット
		this.collection.reset();

		// システムマップを読み込む。

		// TODO 該当クラスタのエージェント名を取得する。
		var agentKeyList = [ "ENdoSnipe-demo_000", "ENdoSnipe-demo_001" ];

		var instance = this;
		// TODO AgentごとのProfilerViewから値を取得する。
		// 該当クラスタの
		var connection = [];
		$.each(agentKeyList, function(index, agentKey) {
			var profileList = [ {
				Target : "jdbc:postgresql://localhost:5432/endosnipedemo-db"
			}, {
				Target : "/ENdoSnipe-demo"
			} ];

			// 関連を抽出する。
			var relTargetList = instance._getRelationTarget(profileList);
			// 関連するオブジェクトを生成する。（関連のないAgentは生成しない。）
			instance._createRelationObj(instance.systemMapId, agentKeyList,
					relTargetList);

			var conListTmp = instance._getRelation(agentKey, relTargetList);

			// 関連を設定する。
			connection = $.merge(connection, conListTmp);
		});

		this._setRelation(connection);

		// システムマップの描画設定。
		this.systemMapSetting = {
			init : function() {
				var stateMachineConnector = {
					connector : [ "Bezier", {
						curviness : 70
					} ],
					paintStyle : {
						lineWidth : 2,
						strokeStyle : "#add8e6",
						outlineWidth : 1,
						outlineColor : "#87ceeb"
					},
					hoverPaintStyle : {
						strokeStyle : "#87ceeb"
					},
					endpoint : "Dot",
					endpointStyle : {
						fillStyle : "#add8e6",
						radius : 5
					},
					anchor : [ "Right", "Left" ],
					overlays : [ [ "Arrow", {
						width : 20,
						length : 20
					// TODO ラベルは関連以外を描画する際に指定する。
					// } ], [ "Label", {
					// label : "3 calls / min, 54ms",
					// id : "label"
					} ] ]
				};

				for (var i = 0; i < connection.length; i++) {
					jsPlumb.connect({
						source : connection[i][0],
						target : connection[i][1]
					}, stateMachineConnector);
				}

				jsPlumb.draggable(jsPlumb.getSelector(".map-elm"), {
					containment : "#" + this.systemMapId
				});
			}
		};

		this.systemMap = jsPlumb.ready(this.systemMapSetting.init);

	},
	_renderClusterList : function() {
		// クラスタ選択ドロップダウンリスト
		var $selectorLabel = $("<span/>").html("Cluster: ");
		$selectorLabel.css("margin-left", "10px");
		var $selector = this._createClusterSelector();
		
		// コンテナ
		var $container = $("#range_area");
		
		// スタイルの変更
		$container.css("font-size", "0.8em");
		$container.css("margin-bottom", "0px");
		
		// コンテナに追加
		var $subcontainer = $("<div/>");
		$subcontainer.css("float", "left");
		$container.append($subcontainer);
		$subcontainer.append($selectorLabel);
		$subcontainer.append($selector);
	},
	_getRelationTarget : function(profileList) {
		var relTargetList = {
			db : [],
			agent : []
		};
		$.each(profileList, function(index, proflie) {
			var target = proflie.Target;
			var targetStr = "";

			// jdbc判定
			if (target.startsWith("jdbc")) {
				var strList = target.split("/");
				targetStr = strList[strList.length - 1];

				if ($.inArray(targetStr, relTargetList.db) === -1) {
					relTargetList.db.push(targetStr);
				}
			} else {
				// リクエスト判定
				targetStr = target.split("/")[1];
				if ($.inArray(targetStr, relTargetList.agent) === -1) {
					relTargetList.agent.push(targetStr);
				}
			}
		});

		return relTargetList;
	},
	_createRelationObj : function(id, agentKeyList, relTargetList) {
		// TODO オブジェクトの種類によりクラスを当て分ける。

		// DBオブジェクトを生成
		$.each(relTargetList.db, function(index, dbName) {
			if ($("#db_" + dbName).length === 0) {
				$("#" + id).append(
						'<div class="map-elm" id="db_' + dbName + '"><span>' + dbName
								+ '</span><div class="data-resource"></div></div>')
			}
		});

		// Agentオブジェクトを生成
		// クラスタ構成のAgent名は末尾に連番が付与されているため、その分生成する。
		$.each(relTargetList.agent, function(index, agentName) {
			$.each(agentKeyList, function(index, agentKey) {
				if ($("#agent_" + agentKey).length === 0) {
					if (agentKey === agentName) {
						$("#" + id).append(
								'<div class="map-elm" id="agent_' + agentKey
										+ '"><span>' + agentKey
										+ '</span><div class="app"></div></div>')
					} else if (agentKey.startsWith(agentName)
							&& agentKey.match(/_\d\d\d$/)) {
						$("#" + id).append(
								'<div class="map-elm" id="agent_' + agentKey
										+ '"><span>' + agentKey
										+ '</span><div class="app"></div></div>')
					}
				}
			});
		});
	},
	_getRelation : function(agentKey, relTargetList) {
		var connection = [];

		// DBの関連を指定。
		$.each(relTargetList.db, function(index, dbName) {
			connection.push([ "agent_" + agentKey, "db_" + dbName ]);
		});

		// Agentの関連を指定。（自身に対する関連は指定しない）
		$.each(relTargetList.agent, function(index, agentName) {
			$.each($("[id^=agent_" + agentName + "]"),
					function(index, agentObj) {
						if ("agent_" + agentKey !== agentObj.id) {
							connection.push([ "agent_" + agentKey,
									"agent_" + agentName ]);
						}
					});
		});

		return connection;
	},
	_setRelation : function(connection) {
		// TODO ライブラリを利用して適切な配置にする。

		var costList = [];
		$.each(connection, function(index, conn) {
			var target1 = conn[0];
			var target2 = conn[1];

			var existTarget1 = -1;
			var existTarget2 = -1;
			$.each(costList, function(index, target) {
				if (target.id === target1) {
					existTarget1 = index;
				} else if (target.id === target2) {
					existTarget2 = index;
				}
			});

			if (existTarget1 == -1) {
				costList.push({
					id : target1,
					cost : 1
				});
			} else {
				costList[existTarget1].cost == costList[existTarget1].cost + 1;
			}
			if (existTarget2 == -1) {
				costList.push({
					id : target2,
					cost : -1
				});
			} else {
				costList[existTarget2].cost == costList[existTarget2].cost - 1;
			}
		});

		// コストの高い順にソートする。
		costList.sort(function(a, b) {
			var costA = a["cost"];
			var costB = b["cost"];
			if (costA < costB)
				return 1;
			if (costA > costB)
				return -1;
			return 0;
		});

		// コストが高いオブジェクト順に左に配置。
		// 配置した列に複数存在する場合は、１行下に配置。
		var targetCost = 0;
		var marginTop = 0;
		var marginLeft = 0;
		$.each(costList, function(index, target) {
			if (index === 0) {
				targetCost = target.cost;
			} else if (target.cost < targetCost) {
				targetCost = target.cost;
				marginLeft += 240;
				marginTop = 0;
			} else {
				marginLeft = 0;
				marginTop += 160;
			}

			$("#" + target.id).css("top", marginTop + "px");
			$("#" + target.id).css("left", marginLeft + "px");

		});
	},
	_createClusterSelector : function() {
		var $selector = $("<select/>");
		$selector.attr("id", this.ID_CLUSTER_NAME);
		
		// Ajax通信用の設定
		var settings = {
				url: ENS.tree.GET_TOP_NODES
		};
		
		// 非同期通信でデータを送信する
		var ajaxHandler = new wgp.AjaxHandler();
		settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
		var result = ajaxHandler.requestServerSync(settings);
		var nodes = $.parseJSON(result);
		this._callbackGetTopNodes($selector, nodes);
		
		return $selector;
	},
	_callbackGetTopNodes : function($selector, topNodes) {
		var cnt = 0;
		for(var i in topNodes){
			var cluster = topNodes[i];
			$option = $("<option/>");
			$option.attr("value", cluster.data);
			$option.html(cluster.data);
			$selector.append($option);
			if (cnt == 0) {
				$selector.val(cluster.data);
			}
			cnt++;
		}
		
		var instance = this;
		$selector.change(function(){
			
		});
	}
});