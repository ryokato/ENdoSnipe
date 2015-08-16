/*******************************************************************************
 * ENdoSnipe 5.0 - (https://github.com/endosnipe)
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013 Acroquest Technology Co.,Ltd.
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
package jp.co.acroquest.endosnipe.collector.notification;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jp.co.acroquest.endosnipe.collector.processor.AlarmType;
import jp.co.acroquest.endosnipe.data.dto.SignalDefinitionDto;

/**
 * シグナルのアラームを通知に使用するEntityクラス。
 * 
 * @author fujii
 */
public class AlarmEntry
{
    /** 閾値判定定義情報ID */
    private long signalId_;

    /** シグナル名 */
    private String signalName_;

    /** 状態値 */
    private Double alarmValue_;

    /** 計測間隔(単位:ミリ秒) */
    private double escalationPeriod_;

    /** 閾値判定結果 */
    private int signalValue_;

    /** アラームを出すかどうか */
    private boolean sendAlarm_;

    /** アラームの種類 */
    private AlarmType alarmType_;

    /** 各レベルの閾値 */
    private String patternValue_;

    /** 閾値判定パターン */
    private String matchingPattern_;

    /** JavelinのIPアドレス */
    private String ipAddress_;

    /** Javelinのポート番号 */
    private int port_;

    /** ログデータの保存されるDBの名前 */
    private String databaseName_;

    /** パラメータ（追加情報） */
    private final Map<String, Object> parameters_;

    /** シグナルのレベル(3 or 5) */
    private int signalLevel_;

    private SignalDefinitionDto definition_;

    /** メール送信するかどうか */
    private boolean sendMail_;

    /**
     * コンストラクタ。
     */
    public AlarmEntry()
    {
        this.parameters_ = new HashMap<String, Object>();
    }

    /**
     * 閾値判定定義IDを取得する。
     * @return アラーム名
     */
    public long getSignalId()
    {
        return this.signalId_;
    }

    /**
     * 閾値判定定義IDを設定する。
     * @param signalId 閾値判定定義ID
     */
    public void setSignalId(final long signalId)
    {
        this.signalId_ = signalId;
    }

    /**
     * シグナル名を取得する。
     * @return シグナル名
     */
    public String getSignalName()
    {
        return signalName_;
    }

    /**
     * シグナル名を設定する。
     * @param signalName シグナル名
     */
    public void setSignalName(final String signalName)
    {
        signalName_ = signalName;
    }

    /**
     * 状態値を取得する。
     * @return 状態値
     */
    public Double getAlarmValue()
    {
        return this.alarmValue_;
    }

    /**
     * 状態値を設定する。
     * @param alarmValue 状態値
     */
    public void setAlarmValue(final Double alarmValue)
    {
        this.alarmValue_ = alarmValue;
    }

    /**
     * 計測間隔(単位:ミリ秒)を取得する。
     * @return 計測間隔(単位:ミリ秒
     */
    public double getEscalationPeriod()
    {
        return this.escalationPeriod_;
    }

    /**
     * エスカレーション期間(単位:ミリ秒)を設定する。
     * @param escalationPeriod 計測間隔(単位:ミリ秒)
     */
    public void setEscalationPeriod(final double escalationPeriod)
    {
        this.escalationPeriod_ = escalationPeriod;
    }

    /**
     * 閾値判定結果を取得する。
     * @return 閾値判定結果
     */
    public int getSignalValue()
    {
        return this.signalValue_;
    }

    /**
     * 閾値判定結果を設定する。
     * @param signalValue 閾値判定結果
     */
    public void setSignalValue(final int signalValue)
    {
        this.signalValue_ = signalValue;
    }

    /**
     * アラームを出すかどうかを設定する。
     * @param sendAlarm セットする sendAlarm
     */
    public void setSendAlarm(final boolean sendAlarm)
    {
        this.sendAlarm_ = sendAlarm;
    }

    /**
     * アラームを出すかどうかを取得する。
     * @return 対象の系列名
     */
    public boolean isSendAlarm()
    {
        return this.sendAlarm_;
    }

    /**
     * @param alarmType セットする alarmType
     */
    public void setAlarmType(final AlarmType alarmType)
    {
        this.alarmType_ = alarmType;
    }

    /**
     * @return alarmType　アラーム種別
     */
    public AlarmType getAlarmType()
    {
        return this.alarmType_;
    }

    /**
     * このアラームを検知したJavelinのIPアドレスを返します。<br />
     * 
     * @return IPアドレス
     */
    public String getIpAddress()
    {
        return ipAddress_;
    }

    /**
     * このアラームを検知したJavelinのIPアドレスを設定します。<br />
     * 
     * @param ipAddress IPアドレス
     */
    public void setIpAddress(final String ipAddress)
    {
        ipAddress_ = ipAddress;
    }

    /**
     * このアラームを検知したJavelinのポート番号を返します。<br />
     * 
     * @return port
     */
    public int getPort()
    {
        return port_;
    }

    /**
     * このアラームを検知したJavelinのポート番号を設定します。<br />
     * 
     * @param port ポート番号
     */
    public void setPort(final int port)
    {
        port_ = port;
    }

    /**
     * このアラームを検知したJavelinからのデータを保存するDBの名前を返します。<br />
     * 
     * @return databaseName DB名
     */
    public String getDatabaseName()
    {
        return databaseName_;
    }

    /**
     * このアラームを検知したJavelinからのデータを保存するDBの名前を設定します。<br />
     * 
     * @param databaseName DB名
     */
    public void setDatabaseName(final String databaseName)
    {
        databaseName_ = databaseName;
    }

    /**
     * パラメータを追加します。
     *
     * @param key キー
     * @param value 値
     */
    public void addParamter(final String key, final Object value)
    {
        this.parameters_.put(key, value);
    }

    /**
     * パラメータを取得します。
     *
     * @return パラメータ
     */
    public Map<String, Object> getParameters()
    {
        return Collections.unmodifiableMap(this.parameters_);
    }

    /**
     * シグナルのレベルを取得する。
     * @return シグナルのレベル
     */
    public int getSignalLevel()
    {
        return signalLevel_;
    }

    /**
     * シグナルのレベルを設定する。
     * @param signalLevel シグナルのレベル
     */
    public void setSignalLevel(final int signalLevel)
    {
        signalLevel_ = signalLevel;
    }

    /**
     * シグナルの定義情報を取得する。
     * @return シグナルの定義情報
     */
    public SignalDefinitionDto getDefinition()
    {
        return definition_;
    }

    /**
     * 各レベルの閾値を取得する。
     * @return 各レベルの閾値
     */
    public String getPatternValue()
    {
        return patternValue_;
    }

    /**
     * 各レベルの閾値を設定する。
     * @param patternValue 各レベルの閾値
     */
    public void setPatternValue(final String patternValue)
    {
        patternValue_ = patternValue;
    }

    /**
     * 閾値判定パターンを取得する。
     * @return 閾値判定パターン
     */
    public String getMatchingPattern()
    {
        return matchingPattern_;
    }

    /**
     * 閾値判定パターンを設定する。
     * @param matchingPattern 閾値判定パターン
     */
    public void setMatchingPattern(final String matchingPattern)
    {
        matchingPattern_ = matchingPattern;
    }

    /**
     * シグナルの定義情報を設定する。
     * @param definition シグナルの定義情報
     */
    public void setDefinition(final SignalDefinitionDto definition)
    {
        definition_ = definition;
    }

    /**
     * メール送信するかどうか。
     * @return true:メールを送信する、false：メールを送信しない
     */
    public boolean isSendMail()
    {
        return sendMail_;
    }

    /**
     * メール送信するかどうあかを設定する。
     * @param sendMail true:メールを送信する、false：メールを送信しない
     */
    public void setSendMail(final boolean sendMail)
    {
        sendMail_ = sendMail;
    }

}
