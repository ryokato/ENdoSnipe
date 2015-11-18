/*******************************************************************************
 * ENdoSnipe 5.2 - (https://github.com/endosnipe)
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Acroquest Technology Co.,Ltd.
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
package jp.co.acroquest.endosnipe.collector.processor;

import static jp.co.acroquest.endosnipe.collector.LogMessageCodes.FIND_SIMILAR_SQL;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.acroquest.endosnipe.collector.JavelinDataLogger;
import jp.co.acroquest.endosnipe.common.entity.MeasurementData;
import jp.co.acroquest.endosnipe.common.entity.ResourceData;
import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;

/**
 * 同一SQL判定を行い、類似したSQLがある場合には、同一SQLと見なし、処理を行うように変換します。<br />
 * 同一かどうかは、パラメータを除いたSQLにて判定します。
 * @author fujii
 *
 */
public class SimilarSqlProcessor
{
    /** SQL判定用の接尾辞 */
    private static final String SQL_SUFFIX = "/process/response/jdbc/";

    /** 保持するSQLの一覧 */
    private final Set<String> sqlSet_ = new CopyOnWriteArraySet<String>();

    /** ロガー */
    private static final ENdoSnipeLogger LOGGER = ENdoSnipeLogger
        .getLogger(JavelinDataLogger.class);

    /** SQL判定時パラメータ置き換え用定数 */
    private static final String PARAM = "__PARAM__";

    /** 数値・数値計算パターン */
    private static final Pattern PATTERN_NUM = Pattern.compile("(^|\\(|\\)|\\s|;|,|<|>|=)"
        + "(\\+|\\-)?[0-9]+(\\s?[\\.\\+\\-\\*/%]\\s*[0-9]+)*" + "($|\\(|\\)|\\s|;|,|<|>|=)");

    /** 計算式整形用パターン */
    private static final Pattern PATTERN_EXP_SPACE = Pattern.compile("([\\+\\-\\*/%\\(^])\\s*"
        + PARAM + "\\s*([\\+\\-\\*/%\\)$])");

    /** 括弧つきパラメータパターン */
    private static final Pattern PATTERN_PAREN = Pattern.compile("[\\+\\-]?\\(" + PARAM + "\\)");

    /** パラメータ同士の計算式パターン */
    private static final Pattern PATTERN_EXP = Pattern.compile("(\\(" + PARAM + "\\)|" + PARAM
        + ")\\s*[\\+\\-\\*/%,]\\s*(\\(" + PARAM + "\\)|" + PARAM + ")");

    /** 連続するパラメータパターン */
    private static final Pattern PATTERN_SEQ_PARAM = Pattern.compile(PARAM + "(" + PARAM + ")+");

    /** キャッシュするSQLの数 */
    private static final int MAX_CACHE_SIZE = 100000;

    /**
     * 類似したSQLが存在すれば、同一情報と判定して変換処理を行う。
     * @param resourceData {@link ResourceData}オブジェクト
     */
    public void convertSameSql(final ResourceData resourceData)
    {
        Map<String, MeasurementData> convertMeasurementMap = new HashMap<String, MeasurementData>();

        Map<String, MeasurementData> resourceDataMap = resourceData.getMeasurementMap();
        Iterator<Entry<String, MeasurementData>> it = resourceDataMap.entrySet().iterator();

        // 変換マップ
        Map<String, String> convertMap = new HashMap<String, String>();

        while (it.hasNext())
        {
            Entry<String, MeasurementData> measurementEntry = it.next();
            String itemName = measurementEntry.getKey();
            String sql = getSql(itemName);
            if (sql == null)
            {
                continue;
            }
            boolean hasSameSql = hasSameSql(sql);
            // 同一のSQLが存在する場合には判定処理を行わない。
            if (hasSameSql)
            {
                continue;
            }
            String similarSql = getSimilarSql(sql, convertMap);

            if (similarSql == null)
            {
                continue;
            }
            else
            {
                convertMap.put(sql, similarSql);
            }
            String newKey = createNewKey(itemName, sql, similarSql);
            MeasurementData current = measurementEntry.getValue();
            MeasurementData convertMeasurementData = current.clone();
            convertMeasurementData.itemName = newKey;
            convertMeasurementMap.put(itemName, convertMeasurementData);
            it.remove();
        }
        resourceDataMap.putAll(convertMeasurementMap);
    }

    /**
     * 系列名からSQLを取得する。
     * @param itemName 系列名
     * @return 系列名から取得したSQL
     */
    private String getSql(final String itemName)
    {
        int position = itemName.indexOf(SQL_SUFFIX);
        // SQL以外の場合は処理を行わない。
        if (position < 0)
        {
            return null;
        }
        String sqlConnection = itemName.substring(position + SQL_SUFFIX.length());

        int sqlStartPosition = sqlConnection.indexOf("/");
        if (sqlStartPosition < 0)
        {
            return null;
        }
        int sqlEndPosition = sqlConnection.lastIndexOf("/");
        if (sqlStartPosition == sqlEndPosition)
        {
            return null;
        }
        return sqlConnection.substring(sqlStartPosition + 1, sqlEndPosition);
    }

    /**
     * 同一のSQLを待つか。
     * @param sql 比較元SQL
     * @return 同一のSQLを持っている場合、trueを返す。
     */
    private boolean hasSameSql(final String sql)
    {
        return sqlSet_.contains(sql);
    }

    /**
     * 類似度の高いSQLを取得する。
     * @param sql 比較元SQL
     * @param convertMap 変換用Map
     * @return 類似度の高いSQL。類似度の高いSQLが見つからない場合はnullを返す。
     */
    private String getSimilarSql(final String sql, final Map<String, String> convertMap)
    {
        // 既に類似度の高いSQLが見つかっている場合は、前回の結果を返す。
        String similarSql = convertMap.get(sql);
        if (similarSql != null)
        {
            return similarSql;
        }
        for (String targetSql : sqlSet_)
        {
            if (isSameSqlWithoutParameter(sql, targetSql))
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.log(FIND_SIMILAR_SQL, sql, targetSql);
                }
                similarSql = targetSql;
                break;
            }
        }
        if (similarSql == null)
        {
            if (sqlSet_.size() > MAX_CACHE_SIZE)
            {
                sqlSet_.clear();
            }
            sqlSet_.add(sql);
        }

        return similarSql;
    }

    /**
     * 類似したSQLで変換した後のキーを作成する。
     * @param itemName 変換前のキー
     * @param baseSql 変換前のSQL
     * @param similarSql 変換前のSQLに類似したSQL
     * @return 類似したSQLで変換した後のキー
     */
    private String
        createNewKey(final String itemName, final String baseSql, final String similarSql)
    {
        int position = itemName.indexOf(baseSql);
        return itemName.substring(0, position) + similarSql
            + itemName.substring(position + baseSql.length());
    }

    /**
     * SQLがパラメータを除いて等しいことを判定する。
     * @param sql 比較するSQL
     * @param targetSql 比較するSQL
     * @return SQLがパラメータを除いて等しければtrue
     */
    private boolean isSameSqlWithoutParameter(String sql, String targetSql)
    {
        sql = replaceParam(sql);
        targetSql = replaceParam(targetSql);
        if (sql != null && targetSql != null && sql.equals(targetSql))
        {
            return true;
        }
        return false;
    }

    /**
     * パラメータを定数に置き換えたSQLを作成する。
     * @param sql 置き換え前SQL
     * @return パラメータ置き換え後SQL
     */
    private String replaceParam(String sql)
    {
        // trim
        sql = sql.trim();
        sql = sql.replaceAll("\\s\\s+", " ");
        sql = sql.replaceAll("\\(\\s", "(");
        sql = sql.replaceAll("\\s\\)", ")");

        // 文字列中のエスケープを置き換え
        sql = sql.replaceAll("''", PARAM);
        // 文字列を置き換え
        sql = sql.replaceAll("'.*?'", PARAM);

        // 数値および括弧を含まない数値計算を置き換え
        Matcher m = PATTERN_NUM.matcher(sql);
        while (m.find())
        {
            sql = m.replaceAll("$1" + PARAM + "$4");
            m = PATTERN_NUM.matcher(sql);
        }

        // 式中のスペースを削除
        Matcher matcherExpSpace = PATTERN_EXP_SPACE.matcher(sql);
        if (matcherExpSpace.find())
        {
            sql = matcherExpSpace.replaceAll("$1" + PARAM + "$2");
        }

        // 不要な括弧を削除
        Matcher matcherParen = PATTERN_PAREN.matcher(sql);
        while (matcherParen.find())
        {
            sql = matcherParen.replaceAll(PARAM);
            matcherParen = PATTERN_PAREN.matcher(sql);
        }

        // パラメータ同士の演算を置き換え
        Matcher matcherExp = PATTERN_EXP.matcher(sql);
        while (matcherExp.find())
        {
            sql = matcherExp.replaceAll(PARAM);
            matcherExp = PATTERN_EXP.matcher(sql);
        }

        // 不要な括弧の削除
        matcherParen = PATTERN_PAREN.matcher(sql);
        while (matcherParen.find())
        {
            sql = matcherParen.replaceAll(PARAM);
            matcherParen = PATTERN_PAREN.matcher(sql);
        }

        Matcher matcherSeqParam = PATTERN_SEQ_PARAM.matcher(sql);
        while (matcherSeqParam.find())
        {
            sql = matcherSeqParam.replaceAll(PARAM);
            matcherSeqParam = PATTERN_SEQ_PARAM.matcher(sql);
        }

        return sql;
    }
}
