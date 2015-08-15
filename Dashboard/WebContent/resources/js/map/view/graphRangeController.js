/**
 * グラフの時間帯調整UIクラス
 */
ENS.graphRangeController = function(id) {
	ENS.graphRange.ID_RANGE = "range_controller_range";
	ENS.graphRange.ID_DATEPICKER = "range_controller_datepicker";
	ENS.graphRange.ID_PREV_BUTTON = "range_controller_prev";
	ENS.graphRange.ID_PLAY_BUTTON = "range_controller_play";
	ENS.graphRange.ID_NEXT_BUTTON = "range_controller_next";
	ENS.graphRange.ID_SEARCH_BUTTON = "range_controller_search";
	ENS.graphRange.ID_SPAN = "range_controller_span";
	ENS.graphRange.ID_CLUSTER_NAME = "cluster_name";
	ENS.graphRange.ID_DASHBOARD_NAME = "dashboard_name";
	ENS.graphRange.TIMER_INTERVAL = 1000;

	this.searchListener = null;
	this.isPlaying = true;

	this._create(id);
};

/**
 * 検索ボタンが押された時のイベントリスナを設定する
 */
ENS.graphRangeController.prototype.setSearchListener = function(func) {
	this.searchListener = func;
};

/**
 * UIを作成する
 */
ENS.graphRangeController.prototype._create = function(id) {
	// 時間の範囲
	var $rangeLabel = $("<span/>").html("Range: ");
	var $range = this._createRange();

	// 日時,
	var $datepicker = this._createDatetimePicker();

	// 15分前ボタン
	var $prev = this._createPrevButton();

	// リアルタイム再生ボタン
	var $play = this._createPlayButton();

	// 15分後ボタン
	var $next = this._createNextButton();
	
	// クラスタ選択ドロップダウンリスト
	var $selectorLabel = $("<span/>").html("Cluster: ");
	$selectorLabel.css("margin-left", "10px");
	var $selector = this._createClusterSelector();
	
	// ダッシュボード選択アイコン
	var $dashboardLabel = $("<span/>").html("Dashboard: ");
	$dashboardLabel.css("margin-left", "10px");
	var $loadDashboard = this._createLoadDashboardIcon();
	
	// 編集モードへの切り替えアイコン
	var $editMode = this._createChangeModeIcon();

	// コンテナ
	var $container = $("#" + id);

	// スタイルの変更
	$container.css("font-size", "0.8em");
	$container.css("margin-bottom", "0px");
	$datepicker.css("margin-left", "7px");
	$prev.css("margin-left", "8px");
	$next.css("margin-left", "3px");
	$play.css("margin-left", "3px");
	// $span.css("margin-left", "16px");

	// コンテナに追加
	var $subcontainer = $("<div/>");
	$subcontainer.css("float", "left");
	$container.append($subcontainer);
	$subcontainer.append($rangeLabel);
	$subcontainer.append($range);
	$subcontainer.append($datepicker);
	$subcontainer.append($prev);
	$subcontainer.append($play);
	$subcontainer.append($next);
	if($loadDashboard !== null){
		$subcontainer.append($selectorLabel);
		$subcontainer.append($selector);
		$subcontainer.append($dashboardLabel);
		$subcontainer.append($loadDashboard);
		$container.append($editMode);
	}

	// 初期表示では現在時刻をセット
	var date = new Date();
	this.setDate(date);
	this._updateSpan(date);
	this._updateTooltip();
};

/**
 * ドロップダウンリストのオプションを生成する
 */
ENS.graphRangeController.prototype._createOption = function(value) {
	var $option = $("<option/>");
	$option.html(value + " h");
	$option.attr("val", value);
	return $option;
};

/**
 * 範囲時間のドロップダウンリストを生成する
 */
ENS.graphRangeController.prototype._createRange = function() {
	var $range = $("<select/>");
	$range.attr("id", ENS.graphRange.ID_RANGE);
	$range.append(this._createOption(0.5));
	$range.append(this._createOption(1).prop("selected", true));
	$range.append(this._createOption(2));
	$range.append(this._createOption(4));
	$range.append(this._createOption(8));
	$range.append(this._createOption(16));
	$range.append(this._createOption(24));

	var instance = this;
	$range.change(function() {
		var rangeMs = instance._getRangeMs();
		var time = (new Date()).getTime() - instance._getDate().getTime();
		instance._updateGraph(time + rangeMs, time);
		instance._updateTooltip();

		if (instance.isPlaying) {
			instance._play();
		}
	});
	return $range;
};

/**
 * 日付時刻選択UIを生成する
 */
ENS.graphRangeController.prototype._createDatetimePicker = function() {
	var $datepicker = $("<input/>");
	$datepicker.attr("id", ENS.graphRange.ID_DATEPICKER);
	$datepicker.attr("type", "text");
	$datepicker.attr("title", "To date");
	
	var instance = this;
	$datepicker.datetimepicker({
		dateFormat : "yy-mm-dd",
		timeFormat : "HH:mm",
		onClose : function() {
			if (instance.isPlaying) {
				instance._stop();
			}

			// テキストボックスの選択状態を解除する
			$datepicker.blur();
			
			// 日付が不正でないかチェックし、不正であれば、アラートを表示し、グラフの更新は行わない
			var range = instance._getFromToMs();
			if (range === null) {
				alert("Invalid date format.");
				return;
			}
			instance._updateGraph(range.from, range.to);
		}
	});

	return $datepicker;
};

ENS.graphRangeController.prototype._createClusterSelector = function(){
	var $selector = $("<select/>");
	$selector.attr("id", ENS.graphRange.ID_CLUSTER_NAME);
	
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
};

ENS.graphRangeController.prototype._callbackGetNames = function(names) {
	var $selector = $("#"+ENS.graphRange.ID_DASHBOARD_NAME);
	for(var i in names){
		var $option = $("<option/>");
		$option.html(names[i]);
		$selector.append($option);
	}
	
	var queries = ENS.Utility.getHashAndQuery();
	var hash = queries["hash"];
	if (hash.length > 0) {
		$selector.val(hash[0]);
	}
	
	var instance = this;
	$selector.change(function(){
		var view = window.resourceDashboardListView;
		var treeId = instance._getSelectedTreeId();
		var treeModel = view.collection.get(treeId);
		view.showModel(treeModel);
	});
};

ENS.graphRangeController.prototype._callbackGetTopNodes = function($selector, topNodes){
	var $option = $("<option/>");
	$option.attr("value", ".*");
	$option.html("ALL");
	$selector.append($option);
	
	for(var i in topNodes){
		var cluster = topNodes[i];
		$option = $("<option/>");
		$option.attr("value", cluster.data);
		$option.html(cluster.data);
		$selector.append($option);
	}
	
	var instance = this;
	$selector.change(function(){
		var view = window.resourceDashboardListView;
		var treeId = instance._getSelectedTreeId();
		var treeModel = view.collection.get(treeId);
		view.showModel(treeModel);
	});
};

/**
 * 現在選択されているTreeIdを取得する
 */
ENS.graphRangeController.prototype._getSelectedTreeId = function(){
	var val = $("#"+ENS.graphRange.ID_DASHBOARD_NAME).val();
	var view = window.resourceDashboardListView;
	for(var i=0, len=view.collection.models.length; i<len; i++){
		var model = view.collection.models[i];
		if(model.attributes.data === val){
			return model.attributes.treeId;
		}
	}
	return "1";
};

/**
 * 日付時刻選択UIにDate型で指定した時刻を表示させる
 */
ENS.graphRangeController.prototype.setDate = function(date) {
	var dateStr = this._getDateStr(date);
	var $datepicker = $("#" + ENS.graphRange.ID_DATEPICKER);
	$datepicker.val(dateStr);
};

/**
 * yyyy/mm/dd hh:mm 形式の文字列を取得する
 */
ENS.graphRangeController.prototype._getDateStr = function(date) {
	var year = date.getFullYear();
	var month = ('00' + (date.getMonth() + 1)).slice(-2);
	var day = ('00' + date.getDate()).slice(-2);
	var hour = ('00' + date.getHours()).slice(-2);
	var min = ('00' + date.getMinutes()).slice(-2);
	var result = year + "-" + month + "-" + day + " " + hour + ":" + min;
	return result;
};

/**
 * 15分前ボタンを生成する
 */
ENS.graphRangeController.prototype._createPrevButton = function() {
	var $prev = $("<button/>");
	$prev.attr("id", ENS.graphRange.ID_PREV_BUTTON);
	$prev.addClass("range_config");
	$prev.html("15 minutes ago");
	$prev.button({
		icons : {
			primary : "ui-icon-seek-prev"
		},
		text : false
	});
	var instance = this;
	$prev.click(function() {
		if (instance.isPlaying) {
			instance._stop();
		}
		instance._shiftTime(-instance._getRangeMs() * 0.5);
	});
	return $prev;
};

/**
 * 再生ボタンを生成する
 */
ENS.graphRangeController.prototype._createPlayButton = function() {
	var $play = $("<button/>");
	$play.attr("id", ENS.graphRange.ID_PLAY_BUTTON);
	$play.addClass("range_config");
	$play.html("Current time");
	var instance = this;
	$play.button({
		icons : {
			primary : "ui-icon-stop"
		},
		text : false
	});
	$play.click(function(event) {
		if (instance.isPlaying) {
			instance._stop();
			var range = instance._getFromToMs();
			instance._updateGraph(range.from, range.to);
		} else {
			instance._play();
		}

	});
	return $play;
};

/**
 * 15分後ボタンを生成する
 */
ENS.graphRangeController.prototype._createNextButton = function() {
	var $next = $("<button/>");
	$next.attr("id", ENS.graphRange.ID_NEXT_BUTTON);
	$next.addClass("range_config");
	$next.html("15 minutes after");
	$next.button({
		icons : {
			secondary : "ui-icon-seek-next"
		},
		text : false
	});
	var instance = this;
	$next.click(function() {
		if (instance.isPlaying) {
			instance._stop();
		}
		instance._shiftTime(instance._getRangeMs() * 0.5);
	});
	return $next;
};

/**
 * Dashboard変更アイコンを生成する
 */
ENS.graphRangeController.prototype._createLoadDashboardIcon = function() {
	if(href.match(/dashboardList\?*.*$/) == null){
		return null;
	}
	var $select = $("<select/>");
	$select.attr("id", ENS.graphRange.ID_DASHBOARD_NAME);
	var settings = {
			url : wgp.common.getContextPath() + "/dashboard/getNames"
	}
	
	// 非同期通信でデータを送信する
	var ajaxHandler = new wgp.AjaxHandler();
	settings[wgp.ConnectionConstants.SUCCESS_CALL_OBJECT_KEY] = this;
	settings[wgp.ConnectionConstants.SUCCESS_CALL_FUNCTION_KEY] = "_callbackGetNames";
	ajaxHandler.requestServerAsync(settings); 
	
	return $select;
};

/**
 * 編集モードへの切り替えアイコンを生成する
 */
ENS.graphRangeController.prototype._createChangeModeIcon = function(){
	var contextPath = wgp.common.getContextPath();
	var $container = $("<div/>");
	var $editMode = $("<img/>");
	$editMode.css("width", "16px");
	$editMode.css("margin", "2px 20px 0px 30px");
	
	
	if($("#dashboardMode").val() == ENS.dashboard.mode.OPERATE){
		$editMode.attr("title", "Edit Mode");
		$editMode.attr("src", contextPath+"/resources/images/map/editModeIcon.png");
		$editMode.click(function(){
			var $selector = $("#"+ENS.graphRange.ID_DASHBOARD_NAME);
			var dashboardName = $selector.val();
			$("#dashboardMode").val(ENS.dashboard.mode.EDIT);
			$("#dashboardListForm").attr("action", "/ENdoSnipe/dashboard/dashboardList#" + dashboardName);
			saveDisplayState();
			$("#dashboardListForm").submit(); 
		});
		$editMode.css("cursor", "pointer");
	}else{
		$editMode.attr("title", "Operate Mode");
		$editMode.attr("src", contextPath+"/resources/images/map/operateModeIcon.png");
		$editMode.click(function(){
			// 編集モードの場合、操作した内容を保存するか確認する。
			window.resourceDashboardListView.saveOperation();
			
			var $selector = $("#"+ENS.graphRange.ID_DASHBOARD_NAME);
			var dashboardName = $selector.val();
			$("#dashboardMode").val(ENS.dashboard.mode.OPERATE);
			$("#dashboardListForm").attr("action", "/ENdoSnipe/dashboard/dashboardList#" + dashboardName);
			saveDisplayState();
			$("#dashboardListForm").submit(); 
		});
		$editMode.css("cursor", "pointer");
	}
	$container.append($editMode);
	return $container;
};


/**
 * 設定に合わせて期間表示を更新する
 */
ENS.graphRangeController.prototype._updateSpan = function(date) {
	var to = date;
	if (to === undefined) {
		to = this._getDate();
	}
	var rangeMs = this._getRangeMs();
	if (to === null) {
		return;
	}
	var from = new Date(to.getTime() - rangeMs);
	var str = "Term: ";
	str += this._getDateStr(from) + " 〜 " + this._getDateStr(to);
	if (this.isPlaying) {
		str += "[Realtime]";
	}
	$("#" + ENS.graphRange.ID_SPAN).html(str);
};

/**
 * リアルタイム表示を開始する
 */
ENS.graphRangeController.prototype._play = function() {
	$("#" + ENS.graphRange.ID_PLAY_BUTTON).button({
		icons : {
			primary : "ui-icon-stop"
		}
	});
	this.isPlaying = true;
	var rangeMs = this._getRangeMs();
	this._updateGraph(rangeMs, 0);
	var instance = this;
	var date = new Date();
	this.setDate(date);
};

/**
 * リアルタイム表示を終了する
 */
ENS.graphRangeController.prototype._stop = function() {
	$("#" + ENS.graphRange.ID_PLAY_BUTTON).button({
		icons : {
			primary : "ui-icon-play"
		}
	});
	this.isPlaying = false;
};

/**
 * 日付時刻指定UIからDateを取得する
 */
ENS.graphRangeController.prototype._getDate = function() {
	// Date型に変換可能なフォーマットに変換する
	var dateStr = $("#" + ENS.graphRange.ID_DATEPICKER).val().replace(/-/g, "/");
	var date = new Date(dateStr);
	// フォーマット異常の場合はアラートを表示する
	if (isNaN(date.getTime())) {
		return null;
	}
	return date;
};

/**
 * ドロップダウンリストの値を元に範囲時間のミリ秒を取得する
 */
ENS.graphRangeController.prototype._getRangeMs = function() {
	var rangeMs = this._getRangeHour() * 60 * 60 * 1000;
	return rangeMs;
};

/**
 * ドロップダウンリストの値を元に範囲時間の時間を取得する
 */
ENS.graphRangeController.prototype._getRangeHour = function() {
	var rangeStr = $("#" + ENS.graphRange.ID_RANGE).val();
	var range = rangeStr.replace(" h", "");
	return range;
};

/**
 * 検索ボタンが押下された時の処理
 */
ENS.graphRangeController.prototype._search = function() {
	var range = this._getFromToMs();
	if (range === null) {
		alert("Invalid date format.");
		return;
	}
	this._updateGraph(range.from, range.to);
};

/**
 * 時間帯をシフトする
 */
ENS.graphRangeController.prototype._shiftTime = function(time) {
	var date = this._getDate();
	if (date === null) {
		return;
	}
	date.setTime(date.getTime() + time);
	this.setDate(date);

	var range = this._getFromToMs();
	if (range === null) {
		return;
	}
	this._updateGraph(range.from, range.to);
};

/**
 * 表示対象の時刻を取得する。フォーマット異常の場合nullを返す。
 */
ENS.graphRangeController.prototype._getFromToMs = function() {
	var date = this._getDate();
	if (date === null) {
		return null;
	}
	var rangeMs = this._getRangeMs();

	// 検索対象のミリ秒を求める
	var currentDate = new Date();
	var now = currentDate.getTime();
	var to = now - date.getTime();
	var from = now - date.getTime() + rangeMs;

	return {
		from : from,
		to : to
	};
};

/**
 * グラフ描画を更新する
 */
ENS.graphRangeController.prototype._updateGraph = function(from, to) {
	this.searchListener(from, to);
	var date = new Date();
	date.setTime(date.getTime() - to);
	this._updateSpan(date);
	this._updateTooltip();
};

/**
 * ツールチップをアップデートする
 */
ENS.graphRangeController.prototype._updateTooltip = function() {
	$("#" + ENS.graphRange.ID_PREV_BUTTON).attr("title",
			(this._getRangeHour() / 2) + " hours ago");
	$("#" + ENS.graphRange.ID_NEXT_BUTTON).attr("title",
			(this._getRangeHour() / 2) + " hours after");
	if (!this.isPlaying) {
		$("#" + ENS.graphRange.ID_PLAY_BUTTON).attr("title", "Start realtime tracking.");
	} else {
		$("#" + ENS.graphRange.ID_PLAY_BUTTON).attr("title", "Stop realtime tracking.");
	}
};
