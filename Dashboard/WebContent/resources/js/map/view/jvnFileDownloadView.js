ENS.jvnFileDwonloadView = wgp.AbstractView.extend({
	initialize : function(argument, treeSettings) {
		this.treeSettings = treeSettings;
		this.tableMargin = 50;
		this.tableWidth = parseInt($("#persArea_drop_0_1").width()
				- this.tableMargin);
		this.render();
	},
	render : function() {
		var instance = this;
		var $br = $("<div class='clear-float'></div>");

		var $downloadDiv = $("<div id='jvnFileDownload'>Javelin Log File Download</div>").append($br.clone(true));
		$downloadDiv.css("font-size", "0.8em");
		$downloadDiv.css("margin-left", 5);
		$downloadDiv.css("margin-top", 10);

		var $divStartDate = $("<div></div>");
		$divStartDate.css("float", "left");
		$divStartDate.css("margin-top", 5);
		
		var $startDatePicker = $("<input/>");
		$startDatePicker.attr("name", "start");
		$startDatePicker.attr("type", "text");
		$startDatePicker.attr("title", "From date");

		
		$startDatePicker.datetimepicker({
			dateFormat : "yy-mm-dd",
			timeFormat : "HH:mm"
		});

		var $divEndDate = $("<div></div>");
		$divEndDate.css("float", "left");
		$divEndDate.css("margin-top", 5);
		
		var $endDatePicker = $("<input/>");
		$endDatePicker.attr("name", "end");
		$endDatePicker.attr("type", "text");
		$endDatePicker.attr("title", "To date");

		$endDatePicker.datetimepicker({
			dateFormat : "yy-mm-dd",
			timeFormat : "HH:mm"
		});

		$divStartDate.append("<label>From：</label>");
		$divStartDate.append($startDatePicker);
		$divStartDate.append($br.clone(true));
		$downloadDiv.append($divStartDate);
		
		$divEndDate.append("<label>　To：</label>");
		$divEndDate.append($endDatePicker);

		$downloadDiv.append($divEndDate);
		
		var $searchButton = $("<button/>");
		$searchButton.attr("id", "jvnFileDownloadSearch");
		$searchButton.attr("class", "default-btn");
		$searchButton.attr("name", "search");
		$searchButton.attr("type", "button");
		$searchButton.attr("float", "left");
		$searchButton.css("margin-left", "10px");
		$searchButton.css("margin-top", "1.5px");
		$searchButton.html("Search");
		$downloadDiv.append($searchButton);

		// ファイルダウンロード用のフォーム
		var $downloadForm = $("<form id='jvnFileDownloadForm'></form>");
		$downloadForm.attr("action", '/ENdoSnipe/jvnFileDownload/download');
		$downloadForm.attr("target", "resultForm")
		$downloadForm.attr("method", "POST");

		// ファイルダウンロード対象として指定するログID格納用（複数）
		var $logIds = $("<input/>");
		$logIds.attr("id", "logIds");
		$logIds.attr("name", "logIds");
		$logIds.attr("type", "hidden");
		$downloadForm.append($logIds);

		
		var $downloadTable = $("<table id='jvnFileDownloadTable'></table>");
		var $downloadPager = $("<div id='jvnFileDownloadPager'></div>");
		var $downloadButton = $("<button/>");
		$downloadButton.attr("id", "jvnFileDownloadExecute");
		$downloadButton.attr("name", "download");
		$downloadButton.attr("type", "button");
		$downloadButton.attr("class", "default-btn");
		$downloadButton.html("Download");
		$downloadButton.css({
			"margin-left" : this.tableWidth - 86 + "px",
		});
		
		$downloadDiv.append($downloadForm);
		$downloadDiv.append($downloadTable).append($br.clone(true));
		$downloadDiv.append($downloadPager);
		$downloadDiv.append($downloadButton);
		$("#" + this.id).append($downloadDiv);

		// ダウンロード用のIフレーム
		var $iframe= $("<iframe/>");
		$iframe.hide();
		$("#" + this.id).append($iframe);

		// JVNファイル絞り込み結果テーブル描画
		this.renderTable();

		// 絞り込みボタンイベント設定
		$searchButton.on("click", function(event) {
			var start = null;
			var end = null;

			if (!$startDatePicker.prop("disabled")) {
				start = $startDatePicker.val();
			}

			if (!$endDatePicker.prop("disabled")) {
				end = $endDatePicker.val();
			} 
			
			var settings = {
				data : {
					agentName : instance.treeSettings.treeId,
					start : start,
					end : end
				},
				url : wgp.common.getContextPath() + ENS.URL.JVNFILEDOWNLOAD_SEARCH,
				successCallObject : instance,
				successCallFunction : "reloadTable"
			}

			var ajaxHandler = new wgp.AjaxHandler();
			ajaxHandler.requestServerAsync(settings);
		});

		// ダウンロードボタンイベント設定
		$downloadButton.on("click", function(event){

			var logIdArray = [];
			var selarrrow = $("#jvnFileDownloadTable").getGridParam("selarrrow");
			_.each(selarrrow, function(rowId, index) {
				var rowData = $("#jvnFileDownloadTable").getRowData(rowId);
				logIdArray.push(rowData.logId);
			});
			$logIds.val(JSON.stringify(logIdArray));

			var document = $iframe[0].contentDocument;
			var $iframeBody = $("body", document);

			var iframeForm = $downloadForm.clone(true);
			$iframeBody.append(iframeForm);
			iframeForm.submit();
		});

	},
	renderTable : function() {
		var instance = this;

		$("#jvnFileDownloadTable").jqGrid(
				{
					datatype : "local",
					data : "",
					colModel : [
			            {
			            	name: "logId",
			            	hidden: true
			            },
					    {
					    	name: "logFileName",
					    	hidden: true
					    },
			            {
			            	name: "startTime",
							width: parseInt(instance.tableWidth * 0.15),
			            },
			            {
			            	name: "endTime",
			            	width: parseInt(instance.tableWidth * 0.15)
			            },
			            {
			            	name: "calleeClass",
			            	width: parseInt(instance.tableWidth * 0.15)
			            },
			            {
			            	name: "calleeName",
			            	width: parseInt(instance.tableWidth * 0.15)
			            },
			            {
			            	name: "elapsedTime",
			            	width: parseInt(instance.tableWidth * 0.15),
			            	sorttype : "int"
			            },
			            {
			            	name: "threadName",
			            	width: parseInt(instance.tableWidth * 0.192)
			            }
		            ],
					colNames : ["log id","log file name", "Start Time", "End Time", "Caller Class", "Callee Method", "Processing Time", "Thread Name"],
					caption : "Download of " + instance.treeSettings.id,
					pager : "jvnFileDownloadPager",
					rowNum : 10000,
					pgbuttons : true,
					pginput : true,
					height : "auto",
					width : instance.tableWidth,
					viewrecords : true,
					rownumbers : false,
					shrinkToFit : false,
					sortname : "startTime",
					sortorder : "desc",
					multiselect : true,
					scrollerbar : true,
					cmTemplate : {
						title : false
					},
					loadComplete : function() {
						instance.reflectSelectState();
					},
					onSelectRow : function(id) {
						instance.reflectSelectState();
					},
					onSelectAll : function(aRowids, status) {
						instance.reflectSelectState();
					}
				});

		$("#jvnFileDownloadTable").filterToolbar({
			defaultSearch : 'cn'
		});
	},
	reloadTable : function(data) {
		$("#jvnFileDownloadTable").clearGridData().setGridParam({
			data : data
		}).trigger("reloadGrid");
	},
	reflectSelectState : function() {
		var selarrrow = $("#jvnFileDownloadTable").getGridParam("selarrrow");
		if(selarrrow && selarrrow.length > 0){
			$("#jvnFileDownloadExecute").prop("disabled", false);
		}
		else {
			$("#jvnFileDownloadExecute").prop("disabled", true);
		}
	},
	onAdd : function(element) {
		// Do nothing
	},
	onChange : function(element) {
		// Do nothing
	},
	onRemove : function(element) {
		// Do nothing
	},
	onComplete : function(element) {
		// Do nothing
	}
});