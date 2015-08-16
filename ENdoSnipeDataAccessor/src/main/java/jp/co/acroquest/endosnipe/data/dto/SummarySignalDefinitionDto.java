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
package jp.co.acroquest.endosnipe.data.dto;

import java.util.List;

import jp.co.acroquest.endosnipe.data.entity.SummarySignalDefinition;

/**
 * SummarySignalのDTOクラス。
 * 
 * @author pin
 * 
 */
public class SummarySignalDefinitionDto
{
    /** SummarySignalのID。 */
    public long summarySignalId_;

    /** SummarySignalのName。 */
    public String summarySignalName_;

    /** SummarySignalのType。 */
    public int summmarySignalType_;

    /** SummarySignalのsignalList。 */
    public List<String> signalList_;

    /** SummarySignalのStatus。 */
    public int summarySignalStatus_;

    /** SummarySignalのpriorityNo。 */
    public String errorMessage_;

    /** SummarySignalのmessage。 */
    public int priority_;

    /** send mail. */
    private boolean sendMail_;

    /**
     * コンストラクタ。
     */
    public SummarySignalDefinitionDto()
    {
        //nothing
    }

    /**
     * Argument コンストラクタ。
     * @param summarySignalDefinition summarysignal
     */
    public SummarySignalDefinitionDto(SummarySignalDefinition summarySignalDefinition)
    {
        this.summarySignalId_ = summarySignalDefinition.summarySignalId;
        this.summarySignalName_ = summarySignalDefinition.summarySignalName;
        this.summmarySignalType_ = summarySignalDefinition.summarySignalType;
        this.summarySignalStatus_ = summarySignalDefinition.summarySignalStatus;
        this.signalList_ = summarySignalDefinition.signalList;
        this.errorMessage_ = summarySignalDefinition.errorMessage;
        this.priority_ = summarySignalDefinition.priority;
        this.sendMail_ = summarySignalDefinition.sendMail;
    }

    /**
     * SummarySignalのpriorityを取得する。
     * 
     * @return SummarySignalのpriority
     */
    public int getPriority()
    {
        return priority_;
    }

    /**
     * SummarySignalのpriorityを設定する。
     * 
     * @param priority
     *            SummarySignalのpriority
     */
    public void setPriority(int priority)
    {
        this.priority_ = priority;
    }

    /**
     * SummarySignalのIdを取得する。
     * 
     * @return SummarySignalのId
     */
    public long getSummarySignalId()
    {
        return summarySignalId_;
    }

    /**
     * SummarySignalのIdを設定する。
     * 
     * @param summarySignalId
     *            SummarySignalのId
     */
    public void setSummarySignalId(long summarySignalId)
    {
        this.summarySignalId_ = summarySignalId;
    }

    /**
     * SummarySignalのNameを取得する。
     * 
     * @return SummarySignalのName
     */
    public String getSummarySignalName()
    {
        return summarySignalName_;
    }

    /**
     * SummarySignalのNameを設定する。
     * 
     * @param summarySignalName
     *            SummarySignalのName
     */
    public void setSummarySignalName(String summarySignalName)
    {
        this.summarySignalName_ = summarySignalName;
    }

    /**
     * SummarySignalのTypeを取得する。
     * 
     * @return SummarySignalのType
     */
    public int getSummmarySignalType()
    {
        return summmarySignalType_;
    }

    /**
     * SummarySignalのTypeを設定する。
     * 
     * @param summmarySignalType
     *            SummarySignalのType
     */
    public void setSummmarySignalType(int summmarySignalType)
    {
        this.summmarySignalType_ = summmarySignalType;
    }

    /**
     * SummarySignalのSignalListを取得する。
     * 
     * @return SummarySignalのSignalList
     */
    public List<String> getSignalList()
    {
        return signalList_;
    }

    /**
     * SummarySignalのSignalListを設定する。
     * 
     * @param signalList
     *            SummarySignalのSignalList
     */
    public void setSignalList(List<String> signalList)
    {
        this.signalList_ = signalList;
    }

    /**
     * SummarySignalのStatusを取得する。
     * 
     * @return SummarySignalのStatus
     */
    public int getSummarySignalStatus()
    {
        return summarySignalStatus_;
    }

    /**
     * SummarySignalのStatusを設定する。
     * 
     * @param summarySignalStatus
     *            SummarySignalのStatus
     */
    public void setSummarySignalStatus(int summarySignalStatus)
    {
        this.summarySignalStatus_ = summarySignalStatus;
    }

    /**
     * SummarySignalのMessageを取得する。
     * 
     * @return SummarySignalのMessage
     */
    public String getErrorMessage()
    {
        return errorMessage_;
    }

    /**
     * SummarySignalのMessageを設定する。
     * 
     * @param errorMessage
     *            SummarySignalのmessage
     */
    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage_ = errorMessage;
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

    @Override
    public String toString()
    {
        return "SummarySignalDefinitionDto [summarySignalId=" + summarySignalId_
            + ", summarySignalName=" + summarySignalName_ + ", summmarySignalType="
            + summmarySignalType_ + ", signalList=" + signalList_ + ", summarySignalStatus="
            + summarySignalStatus_ + ", errorMessage=" + errorMessage_ + ", priority=" + priority_
            + ", sendMail=" + sendMail_ + "]";
    }

}
