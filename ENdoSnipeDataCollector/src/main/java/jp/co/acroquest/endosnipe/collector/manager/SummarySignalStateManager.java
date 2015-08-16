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
package jp.co.acroquest.endosnipe.collector.manager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import jp.co.acroquest.endosnipe.collector.data.SignalStateNode;
import jp.co.acroquest.endosnipe.collector.processor.AlarmData;
import jp.co.acroquest.endosnipe.collector.util.SignalSummarizer;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;
import jp.co.acroquest.endosnipe.data.dao.SummarySignalDefinitionDao;
import jp.co.acroquest.endosnipe.data.dto.SignalDefinitionDto;
import jp.co.acroquest.endosnipe.data.dto.SummarySignalDefinitionDto;
import jp.co.acroquest.endosnipe.data.entity.SummarySignalDefinition;

/**
 * SummarySignalの状態を保持するクラス
 * @author pin
 *
 */
public class SummarySignalStateManager
{
    private static SummarySignalStateManager instance__ = new SummarySignalStateManager();

    private static Map<Long, SummarySignalDefinitionDto> summarySignalDefinitionMap__ =
        new ConcurrentHashMap<Long, SummarySignalDefinitionDto>();

    private static Map<String, SignalStateNode> parChildMap__ =
        new HashMap<String, SignalStateNode>();

    /** SummarySignalDefinition の　Dao*/
    protected SummarySignalDefinitionDao summarySignalDefinitionDao_ =
        new SummarySignalDefinitionDao();

    /** signal Child List that change the state*/
    private final Set<String> alarmChildList_ = new TreeSet<String>();

    /** Summary signal Child List that change the state*/
    private final Set<String> alarmSummaryList_ = new TreeSet<String>();

    /** アラームを出すか判定するために保持するシグナルレベルのデータ  */
    private final Map<Long, Integer> signalLevelMap_ = new ConcurrentHashMap<Long, Integer>();

    /** Database name to be access*/
    public String dataBaseName;

    /** シグナルのレベルが3段階 */
    private static final int SIGNAL_LEVEL_3 = 3;

    /** シグナルのレベルが5段階 */
    private static final int SIGNAL_LEVEL_5 = 5;

    /** シグナルのタイプ OR */
    private static final int SIGNAL_TYPE_OR = 1;

    /** シグナルのタイプ AND */
    private static final int SIGNAL_TYPE_AND = 0;

    /**
     * {@link SummarySignalStateManager}インスタンスを取得する。
     * @return {@link SummarySignalStateManager}インスタンス
     */
    public static SummarySignalStateManager getInstance()
    {
        return instance__;
    }

    /**
     *SummarySignal定義情報のマップを返却する。
     * @return SummarySignal定義情報のマップ
     */
    public Map<Long, SummarySignalDefinitionDto> getSummarySignalDefinitionMap()
    {
        return summarySignalDefinitionMap__;
    }

    /**
     * SummarySignal定義情報のマップを設定する。
     * @param summarySignalDefinitionMap SummarySignal定義情報のマップ
     */
    public void setSummarySignalDefinitionMap(
        final Map<Long, SummarySignalDefinitionDto> summarySignalDefinitionMap)
    {
        summarySignalDefinitionMap__.putAll(summarySignalDefinitionMap);
    }

    /**
     * Child alarm dataを登録する。
     * @param signalId シグナルを一意にする名称
     */
    public void addChildAlarmData(final String signalId)
    {
        this.alarmChildList_.add(signalId);
    }

    /**
     *Summary Signal Alarm dataを登録する。
     * @param summarySignalId SummarySignalを一意にする名称
     */
    public void addSummaryAlarmData(final String summarySignalId)
    {
        this.alarmSummaryList_.add(summarySignalId);
    }

    /**
     * get the parChild Map
     * @return data of pair of child and parent
     */
    public Map<String, SignalStateNode> getParChildMap()
    {
        return parChildMap__;
    }

    /**
     * get the summary alarm data
     * @return list of summary signal
     */
    public Set<String> getAlarmSummaryList()
    {
        return alarmSummaryList_;
    }

    /**
     * get the child alarm data
     * @return list of signal
     */
    public Set<String> getAlarmChildList()
    {
        return alarmChildList_;
    }

    /**
     * remove the summary signal data from the map
     * @param summarySignalId of summary signal
     * @return removed summary signal data
     */
    public SummarySignalDefinitionDto removeSummarySignalDefinition(final long summarySignalId)
    {
        if (summarySignalDefinitionMap__ == null)
        {
            return null;
        }
        return summarySignalDefinitionMap__.remove(summarySignalId);
    }

    /**
     * add summary signal definition to the database and map
     * @param summarySignalDefinitionDto of summary signal
     */
    public void addSummarySignalDefinition(
        final SummarySignalDefinitionDto summarySignalDefinitionDto)
    {

        long summarySignalId = 0;
        String summarySignalName = summarySignalDefinitionDto.getSummarySignalName();

        summarySignalDefinitionDto.setSummarySignalStatus(-1);
        boolean isDuplicate = this.checkDuplicate(summarySignalName);
        summarySignalDefinitionDto.summarySignalStatus_ = 0;
        summarySignalDefinitionDto.errorMessage_ = "";
        if (isDuplicate == true)
        {
            summarySignalDefinitionDto.errorMessage_ = "Summary signal name cannot be duplicate";
        }
        else
        {
            try
            {
                this.calculatePriority(summarySignalDefinitionDto,
                                       summarySignalDefinitionDto.getSignalList());

                SummarySignalDefinition summarySignalDefinition =
                    this.convertSummarySignalDefinition(summarySignalDefinitionDto);

                SummarySignalDefinitionDao.insert(this.dataBaseName, summarySignalDefinition);
                summarySignalId =
                    summarySignalDefinitionDao_.selectSequenceNum(this.dataBaseName,
                                                                  summarySignalDefinition);
                summarySignalDefinitionDto.summarySignalId_ = summarySignalId;
                summarySignalDefinitionMap__.put(summarySignalId, summarySignalDefinitionDto);
                createAllSummarySignalMapValue();

            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    /**
     * calculate the priority for add or update summary signal
     * @param summarySignalDefinitionDto Summary Signalグラフ定義情報
     * @param signalList Summary SignalグラフのchildList 
     */
    private void calculatePriority(final SummarySignalDefinitionDto summarySignalDefinitionDto,
        final List<String> signalList)
        throws SQLException
    {
        Map<String, SummarySignalDefinition> summarySignalDefinition = null;
        SummarySignalDefinition summaryDefinition = new SummarySignalDefinition();
        int childMaxPriority = 0;
        try
        {
            summarySignalDefinition = summarySignalDefinitionDao_.selectAllSignalName(dataBaseName);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }

        if (summarySignalDefinition != null && summarySignalDefinition.size() > 0)
        {
            for (String signalName : signalList)
            {
                if (summarySignalDefinition.containsKey(signalName))
                {
                    summaryDefinition = summarySignalDefinition.get(signalName);
                    int priority = summaryDefinition.getPriority();
                    if (childMaxPriority < priority)
                    {
                        childMaxPriority = priority;
                    }
                }
            }
        }
        if (childMaxPriority >= summarySignalDefinitionDto.getPriority())
        {
            summarySignalDefinitionDto.priority_ = childMaxPriority + 1;
        }
    }

    /**
     * recalculate the priority for add or update summary signal
     * @param childName that change the priority
     * @param parentListSet of changed child
     */
    private void reCalculatePriority(final SummarySignalDefinitionDto childName,
        final Set<String> parentListSet)
        throws SQLException
    {
        if (parentListSet == null || parentListSet.size() == 0)
        {
            return;
        }
        else
        {
            for (String parentSummarySignal : parentListSet)
            {
                if (childName.priority_ >= parChildMap__.get(parentSummarySignal).priority)
                {
                    parChildMap__.get(parentSummarySignal).priority = childName.priority_ + 1;
                    SummarySignalDefinition parentDef = new SummarySignalDefinition();
                    parentDef.summarySignalName = parentSummarySignal;
                    parentDef.summarySignalId =
                        parChildMap__.get(parentDef.summarySignalName).getNodeId();

                    parentDef.priority = parChildMap__.get(parentSummarySignal).priority;
                    SummarySignalDefinitionDto parendDto =
                        new SummarySignalDefinitionDto(parentDef);
                    summarySignalDefinitionDao_.update(dataBaseName, parendDto.summarySignalId_,
                                                       parendDto, true);
                    summarySignalDefinitionMap__.get(parendDto.summarySignalId_).priority_ =
                        parendDto.priority_;
                    reCalculatePriority(parendDto,
                                        parChildMap__.get(parentSummarySignal).parentListSet);
                }
            }
        }
    }

    /**
     * convert from SummarySignalDefinitionDto to SummarySignalDefinition
     * @param definitionDto SummarySignalDefinitionDto
     * @return SummarySignal Definition
     */
    private SummarySignalDefinition convertSummarySignalDefinition(
        final SummarySignalDefinitionDto definitionDto)
    {
        SummarySignalDefinition summarySignalInfo = new SummarySignalDefinition();

        summarySignalInfo.summarySignalId = definitionDto.getSummarySignalId();
        summarySignalInfo.summarySignalName = definitionDto.getSummarySignalName();
        summarySignalInfo.summarySignalType = definitionDto.getSummmarySignalType();
        summarySignalInfo.signalList = definitionDto.getSignalList();
        summarySignalInfo.errorMessage = definitionDto.getErrorMessage();
        summarySignalInfo.priority = definitionDto.getPriority();
        summarySignalInfo.summarySignalStatus = definitionDto.getSummarySignalStatus();
        summarySignalInfo.sendMail = definitionDto.isSendMail();
        return summarySignalInfo;
    }

    /**
     * check the data is duplicate or not
     * @param summarySignalName Name of Summary Signal
     * @return true or false depend on duplicate or not
     */
    private boolean checkDuplicate(final String summarySignalName)
    {
        List<SummarySignalDefinition> summarySignalDefinition = null;
        try
        {
            summarySignalDefinition =
                summarySignalDefinitionDao_.selectByName(this.dataBaseName, summarySignalName);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        if (summarySignalDefinition != null && summarySignalDefinition.size() > 0)
        {
            return true;
        }

        return false;
    }

    /**
     * Set the database name 
     * @param dataBase Database Name
     */
    public void setDataBaseName(final String dataBase)
    {
        this.dataBaseName = dataBase;
    }

    /**
     * create Summary Signal Map that is the pair of parents and child
     */
    public void createAllSummarySignalMapValue()
    {
        parChildMap__.clear();
        List<String> childData = new ArrayList<String>();
        try
        {
            List<SummarySignalDefinition> summarySignalList =
                summarySignalDefinitionDao_.selectAllByPriority(this.dataBaseName);
            for (SummarySignalDefinition summarySignal : summarySignalList)
            {
                childData = summarySignal.getSignalList();
                String currentSignalName = summarySignal.getSummarySignalName();

                if (!parChildMap__.containsKey(currentSignalName))
                {
                    SignalStateNode signalStateNode = new SignalStateNode();
                    signalStateNode.name = summarySignal.getSummarySignalName();
                    signalStateNode.priority = summarySignal.getPriority();
                    signalStateNode.childList = summarySignal.getSignalList();
                    signalStateNode.signalType = summarySignal.getSummarySignalType();
                    signalStateNode.nodeId = summarySignal.getSummarySignalId();
                    parChildMap__.put(currentSignalName, signalStateNode);
                }
                else
                {
                    parChildMap__.get(currentSignalName).name =
                        summarySignal.getSummarySignalName();
                    parChildMap__.get(currentSignalName).priority = summarySignal.getPriority();
                    parChildMap__.get(currentSignalName).childList = summarySignal.getSignalList();
                    parChildMap__.get(currentSignalName).nodeId =
                        summarySignal.getSummarySignalId();
                    parChildMap__.get(currentSignalName).signalType =
                        summarySignal.getSummarySignalType();
                }
                for (String child : childData)
                {
                    if (!parChildMap__.containsKey(child))
                    {
                        SignalStateNode signalState = new SignalStateNode();
                        signalState.name = child;
                        signalState.priority = 0;
                        signalState.parentListSet.add(currentSignalName);
                        signalState.signalType = -1;
                        signalState.nodeId = -1;
                        parChildMap__.put(child, signalState);
                    }
                    else
                    {
                        parChildMap__.get(child).getParentListSet().add(currentSignalName);
                    }
                }
            }

        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * delete the summarysignal definition by matching name
     * @param summarySignalName summarySignal's Name
     * @return SummarySignalDefinitionのDto
     */
    public SummarySignalDefinitionDto deleteSummarySignalDefinition(final String summarySignalName)
    {
        SummarySignalDefinition sumDef = new SummarySignalDefinition();
        SummarySignalDefinitionDto sumDefDto = null;
        try
        {
            int summarySignalId = -1;

            sumDef.summarySignalName = summarySignalName;
            sumDef.errorMessage = "Cannot Delete";
            summarySignalId = (int)parChildMap__.get(summarySignalName).nodeId;
            sumDef.summarySignalId = summarySignalId;
            if (summarySignalId != -1)
            {

                summarySignalDefinitionDao_.delete(this.dataBaseName, summarySignalName);
                sumDef.errorMessage = "";
            }

            checkDeleteNode(summarySignalName, false);
            sumDefDto = new SummarySignalDefinitionDto(sumDef);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();

        }
        finally
        {
            if (sumDefDto == null)
            {
                sumDefDto = new SummarySignalDefinitionDto(sumDef);
            }
        }
        return sumDefDto;
    }

    /**
     * check the deleted node and delete on the parent or child list
     * @param summarySignalName summarySignal's Name
     * @param flag for separate the summary signal or signal
     * @return SummarySignalDefinitionのDto list
     * @throws SQLException for sql 
     */
    public List<SummarySignalDefinitionDto> checkDeleteNode(final String summarySignalName,
        final boolean flag)
        throws SQLException
    {

        if (this.alarmChildList_.contains(summarySignalName))
        {
            this.alarmChildList_.remove(summarySignalName);
        }
        for (Entry<Long, SummarySignalDefinitionDto> summarySignalDtoList : summarySignalDefinitionMap__
            .entrySet())
        {
            SummarySignalDefinitionDto summarySignalDto = summarySignalDtoList.getValue();

            if (summarySignalDto.getSignalList().contains(summarySignalName))
            {
                summarySignalDto.getSignalList().remove(summarySignalName);
                summarySignalDefinitionMap__.put(summarySignalDto.getSummarySignalId(),
                                                 summarySignalDto);
                summarySignalDefinitionDao_.update(dataBaseName,
                                                   summarySignalDto.getSummarySignalId(),
                                                   summarySignalDto, false);

            }

        }

        List<String> removeSignal = new ArrayList<String>();
        for (Entry<String, SignalStateNode> summarySignalDtoList : parChildMap__.entrySet())
        {
            SignalStateNode summarySignalDto = summarySignalDtoList.getValue();

            if (summarySignalDto.getParentListSet() != null
                && summarySignalDto.getParentListSet().contains(summarySignalName))
            {
                summarySignalDto.getParentListSet().remove(summarySignalName);

            }

            if (summarySignalDto.getChildList() != null
                && summarySignalDto.getChildList().contains(summarySignalName))
            {
                summarySignalDto.getChildList().remove(summarySignalName);

            }

            if (summarySignalDto.getParentListSet() != null
                && summarySignalDto.getChildList() != null
                && summarySignalDto.getParentListSet().size() == 0
                && summarySignalDto.childList.size() == 0
                && !summarySignalDefinitionMap__.containsKey(summarySignalDto.nodeId))
            {
                removeSignal.add(summarySignalDto.name);
            }
        }

        if (removeSignal.size() != 0)
        {
            for (String name : removeSignal)
            {
                parChildMap__.remove(name);
            }
        }
        if (flag)
        {
            Set<String> parentSummary = parChildMap__.get(summarySignalName).getParentListSet();
            List<SummarySignalDefinitionDto> summaryDtoList =
                new ArrayList<SummarySignalDefinitionDto>();
            for (String summary : parentSummary)
            {
                summaryDtoList.add(summarySignalDefinitionMap__.get(parChildMap__.get(summary)
                    .getNodeId()));
            }

            return SignalSummarizer
                .getInstance()
                .calculateChangeSummarySignalState(summaryDtoList,
                                                   TelegramConstants.ITEMNAME_SUMMARY_SIGNAL_UPDATE);
        }
        return null;
    }

    /**
     * update the summarysignal definition by matching name
     * @param summarySignalDefinitionDto SummarySignalDefinitionのDto
     * @return SummarySignalDefinitionのDto
     */
    public SummarySignalDefinitionDto updateSummarySignalDefinition(
        final SummarySignalDefinitionDto summarySignalDefinitionDto)
    {
        try
        {
            summarySignalDefinitionDto.setSummarySignalStatus(-1);
            long id =
                parChildMap__.get(summarySignalDefinitionDto.getSummarySignalName()).getNodeId();
            summarySignalDefinitionDto.summarySignalId_ = id;
            summarySignalDefinitionDto.priority_ = summarySignalDefinitionMap__.get(id).priority_;

            boolean isLoop =
                this.checkLoop(summarySignalDefinitionDto.getSummarySignalName(),
                               summarySignalDefinitionDto.getSummarySignalName(),
                               summarySignalDefinitionDto.getSignalList());
            if (isLoop == true)
            {
                summarySignalDefinitionDto.errorMessage_ = "It cannot be happen loop";
            }
            else
            {
                this.calculatePriority(summarySignalDefinitionDto,
                                       summarySignalDefinitionDto.getSignalList());
                summarySignalDefinitionDao_.update(dataBaseName,
                                                   summarySignalDefinitionDto.getSummarySignalId(),
                                                   summarySignalDefinitionDto, false);
                summarySignalDefinitionMap__.put(summarySignalDefinitionDto.getSummarySignalId(),
                                                 summarySignalDefinitionDto);
                createAllSummarySignalMapValue();
                String summarySignalName = summarySignalDefinitionDto.getSummarySignalName();
                SignalStateNode signalStateNode = new SignalStateNode();
                signalStateNode = parChildMap__.get(summarySignalName);
                if (signalStateNode != null && signalStateNode.getParentListSet() != null
                    && signalStateNode.getParentListSet().size() > 0)
                {
                    this.reCalculatePriority(summarySignalDefinitionDto,
                                             signalStateNode.getParentListSet());
                }

            }
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Check loop is occur or not for update prent or child
     * @param updatedSummarySignal summarysignal that was update
     * @param parentSummarySignalName that is the parent of updated child
     * @param signalList childList of updated summary signal
     */
    private boolean checkLoop(final String updatedSummarySignal,
        final String parentSummarySignalName, final List<String> signalList)
    {
        for (String targetSignal : signalList)
        {
            if (!updatedSummarySignal.equals(targetSignal))
            {
                if (parChildMap__.get(targetSignal) == null
                    || parChildMap__.get(targetSignal).childList == null
                    || parChildMap__.get(targetSignal).childList.size() == 0)
                {
                    continue;
                }
                else
                {
                    boolean result =
                        checkLoop(updatedSummarySignal, targetSignal,
                                  parChildMap__.get(targetSignal).childList);
                    if (result)
                    {
                        return true;
                    }
                }
            }
            else
            {
                return true;
            }
        }
        return false;
    }

    /**
     * サマリシグナルレベルデータを格納する
     * @param id サマリシグナルID
     * @param level サマリシグナルのレベル
     */
    public void setSignalLevel(final Long id, final Integer level)
    {
        signalLevelMap_.put(id, level);
    }

    /**
     * サマリシグナルのレベルを取得する
     * @param id サマリシグナルID
     * @return サマリシグナルのレベル(指定したIDが存在しない場合は-1)
     */
    public int getSignalLevel(final Long id)
    {
        Integer level = signalLevelMap_.get(id);
        if (level == null)
        {
            return -1;
        }
        return level;
    }

    /**
     * サマリシグナルのレベル値を取得する
     * @param summarySignalDefinitionDto サマリシグナルのDTO
     * @return サマリシグナルのレベル値
     */
    public int getSummaryLevel(final SummarySignalDefinitionDto summarySignalDefinitionDto)
    {
        if (summarySignalDefinitionDto.summmarySignalType_ == SIGNAL_TYPE_OR)
        {
            return getWorstLevel(summarySignalDefinitionDto);
        }
        else if (summarySignalDefinitionDto.summmarySignalType_ == SIGNAL_TYPE_AND)
        {
            return getBestLevel(summarySignalDefinitionDto);
        }
        return -1;
    }

    /**
     * サマリシグナルを構成するシグナル郡から、最も危険なレベルの値を取得する
     * @param summarySignalDefinitionDto サマリシグナルのDTO
     * @return 最も危険なレベル値
     */
    private int getWorstLevel(final SummarySignalDefinitionDto summarySignalDefinitionDto)
    {
        SignalStateManager signalStateManager = SignalStateManager.getInstance();
        int worstLevel = -1;
        for (String signalName : summarySignalDefinitionDto.signalList_)
        {
            Long signalId = signalStateManager.getSignalId(signalName);
            if (signalId == null)
            {
                continue;
            }
            AlarmData alarmData = signalStateManager.getAlarmData(signalId);
            SignalDefinitionDto signalDto =
                signalStateManager.getSignalDeifinitionMap().get(signalId);
            if (alarmData == null || signalDto == null)
            {
                continue;
            }
            int level = calculateSignalLevel(alarmData.getAlarmLevel(), signalDto.getLevel());
            if (worstLevel < level)
            {
                worstLevel = level;
            }
        }
        return worstLevel;
    }

    /**
     * サマリシグナルを構成するシグナル郡から、最も安全なレベルの値を取得する
     * @param summarySignalDefinitionDto サマリシグナルのDTO
     * @return 最も安全なレベル値
     */
    private int getBestLevel(final SummarySignalDefinitionDto summarySignalDefinitionDto)
    {
        SignalStateManager signalStateManager = SignalStateManager.getInstance();
        int bestLevel = -1;
        for (String signalName : summarySignalDefinitionDto.signalList_)
        {
            Long signalId = signalStateManager.getSignalId(signalName);
            if (signalId == null)
            {
                continue;
            }
            AlarmData alarmData = signalStateManager.getAlarmData(signalId);
            SignalDefinitionDto signalDto =
                signalStateManager.getSignalDeifinitionMap().get(signalId);
            if (alarmData == null || signalDto == null)
            {
                continue;
            }
            int level = calculateSignalLevel(alarmData.getAlarmLevel(), signalDto.getLevel());
            if (bestLevel == -1)
            {
                bestLevel = level;
            }
            if (bestLevel > level)
            {
                bestLevel = level;
            }
        }
        return bestLevel;
    }

    private static int calculateSignalLevel(final int signalValue, final int level)
    {
        if (level == SIGNAL_LEVEL_3)
        {
            if (0 <= signalValue && signalValue < SIGNAL_LEVEL_3)
            {
                return 2 * signalValue;
            }
            else
            {
                return -1;
            }
        }
        else if (level == SIGNAL_LEVEL_5)
        {
            if (0 <= signalValue && signalValue < SIGNAL_LEVEL_5)
            {
                return signalValue;
            }
            else
            {
                return -1;
            }
        }

        return -1;
    }

}
