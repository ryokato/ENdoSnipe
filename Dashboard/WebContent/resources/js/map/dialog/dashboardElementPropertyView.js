ENS.DashboardElemenPropertyModel = Backbone.Model.extend({
    defaults:{
    	objectId : null,
        pointX : null,
        pointY : null,
        elementAttrList : null
    },
    idAttribute:"objectId",
});

ENS.dashboardElementPropertyTab = {};
ENS.dashboardElementPropertyTab["ENS.ShapeElementView"] =  ["Shape"];
ENS.dashboardElementPropertyTab["ENS.SignalElementView"] = [null, "Text"];
ENS.dashboardElementPropertyTab["ENS.TextBoxElementView"] = ["Shape", "Textbox"];
ENS.dashboardElementPropertyTab["ENS.ResourceLinkElementView"] = ["Text"];
ENS.dashboardElementPropertyTab["ENS.BackgroundElementView"] = ["Background"];
ENS.dashboardElementPropertyTab["ENS.ResourceGraphElementView"] = [];

/**
 * ダッシュボード要素のプロパティを表示する。
 */
ENS.DashboardElementPropertyView = Backbone.View.extend({
	initialize : function(dashboardElementView){
		this.targetView = dashboardElementView;
		this.propertyViews = [];

		var targetViewModel = dashboardElementView.model;

		var model = new ENS.DashboardElemenPropertyModel({
			objectId : targetViewModel.get("objectId"),
			objectName : targetViewModel.get("objectName"),
			pointX : targetViewModel.get("pointX"),
			pointY : targetViewModel.get("pointY"),
			width : targetViewModel.get("width"),
			height : targetViewModel.get("height"),
			elementAttrList : targetViewModel.get("elementAttrList")
		});

		this.model = model;
		this.initialModel = model.toJSON();
		var result = this.render();
		this.setElement(result);
	},
	render : function(){
		var instance = this;
		var div = this.createDiv();

		// 座標及び大きさに関するタブは必ず作成する。
		var property = {
			pointX : this.model.get("pointX"),
			pointY : this.model.get("pointY"),
			width : this.model.get("width"),
			height : this.model.get("height")
		};

		var objectName = this.model.get("objectName");
		if(objectName != "ENS.BackgroundElementView"){
			this.basicPropertyView = new ENS.DashboardElementPropertyPositionTab(property, "Basic", this.targetView);
		}else{
			this.basicPropertyView = new ENS.BackgroundPropertyPositionTab(property, "Basic", this.targetView);
		}

		this.addTabView(this.basicPropertyView);

		// ダッシュボードの要素ごとに決められたタブを追加する。
		this.addPropertyView(this.model.get("objectName"));

		this.tabDiv.tabs();
		div.appendTo("body");
		div.dialog({
			width : 500,
			buttons : [{
				text : "OK",
				click : function(event){
					// タブごとにバリデーションチェックする。
					if(instance.validation() == false){
						alert("An invalid value has been entered.\nPlease correct the value.");
						return;
					}

					// 設定を適用する。
					instance.applySettings();
					div.dialog("close");
				}
			},{
				text : "Apply Settings",
				click : function(event){

					// タブごとにバリデーションチェックする。
					if(instance.validation() == false){
						alert("An invalid value has been entered.\nPlease correct the value.");
						return;
					}
					instance.applySettings();
				}
			},{
				text : "Cancel",
				click : function(event){
					instance.destroy();

				}
			}],
			close : function(event){
				instance.destroy();
			}
		});
		return div;
	},
	createDiv : function(){
		var div = $("<div class='dashboardProperty' id='"+ this.cid +"', title='Property Setting'></div>");
		this.tabDiv = $("<div class='tab' id='"+ this.cid +"_tab'></div>");
		this.tabUl = $("<ul id='"+ this.cid +"'_ul></ul>");
		this.tabDiv.append(this.tabUl);
		div.append(this.tabDiv);
		return div;
	},
	addPropertyView : function(objectName){

		var instance = this;
		var propertyTabArray =
			ENS.dashboardElementPropertyTab[objectName];

		var elementAttrList = this.model.get("elementAttrList");
		_.each(propertyTabArray, function(tabName, index){
			var propertyView = null;
			if(tabName != null){
				var property = elementAttrList[index];
				if(tabName == "Shape"){
					propertyView = new ENS.DashboardElementPropertyShapeTab(property, "Shape", instance.targetView);
					instance.addTabView(propertyView);

				} else if(tabName == "Text"){
					propertyView = new ENS.DashboardElementPropertyTextTab(property, "Text", instance.targetView);
					instance.addTabView(propertyView);

				} else if(tabName == "Textbox"){
					propertyView = new ENS.DashboardElementPropertyTextboxTab(property, "Text", instance.targetView);
					instance.addTabView(propertyView);

				} else if(tabName == "Background"){
					propertyView = new ENS.BackgroundPropertyTab(property, "Background", instance.targetView);
					instance.addTabView(propertyView);
				}
			}
			instance.propertyViews.push(propertyView);
		});
	},
	addTabView : function(propertyView){
		var li = $("<li><a href='#"+ propertyView.cid +"'>"+ propertyView.title +"</a></li>")
		this.tabUl.append(li);
		this.tabDiv.append(propertyView.$el);
	},
	destroy : function(){
		this.$el.remove();
		this.remove();
	},
	applySettings : function(){
		// タブごとにバリデーションチェックする。

		// タブごとに値を取得してビューに設定する。
		var positionProperty = {
			pointX : this.model.get("pointX"),
			pointY : this.model.get("pointY"),
			width : this.model.get("width"),
			height : this.model.get("height")
		};
		_.extend(positionProperty, this.basicPropertyView.getAllProperty());
		this.targetView.setModelPosition(positionProperty);

		// 各タブから値を取得する。
		var elementAttrList = [];
		_.each(this.propertyViews, function(propertyView, index){
			var property = {};
			if(propertyView != null){
				property = propertyView.getAllProperty();
			}
			elementAttrList.push(property);
		});
		this.targetView.setAttributes(elementAttrList);
	},
	validation : function(){

		var result = this.basicPropertyView.validation();
		if(result == false){
			return result;
		}

		_.each(this.propertyViews, function(propertyView, index){
			if(propertyView && propertyView.validation() == false){
				result = false;
			}
		});
		return result;
	}
});

/**
 * プロパティ画面内の各タブ基底クラス
 */
ENS.DashboardElementPropertyTab = Backbone.View.extend({
	initialize : function(property, propertyViewName, targetView){
		this.property = property;
		this.title = propertyViewName;
		this.targetView = targetView;
		var result = this.render(this.cid);
		this.setElement(result);
	},
	render : function(cid){
		var parentDiv = $("<div id='"+ cid +"' class='propertyKind'></div>")
		return parentDiv;
	},
	createSettingItem : function(
			propertyName,
			propertyType,
			initialValue,
			options){

		if(propertyType == "number" || propertyType == "string"){
			return this.createTextbox(propertyName, propertyType, initialValue);

		}else if(propertyType == "select"){
			return this.createSelectbox(propertyName, propertyType, initialValue, options);

		}else if(propertyType == "color"){
			return this.createColorbox(propertyName, propertyType, initialValue);

		}else{
			return this.createTextbox(propertyName, propertyType, initialValue);
		}

	},
	createTextbox : function(propertyName, propertyType, initialValue){
		var tag = $("<input name='"+ propertyName +
			"' type='text' class='dashboardProperty "+ propertyType +"' value="+ initialValue +">");
		return tag;
	},
	createSelectbox : function(propertyName, propertyType, initialValue, options){
		var tag = $("<select name='"+ propertyName +"' class='dashboardProperty "+ propertyType +"'></select>");
		_.each(options, function(value, label){

			var option = $("<option value='"+ value +"' label='"+ label +"'>" + label +"</option>");
			if(initialValue == value){
				option.prop("selected", true);
			}
			tag.append(option);
		});
		return tag;
	},
	createColorbox : function(propertyName, propertyType, initialValue){

		if(initialValue == null || initialValue == undefined){
			initialValue = "#FFFFFF";
		}

		var tag = this.createTextbox(propertyName, propertyType, initialValue);
		tag.css("backgroundColor", initialValue);

		// テキストボックスの文字色をRGBから輝度を抽出して決定する。
		var textColor = this._getTextboxColor(initialValue);
		tag.css("color", textColor);

		tag.prop("readonly", true);
		$(tag).on("focus", function(event){
			var colorView = new ENS.ColorWheelDialog(tag);
		})
		return tag;
	},
	_getTextboxColor : function(color){
		var rgb = Raphael.getRGB(color);
		var hsb = Raphael.rgb2hsb(rgb.r , rgb.g, rgb.b);
		if(hsb.b < 0.5){
			return "#FFF";
		}else{
			return "#000";
		}
	},
	createFontFamilybox : function(propertyName, propertyType, initialValue, options){
		var tag = this.createSelectbox(propertyName, propertyType, initialValue, options);
		_.each(tag.find("option"), function(option, index){
			var fontFamily = $(option).attr("value");
			$(option).css("font-family", fontFamily);
		})
		return tag;
	},
	getAllProperty : function(){
		var instance = this;
		var property = {};
		var inputTags = this.$el.find("input");
		var selectTags = this.$el.find("select");

		_.each(inputTags, function(inputTag, inputTags){

			if(instance.checkAvailable(inputTag)){
				var name = $(inputTag).attr("name");
				var value = $(inputTag).val();
				if($(inputTag).hasClass("number")){
					value = parseInt(value);
				}
				property[name] = value;
			}
		});

		_.each(selectTags, function(selectTag, selectTags){

			if(instance.checkAvailable(selectTag)){
				var name = $(selectTag).attr("name");
				var value = $(selectTag).val();
				property[name] = value;
			}
		})
		return property;
	},
	validation : function(){
		var instance = this;
		var property = {};
		var inputTags = this.$el.find("input");
		var selectTags = this.$el.find("select");

		var result = true;
		_.each(inputTags, function(inputTag, inputTags){

			// 有効な値のみチェックする。
			if(instance.checkAvailable(inputTag)){
				var name = $(inputTag).attr("name");
				var value = $(inputTag).val();

				if($(inputTag).hasClass("number")){
					if(isNaN(value) || parseInt(value) < 0){
						result = false;
					}
				}
			}
		});
		return result;
	},
	checkAvailable : function(tag){

		if($(tag).is(':disabled')){
			return false;
		}

		var tagName = tag.tagName;
		if("INPUT" == tagName){

			var type = tag.type;
			if(("radio" == type || "checkbox" == type)
				&& $(tag).is(':checked') == false){
				return false;
			}
		}
		return true;
	}
});

/**
 * 位置、大きさのタブ
 */
ENS.DashboardElementPropertyPositionTab = ENS.DashboardElementPropertyTab.extend({
	render : function(cid){

		// 継承元のレンダリングメソッド実行
		var parentDiv = this.__proto__.__proto__.render(cid);
		var titleSpan = $("<span class='propertyTitle'>Position</span><hr color='white'>");
		parentDiv.append(titleSpan);

		var pointXSpan = $("<div>Point x</div>");
		var pointXInput = this.createSettingItem("pointX", "number", parseInt(this.property.pointX));
		var pointXRow = $("<div class='dashboardPropertyItem'></div>");
		pointXRow.append(pointXSpan).append(pointXInput);

		var pointYSpan = $("<div>Point y</div>");
		var pointYInput = this.createSettingItem("pointY", "number", parseInt(this.property.pointY));
		var pointYRow = $("<div class='dashboardPropertyItem'></div>");
		pointYRow.append(pointYSpan).append(pointYInput);

		parentDiv.append(pointXRow);
		parentDiv.append(pointYRow);

		// リサイズ可能な場合のみ幅、高さ指定可能とする。
		if(this.targetView.isResizable()){
			var widthSpan = $("<div>Width</div>");
			var widthInput = this.createSettingItem("width", "number", parseInt(this.property.width));
			var widthRow = $("<div class='dashboardPropertyItem'></div>");
			widthRow.append(widthSpan).append(widthInput);

			var heightspan = $("<div>Height</div>");
			var heightInput = this.createSettingItem("height", "number", parseInt(this.property.height));
			var heightRow = $("<div class='dashboardPropertyItem'></div>");
			heightRow.append(heightspan).append(heightInput);

			parentDiv.append(widthRow);
			parentDiv.append(heightRow);
		}
		return parentDiv;
	}
});

/**
 * 背景の大きさタブ
 */
ENS.BackgroundPropertyPositionTab = ENS.DashboardElementPropertyTab.extend({
	render : function(cid){

		// 継承元のレンダリングメソッド実行
		var parentDiv = this.__proto__.__proto__.render(cid);
		var titleSpan = $("<span class='propertyTitle'>Background Size</span><hr color='white'>");
		parentDiv.append(titleSpan);

		// 継承元のレンダリングメソッド実行
		var parentDiv = this.__proto__.__proto__.render(cid);

		var widthSpan = $("<div>Width</div>");
		var widthInput = this.createSettingItem("width", "number", parseInt(this.property.width));
		var widthRow = $("<div class='dashboardPropertyItem'></div>");
		widthRow.append(widthSpan).append(widthInput);

		var heightspan = $("<div>Height</div>");
		var heightInput = this.createSettingItem("height", "number", parseInt(this.property.height));
		var heightRow = $("<div class='dashboardPropertyItem'></div>");
		heightRow.append(heightspan).append(heightInput);

		parentDiv.append(widthRow);
		parentDiv.append(heightRow);

		return parentDiv;
	},
	validation : function(){

		// 継承元のバリデーションメソッド実行
		var result = ENS.DashboardElementPropertyTab.prototype.validation.apply(this);

		// 入力不正の場合は大きさチェックは行わない。
		if(!result){
			return result;
		}

		var inputProperty = this.getAllProperty();
		var dashboardView = this.targetView.dashboardView;
		_.each(dashboardView.collection.models, function(model, index){

			var pointX = model.get("pointX");
			var pointY = model.get("pointY");
			var width = model.get("width");
			var height = model.get("height");

			if(pointX + width > inputProperty["width"]){
				result = false;
			}

			if(pointY + height > inputProperty["height"]){
				result = false;
			}
		});

		return result;
	}
});

/**
 * 背景設定のタブ
 */
ENS.BackgroundPropertyTab = ENS.DashboardElementPropertyTab.extend({
	render : function(cid){

		// 継承元のレンダリングメソッド実行
		var parentDiv = this.__proto__.__proto__.render(cid);

		var setting = {
			data : {},
			url : wgp.common.getContextPath()
					+ "/dashboard/getBackgroundImage"
		}

		var ajaxHandler = new wgp.AjaxHandler();
		var telegram = ajaxHandler.requestServerSync(setting);
		var returnData = $.parseJSON(telegram);
		if(returnData.result == "fail"){
			alert(returnData.message);
			return;
		}
		var imageDataList = returnData.data;

		var titleSpan = $("<span class='propertyTitle'>Setting</span><hr color='white'>");
		parentDiv.append(titleSpan);

		var fillColorRadio = $("<input type='radio' name='objectType' value='"+ raphaelMapConstants.POLYGON_TYPE_NAME +"'>");
		var fillColorSpan = $("<div>color</div>");
		var fillAttribute = ENS.svg.attribute["fill"];
		var fillColorInput =
			this.createSettingItem("fill", fillAttribute.type, this.property.fill);
		var fillRow = $("<div class='dashboardPropertyItem'></div>");
		fillRow.append(fillColorSpan).append(fillColorInput);
		fillColorSpan.prepend(fillColorRadio);
		parentDiv.append(fillRow);

		var imageRadio = $("<input type='radio' name='objectType' value='"+ raphaelMapConstants.IMAGE_TYPE_NAME +"'>");
		var imageSpan = $("<div>image</div>");
		var imageAttribute = ENS.svg.attribute["href"];

		var src = ""
		if(this.property.src){
			src = this.property.src.replace(wgp.common.getContextPath() + "/", "");
		}
		var imageInput =
			this.createSettingItem("src", "select", this.property.src, imageDataList);
		var imageRow = $("<div class='dashboardPropertyItem'></div>");
		imageRow.append(imageSpan).append(imageInput);
		imageSpan.prepend(imageRadio);
		parentDiv.append(imageRow);

		parentDiv.on("change", "input[name='objectType']:radio", function(event){
			var objectType = $(this).val();

			if(objectType != raphaelMapConstants.POLYGON_TYPE_NAME){
				fillColorInput.prop("disabled", true);
				var fillColor = fillColorInput.val();
				fillColorInput.css("backgroundColor", "");
				fillColorInput.css("color", "");

			}else{
				fillColorInput.prop("disabled", false);
				var fillColor = fillColorInput.val();
				if(fillColor && fillColor.length > 0){
					fillColorInput.css("backgroundColor", fillColor);
				}
			}

			if(objectType != raphaelMapConstants.IMAGE_TYPE_NAME){
				imageInput.prop("disabled", true);
			}else{
				imageInput.prop("disabled", false);
			}
		});

		if(this.property.fill){
			fillColorRadio.prop("checked", true);
			imageInput.prop("disabled", true);

		}else{
			imageRadio.prop("checked", true);
			fillColorInput.prop("disabled", true);
			fillColorInput.css("backgroundColor", "");
			fillColorInput.css("color", "#000");
		}

		return parentDiv;
	}
});

/**
 * 図形のタブ
 */
ENS.DashboardElementPropertyShapeTab = ENS.DashboardElementPropertyTab.extend({
	render : function(cid){

		// 継承元のレンダリングメソッド実行
		var parentDiv = this.__proto__.__proto__.render(cid);

		// 図形の塗りつぶし色入力項目を作成
		var fillTitleSpan = $("<span class='propertyTitle'>Fill color</span><hr color='white'>");
		parentDiv.append(fillTitleSpan);

		var fillColorSpan = $("<div>color</div>");
		var fillAttribute = ENS.svg.attribute["fill"];
		var fillColorInput =
			this.createSettingItem("fill", fillAttribute.type, this.property.fill);
		var fillRow = $("<div class='dashboardPropertyItem'></div>");
		fillRow.append(fillColorSpan).append(fillColorInput);
		parentDiv.append(fillRow);

		// 図形の枠線入力項目を作成
		var StrokeTitleSpan = $("<span class='propertyTitle'>Stroke Style</span><hr color='white'>");
		parentDiv.append(StrokeTitleSpan);

		// 枠線色
		var strokeSpan = $("<div>color</div>");
		var strokeAttribute = ENS.svg.attribute["stroke"];
		var strokeInput =
			this.createSettingItem("stroke", strokeAttribute.type, this.property.stroke);
		var strokeRow = $("<div class='dashboardPropertyItem'></div>");
		strokeRow.append(strokeSpan).append(strokeInput);
		parentDiv.append(strokeRow);

		// 枠線種類
		var strokeDasharraySpan = $("<div>type</div>");
		var strokeDasharrayAttribute = ENS.svg.attribute["strokeDasharray"];
		var strokeDasharraySelect =
			this.createSettingItem(
				"strokeDasharray",
				strokeDasharrayAttribute.type,
				this.property.strokeDasharray,
				strokeDasharrayAttribute.selection);
		var strokeDasharrayRow = $("<div class='dashboardPropertyItem'></div>");
		strokeDasharrayRow.append(strokeDasharraySpan).append(strokeDasharraySelect);
		parentDiv.append(strokeDasharrayRow);

		// 枠線幅
		var strokeWidthSpan = $("<div>width</div>");
		var strokeWidthAttribute = ENS.svg.attribute["strokeWidth"];
		var strokeWidthInput =
			this.createSettingItem(
				"strokeWidth",
				strokeWidthAttribute.type,
				this.property.strokeWidth);
		var strokeWidthRow = $("<div class='dashboardPropertyItem'></div>");
		strokeWidthRow.append(strokeWidthSpan).append(strokeWidthInput);
		parentDiv.append(strokeWidthRow);

		return parentDiv;
	}
});

/**
 * テキストのタブ
 */
ENS.DashboardElementPropertyTextTab = ENS.DashboardElementPropertyTab.extend({
	render : function(cid){

		// 継承元のレンダリングメソッド実行
		var parentDiv = ENS.DashboardElementPropertyTab.prototype.render.apply(this, [cid]);

		var textTitleSpan = $("<span class='propertyTitle'>Text style</span><hr color='white'>");
		parentDiv.append(textTitleSpan);

		// テキストの色
		var textColorSpan = $("<div>color</div>");
		var textColorAttribute = ENS.svg.attribute["fill"];
		var textColorInput =
			this.createSettingItem("fill", textColorAttribute.type, this.property.fill);
		var textColorRow = $("<div class='dashboardPropertyItem'></div>");
		textColorRow.append(textColorSpan).append(textColorInput);
		parentDiv.append(textColorRow);

		// テキストのサイズ
		var textSizeSpan = $("<div>size</div>");
		var textSizeAttribute = ENS.svg.attribute["fontSize"];
		var textSizeSelect =
			this.createSettingItem(
				"fontSize",
				textSizeAttribute.type,
				this.property.fontSize,
				textSizeAttribute.selection);
		var textSizeRow = $("<div class='dashboardPropertyItem'></div>");
		textSizeRow.append(textSizeSpan).append(textSizeSelect);
		parentDiv.append(textSizeRow);

		// テキストのフォント
		var fontSpan = $("<div>font</div>");
		var fontAttribute = ENS.svg.attribute["fontFamily"];
		var fontSizeSelect =
			this.createFontFamilybox(
				"fontFamily",
				fontAttribute.type,
				this.property.fontFamily,
				fontAttribute.selection);
		var fontRow = $("<div class='dashboardPropertyFontItem'></div>");
		fontRow.append(fontSpan).append(fontSizeSelect);
		parentDiv.append(fontRow);

		return parentDiv;
	}
});

/**
 * テキストボックスのタブ
 */
ENS.DashboardElementPropertyTextboxTab = ENS.DashboardElementPropertyTextTab.extend({
	render : function(cid){

		// 継承元のレンダリングメソッド実行
		var parentDiv = ENS.DashboardElementPropertyTextTab.prototype.render.apply(this, [ cid ]);

		// テキストのフォント揃え
		var textAnchorSpan = $("<div>align</div>");
		var textAnchorAttribute = ENS.svg.attribute["textAnchor"];
		var textAnchorSelect =
			this.createSettingItem(
				"textAnchor",
				textAnchorAttribute.type,
				this.property.textAnchor,
				textAnchorAttribute.selection);
		var textAnchorRow = $("<div class='dashboardPropertyFontItem'></div>");
		textAnchorRow.append(textAnchorSpan).append(textAnchorSelect);
		parentDiv.append(textAnchorRow);
		return parentDiv;
	}
});

ENS.ColorWheelDialog = Backbone.View.extend({
	initialize : function(inputTextbox){
		var result = this.render(this.cid, inputTextbox);
		$(result).appendTo("body");

		this.renderExtend(result, this.cid, inputTextbox, $("#" + this.cid + " input"));
		this.setElement(result);
	},
	render : function(cid, inputTextbox){
		var div = $("<div id='"+ cid +"'></div>");
		var colorwheel = $("<div class='colorwheel'></div>");
		var colorInput = $("<input type='text'>");
		colorInput.val(inputTextbox.val());
		colorInput.prop("readonly", true);
		div.append(colorwheel);
		div.append(colorInput);
		return div;
	},
	renderExtend : function(
		div,
		cid,
		inputTextbox,
		colorInput){

		// カラーホイールを作成、初期値を設定。
		var cw = Raphael.colorwheel($("#" + cid + " .colorwheel")[0],150);
		cw.input(colorInput[0]);
		cw.color(colorInput.val());

		var instance = this;
		div.dialog({
			title : "Please select a color.",
			modal : true,
			buttons : [{
				text : "OK",
				click : function(event){
					var rgb = $(colorInput).css("backgroundColor");
					var rgbHex = Raphael.getRGB(rgb).hex;
					inputTextbox.css("backgroundColor", rgbHex);

					var textColor = $(colorInput).css("color");
					inputTextbox.css("color", textColor);

					inputTextbox.val(rgbHex);
					div.dialog("close");
				}
			},{
				text : "Cancel",
				click : function(event){
					instance.destroy();

				}
			}],
			close : function(event){
				instance.destroy();
			}
		});
	},
	destroy : function(){
		this.$el.remove();
		this.remove;
	}
});