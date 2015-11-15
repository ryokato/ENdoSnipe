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
package jp.co.acroquest.endosnipe.web.explorer.dao;

import java.util.Date;
import java.util.List;

import jp.co.acroquest.endosnipe.web.explorer.entity.ReportDefinition;

import org.apache.ibatis.annotations.Param;

/**
 * {@link ReportDefinition} のための DAO のインターフェースです。
 * 
 * @author miyasaka
 *
 */
public interface ReportDefinitionDao
{
    /**
     * 指定されたデータベースのレポート定義を全て取得します。<br />
     *
     * レポート定義が登録されていない場合は空のリストを返します。<br />
     *
     * @return レポート定義のリスト
     */
    List<ReportDefinition> selectAll();

    /**
     * レポート定義情報を取得する。
     *
     * @param reportId
     *            レポートID
     * @return レポート定義
     */
    ReportDefinition selectById(int reportId);

    /**
     * レポート定義情報をレポート名をキーにして取得する。
     * @param reportName レポート名
     * @return レポート定義の配列
     */
    List<ReportDefinition> selectByName(String reportName);

    /**
     * レポート定義情報の配列をレポート対象名をキーにして取得する。
     * @param targetMeasurementName レポート出力の対象の計測対象名
     * @return レポート定義の配列
     */
    List<ReportDefinition> selectByTargetName(String targetMeasurementName);

    /**
     * {@link ReportDefinition} オブジェクトを挿入します。<br />
     *
     * @param reportDefinition
     *            対象オジェクト
     */
    void insert(final ReportDefinition reportDefinition);

    /**
     * レポート定義を更新する。
     *
     * @param reportDefinition
     *            レポート定義
     */
    void update(final ReportDefinition reportDefinition);

    /**
     * 指定されたレポート名に該当するレポート情報をDBから削除する。
     *
     * @param reportName
     *            レポート名
     */
    void deleteByName(final String reportName);

    /**
     * 指定されたレポートIDに該当するレポート情報をDBから削除する。
     * 
     * @param reportId レポートID
     */
    void deleteById(final int reportId);

    /**
     * すべてのレコードを削除します。<br />
     */
    void deleteAll();

    /**
     * 直前のシーケンス情報を取得する。<br />
     * 
     * @param reportDefinition
     *            レポート情報
     * @return 直前のシーケンス番号
     */
    int selectSequenceNum(final ReportDefinition reportDefinition);

    /**
     * to check report Name and report term exist or not 
     * in dataBase
     * @param reportName is report name
     * @param startTime is start time of report
     * @param endTime is end time of report
     * @return reportDefinition 
     */
    ReportDefinition selectName(@Param("reportName") String reportName,
            @Param("fmTime") Date startTime, @Param("toTime") Date endTime);
}
