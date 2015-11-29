/*******************************************************************************
 * ENdoSnipe 5.0 - (https://github.com/endosnipe)
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2012 Acroquest Technology Co.,Ltd.
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
package jp.co.acroquest.endosnipe.web.explorer.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.data.dao.JavelinMeasurementItemDao;
import jp.co.acroquest.endosnipe.data.dto.GraphTypeDto;
import jp.co.acroquest.endosnipe.data.entity.JavelinMeasurementItem;
import jp.co.acroquest.endosnipe.web.explorer.constants.LogMessageCodes;
import jp.co.acroquest.endosnipe.web.explorer.constants.TreeMenuConstants;
import jp.co.acroquest.endosnipe.web.explorer.dao.MultipleResourceGraphInfoDao;
import jp.co.acroquest.endosnipe.web.explorer.dao.SignalInfoDao;
import jp.co.acroquest.endosnipe.web.explorer.dao.SummarySignalInfoDao;
import jp.co.acroquest.endosnipe.web.explorer.dto.TreeMenuDto;
import jp.co.acroquest.endosnipe.web.explorer.entity.ChildResourceInfo;
import jp.co.acroquest.endosnipe.web.explorer.manager.DatabaseManager;
import jp.co.acroquest.endosnipe.web.explorer.util.TreeMenuUtil;

/**
 *
 * ツリーメニューに関する操作を行うクラスです。
 *
 * @author fujii
 *
 */
@Service
public class TreeMenuService
{
    /** ロガー。 */
    private static final ENdoSnipeLogger LOGGER = ENdoSnipeLogger.getLogger(DashboardService.class);

    /** ツリー階層の区切り文字 */
    private static final String TREE_SEPARATOR = "/";

    /** ツリー階層のセパレートパターン */
    private static final Pattern TREE_SEPARATE_PATTERN = Pattern.compile(TREE_SEPARATOR);

    /** 単位の区切り文字 */
    private static final String UNIT_SEPARATOR = ":";

    /** タイプ：グループ。 */
    private static final String GROUP_TYPE = "group";

    /** タイプ：ターゲット。 */
    private static final String TARGET_TYPE = "target";

    /** アイコン：グループ。 */
    private static final String GROUP_ICON = "center";

    /** アイコン：ターゲット。 */
    private static final String TARGET_ICON = "leaf";

    /** 複数グラフ情報Dao。 */
    @Autowired
    protected MultipleResourceGraphInfoDao multipleResourceGraphDao;

    /** 単数シグナル情報Dao。 */
    @Autowired
    protected SignalInfoDao signalInfoDao;

    /** サマリシグナル情報Dao。*/
    @Autowired
    protected SummarySignalInfoDao summarySignalInfoDao;

    /**
     * コンストラクタ
     */
    public TreeMenuService()
    {

    }

    /**
     * 初期化を行う。
     * @return 初期描画時のツリーメニュー
     */
    public List<TreeMenuDto> initialize()
    {
        DatabaseManager dbMmanager = DatabaseManager.getInstance();
        String dbName = dbMmanager.getDataBaseName(1);
        List<JavelinMeasurementItem> javelinMeasurementItemList = null;
        List<TreeMenuDto> treeMenuDtoList = new ArrayList<TreeMenuDto>();
        Map<String, TreeMenuDto> treeMenuMap = new LinkedHashMap<String, TreeMenuDto>();
        try
        {
            javelinMeasurementItemList = JavelinMeasurementItemDao.selectAll(dbName);
        }
        catch (SQLException sqlEx)
        {
            LOGGER.log(LogMessageCodes.SQL_EXCEPTION, sqlEx, sqlEx.getMessage());
        }

        if (javelinMeasurementItemList == null)
        {
            return treeMenuDtoList;
        }
        // TODO ツリー階層を作成すること。
        for (JavelinMeasurementItem javelinMeasurementItem : javelinMeasurementItemList)
        {
            String itemName = javelinMeasurementItem.itemName;
            addTreeData(treeMenuMap, itemName);
        }
        treeMenuDtoList = new ArrayList<TreeMenuDto>(treeMenuMap.values());
        return treeMenuDtoList;
    }

    /**
     * ツリーのデータを追加します。
     * @param treeMenuDtoMap ツリーメニュー情報のMap
     * @param itemName 項目名
     */
    private void addTreeData(final Map<String, TreeMenuDto> treeMenuDtoMap, final String itemName)
    {
        String[] itemList = TREE_SEPARATE_PATTERN.split(itemName);
        if (itemList.length < 1)
        {
            return;
        }
        String currentId = TREE_SEPARATOR;
        for (int cnt = 1; cnt < itemList.length; cnt++)
        {
            // ツリーの階層が最下層である場合はターゲット
            // そうではない場合はグループ
            String treeMenuType = "";
            if (cnt + 1 == itemList.length)
            {
                treeMenuType = TreeMenuConstants.TREE_MENU_TYPE_TARGET;
            }
            else
            {
                treeMenuType = TreeMenuConstants.TREE_MENU_TYPE_GROUP;
            }

            addTree(treeMenuDtoMap, currentId, itemList[cnt], treeMenuType);
            currentId += itemList[cnt] + TREE_SEPARATOR;
        }
    }

    /**
     * ツリーメニューを追加します。
     * @param treeMenuDtoMap ツリーメニューのMap
     * @param parentId 親ノードのID
     * @param itemName 項目名
     * @param treeMenuType ツリーメニューのタイプ
     */
    private void addTree(final Map<String, TreeMenuDto> treeMenuDtoMap, final String parentId,
            final String itemName, final String treeMenuType)
    {
        String currentItemName = itemName;
        String unitName = "";
        int unitPosition = itemName.indexOf(UNIT_SEPARATOR);
        if (unitPosition > 0)
        {
            unitName = itemName.substring(unitPosition + 1);
        }
        String currentId = parentId + currentItemName;
        TreeMenuDto menuDto = treeMenuDtoMap.get(currentId);
        if (menuDto != null)
        {
            return;
        }
        menuDto = new TreeMenuDto();
        menuDto.setId(TreeMenuUtil.getCannonicalId(currentId));
        menuDto.setTreeId(currentId);
        menuDto.setData(currentItemName);
        menuDto.setMeasurementUnit(unitName);
        menuDto.setParentTreeId(parentId.substring(0, parentId.length() - 1));
        menuDto.setType(treeMenuType);
        treeMenuDtoMap.put(currentId, menuDto);
    }

    /**
     * 指定された親ノードの直下にある子要素のパスの一覧を取得する。
     *
     * @param parentTreeId 親ノードのID
     * @return 子要素のリスト
     */
    public List<TreeMenuDto> getDirectChildNodes(final String parentTreeId)
    {
        List<TreeMenuDto> directChildPathList = new ArrayList<TreeMenuDto>();

        DatabaseManager dbMmanager = DatabaseManager.getInstance();
        String dbName = dbMmanager.getDataBaseName(1);
        try
        {
            // 計測情報の子要素を追加する。
            List<String> itemNameList =
                    JavelinMeasurementItemDao.selectDirectChildren(dbName, parentTreeId);

            Set<String> treeElementSet = new TreeSet<String>(itemNameList);

            List<String> graphNameList = getResourceNameList(parentTreeId);

            treeElementSet.addAll(graphNameList);
            for (String itemName : treeElementSet)
            {
                String[] itemNameSplits = itemName.split("/");

                String childNodeName = itemNameSplits[0];
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(parentTreeId);
                stringBuilder.append("/");
                stringBuilder.append(childNodeName);

                TreeMenuDto treeMenuDto = new TreeMenuDto();

                if (itemName.endsWith("/"))
                {
                    treeMenuDto.setType(GROUP_TYPE);
                    treeMenuDto.setIcon(GROUP_ICON);
                }
                else
                {
                    treeMenuDto.setType(TARGET_TYPE);
                    treeMenuDto.setIcon(TARGET_ICON);
                }
                String treeId = stringBuilder.toString();

                treeMenuDto.setId(TreeMenuUtil.getCannonicalId(treeId));
                treeMenuDto.setTreeId(treeId);
                treeMenuDto.setData(childNodeName);
                treeMenuDto.setParentTreeId(parentTreeId);

                directChildPathList.add(treeMenuDto);
            }

        }
        catch (SQLException sqlEx)
        {
            LOGGER.log(LogMessageCodes.SQL_EXCEPTION, sqlEx, sqlEx.getMessage());
        }
        return directChildPathList;
    }

    /**
     * 複数グラフの中から、ツリーに表示する子要素を取得する。
     * @param parentTreeId 親TreeId
     * @return 親要素配下に存在する子要素のID
     */
    private List<String> getResourceNameList(final String parentTreeId)
    {
        List<String> graphChildList = new ArrayList<String>();
        // MultiResourceGraphのツリーを追加する。
        String[] resourcenamePart = parentTreeId.split("/");
        int length = resourcenamePart.length;
        String searchTreeId = parentTreeId + "%";
        String selfElementName = "";
        if (resourcenamePart.length > 0)
        {
            selfElementName = resourcenamePart[length - 1];
        }

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("length", length);
        paramMap.put("childLength", length + 1);
        paramMap.put("grandChildLength", length + 2);
        paramMap.put("searchTreeId", searchTreeId);
        paramMap.put("selfElementName", selfElementName);

        List<ChildResourceInfo> graphInfoList =
                this.multipleResourceGraphDao.selectDirectChildren(paramMap);
        List<ChildResourceInfo> signalInfoList = this.signalInfoDao.selectDirectChildren(paramMap);

        graphInfoList.addAll(signalInfoList);

        for (ChildResourceInfo graphInfo : graphInfoList)
        {
            String child = graphInfo.child;
            boolean grandChild = graphInfo.grandChild;
            if (grandChild == true)
            {
                child += "/";
            }
            if ("".equals(child))
            {
                continue;
            }

            graphChildList.add(child);
        }
        return graphChildList;
    }

    /**
     * 一番階層が上のノードの一覧を取得する。
     *
     * @return 一番階層が上のノードの一覧
     */
    public List<TreeMenuDto> getTopNodes()
    {
        return getDirectChildNodes("");
    }

    /**
     * 指定された親ノードの子要素のターゲットのパスを全て取得する。
     *
     * @param parentTreeId 親ノードのID
     * @return 子要素のターゲットのパスのリスト
     */
    public List<GraphTypeDto> getChildTargetNodes(final String parentTreeId)
    {
        DatabaseManager dbMmanager = DatabaseManager.getInstance();
        String dbName = dbMmanager.getDataBaseName(1);

        List<GraphTypeDto> childNodes = null;
        try
        {
            childNodes = JavelinMeasurementItemDao.selectItemNameListByParentItemName(dbName,
                                                                                      parentTreeId);

        }
        catch (SQLException sqlEx)
        {
            LOGGER.log(LogMessageCodes.SQL_EXCEPTION, sqlEx, sqlEx.getMessage());
        }

        return childNodes;
    }

    /**
     * 指定された親ノードの子要素のパスを全て取得する。
     *
     * @param parentTreeId 親ノードのID
     * @return 子要素のパスのリスト
     */
    public List<GraphTypeDto> getAllChildNodes(final String parentTreeId)
    {
        DatabaseManager dbMmanager = DatabaseManager.getInstance();
        String dbName = dbMmanager.getDataBaseName(1);

        List<GraphTypeDto> childAllNodes = new ArrayList<GraphTypeDto>();
        try
        {
            List<GraphTypeDto> childNodes =
                    JavelinMeasurementItemDao.selectItemNameListByParentItemName(dbName,
                                                                                 parentTreeId);

            // 親パスの階層の深さ
            int parentSplitLength = parentTreeId.split(TREE_SEPARATOR).length;

            int childNodesLength = childNodes.size();
            for (int childIndex = 0; childIndex < childNodesLength; childIndex++)
            {
                String childNode = childNodes.get(childIndex).getItemName();
                String[] childNodeSplits = childNode.split(TREE_SEPARATOR);
                // 子パスの階層の深さ
                int childSplitsLength = childNodeSplits.length;

                for (int index = childSplitsLength; index > parentSplitLength; index--)
                {
                    StringBuilder pathBuilder = new StringBuilder();
                    for (int pathIndex = 1; pathIndex < index; pathIndex++)
                    {
                        pathBuilder.append(TREE_SEPARATOR);
                        pathBuilder.append(childNodeSplits[pathIndex]);
                    }
                    childNodes.get(childIndex).setItemName(pathBuilder.toString());
                    childAllNodes.add(childNodes.get(childIndex));
                }
            }
        }
        catch (SQLException sqlEx)
        {
            LOGGER.log(LogMessageCodes.SQL_EXCEPTION, sqlEx, sqlEx.getMessage());
        }

        return childAllNodes;
    }

    /**
     * Agent名の一覧を取得する。
     * @param parentTreeId ここで設定されたツリーノードID配下のAgent名を取得する
     * @return Agent名の一覧
     */
    public List<String> getAgentNameList(final String parentTreeId)
    {
        DatabaseManager dbMmanager = DatabaseManager.getInstance();
        String dbName = dbMmanager.getDataBaseName(1);
        // Agent名の一覧を追加する。
        List<String> agentNameList = new ArrayList<String>();
        try
        {
            agentNameList = JavelinMeasurementItemDao.selectAgentNameList(dbName, parentTreeId);
        }
        catch (SQLException sqlEx)
        {
            LOGGER.log(LogMessageCodes.SQL_EXCEPTION, sqlEx, sqlEx.getMessage());
        }

        return agentNameList;
    }
}
