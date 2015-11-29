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
package jp.co.acroquest.endosnipe.data.dto;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import jp.co.acroquest.endosnipe.data.entity.SignalDefinition;

/**
 * 閾値判定条件定義情報Dtoです。<br />
 * 
 * @author fujii
 *
 */
public class SignalDefinitionDto
{
    /** シグナル定義テーブルのID */
    private long signalId_;

    /** シグナル名 */
    private String signalName_;

    /** マッチングパターン */
    private String matchingPattern_;

    /** 設定できる閾値の上限レベル */
    private int level_;

    /** レベルごとの閾値 */
    private Map<Integer, Double> thresholdMaping_;

    /** エスカレーション期間 */
    private double escalationPeriod_;

    /** 各レベル毎の閾値 */
    private String patternValue_;

    /** メール送信フラグ */
    private boolean sendMail_;

    /** 閾値のスプリットパターン */
    private static final Pattern SPLIT_PATERN = Pattern.compile(",");

    /**
     * シグナル定義テーブルのIDを取得する。
     * @return シグナル定義テーブルのID
     */
    public long getSignalId()
    {
        return signalId_;
    }

    /**
     * シグナル定義テーブルのIDを設定する。
     * @param signalId シグナル定義テーブルのID
     */
    public void setSignalId(long signalId)
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
    public void setSignalName(String signalName)
    {
        this.signalName_ = signalName;
    }

    /**
     * マッチングパターンを取得する。
     * @return マッチングパターン名
     */
    public String getMatchingPattern()
    {
        return matchingPattern_;
    }

    /**
     * マッチングパターンを設定する。
     * @param matchingPattern マッチングパターン名
     */
    public void setMatchingPattern(String matchingPattern)
    {
        this.matchingPattern_ = matchingPattern;
    }

    /**
     * 設定できる閾値の上限レベルを取得する。
     * @return 設定できる閾値の上限レベル
     */
    public int getLevel()
    {
        return level_;
    }

    /**
     * 設定できる閾値の上限レベルを設定する。
     * @param level 設定できる閾値の上限レベル
     */
    public void setLevel(int level)
    {
        this.level_ = level;
    }

    /**
     * レベルごとの閾値を取得する。
     * @return レベルごとの閾値
     */
    public Map<Integer, Double> getThresholdMaping()
    {
        return thresholdMaping_;
    }

    /**
     * レベルごとの閾値を設定する。
     * @param thresholdMaping レベルごとの閾値
     */
    public void setThresholdMaping(Map<Integer, Double> thresholdMaping)
    {
        this.thresholdMaping_ = thresholdMaping;
    }

    /**
     * エスカレーション期間を取得する。
     * @return エスカレーション期間
     */
    public double getEscalationPeriod()
    {
        return escalationPeriod_;
    }

    /**
     * エスカレーション期間を設定する。
     * @param escalationPeriod エスカレーション期間
     */
    public void setEscalationPeriod(double escalationPeriod)
    {
        this.escalationPeriod_ = escalationPeriod;
    }

    /**
     * 各レベル毎の閾値を取得する。
     * @return 各レベル毎の閾値
     */
    public String getPatternValue()
    {
        return patternValue_;
    }

    /**
     * 各レベル毎の閾値を設定する。
     * @param patternValue 各レベル毎の閾値
     */
    public void setPatternValue(String patternValue)
    {
        patternValue_ = patternValue;
    }

    /**
     * メール送信フラグを取得する。
     * @return メール送信フラグ
     */
    public boolean isSendMail()
    {
        return sendMail_;
    }

    /**
     * メール送信フラグを設定する。
     * @param sendMail メール送信フラグ
     */
    public void setSendMail(boolean sendMail)
    {
        sendMail_ = sendMail;
    }

    /**
     * {@link SignalDefinitionDto} オブジェクトを生成します。
     * @param signalDefinition {@link SignalDefinition}オブジェクト
     */
    public SignalDefinitionDto(SignalDefinition signalDefinition)
    {
        this.signalId_ = signalDefinition.signalId;
        this.signalName_ = signalDefinition.signalName;
        this.matchingPattern_ = signalDefinition.matchingPattern;
        this.level_ = signalDefinition.level;
        this.escalationPeriod_ = signalDefinition.escalationPeriod;
        this.thresholdMaping_ = new TreeMap<Integer, Double>();
        this.patternValue_ = signalDefinition.patternValue;
        this.sendMail_ = signalDefinition.sendMail;

        String[] thresholdings = SPLIT_PATERN.split(signalDefinition.patternValue);
        if (thresholdings != null)
        {
            for (int cnt = 0; cnt < thresholdings.length; cnt++)
            {
                Double threshold = null;
                try
                {
                    String thresholdStr = thresholdings[cnt];
                    threshold = Double.valueOf(thresholdStr);
                }
                catch (NumberFormatException ex)
                {
                    // Do Nothing.
                }
                thresholdMaping_.put(Integer.valueOf(cnt + 1), threshold);
            }
        }
    }

    @Override
    public String toString()
    {
        return "SignalInfoDto [signalId=" + signalId_ + ", signalName=" + signalName_
            + ", matchingPattern=" + matchingPattern_ + ", level=" + level_ + ", thresholdMaping="
            + thresholdMaping_ + ", escalationPeriod=" + escalationPeriod_ + ", sendMail="
            + sendMail_ + "]";
    }
}
