/*
 * Copyright (c) 2004-2015 Acroquest Technology Co., Ltd. All Rights Reserved.
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

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wgp.manager.WgpDataManager;

import jp.co.acroquest.endosnipe.web.explorer.manager.ControlSender;
import jp.co.acroquest.endosnipe.web.explorer.manager.EventManager;
import jp.co.acroquest.endosnipe.web.explorer.manager.ProfileSender;
import jp.co.acroquest.endosnipe.web.explorer.manager.ResourceSender;
import jp.co.acroquest.endosnipe.web.explorer.service.TreeMenuService;

/**
 * システムマップ機能のコントローラクラス。
 * @author kato
 *
 */
@Controller
@RequestMapping("/systemMap")
public class SystemMapController
{
    /** ツリーメニューに関する操作を行うクラスのオブジェクト。 */
    @Autowired
    protected TreeMenuService treeMenuService;

    /** WGPのデータを扱うクラスのオブジェクト。 */
    @Autowired
    protected WgpDataManager wgpDataManager;

    /** リソース送信クラスのオブジェクト。 */
    @Autowired
    protected ResourceSender resourceSender;

    /** メソッド情報送信クラスのオブジェクト */
    @Autowired
    protected ProfileSender profileSender;

    /** プロパティ情報送信クラスのオブジェクト */
    @Autowired
    protected ControlSender controlSender;

    /**
     * コンストラクタ。
     */
    public SystemMapController()
    {
        // Nothing.
    }

    /**
     * Show SystemMap View.
     *
     * @param request HTTPサーブレットリクエスト
     * @return 表示するjspファイルの名前
     */
    @RequestMapping(value = "/mapView")
    public String initializeDashboardList(final HttpServletRequest request)
    {
        EventManager eventManager = EventManager.getInstance();
        eventManager.setWgpDataManager(wgpDataManager);
        eventManager.setResourceSender(resourceSender);
        eventManager.setProfileSender(profileSender);
        eventManager.setControlSender(controlSender);

        return "SystemMap";
    }

}
