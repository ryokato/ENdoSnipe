ENS.ResourceLinkElementModel = wgp.MapElement.extend({
	defaults : {
		text : null,
		fontSize : 8,
		textAnchor : "start",
		linkUrl : "",
		linkType : "basicURL"
	}
});

ENS.ResourceLinkElementView = ENS.ShapeElementView.extend({
	render : function(model){

		// 継承元のrenderメソッド実行
		wgp.MapElementView.prototype.render.apply(this, [ model ]);

		var shapeName = "textField";
		var elementProperty = model.attributes;
		var elementAttrList_ = model.get("elementAttrList");

		// テキストを描画する。
		this.elementList_ = [];
		var element =
			this.dashboardView.dashboardManager.createElementText(
				shapeName,
				elementProperty);
		element.object.node.setAttribute("cid", model.cid);
		this.addElement(element);
		this.setAttributes(elementAttrList_);
	},
	/**
	 * 運用時のイベントを設定する。
	 */
	setOperateFunction : function() {

		var instance = this;
		var element = this.getElement(0);
		element.object.dblclick(
		// ダブルクリック時の画面遷移処理
		function(event) {

			var linkUrl = instance.model.get("linkUrl");
			var linkType = instance.model.get("linkType");

			// ダッシュボードリンクの場合はリンク先が存在する場合に遷移を行う。
			if (linkType == "dashboardLinkURL") {
				var targetDashboardLink = resourceDashboardListView.collection.get(linkUrl);

				// ツリー要素をクリックしてイベントを発生させる。
				if (targetDashboardLink) {
					resourceDashboardListView.showModel(targetDashboardLink);
				}
			}
		});
	},
	getWidth : function(){
		return this.getElement(0).object.getBBox()["width"];
	},
	getHeight : function(){
		return this.getElement(0).object.getBBox()["height"];
	},
	isResizable : function(){
		return false;
	},
	setAttributes : function(elementAttributeList){

		// 継承元のrenderメソッド実行
		ENS.ShapeElementView.prototype.setAttributes.apply(this, [ elementAttributeList ]);

		// フレームをリセット
		this.getElement(0).resetFrame();
	}
});