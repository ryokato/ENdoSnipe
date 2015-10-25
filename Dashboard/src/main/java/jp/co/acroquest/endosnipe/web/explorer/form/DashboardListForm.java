/*
 * Copyright (c) 2004-2013 Acroquest Technology Co., Ltd. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 *
 * THE  SOFTWARE IS  PROVIDED BY  Acroquest Technology Co., Ltd., WITHOUT  WARRANTY  OF
 * ANY KIND,  EXPRESS  OR IMPLIED,  INCLUDING BUT  NOT LIMITED  TO THE
 * WARRANTIES OF  MERCHANTABILITY,  FITNESS FOR A  PARTICULAR  PURPOSE
 * AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDER BE LIABLE FOR ANY
 * CLAIM, DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package jp.co.acroquest.endosnipe.web.explorer.form;

/**
 * ダッシュボードの情報を持つクラス。
 * 
 * @author miyasaka
 *
 */
public class DashboardListForm
{
    /** ダッシュボードモード(運用or編集) */
    private String dashboardMode_;

    /** ResourceTreeViewの展開状態 */
    private String treeViewOpenNodeData_;

    /** 表示しているツリー名 */
    private String displayTreeArea_;

    /** 選択しているResourceTreeViewのID */
    private String selectedTreeId_;

    /** 選択しているResourceDashboardListViewのID */
    private String selectedDashboardListId_;

    /** DashBoardデフォルト系列数 */
    private Integer defautlSeriesNumber_;

    /**
     * コンストラクタ。
     */
    public DashboardListForm()
    {

    }

    /**
     * ダッシュボードモードを取得する。
     * 
     * @return ダッシュボードモード
     */
    public String getDashboardMode()
    {
        return dashboardMode_;
    }

    /**
     * ダッシュボードモードを設定する。
     * 
     * @param dashboardMode ダッシュボードモード
     */
    public void setDashboardMode(final String dashboardMode)
    {
        this.dashboardMode_ = dashboardMode;
    }

    /**
     * ResourceTreeViewの展開状態を取得する。
     * 
     * @return ResourceTreeViewの展開状態
     */
    public String getTreeViewOpenNodeData()
    {
        return treeViewOpenNodeData_;
    }

    /**
     * ResourceTreeViewの展開状態を設定する。
     * 
     * @param treeViewOpenNodeData ResourceTreeViewの展開状態
     */
    public void setTreeViewOpenNodeData(final String treeViewOpenNodeData)
    {
        this.treeViewOpenNodeData_ = treeViewOpenNodeData;
    }

    /**
     * 表示しているツリー名を取得する。
     * 
     * @return 表示しているツリー名
     */
    public String getDisplayTreeArea()
    {
        return displayTreeArea_;
    }

    /**
     * 表示しているツリー名を設定する。
     * 
     * @param displayTreeArea 表示しているツリー名
     */
    public void setDisplayTreeArea(final String displayTreeArea)
    {
        this.displayTreeArea_ = displayTreeArea;
    }

    /**
     * 選択しているResourceTreeViewのIDを取得する。
     * 
     * @return 選択しているResourceTreeViewのID
     */
    public String getSelectedTreeId()
    {
        return selectedTreeId_;
    }

    /**
     * 選択しているResourceTreeViewのID
     * 
     * @param selectedTreeId 選択しているResourceTreeViewのID
     */
    public void setSelectedTreeId(final String selectedTreeId)
    {
        this.selectedTreeId_ = selectedTreeId;
    }

    /**
     * 選択しているResourceDashboardListViewのIDを取得する。
     * 
     * @return 選択しているResourceDashboardListViewのID
     */
    public String getSelectedDashboardListId()
    {
        return selectedDashboardListId_;
    }

    /**
     * 選択しているResourceDashboardListViewのIDを設定する。
     * 
     * @param selectedDashboardListId 選択しているResourceDashboardListViewのID
     */
    public void setSelectedDashboardListId(final String selectedDashboardListId)
    {
        this.selectedDashboardListId_ = selectedDashboardListId;
    }

    /**
     * DashBoardデフォルト系列数を取得する。
     *
     * @return DashBoardデフォルト系列数
     */
    public Integer getDefautlSeriesNumber()
    {
        return defautlSeriesNumber_;
    }

    /**
     * DashBoardデフォルト系列数を設定する。
     *
     * @param defautlSeriesNumber DashBoardデフォルト系列数
     */
    public void setDefautlSeriesNumber(final Integer defautlSeriesNumber)
    {
        this.defautlSeriesNumber_ = defautlSeriesNumber;
    }

}
