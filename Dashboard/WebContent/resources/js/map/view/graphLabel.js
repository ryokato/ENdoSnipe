ENS.graphLabel = {};

// グラフラベルの横幅
ENS.graphLabel.LABEL_WIDTH = 720;
// カーソルとグラフラベルのトップオフセット
ENS.graphLabel.LABEL_TOP_OFFSET = 5;
// カーソルとグラフラベルのサイドオフセット
ENS.graphLabel.LABEL_LEFT_OFFSET = -60;
// 最大系列表示文字数
ENS.graphLabel.MAX_LABEL_LENGTH = 200;
// グラフのフルパスのタイトル
ENS.graphLabel.fullPathTitle = "";

// IDを指定してグラフラベルのDIV要素を作成する
ENS.graphLabel.create = function(id) {
	var $label = $("<div/>");
	$label.attr("id", id);
	ENS.graphLabel._setAttr($label);
	return $label;
};

// 引数で与えたDIV要素をグラフラベル化する
ENS.graphLabel.modify = function($div) {
	ENS.graphLabel._setAttr($div);
};

ENS.graphLabel._setAttr = function($label) {
	$label.addClass("ensLabel");
	$label.css("width", ENS.graphLabel.LABEL_WIDTH + "px");
	$label.hide();
};

// マウスイベントのリスナを設定する
ENS.graphLabel.setEventListener = function($labelDiv, $graphDiv) {
	$graphDiv
			.mousemove(function(e) {
				var $contents = $("#contents_area");
				var cLeft = $contents.offset().left;
				var cTop = $contents.offset().top;
				var cWidth = $contents.width();
				var cHeight = $contents.height();
				var cScrollX = $contents.scrollLeft();
				var cScrollY = $contents.scrollTop();
				var graphPos = $(this).position();
				var labelPos = {};
				labelPos.top = e.pageY - cTop + ENS.graphLabel.LABEL_TOP_OFFSET
						+ cScrollY;
				labelPos.left = e.pageX - cLeft
						+ ENS.graphLabel.LABEL_LEFT_OFFSET + cScrollX;

				$labelDiv.show();

				// マップ画面の場合は位置を修正する
				if (graphPos.left < 1 && graphPos.top < 1) {
					labelPos.top = e.pageY - $(this).offset().top
							+ ENS.graphLabel.LABEL_TOP_OFFSET;
					labelPos.left = e.pageX - $(this).offset().left
							+ ENS.graphLabel.LABEL_LEFT_OFFSET;
				}

				// ラベルが画面右端に到達している場合、ラベルをカーソルの左側に移動させる
				if (cLeft + cWidth - ENS.graphLabel.LABEL_WIDTH - ENS.graphLabel.LABEL_LEFT_OFFSET < e.pageX) {
					labelPos.left = labelPos.left
							- (ENS.graphLabel.LABEL_LEFT_OFFSET * 2 + ENS.graphLabel.LABEL_WIDTH);
				}

				var labelHeight = $labelDiv.height();
				// ラベルが画面下端に到達している場合、ラベルをカーソルの左側に移動させる
				if (cTop + cHeight - labelHeight < e.pageY) {
					labelPos.top = labelPos.top
							- (ENS.graphLabel.LABEL_TOP_OFFSET * 3 + labelHeight);
				}

				// ラベルの位置を設定する
				$labelDiv.css("left", labelPos.left);
				$labelDiv.css("top", labelPos.top);

				// マウスカーソルがデータの存在しない場所にあるとき、
				// または固定値のデータ(1970/01/01 9:00)であるときは項目名をラベルに表示
				var labelDivHtml = $labelDiv.html();
				var compareLabelDivHtml = " " + labelDivHtml;
				var defaultDate = "1970/01/01 09:00";
				if (labelDivHtml.length <= 0
						|| compareLabelDivHtml.indexOf(" " + defaultDate) !== -1) {
					$labelDiv.html(ENS.graphLabel.fullPathTitle);
				}

				// 改行を追加する
				var text = $labelDiv.html();
				text = text.replace(/<b>/g, "<br/><b>");
				$labelDiv.html(text);
			});
	$graphDiv.mouseout(function(e) {
		$labelDiv.hide();
	});
	$graphDiv.focusout(function(e) {
		$labelDiv.hide();
	});
};