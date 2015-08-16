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
package jp.co.acroquest.endosnipe.data.entity;

import java.util.List;

/**
 * 
 * @author pin
 *
 */
public class SummarySignalDefinition
{
    /**  Id of summarySignal */
    public long summarySignalId;

    /**  Name of summarySignal */
    public String summarySignalName;

    /**  Type of summarySignal */
    public int summarySignalType;

    /**  childList of summarySignal */
    public List<String> signalList;

    /**  Status of summarySignal */
    public int summarySignalStatus;

    /**  errorMessage of summarySignal */
    public String errorMessage;

    /**  priority of summarySignal */
    public int priority;
    
    /** send mail */
    public boolean sendMail;

    /**
     * constructor
     */
    public SummarySignalDefinition()
    {
        this.summarySignalId = -1;
    }
    
    /**
     * get the childList of summary Signal
     *
     * @return childList of summary signal
     */
    
    public List<String> getSignalList()
    {
        return signalList;
    }
   
    /**
     * get the errorMessage of process
     *
     * @return errorMessage of process
     */
    public String getErrorMessage()
    {
        return errorMessage;
    }
   
   /**
     * get the priority of summary Signal
     *
     * @return priority of summary signal
     */
    public int getPriority()
    {
        return priority;
    }
   
    /**
     * get the Id of summary Signal
     *
     * @return Id of summary signal
     */
    public long getSummarySignalId()
    {
        return summarySignalId;
    }
   /**
     * get the signal name of summary Signal
     *
     * @return signal name of summary signal
     */
    public String getSummarySignalName()
    {
        return summarySignalName;
    }
  /**
     * get the signal type of summary Signal
     *
     * @return signal type of summary signal
     */
    public int getSummarySignalType()
    {
        return summarySignalType;
    }
    /**
     * get the status of summary Signal
     *
     * @return status of summary signal
     */
    public int getSummarySignalStatus()
    {
        return summarySignalStatus;
    }

    
    
}
