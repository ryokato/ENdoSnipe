ENS.SystemMapView = wgp.AbstractView
    .extend({
      tagName : "div",
      initialize : function(argument) {
        // 定数の定義
        this.DIV_ID_CONTROLLER = "range_controller";
        this.ID_CLUSTER_NAME = "cluster_name";
        this.ID_RELOAD_BTN = "reload_btn";
        this.SOCKET_CLOSE_TIME = 5000;

        // 配置位置の定数
        this.OBJ_MARGIN = 10;
        this.OBJ_MARGIN_TOP = 120;
        this.OBJ_MARGIN_LEFT = 280;

        this.profileList = [];

        // this.websocketClient = this._connectSocket();

        // リアルタイム描画の制御フラグ（falseでコネクションを閉じる。）
        this.isOpenSocket = true;

        var ajaxHandler = new wgp.AjaxHandler();
        this.ajaxHandler = ajaxHandler;

        this.systemMapId = argument["systemMapId"];

        // 本クラスのrenderメソッド実行
        this.renderExtend();
      },
      _connectSocket : function() {
        var websocketClient = new wgp.WebSocketClient(this, "notifyEvent");
        websocketClient.initialize();
        return websocketClient;
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
        this._renderSelectArea();

        // SystemMapの読み込み。
        this._onLoadSystemMap();

        // スクロール位置をリセット
        $("#" + this.$el.attr("id")).scrollTop(0);
        $("#" + this.$el.attr("id")).scrollLeft(0);

      },
      _onLoadSystemMap : function() {
        // マップのクリア
        jsPlumb.empty(this.systemMapId);
        jsPlumb.detachEveryConnection();

        // クラスタ単位でエージェント名を取得し、Profile取得通知を呼び出す。
        this.agentKeyList = this._getAgentList($("#" + this.ID_CLUSTER_NAME)
            .val());
        this.targetAgentKeyList = $.extend(true, [], this.agentKeyList);
        this._loadNotify();

        // profileを取得して描画する。
        var instance = this;
        setTimeout(
            function() {
              $
                  .each(
                      instance.agentKeyList,
                      function(index, agentKey) {
                        var settings = {
                          url : ENS.tree.PROFILE_GET,
                          data : {
                            agentName : agentKey
                          }
                        };

                        // 非同期通信でデータを送信する
                        settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = instance;
                        var result = instance.ajaxHandler
                            .requestServerSync(settings);

                        var profileListTmp = $.parseJSON(result);
                        var dataTmp = [];
                        $.each(profileListTmp,
                            function(index, notificationData) {

                              var existFlag = false;
                              $.each(instance.profileList, function(index,
                                  profile) {
                              });
                              dataTmp.push(notificationData);
                            });
                        instance.profileList.push({
                          agentKey : agentKey,
                          dataList : dataTmp
                        });
                      });

              instance._renderSystemMap();
              $.each(instance.agentKeyList, function(index, agentKey) {
                instance._resetNotify(agentKey);
              });
            }, this.SOCKET_CLOSE_TIME);

      },
      _loadNotify : function() {
        var instance = this;
        $.each(this.agentKeyList, function(index, agentKey) {
          // if ($.inArray(agentKey, instance.targetAgentKeyList) !== -1) {
          ENS.tree.agentName = agentKey;
          var settings = {
            data : {
              agentName : ENS.tree.agentName
            },
            url : ENS.tree.PROFILER_RELOAD
          };
          instance._startNotify(agentKey);

          // instance.targetAgentKeyList = instance.targetAgentKeyList
          // .filter(function(v) {
          // return v != agentKey;
          // });
          // return false;
          // }
        });
      },
      _startNotify : function(agentKey) {
        var instance = this;
        ENS.tree.agentName = agentKey;
        var settings = {
          data : {
            agentName : ENS.tree.agentName
          },
          url : ENS.tree.PROFILER_RELOAD
        };
        instance.ajaxHandler.requestServerAsync(settings);
      },
      notifyEvent : function(notificationList) {

        // 受け取った通知からProfileを抽出し、画面に保持する。
        var instance = this;
        var agentKey = "";
        $.each(notificationList, function(notification, dataObj) {

          // Profile以外の通知は処理を行わない。
          if (!notification.endsWith("profiler")) {
            return false;
          }
          agentKey = notification.split("/")[0];
          $.each(dataObj, function(key, notificationData) {

            var existFlag = false;
            var dataTmp = [];
            $.each(instance.profileList, function(index, profile) {
              if (profile.agentKey === agentKey) {
                existFlag = true;
                profile.dataList.push(notificationData.updateData);
              } else {
                dataTmp.push(notificationData.updateData);
              }
            });

            if (!existFlag) {
              instance.profileList.push({
                agentKey : agentKey,
                dataList : dataTmp
              });
            }
          });
        });

        // 最初の通知を受け取った数秒後に描画をやめる。
        setTimeout(function() {
          if (agentKey) {
            instance._resetNotify(agentKey);
            instance._loadNotify();
          }

          // agentKeyが全て通知し終わっていれば、マップを描画する。
          if (instance.targetAgentKeyList.length === 0) {
            if (instance.isOpenSocket) {
              instance.isOpenSocket = false;
              instance._renderSystemMap();
            }
          }
        }, this.SOCKET_CLOSE_TIME);
      },
      _resetNotify : function(agentName) {
        var settings = {
          data : {
            agentName : agentName
          },
          url : ENS.tree.PROFILER_RESET
        };
        var ajaxHandler = new wgp.AjaxHandler();
        ajaxHandler.requestServerAsync(settings);
      },
      _renderSystemMap : function() {

        // TODO DEMO処理ここから //
        // this.agentKeyList = [ "web_000", "web_001", "web_002",
        // "batch-manager_000", "batch-cluster_000", "batch-cluster_001",
        // "batch-cluster_002" ];
        //
        // var authDB = "jdbc:postgresql://postgres:5432/auth-db";
        // var batchDB = "jdbc:postgresql://postgres:5432/batch-db";
        // var appDB = "jdbc:postgresql://postgres:5432/app-db";
        //
        // this.profileList = [];
        // this.profileList.push({
        // agentKey : "web_000",
        // dataList : [ {
        // className : "/batch-manager"
        // }, {
        // className : authDB
        // }, {
        // className : appDB
        // } ]
        // });
        // this.profileList.push({
        // agentKey : "web_001",
        // dataList : [ {
        // className : "/batch-manager"
        // }, {
        // className : authDB
        // }, {
        // className : appDB
        // } ]
        // });
        // this.profileList.push({
        // agentKey : "web_002",
        // dataList : [ {
        // className : "/batch-manager"
        // }, {
        // className : authDB
        // }, {
        // className : appDB
        // } ]
        // });
        // this.profileList.push({
        // agentKey : "batch-manager_000",
        // dataList : [ {
        // className : "/batch-cluster"
        // }, {
        // className : batchDB
        // } ]
        // });
        // this.profileList.push({
        // agentKey : "batch-cluster_000",
        // dataList : [ {
        // className : appDB
        // } ]
        // });
        // this.profileList.push({
        // agentKey : "batch-cluster_001",
        // dataList : [ {
        // className : appDB
        // } ]
        // });
        // this.profileList.push({
        // agentKey : "batch-cluster_002",
        // dataList : [ {
        // className : appDB
        // } ]
        // });
        // TODO DEMO処理ここまで //

        // 関連を抽出する。
        var relTargetList = this._getRelationTarget(this.profileList);
        // 関連するオブジェクトを生成する。（関連のないAgentは生成しない。）
        this._createRelationObj(this.systemMapId, this.agentKeyList,
            relTargetList);

        // 関連を設定する。
        var instance = this;
        var connection = instance._getRelation(relTargetList);

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
                strokeStyle : "#b0c4de",
                outlineWidth : 1,
                outlineColor : "#b0c4de"
              },
              hoverPaintStyle : {
                strokeStyle : "#6495ed"
              },
              endpoint : "Dot",
              endpointStyle : {
                fillStyle : "#b0c4de",
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
      _renderSelectArea : function() {
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

        // リロードボタン配置
        var $reloadBtn = $("<button/>");
        $reloadBtn.attr("id", this.ID_RELOAD_BTN);
        $reloadBtn.css("margin-left", "10px");
        $reloadBtn.html("reload");

        var instance = this;
        $reloadBtn.click(function() {
          // SystemMapの読み込み。
          // instance.websocketClient = instance._connectSocket();
          instance.isOpenSocket = true;
          instance._onLoadSystemMap();
        });

        $subcontainer.append($reloadBtn);
      },
      _getRelationTarget : function(profileList) {
        var relTargetList = {};
        var instance = this;
        $.each(profileList, function(index, proflie) {
          var relationTarget = {
            db : [],
            agent : []
          };

          var agentKey = proflie.agentKey;
          var dataList = proflie.dataList;

          $.each(dataList, function(index, data) {
            var target = data.className;
            var targetStr = "";

            // jdbc判定
            if (target.startsWith("jdbc")) {
              var strList = target.split("/");
              targetStr = strList[strList.length - 1];

              if ($.inArray(targetStr, relationTarget.db) === -1) {
                relationTarget.db.push(targetStr);
              }
            } else {
              // リクエスト判定
              targetStr = target.split("/")[1];
              if (targetStr) {
                $.each(instance.agentKeyList, function(index, baseAgentKey) {
                  if (baseAgentKey.startsWith(targetStr)) {
                    if ($.inArray(baseAgentKey, relationTarget.agent) === -1) {
                      relationTarget.agent.push(baseAgentKey);
                    }
                  }
                });
              }
            }
          });

          if (!relTargetList[agentKey] || relTargetList[agentKey] === 0) {
            relTargetList[agentKey] = {
              db : [],
              agent : []
            };
          }

          relTargetList[agentKey].db = $.merge(relTargetList[agentKey].db,
              relationTarget.db);
          relTargetList[agentKey].agent = $.merge(
              relTargetList[agentKey].agent, relationTarget.agent);

        });

        return relTargetList;
      },
      _createRelationObj : function(id, agentKeyList, relTargetListAll) {
        // TODO オブジェクトの種類によりクラスを当て分ける。

        // Agentオブジェクトを生成
        $.each(agentKeyList, function(index, agentKey) {
          if ($("#agent_" + agentKey).length === 0) {
            $("#" + id).append(
                '<div class="map-elm" id="agent_' + agentKey + '"><span>'
                    + agentKey + '</span><div class="app"></div></div>');
          }

          var relTargetList = relTargetListAll[agentKey];

          if (!relTargetList) {
            return true;
          }

          // DBオブジェクトを生成
          if (relTargetList.db) {
            $.each(relTargetList.db, function(index, dbName) {
              if ($("#db_" + dbName).length === 0) {
                $("#" + id).append(
                    '<div class="map-elm" id="db_' + dbName + '"><span>'
                        + dbName
                        + '</span><div class="data-resource"></div></div>');
              }
            });
          }

          // クラスタ構成のAgent名は末尾に連番が付与されているため、その分生成する。
          if (relTargetList.agent) {
            $.each(relTargetList.agent, function(index, agentName) {
              $.each(agentKeyList, function(index, agentKey) {
                if ($("#agent_" + agentKey).length === 0) {
                  if (agentKey === agentName) {
                    $("#" + id).append(
                        '<div class="map-elm" id="agent_' + agentKey
                            + '"><span>' + agentKey
                            + '</span><div class="app"></div></div>');
                  } else if (agentKey.startsWith(agentName)
                      && agentKey.match(/_\d\d\d$/)) {
                    $("#" + id).append(
                        '<div class="map-elm" id="agent_' + agentKey
                            + '"><span>' + agentKey
                            + '</span><div class="app"></div></div>');
                  }
                }
              });
            });
          }

        });
      },
      _getRelation : function(relTargetListAll) {

        var connection = [];
        $.each(relTargetListAll, function(agentKey, relTargetList) {
          // 存在しないエージェントは対象外とする。
          if ($("#agent_" + agentKey).length === 0) {
            return true;
          }

          // DBの関連を指定。
          $.each(relTargetList.db, function(index, dbName) {
            connection.push([ "agent_" + agentKey, "db_" + dbName ]);
          });

          // Agentの関連を指定。（自身に対する関連は指定しない）
          $.each(relTargetList.agent, function(index, agentName) {
            $.each($("[id^=agent_" + agentName + "]"),
                function(index, agentObj) {
                  if ("agent_" + agentKey !== agentObj.id) {
                    connection
                        .push([ "agent_" + agentKey, "agent_" + agentName ]);
                  }
                });
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
              cost : 0
            });
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
        var instance = this;
        var targetCost = 0;
        var marginTop = this.OBJ_MARGIN;
        var marginLeft = this.OBJ_MARGIN;
        $.each(costList, function(index, target) {
          if (index === 0) {
            targetCost = target.cost;
          } else if (target.cost < targetCost) {
            targetCost = target.cost;
            marginLeft += instance.OBJ_MARGIN_LEFT;
            marginTop = instance.OBJ_MARGIN;
          } else {
            marginTop += instance.OBJ_MARGIN_TOP;
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
          url : ENS.tree.GET_TOP_NODES
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
        for ( var i in topNodes) {
          var cluster = topNodes[i];

          // クラスタ名がない場合は追加しない。
          if (!cluster.data) {
            continue;
          }

          var $option = $("<option/>");
          $option.attr("value", cluster.data);
          $option.html(cluster.data);
          $selector.append($option);
          if (i === 0) {
            $selector.val(cluster.data);
          }
        }

        var instance = this;
        $selector.change(function() {
          // SystemMapの読み込み。
          // instance.websocketClient = instance._connectSocket();
          instance.isOpenSocket = true;
          instance._onLoadSystemMap();
        });
      },
      _getAgentList : function(clusterName) {
        // Ajax通信用の設定
        var settings = {
          url : ENS.tree.GET_AGENT_NAME_LIST,
          data : {
            parentTreeId : "/" + clusterName
          }
        };

        // 非同期通信でデータを送信する
        settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
        var result = this.ajaxHandler.requestServerSync(settings);

        return $.parseJSON(result);
      }
    });