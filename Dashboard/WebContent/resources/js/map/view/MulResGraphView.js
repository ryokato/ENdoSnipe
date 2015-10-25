/*******************************************************************************
 * ENdoSnipe 5.0 - (https://github.com/endosnipe)
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2012 Acroquest Technology Co.,Ltd.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

ENS.MultipleResourceGraphElementView = ENS.ResourceGraphElementView
		.extend({
			initialize : function(argument, treeSettings) {
				this.isFirstRender = true;
				this.isRealTime = true;
				this.isFirstCreate = true;
				this.count = 0;
				this._initData(argument, treeSettings);
				// %グラフのY軸の最大値
				this.percentGraphMaxYValue = argument.percentGraphMaxYValue;
				// グラフの上限を決める際の、グラフのY値の最大に対する係数
				this.yValueMagnification = argument.yValueMagnification;
				// 最大化グラフの横のマージン
				this.maxGraphSideMargin = argument.maxGraphSideMargin;
				// 最大化グラフの縦のマージン
				this.maxGraphVerticalMargin = argument.maxGraphVerticalMargin;
				// グラフタイトル横のボタン用スペースの大きさ
				this.titleButtonSpace = argument.titleButtonSpace;
				// グラフラベルの横幅
				this.labelWidth = 200;
				// グラフラベルのオフセット
				this.labelOffset = 50;
				// 前回表示したKey
				this.previousKeys = [];
				// 前回表示したラベル名
				this.labelNames = [];
				// クラスタ名
				this.CLUSTER_NAME = '${ClusterName}';
				
				var appView = new ENS.AppView();

				// measurementItemNameのキー文字列を生成する。
				this.graphIds = this.createGraphIds(argument.graphId);

				appView.addView(this, this.graphIds);

				if (!this.noTermData) {

					appView.getTermData([ this.graphIds ], this.timeStart,
							this.timeEnd);

				}

				var realTag = $("#" + this.$el.attr("id"));
				if (this.width == null) {
					this.width = realTag.width();
				} else {
					realTag.width(this.width);
				}
				if (this.height == null) {
					this.height = realTag.height();
				} else {
					realTag.height(this.height);
				}

				$("#" + this.$el.attr("id")).attr("class", "graphbox");
				$("#" + this.$el.attr("id")).css({
					margin : "10px",
					float : "left"
				});
				this.windowResize();
				this.addDragEvent();
			},
			// 本グラフが表示対象とするmeasurementItemNameのパターンを取得する。
			createGraphIds : function(graphId){

				// グラフIDからグラフの定義情報を取得する。
				var multipleGraphDefinition = this.getMultipleGraphDefinition(graphId);
				var measurementItemIds = multipleGraphDefinition["measurementItemIdList"].split(",");
				var measurementItemPattern = multipleGraphDefinition["measurementItemPattern"];

				// ClusterNameが含まれている場合は置換をおこなう
				var $cluster = $("#cluster_name");
				var val = $cluster.val();
				if(val === "*"){
					val = ".*";
				}
				measurementItemPattern = measurementItemPattern.replace(this.CLUSTER_NAME, val);
				
				// 系列の指定方法が正規表現によるマッチングがidによる直接指定かによって
				// 系列のキー文字列作成元となる系列のリストの作成方法を変更する。

				// 正規表現によるマッチングの場合
				// 正規表現をそのまま使用する。
				if(measurementItemPattern.length > 0){
					return measurementItemPattern;

				// idによる直接指定の場合
				// 取得した系列のリストをそのまま使用する。
				}else{
					var measurementItemNameList = measurementItemIds;

					// measurementItemNameのリストから以下のようなパイプ区切りの正規表現を生成する。
					// "(
					// measurementItemName1|measurementItemName2|...|measurementItemNameN
					// )"
					var graphIds = "(";
					for ( var index = 0; index < measurementItemNameList.length; index++) {
						if (index == measurementItemNameList.length - 1) {

							// measurementItemName中の括弧などは正規表現として解釈されてしまうため、
							// サーバ上では正規表現として扱われないように処理する。
							graphIds = graphIds
									+ "\\\Q"
									+ measurementItemNameList[index]
									+ "\\\E)";
						} else {
							graphIds =　graphIds
									+ "\\\Q"
									+ measurementItemNameList[index]
									+ "\\\E|";
						}
					}
					return graphIds;
				}
			},
			// 指定した複数グラフの定義情報を取得する。
			getMultipleGraphDefinition : function(parentId){
				var settings = {
					url : ENS.tree.MULTIPLE_RESOURCE_GRAPH_GET_URL,
					data : {
						multipleResourceGraphName : parentId
					}
				};

				var ajaxHandler = new wgp.AjaxHandler();
				var result = ajaxHandler.requestServerSync(settings);
				return JSON.parse(result);
			},
			keysByValue : function(data) {
				var arrayData = [];
				for ( var key in data) {
					if (data.hasOwnProperty(key)) {
						arrayData.push([ key, data[key] ]);
					}
				}

				arrayData.sort(function(data1, data2) {
					var value1 = data1[1], value2 = data2[1];
					return value2.length - value1.length;
				});
				for ( var index = 0, length = arrayData.length; index < length; index++) {
					arrayData[index] = arrayData[index][0];
				}

				return arrayData;
			},
			render : function() {
				var instance = this;
				var graphPath = this.graphId;
				var graphId = this.$el.attr("id") + "_ensgraph";
				var graphdiv = $("<div id='" + graphId + "'><div>");
				$("#" + this.$el.attr("id")).append(graphdiv);

				var labelId = this.$el.attr("id") + "_enslabel";

				var labeldiv = ENS.graphLabel.create(labelId);
				$("#" + this.$el.attr("id")).append(labeldiv);
				var labelDom = document.getElementById(labelId);

				var data = this.getData();
				var dataFinal = this.createDataList(data);
				
				var displayLabelNames = [];
				_.each(this.labelNames, function(index, data){
					if (index.length <= ENS.graphLabel.MAX_LABEL_LENGTH) {
						displayLabelNames.push(index);
					} else {
						displayLabelNames.push(index.slice(0, ENS.graphLabel.MAX_LABEL_LENGTH) + "...");
					}
				});
				
				var optionSettings = {
					// labels : this.datalabel,
					labels : displayLabelNames,
					valueRange : [ 0, this.maxValue * this.yValueMagnification ],
					title : this.title,
					xlabel : this.labelX,
					ylabel : this.labelY,
					axisLabelColor : "#000000",
					labelsDivStyles : {
						"background" : "none repeat scroll 0 0 #000000",
						"position" : "relative",
						"background-color" : "black",
						"word-break" : "break-word"
					},
					dateWindow : this.dateWindow,
					axisLabelFontSize : 10,
					titleHeight : 22,
					connectSeparatedPoints : true
				};

				this.attributes = undefined;
				var attributes = this.getAttributes(ENS.ResourceGraphAttribute);

				optionSettings = $.extend(true, optionSettings, attributes);
				optionSettings.labelsDiv = labelDom;

				optionSettings.title = optionSettings.title.split("&#47;")
						.join("/");
				var tmpTitle = optionSettings.title;
				var isShort = false;
				if (optionSettings.title.length > ENS.nodeinfo.GRAPH_TITLE_LENGTH) {
					optionSettings.title = optionSettings.title.substring(0,
							ENS.nodeinfo.GRAPH_TITLE_LENGTH)
							+ "......";
					isShort = true;
				}

				if (this.labelY == "%") {
					optionSettings.valueRange = [ 0, this.percentGraphMaxYValue ];
				} else {
					optionSettings.valueRange = [
													0,
													this.maxValue
															* this.yValueMagnification ];
				}
				if ($("#" + this.maximumButton).length > 0) {
					$("#" + this.maximumButton).remove();
				}

				
				optionSettings.dateWindow = this.dateWindow;
				optionSettings.axisLabelFontSize = 10;
				optionSettings.titleHeight = 22;
				
				var element = document.getElementById(graphId);
				optionSettings.width = this.width;
				optionSettings.height = this.graphHeight;
				this.entity = new Dygraph(element, dataFinal, optionSettings);
				
				$("#" + graphId).height(this.height);

				if (this.viewMaximumButtonFlag) {
					var childElem = $("#" + graphId + "").children("div");
					$(childElem).append(this.maximumButtonImg);
					$("#" + this.maximumButton).css("float", "right");
					var minButton = this.normalButton, maxButton = this.maximumButton;
					$(element)
							.click(
									function(e) {
										var offsetLeft = $("#" + graphId)
												.offset().left;
										var offsetTop = $("#" + graphId)
												.offset().top;
										var position = {
											x : Math.floor(e.clientX
													- offsetLeft),
											y : Math.floor(e.clientY
													- offsetTop)
										};

										if (position.x > ($("#" + graphId)
												.width() - 20)
												&& position.x < ($("#"
														+ graphId).width())
												&& position.y > 0
												&& position.y < 28) {
											if ($("." + maxButton).length > 0) {
												instance.addMaximizeEvent(
														offsetLeft, offsetTop);
											}
										}
									});

					$(".dygraph-title").width(
							$("#" + graphId).width() - this.titleButtonSpace);
				}
				this.mouseEvent(graphId, isShort, tmpTitle, optionSettings);

				ENS.graphLabel.setEventListener(labeldiv, graphdiv);
			},

			onAdd : function(graphModel) {
				var data = this.getData();
				var len = Object.keys(data).length;

				if (this.isRealTime) {

					if (this.collection.length > this.graphMaxNumber * len) {
						for ( var index = 0; index < len; index++) {
							var removeIndex = (this.graphMaxNumber * index)
									- (this.count * index) - index;
							if (removeIndex < 0) {
								removeIndex = 0;
							}
							this.collection.remove(this.collection
									.at(removeIndex));
						}

						this.count++;
					}
					data = this.getData();
					var dataList = this.createDataList(data);
					var tempStart;
					var tempEnd;
					var time = new Date();
					if (this.timeFrom === 0) {
						tempEnd = time;
						tempStart = new Date(new Date().getTime() - this.term
								* 1000);
					} else {
						tempEnd = time;
						tempStart = new Date(time.getTime() - this.timeFrom);
					}

					var updateOption;
					if (this.labelY == "%") {
						updateOption = {
							valueRange : [ 0, this.percentGraphMaxYValue ],
							'file' : dataList
						};
					} else {
						updateOption = {
							'file' : dataList,
							'valueRange' : [ 0,
									this.maxValue * this.yValueMagnification ]
						};
					}

					if (dataList.length !== 0) {
						updateOption['dateWindow'] = [ tempStart, tempEnd ];
					}
					
					// ラベル名を再設定する。
					var displayLabelNames = [];
					_.each(this.labelNames, function(index, data){
						if (index.length <= ENS.graphLabel.MAX_LABEL_LENGTH) {
							displayLabelNames.push(index);
						} else {
							displayLabelNames.push(index.slice(0, ENS.graphLabel.MAX_LABEL_LENGTH) + "...");
						}
					});
					updateOption['labels'] = displayLabelNames;
					
					this.entity.updateOptions(updateOption);

					var graphId = this.$el.attr("id") + "_ensgraph";

					if ($("#tempDiv").length > 0) {
						if (this.tempGraphId !== undefined) {
							this.tempEntity.updateOptions(updateOption);
						}

					}

					if (this.viewMaximumButtonFlag) {
						$(".dygraph-title").width(
								$("#tempDiv").width() - this.titleButtonSpace);
					}
				}
			},
			_getTermData : function() {

				var data = this.getData();
				var dataList = this.createDataList(data);
				if (dataList.length !== 0) {
					this.maxValue = this.getMaxValue(dataList);
				}else{
					// データが存在しない場合は以降の更新処理を行わない。
					return;
				}
				var updateOption;
				if (this.labelY == "%") {
					updateOption = {
						valueRange : [ 0, this.percentGraphMaxYValue ],
						'file' : dataList,
						labels: this.labelNames
					};
				} else {
					updateOption = {
						valueRange : [ 0,
								this.maxValue * this.yValueMagnification ],
						'file' : dataList,
						labels: this.labelNames
					};
				}

				this.entity.updateOptions(updateOption);
				if ($("#tempDiv").length > 0) {
					if (this.tempGraphId !== undefined) {
						this.tempEntity.updateOptions(updateOption);
					}
				}
				
				if(this.isRealTime){
					var tmpAppView = new ENS.AppView();
					tmpAppView.syncData([ this.graphIds ]);
				}
			},
			onComplete : function(syncType) {

				if (this.isFirstRender === true) {
					this.render();
					this.isFirstRender = false;
					
					if(this.isRealTime){
						var tmpAppView = new ENS.AppView();
						tmpAppView.syncData([ this.graphIds ]);
					}
				} else if (syncType == wgp.constants.syncType.SEARCH) {
					this._getTermData();
				}
				var graphId = this.$el.attr("id") + "_ensgraph";

				if ($("#tempDiv").length > 0) {

					if (this.viewMaximumButtonFlag) {
						$(".dygraph-title").width(
								$("#tempDiv").width() - this.titleButtonSpace);
					}

				} else {

					if (this.viewMaximumButtonFlag) {
						$(".dygraph-title").width(
								$("#" + graphId).width()
										- this.titleButtonSpace);
					}
				}
			},
			getMaxValue : function(dataList) {
				var maxValue = 0;

				_.each(dataList, function(data, index) {
					for ( var i = 1; i < data.length; i++) {
						var value = data[i];

						if (value) {
							if (value > maxValue) {
								maxValue = value;
							}
						}
					}
				});

				if (maxValue === 0) {
					maxValue = 1;
				}

				return maxValue;
			},
			updateGraphData : function(graphId, from, to) {
				if (to === 0) {
					this._startRealtime();
				} else {
					this._stopRealtime();
				}

				var startTime = new Date(new Date().getTime() - from);
				var endTime = new Date(new Date().getTime() - to);
				this.timeStart = startTime;
				this.timeEnd = endTime;
				this.timeFrom = from;
				var graphIds = this.createGraphIds(graphId);
				appView.getTermData([ graphIds ], startTime, endTime);
			},
			getData : function() {

				var measurementListMap = {};
				var dataMap = [];
				var data = {};
				var instance = this;
				var measurementItemName;
				var measurmentListm = {};

				_.each(this.collection.models, function(model, index) {
					measurementItemName = model
							.get("measurementItemName");
					if(data[measurementItemName] === undefined ){
						data[measurementItemName] = [];
						data[measurementItemName].push([new Date(0), null]);
					}
					data[measurementItemName].push(instance
							._parseModel(model));
					measurementListMap[measurementItemName] = data[measurementItemName];
				});

				// データが1件もない場合
				if(_.keys(data).length === 0){
					data['DEFAULT_NO_DATA'] = [];
					data['DEFAULT_NO_DATA'].push([new Date(0)]);
				}

				_.each(data, function(dataArray, index){
					measurementListMap[index] = dataArray;
				});
				return measurementListMap;
			},
			addMaximizeEvent : function(offsetLeft, offsetTop) {
				var instance = this;

				var divArea = $("#" + this.$el.attr("id"));
				var tempDiv = this.$el.attr("id").substring(0, 29);
				this.tempGraphId = this.$el.attr("id");

				$("#" + tempDiv)
						.append(
								"<div id='tempDiv' class='graphbox' style='margin: 10px;'></div>");
				$("#tempDiv").append("<div id='tempDiv_ensgraph'></div>");
				$("#tempDiv").append(
						"<div id='tempDiv_enslabel'class='ensLabel'></div>");
				var data = this.getData();
				var dataFinal = this.createDataList(data);
				var optionSettings = {
					labels : this.labelNames,
					title : this.title,
					xlabel : this.labelX,
					ylabel : this.labelY,
					axisLabelColor : "#000000",
					labelsDivStyles : {
						background : "none repeat scroll 0 0 #000000"
					}
				};

				var attributes = this.getAttributes(ENS.ResourceGraphAttribute);
				var labelDom = document.getElementById("tempDiv_enslabel");
				ENS.graphLabel.modify($(labelDom));

				optionSettings = $.extend(true, optionSettings, attributes);
				optionSettings.labelsDiv = labelDom;

				optionSettings.title = optionSettings.title.split("&#47;")
						.join("/");

				var tmpTitle = optionSettings.title;
				var isShort = false;
				if (optionSettings.title.length > ENS.nodeinfo.GRAPH_TITLE_LENGTH) {
					optionSettings.title = optionSettings.title.substring(0,
							ENS.nodeinfo.GRAPH_TITLE_LENGTH)
							+ "......";
					isShort = true;
				}

				this.tempEntity = new Dygraph(document
						.getElementById("tempDiv_ensgraph"), dataFinal,
						optionSettings);

				this.tempEntity.updateOptions({
					dateWindow : this.dateWindow,
					axisLabelFontSize : 10,
					titleHeight : 22
				});

				if (this.fromScale !== undefined && this.toScale !== undefined) {
					this.tempEntity.updateOptions({
						dateWindow : [ this.fromScale, this.toScale ]
					});
				}

				if (this.labelY == "%") {
					this.tempEntity.updateOptions({
						valueRange : [ 0, this.percentGraphMaxYValue ]
					});
				} else {
					this.tempEntity.updateOptions({
						valueRange : [ 0,
								this.maxValue * this.yValueMagnification ]
					});
				}

				this.mouseEvent("tempDiv_ensgraph", isShort, tmpTitle,
						optionSettings);

				var graphId = this.$el.attr("id") + "_ensgraph";

				$("#" + graphId).hide();
				$(divArea).hide();

				var graphWidth = parseInt($("#contents_area").width()
						- this.maxGraphSideMargin, 10);
				var graphHeight = parseInt($("#contents_area").height()
						- this.maxGraphVerticalMargin, 10);

				var resizeStyle = [];

				resizeStyle["width"] = graphWidth;
				resizeStyle["height"] = graphHeight;

				$("#tempDiv").css(resizeStyle);
				this.addDragEvent();

				var divNo = parseInt(this.$el.attr("id").substring(30,
						this.$el.attr("id").length), 10);
				var divCut = this.$el.attr("id").substring(0, 30);

				var hiddenGraph = [];

				var start = divNo - (divNo % 12);
				var end = 12 - (divNo % 12);
				var j = divNo - start;

				hiddenGraph[j] = this.$el.attr("id");

				if (this.noOfGraph != 1) {

					if ((divNo + end) > this.noOfGraph) {
						end = this.noOfGraph - divNo;
					}

					var graphPoint = 0;
					for ( var cnt1 = start; cnt1 < divNo; cnt1++) {
						if (graphPoint == j) {
							graphPoint++;
						}
						hiddenGraph[graphPoint++] = divCut + cnt1;
						$("#" + divCut + cnt1).hide();
						$("#" + divCut + cnt1 + "_ensgraph").hide();
					}
					for ( var cnt2 = (divNo + 1); cnt2 < divNo + end; cnt2++) {
						if (graphPoint == j) {
							graphPoint++;
						}

						hiddenGraph[graphPoint++] = divCut + cnt2;
						$("#" + divCut + cnt2).hide();
						$("#" + divCut + cnt2 + "_ensgraph").hide();
					}
				}

				this.hiddenGraph = hiddenGraph;
				
				this.tempEntity.resize(graphWidth, graphHeight);

				$(".dygraph-title").width(
						$("#tempDiv").width() - this.titleButtonSpace);
				var childElem = $("#tempDiv_ensgraph").children("div");
				$(childElem).append(this.normalButtonImg);

				$("#" + this.normalButton).css("float", "right");
				var minButton = this.normalButton;

				$("#tempDiv")
						.click(
								function(e) {
									var offsetLeft = $("#tempDiv_ensgraph")
											.offset().left;
									var offsetTop = $("#tempDiv_ensgraph")
											.offset().top;
									var position = {
										x : Math.floor(e.clientX - offsetLeft),
										y : Math.floor(e.clientY - offsetTop)
									};
									if (position.x > ($("#tempDiv_ensgraph")
											.width() - 20)
											&& position.x < ($("#tempDiv_ensgraph")
													.width())
											&& position.y > 0
											&& position.y < 28) {
										if ($("." + minButton).length > 0) {
											instance.addNormalizeEvent();
										}
									}
								});
				// ズームアウト時（ダブルクリック）のイベントを設定。
				$("#tempDiv").dblclick(
						function(event) {
							instance.zoomOut(instance.tempEntity);
							$(".dygraph-title").width(
									$("#tempDiv").width()
											- instance.titleButtonSpace);
						});

				ENS.graphLabel.setEventListener($(labelDom), $("#tempDiv"));
			},
			getMaxValueFromSeriesData : function(seriesData){
				var max = seriesData[0][1];
				for(var i=0, len=seriesData.length; i<len; i++){
					if(seriesData[i][1] <= max){
						continue;
					}
					max = seriesData[i][1];
				}
				return max;
			},
			createDataList : function(getData) {
				var instance = this;
				var currentKeys = [];

				// データが表示可能最大系列数より多い場合は、
				// 各系列の中で最大値が大きい順に表示する。
				// 同条件の場合は辞書式とする。
				var getDataKeysTemp = [];
				_.each(getData, function(seriesData, seriesDataKey){

					// 系列が持つ直近の時刻およびデータを取得する。
					var seriesDataDate = new Date(0);
					var seriesDataValue = null;
					if(seriesData.length > 0){

						var maxValue = instance.getMaxValueFromSeriesData(seriesData);
						getDataKeysTemp.push({
							"seriesKey" : seriesDataKey,
							"maxValue" : maxValue
						});
					}

				});

				getDataKeysTemp.sort(function(before, after){

					// 最大値を比較する。
					if(before.maxValue > after.maxValue){
						return -1;
					}else if(after.maxValue > before.maxValue){
						return 1;
					}

					// 系列のキー文字列を比較する。
					if(before.seriesKey > after.seriesKey){
						return -1;

					}else if(after.seriesKey > before.seriesKey){
						return 1;

					}else{
						return 0;
					}
				});

				// ソート結果から表示可能系列数のみを抽出する。
				var defautlSeriesNumber = $("#" + ENS.mulResGraphView.series.numberId).val();
				getDataKeysTemp = getDataKeysTemp.slice(0, defautlSeriesNumber);
				var currentKeysTemp = _.pluck(getDataKeysTemp, "seriesKey");

				// previousKeysとcurrentKeysTempを比較して含まれていないキーを算出する。
				// キーが全て含まれている場合は、前回のソート結果を優先する。
				if(_.difference(currentKeysTemp, this.previousKeys).length === 0 &&
					currentKeysTemp.length === this.previousKeys.length){
					currentKeys = this.previousKeys;
				}else {
					currentKeys = currentKeysTemp;
					this.previousKeys = currentKeys;
				}

				this.labelNames = [];
				this.labelNames.push("Date");

				var dataMap = {};
				_.each(currentKeys, function(currentKey, currentKeysIndex){

					// ラベル名に系列値を設定。
					instance.labelNames.push(currentKey);

					var dataListOfEveryKey = getData[currentKey];
					_.each(dataListOfEveryKey, function(dataList, dataListIndex){

						// 時刻データを取得する。
						// 同一時刻データが未取得の場合は配列を生成する。
						var dateData = dataList[0];
						var valueData = dataList[1];

						var dataArray;
						if(_.has(dataMap, dateData)){
							dataArray = dataMap[dateData];

						}else{
							dataArray = new Array(currentKeys.length);

							// いずれかの系列にデータが存在しない場合を考慮し、
							// あらかじめnullで初期化しておく。
							var cnt;
							for (cnt = 0; cnt < currentKeys.length + 1; cnt++) {
								dataArray[cnt] = null;
							}
							dataMap[dateData] = dataArray;

						}
						if (currentKeysIndex !== 0 || valueData !== undefined) {
							dataArray[currentKeysIndex] = valueData;
						}
					});
				});

				var returnDataArray = [];

				// 時刻を「キー値」、各系列ごとの値の配列を「値」とする連想配列を、
				// 時刻ごとの[時刻, 系列の値1,系列の値2....]の2次元配列に変換する。
				for(var key in dataMap){
					var value = dataMap[key];
					var date = new Date(key);
					var data = [].concat(date, value);
					instance._insertArray(returnDataArray, data);
				}

				return returnDataArray;
			},
			_insertArray : function(array, data){
				var len = array.length;
				
				//配列が空か末尾のデータよりも挿入データが新しい場合
				if(len === 0 || array[len-1][0] < data[0]){
					array.push(data);
					return;
				}
				
				//時系列になるようにデータを挿入する
				for(var i=0; i<len; i++){
					var targetDate = array[i][0];
					if(targetDate < data[0]){
						continue;
					}
					array.splice(i, 0, data);
					return;
				}
			},
			_startRealtime : function(){
				this.isRealTime = true;
				var tmpAppView = new ENS.AppView();
				tmpAppView.syncData([ this.graphIds ]);
			},
			_stopRealtime : function(){
				this.isRealTime = false;
				var tmpAppView = new ENS.AppView();
				tmpAppView.stopSyncData([ this.graphIds ]);
			}
		});