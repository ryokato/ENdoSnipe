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
package jp.co.acroquest.endosnipe.web.explorer.entity;

import java.util.Date;

/**
 * レポート定義テーブルに対するエンティティクラスです。<br />
 * 
 * @author miyasaka
 * 
 */
public class ReportDefinition
{
    /** テーブル名 */
    public String tableName_;

    /** レポート出力定義のID。 */
    public int reportId_;

    /** レポート名。 */
    public String reportName_;

    /** レポート出力の対象の計測対象名。 */
    public String targetMeasurementName_;

    /** 期間の始まりの日時。 */
    public Date fmTime_;

    /** 期間の終わりの日時。 */
    public Date toTime_;

    /**
     * for report's status
     */
    public String status_;

    /**
     * {@link SignalInfo} オブジェクトを生成します。<br />
     */
    public ReportDefinition()
    {
        this.reportId_ = -1;
    }

    @Override
    public String toString()
    {
        return String.format("ReportID%d ReportName:%s TargetMeasurementName:%s "
                                     + "FmTime:%s ToTime:%s Status:%s", reportId_, reportName_,
                             targetMeasurementName_,
                             fmTime_, toTime_, status_);
    }
}
