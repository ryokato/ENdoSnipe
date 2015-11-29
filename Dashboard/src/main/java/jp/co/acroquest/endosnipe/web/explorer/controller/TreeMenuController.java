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
package jp.co.acroquest.endosnipe.web.explorer.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wgp.manager.WgpDataManager;

import jp.co.acroquest.endosnipe.data.dto.GraphTypeDto;
import jp.co.acroquest.endosnipe.web.explorer.dto.TreeMenuDto;
import jp.co.acroquest.endosnipe.web.explorer.manager.ResourceSender;
import jp.co.acroquest.endosnipe.web.explorer.service.TreeMenuService;

/**
 * ツリーメニューのコントローラクラス。
 *
 * @author miyasaka
 *
 */
@Controller
@RequestMapping("/tree")
public class TreeMenuController
{
    private static final String KEY_OF_CHILD_NODE = "childNodes";

    private static final String KEY_OF_PARENT_NODE_ID = "parentNodeId";

    /** Wgpのデータを管理するクラス。 */
    @Autowired
    protected WgpDataManager wgpDataManager_;

    /** リソースを送信するクラスのオブジェクト。 */
    @Autowired
    protected ResourceSender resourceSender_;

    /** ツリーメニューに関する操作を行うクラスのオブジェクト。 */
    @Autowired
    protected TreeMenuService treeMenuService;

    /**
     * コンストラクタ。
     */
    public TreeMenuController()
    {

    }

    /**
     * 指定された親ノードの直下にある子要素のパスの一覧を取得する。
     *
     * @param parentTreeId
     *            親ノードのID
     * @return 指定された親ノードの直下にある子要素のパスの一覧
     */
    @RequestMapping(value = "/getDirectlChildNodes", method = RequestMethod.POST)
    @ResponseBody
    public List<TreeMenuDto> getDirectlChildNodes(
            @RequestParam(value = "parentTreeId") final String parentTreeId)
    {
        List<TreeMenuDto> childNodeList = this.treeMenuService.getDirectChildNodes(parentTreeId);

        return childNodeList;
    }

    /**
     * 階層が一番上のノードの一覧を取得する。
     *
     * @return 階層が一番上のノードの一覧
     */
    @RequestMapping(value = "/getTopNodes", method = RequestMethod.POST)
    @ResponseBody
    public List<TreeMenuDto> getTopNodes()
    {
        List<TreeMenuDto> topNodeList = this.treeMenuService.getTopNodes();

        return topNodeList;
    }

    /**
     * 指定された親ノードの子要素のターゲットのパスを全て取得する。
     *
     * @param parentTreeId 親ノードのID
     * @return 子要素のターゲットのパスのリスト
     */
    @RequestMapping(value = "/getChildTargetNodes", method = RequestMethod.POST)
    @ResponseBody
    public List<GraphTypeDto> getChildTargetNodes(
            @RequestParam(value = "parentTreeId") final String parentTreeId)
    {
        List<GraphTypeDto> childNodes = this.treeMenuService.getChildTargetNodes(parentTreeId);

        return childNodes;
    }

    /**
     * 指定された親ノードの子要素のパスを全て取得する。
     *
     * @param parentTreeId 親ノードのID
     * @return 子要素のパスのリスト
     */
    @RequestMapping(value = "/getAllChildNodes", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> getAllChildNodes(
            @RequestParam(value = "parentTreeId") final String parentTreeId)
    {
        List<GraphTypeDto> childNodes = this.treeMenuService.getAllChildNodes(parentTreeId);

        Map<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.put(KEY_OF_CHILD_NODE, childNodes);
        returnMap.put(KEY_OF_PARENT_NODE_ID, parentTreeId);

        return returnMap;
    }

    /**
     * 指定された親ノードの子要素のAgent名の一覧を取得する。
     * @param parentTreeId ここで設定されたツリーノードID配下のAgent名を取得する
     * @return Agent名の一覧
     */
    @RequestMapping(value = "/getAgentNameList", method = RequestMethod.POST)
    @ResponseBody
    public List<String> getAgentNameList(
            @RequestParam(value = "parentTreeId") final String parentTreeId)
    {
        List<String> agentNameList = this.treeMenuService.getAgentNameList(parentTreeId);

        return agentNameList;
    }
}
