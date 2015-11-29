/*
 * Copyright (c) 2004-2014 Acroquest Technology Co., Ltd. All Rights Reserved.
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
package jp.co.acroquest.endosnipe.web.explorer.template.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.acroquest.endosnipe.web.explorer.template.meta.Property;
import jp.co.acroquest.endosnipe.web.explorer.template.meta.Resource;

/**
 * リソースをMapに変換するユーティリティクラス
 * @author kamo
 *
 */
public class ResourceConvertUtil extends TemplateConvertUtil
{
    /** オブジェクトIDのキー */
    protected static final String KEY_OBJECT_ID = "objectId";

    /** x座標のキー */
    protected static final String KEY_X = "pointX";

    /** y座標のキー */
    protected static final String KEY_Y = "pointY";

    /** 横幅のキー */
    protected static final String KEY_W = "width";

    /** 高さのキー */
    protected static final String KEY_H = "height";

    /** 図形名のキー */
    protected static final String KEY_SHAPE_NAME = "shapeName";

    /** 属性要素のキー */
    protected static final String KEY_PROPERTIES = "elementAttrList";

    /** 深度のキー */
    protected static final String KEY_DEPTH = "zIndex";

    /** 図形タイプのキー */
    protected static final String KEY_SHAPE_TYPE = "shapeType";

    /** オブジェクトタイプのキー */
    protected static final String KEY_OBJECT_TYPE = "objectType";

    /** 塗りつぶし色のキー */
    protected static final String KEY_FILL = "fill";

    /** オブジェクト名のキー */
    protected static final String KEY_OBJECT_NAME = "objectName";

    /** 図形タイプの値 */
    protected static final Object VALUE_SHAPE_TYPE = "POLYGON";

    /** フォントファミリーのキー */
    private static final String KEY_FONT_FAMILY = "fontFamily";

    /** フォントサイズのキー */
    private static final String KEY_FONT_SIZE = "fontSize";

    /** リソースIDのキー */
    private static final String KEY_RESOUCE_ID = "resourceId";

    /** ストローク色のキー */
    private static final String KEY_STROKE = "stroke";

    /** 線の種類のキー */
    private static final String KEY_STROKE_DASHARRAY = "strokeDasharray";

    /** ターゲットのキー */
    private static final String KEY_TARGET = "target";

    /** 枠線の幅のキー */
    private static final String KEY_STROKE_WIDTH = "strokeWidth";

    /** テキストのキー */
    private static final String KEY_TEXT = "text";

    /** テキスト位置のキー */
    private static final String KEY_TEXT_ANCHOR = "textAnchor";

    /** 枠線プロパティのキー */
    private static final String KEY_BORDER = "border";

    /** ラベルプロパティのキー */
    private static final String KEY_LABEL = "label";

    /** シグナルプロパティのキー */
    private static final String KEY_SIGNAL = "signal";

    /** リソースIDのキー */
    private static final String KEY_ID = "resourceId";

    /** 深度の値 */
    private static final Object VALUE_DEPTH = 0;

    /** 空文字 */
    private static final String VALUE_BLANK = "";

    /** グラフのリソースタイプの値 */
    private static final String RESOURCE_TYPE_GRAPH = "graph";

    /** シグナルのリソースタイプの値 */
    private static final String RESOURCE_TYPE_SIGNAL = "signal";

    /** オブジェクトIDを一意にするためのカウンタ */
    private static int currentObjectId__ = 0;

    /**
     * コンストラクタ
     */
    protected ResourceConvertUtil()
    {

    }

    /**
     * プロパティをMapに変換する
     * @param property 変換するプロパティ
     * @return 変換結果のMap
     */
    private static Map<String, Object> convertProperty(final Property property)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        if (property.getBorder() != null)
        {
            result.put(KEY_BORDER, convertProperty(property.getBorder()));
        }
        if (property.getFill() != null)
        {
            result.put(KEY_FILL, property.getFill());
        }
        if (property.getFontFamily() != null)
        {
            result.put(KEY_FONT_FAMILY, property.getFontFamily());
        }
        if (property.getFontSize() != null)
        {
            result.put(KEY_FONT_SIZE, property.getFontSize());
        }
        if (property.getLabel() != null)
        {
            result.put(KEY_LABEL, property.getLabel());
        }
        if (property.getResourceId() != null)
        {
            result.put(KEY_RESOUCE_ID, property.getResourceId());
        }
        if (property.getShapeName() != null)
        {
            result.put(KEY_SHAPE_NAME, property.getShapeName());
        }
        if (property.getSignal() != null)
        {
            result.put(KEY_SIGNAL, property.getSignal());
        }
        if (property.getStroke() != null)
        {
            result.put(KEY_STROKE, property.getStroke());
        }
        if (property.getStrokeDasharray() != null)
        {
            result.put(KEY_STROKE_DASHARRAY, property.getStrokeDasharray());
        }
        if (property.getStrokeWidth() != null)
        {
            result.put(KEY_STROKE_WIDTH, property.getStrokeWidth());
        }
        if (property.getTarget() != null)
        {
            result.put(KEY_TARGET, property.getTarget());
        }
        if (property.getText() != null)
        {
            result.put(KEY_TEXT, property.getText());
        }
        if (property.getTextAnchor() != null)
        {
            result.put(KEY_TEXT_ANCHOR, property.getTextAnchor());
        }

        return result;
    }

    /**
     * リソースをマップに変換する
     * @param name テンプレートの名前
     * @param resource リソース
     * @return リソースを変換したマップ
     */
    public static Map<String, Object> convert(final String name, final Resource resource)
    {
        Map<String, Object> result = new HashMap<String, Object>();

        //固定値
        result.put(KEY_OBJECT_ID, currentObjectId__++);
        result.put(KEY_DEPTH, VALUE_DEPTH);

        //テンプレートからの読み込み値
        if (resource.getObjectName() != null)
        {
            result.put(KEY_OBJECT_NAME, resource.getObjectName());
        }
        if (resource.getX() != null)
        {
            result.put(KEY_X, resource.getX());
        }
        if (resource.getY() != null)
        {
            result.put(KEY_Y, resource.getY());
        }
        if (resource.getW() != null)
        {
            result.put(KEY_W, resource.getW());
        }
        if (resource.getH() != null)
        {
            result.put(KEY_H, resource.getH());
        }
        if (resource.getProperty() != null)
        {
            setAttrList(result, resource);
            setProperty(name, result, resource);
        }
        return result;
    }

    /**
     * elementEttrListを設定する
     * @param map 変換結果のマップ
     * @param resource 返還前のリソースオブジェクト
     */
    private static void setAttrList(final Map<String, Object> map, final Resource resource)
    {
        List<Map<String, Object>> attrList = new ArrayList<Map<String, Object>>();
        Property property = resource.getProperty();
        String objectName = resource.getObjectName();

        if (objectName.equals(Resource.OBJ_NAME_MULTIPLE_GRAPH)
                || objectName.equals(Resource.OBJ_NAME_GRAPH))
        {
            return;
        }
        else if (objectName.equals(Resource.OBJ_NAME_SHAPE))
        {
            attrList.add(convertProperty(property));
        }
        else if (objectName.equals(Resource.OBJ_NAME_TEXT))
        {
            Map<String, Object> border = convertProperty(property.getBorder());
            Map<String, Object> label = convertProperty(property.getLabel());
            attrList.add(border);
            attrList.add(label);
        }
        else if (objectName.equals(Resource.OBJ_NAME_SIGNAL))
        {
            return;
        }

        map.put(KEY_PROPERTIES, attrList);
    }

    /**
     * テンプレート定義ではプロパティ配下に位置づいているが、
     * 内部定義ではリソース直下に位置づいている属性を移動させる。
     * @param name テンプレート名
     * @param map 変換結果マップ
     * @param resource 変換前のリソースオブジェクト
     */
    private static void setProperty(final String name, final Map<String, Object> map,
            final Resource resource)
    {
        Property property = resource.getProperty();
        String objectName = resource.getObjectName();

        if (objectName.equals(Resource.OBJ_NAME_TEXT))
        {
            return;
        }
        else if (objectName.equals(Resource.OBJ_NAME_SHAPE))
        {
            map.put(KEY_SHAPE_NAME, property.getShapeName());
            map.put(KEY_SHAPE_TYPE, VALUE_SHAPE_TYPE);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> attr = (List<Map<String, Object>>)map.get(KEY_PROPERTIES);
            Map<String, Object> propertyMap = attr.get(0);
            propertyMap.remove(KEY_SHAPE_NAME);
            propertyMap.remove(KEY_SHAPE_TYPE);
            return;
        }
        else if (objectName.equals(Resource.OBJ_NAME_SIGNAL))
        {
            String signalName = getTreeName(name, objectName, property);
            map.put(KEY_ID, signalName);
            return;
        }
        else if (objectName.equals(Resource.OBJ_NAME_MULTIPLE_GRAPH)
                || objectName.equals(Resource.OBJ_NAME_GRAPH))
        {
            String graphName = getTreeName(name, objectName, property);
            map.put(KEY_ID, graphName);
            return;
        }

    }

    /**
     * 命名規則に従ってツリー上の配置を表す名前を取得する
     * @param templateName テンプレート名
     * @param objectName オブジェクト名
     * @param property プロパティオブジェクト
     * @return ツリー上の配置を表す名前
     */
    public static String getTreeName(final String templateName, final String objectName,
            final Property property)
    {
        String resourceId = property.getResourceId();
        String resourceType = null;
        if (objectName.equals(Resource.OBJ_NAME_MULTIPLE_GRAPH))
        {
            resourceType = RESOURCE_TYPE_GRAPH;
        }
        else if (objectName.equals(Resource.OBJ_NAME_SIGNAL))
        {
            resourceType = RESOURCE_TYPE_SIGNAL;
        }

        if (resourceId == null || VALUE_BLANK.equals(resourceId))
        {
            String name = property.getName();
            return TEMPLATE_PREFIX + SLASH + templateName + SLASH + resourceType + SLASH + name;
        }
        else
        {
            return property.getResourceId();
        }
    }

}
