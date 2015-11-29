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
package jp.co.acroquest.endosnipe.web.explorer.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import blanco.commons.sql.format.BlancoSqlFormatter;
import blanco.commons.sql.format.BlancoSqlFormatterException;
import blanco.commons.sql.format.BlancoSqlRule;
import jp.co.acroquest.endosnipe.web.explorer.dao.SqlPlanDao;
import jp.co.acroquest.endosnipe.web.explorer.dto.SqlPlanDto;
import jp.co.acroquest.endosnipe.web.explorer.entity.SqlPlan;

/**
 * SQL実行計画機能のサービスクラス。
 *
 * @author miyasaka
 *
 */
@Service
public class SqlPlanService
{
    /** SQL実行計画のDaoオブジェクト。 */
    @Autowired
    protected SqlPlanDao sqlPlanDao;

    /**
     * コンストラクタ。
     */
    public SqlPlanService()
    {
        // Do nothing
    }

    /**
     * SQL実行計画のリストを取得する。
     *
     * @param itemName 項目名
     * @return SQL実行計画のリスト
     */
    public SqlPlanDto getSqlPlan(final String itemName)
    {
        List<SqlPlan> sqlPlanList = this.sqlPlanDao.selectByItemName(itemName);

        SqlPlanDto sqlPlanDto = convertSqlPlanDto(sqlPlanList);

        return sqlPlanDto;
    }

    /**
     * SqlPlanオブジェクトのリストを一つのSqlPlanDtoオブジェクトに変換する。
     *
     * @param sqlPlanList SqlPlanオブジェクトのリスト
     * @return SqlPlanDtoオブジェクト
     */
    private SqlPlanDto convertSqlPlanDto(final List<SqlPlan> sqlPlanList)
    {
        SqlPlanDto sqlPlanDto = new SqlPlanDto();

        if (sqlPlanList == null || sqlPlanList.size() == 0)
        {
            return sqlPlanDto;
        }
        // 項目名、SQL文、スタックとレースは、リスト内のどのIndexにも同じ値が入っているので、
        // 代表としてIndex=0の値を取得しSqlPlanDtoオブジェクトに設定する
        SqlPlan sqlPlanFirstIndex = sqlPlanList.get(0);
        if (sqlPlanFirstIndex == null)
        {
            return sqlPlanDto;
        }

        String formattedSqlStatement = this.formatTextSQL(sqlPlanFirstIndex.sqlStatement, null);
        sqlPlanDto.setSqlStatement(formattedSqlStatement);
        sqlPlanDto.setMeasurementItemName(sqlPlanFirstIndex.measurementItemName);

        // SQL実行計画、SQL実行計画の取得時間は、リストの各Indexで違う値が入っている可能性があるので、
        // リストにして、SqlPlanDtoオブジェクトに設定する
        List<String> executionPlanList = new ArrayList<String>();
        List<String> gettingPlanTimeList = new ArrayList<String>();
        List<String> stackTraceList = new ArrayList<String>();

        for (SqlPlan sqlPlan : sqlPlanList)
        {
            if (sqlPlan == null)
            {
                return new SqlPlanDto();
            }

            String timeStr =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sqlPlan.gettingPlanTime);

            // 実行計画と実行計画の取得時間は、取得時間が最新のレコードのみ返却する
            // なお、SQLの取得条件で取得時間が新しいものから順番に値がsqlPlanListには格納されていることが保証されている為、
            // sqlPlanListのindex=0の値が最新取得時間のレコードになる
            if (executionPlanList.size() == 0 && gettingPlanTimeList.size() == 0)
            {
                executionPlanList.add(sqlPlan.executionPlan);
                gettingPlanTimeList.add(timeStr);
            }

            String stackTrace = this.formatStackTrace(sqlPlan.stackTrace);
            stackTraceList.add(stackTrace);
        }

        sqlPlanDto.setExecutionPlanList(executionPlanList);
        sqlPlanDto.setGettingPlanTimeList(gettingPlanTimeList);
        sqlPlanDto.setStackTraceList(stackTraceList);

        return sqlPlanDto;
    }

    /**
     * SQL文をHTML表示用に整形する。
     *
     * @param text SQL文
     * @param sqlName SQL名
     * @return 整形されたSQL文
     */
    private String formatTextSQL(final String text, final String sqlName)
    {
        BlancoSqlFormatter formatter = new BlancoSqlFormatter(new BlancoSqlRule());

        String formattedSql;
        try
        {
            formattedSql = formatter.format(text);
            formattedSql = formattedSql.replace("\r\n", "<br>");
            formattedSql = formattedSql.replace("\n", "<br>");
            formattedSql = formattedSql.replace("\r", "<br>");

            // 半角スペース2つを全角スペース2つに変更する
            formattedSql = formattedSql.replace("  ", "　　");
        }
        catch (BlancoSqlFormatterException ex)
        {
            formattedSql = text;
        }

        return formattedSql;
    }

    /**
     * スタックトレースをHTML表示用に整形する。
     *
     * @param stackTrace スタックトレース
     * @return 整形後のスタックトレース
     */
    private String formatStackTrace(final String stackTrace)
    {
        String formattedStackTrace = stackTrace.replace("\r\n", "<br>");
        formattedStackTrace = formattedStackTrace.replace("\n", "<br>");
        formattedStackTrace = formattedStackTrace.replace("\r", "<br>");

        // スタックトレースの先頭に改行が入っている場合は削除する
        Pattern pattern = Pattern.compile("\\A<br>");
        Matcher matcher = pattern.matcher(formattedStackTrace);
        formattedStackTrace = matcher.replaceAll("");

        return formattedStackTrace;
    }
}
