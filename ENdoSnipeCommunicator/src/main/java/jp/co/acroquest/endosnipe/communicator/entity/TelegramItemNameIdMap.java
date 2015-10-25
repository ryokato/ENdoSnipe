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
package jp.co.acroquest.endosnipe.communicator.entity;

/**
 * 電文サイズを解凍する際に使う、項目名とIDのセット。
 * @author miyasaka
 *
 */
public class TelegramItemNameIdMap
{
    /** ID。 */
    private String id_;

    /** 項目名 */
    private String itemName_;

    /**
     * IDを設定する。
     * @param id ID
     */
    public void setId(final String id)
    {
        this.id_ = id;
    }

    /**
     * IDを取得する。
     * @return ID
     */
    public String getId()
    {
        return this.id_;
    }

    /**
     * 項目名を設定する。
     * @param itemName 項目名
     */
    public void setItemName(final String itemName)
    {
        this.itemName_ = itemName;
    }

    /**
     * 項目名を取得する。
     * @return 項目名
     */
    public String getItemName()
    {
        return this.itemName_;
    }
}
