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
package jp.co.acroquest.endosnipe.javelin.converter.servlet;

import java.io.IOException;

import jp.co.acroquest.endosnipe.common.config.JavelinConfig;
import jp.co.acroquest.endosnipe.common.logger.SystemLogger;
import jp.co.acroquest.endosnipe.javassist.CannotCompileException;
import jp.co.acroquest.endosnipe.javassist.ClassPool;
import jp.co.acroquest.endosnipe.javassist.CtClass;
import jp.co.acroquest.endosnipe.javassist.CtMethod;
import jp.co.acroquest.endosnipe.javassist.NotFoundException;
import jp.co.acroquest.endosnipe.javelin.converter.AbstractConverter;
import jp.co.acroquest.endosnipe.javelin.converter.servlet.monitor.HttpServletMonitor;

/**
 * ServletJavelin
 * 
 * @author yamasaki
 */
public class HttpServletConverter extends AbstractConverter
{
    /** HttpServletモニタのクラス名称 */
    private static final String SERVLET_MONITOR_NAME = HttpServletMonitor.class.getCanonicalName();

    /** ThrowableのCtClass。 */
    private CtClass throwableClass_;

    /** Cookieから取得する値のキー */
    private static String cookieKey__ = new JavelinConfig().getHttpCookieKey();

    private static final String BEFORE =
            "if ($1 instanceof javax.servlet.http.HttpServletRequest) {" + 
            "javax.servlet.http.HttpServletRequest httpRequest "+
            "= (javax.servlet.http.HttpServletRequest)$1;" +
            "jp.co.acroquest.endosnipe.javelin.converter.servlet.monitor.HttpRequestValue "+
            "requestValue = " + 
            "    new jp.co.acroquest.endosnipe.javelin.converter."+
            "servlet.monitor.HttpRequestValue();" + 
            "requestValue.setPathInfo(httpRequest.getPathInfo());" +
            "requestValue.setContextPath(httpRequest.getContextPath());" +
            "requestValue.setServletPath(httpRequest.getServletPath());" +
            "requestValue.setRemoteHost(httpRequest.getRemoteHost());" +
            "requestValue.setRemotePort(httpRequest.getRemotePort());" +
            "requestValue.setMethod(httpRequest.getMethod());" +
            "requestValue.setQueryString(httpRequest.getQueryString());" +
            "requestValue.setCharacterEncoding(httpRequest.getCharacterEncoding());" +
            "requestValue.setSessionId(httpRequest.getSession().getId());" +
            "java.lang.String ipAddress = httpRequest.getHeader(\"X-Forwarded-For\");" +
            "if (ipAddress != null) {" +
            "  ipAddress = ipAddress.split(\",\")[0];" +
            "} else {" +
            "  ipAddress = httpRequest.getRemoteAddr();" +
            "}" +
            "requestValue.setIpAddress(ipAddress);" +
            "if(!\"" + cookieKey__ + "\".equals(\"\")){" +
            "javax.servlet.http.Cookie[] cookies = httpRequest.getCookies();" +
            "for(int cnt=0; cnt<cookies.length; cnt++){" +
            "javax.servlet.http.Cookie cookie = cookies[cnt];" +
            "if(\"" + cookieKey__ + "\".equals(cookie.getName())){" +
            "requestValue.setCookieValue(cookie.getValue());" +
            "}" +
            "}" +
            "}" +
            "if (requestValue.getCharacterEncoding() != null) {" + 
            "    requestValue.setParameterMap(httpRequest.getParameterMap()); " +
            "}" +
            SERVLET_MONITOR_NAME + ".preProcess(requestValue);" +
            "}";
        
        private static final String AFTER =
            "if ($1 instanceof javax.servlet.http.HttpServletRequest) {" + 
            "javax.servlet.http.HttpServletRequest httpRequest "+
            "= (javax.servlet.http.HttpServletRequest)$1;" +
            "jp.co.acroquest.endosnipe.javelin.converter.servlet.monitor.HttpRequestValue "+
            "requestValue = " + 
            "    new jp.co.acroquest.endosnipe.javelin.converter."+
            "servlet.monitor.HttpRequestValue();" + 
            "requestValue.setPathInfo(httpRequest.getPathInfo());" +
            "requestValue.setContextPath(httpRequest.getContextPath());" +
            "requestValue.setServletPath(httpRequest.getServletPath());" +
            "requestValue.setSessionId(httpRequest.getSession().getId());" +
            "javax.servlet.http.HttpServletResponse httpResponse = "+
            "(javax.servlet.http.HttpServletResponse)$2;" +
            "jp.co.acroquest.endosnipe.javelin.converter.servlet.monitor.HttpResponseValue "+
            "responseValue = " + 
            "    new jp.co.acroquest.endosnipe.javelin.converter."+
            "servlet.monitor.HttpResponseValue();" + 
            "responseValue.setContentType(httpResponse.getContentType());" +
            SERVLET_MONITOR_NAME + ".postProcess(requestValue, responseValue);" +
            "}";
        
        private static final String NG =
            "if ($1 instanceof javax.servlet.http.HttpServletRequest) {" + 
            "javax.servlet.http.HttpServletRequest httpRequest "+
            "= (javax.servlet.http.HttpServletRequest)$1;" +
            "jp.co.acroquest.endosnipe.javelin.converter.servlet.monitor.HttpRequestValue "+
            "requestValue = " + 
            "    new jp.co.acroquest.endosnipe.javelin.converter."+
            "servlet.monitor.HttpRequestValue();" + 
            "requestValue.setPathInfo(httpRequest.getPathInfo());" +
            "requestValue.setContextPath(httpRequest.getContextPath());" +
            "requestValue.setServletPath(httpRequest.getServletPath());" +
            SERVLET_MONITOR_NAME + ".postProcessNG(requestValue, $e);" +
            "}" + 
            "throw $e;";

    /**
     * {@inheritDoc}
     */
    public void init()
    {
        try
        {
            throwableClass_ = ClassPool.getDefault().get(Throwable.class.getCanonicalName());
        }
        catch (NotFoundException nfe)
        {
            // 発生しない。
            SystemLogger.getInstance().warn(nfe);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void convertImpl()
        throws CannotCompileException,
            IOException
    {
        CtClass ctClass = getCtClass();

        // serviceメソッドを呼び出し、serviceメソッドが存在する場合のみ、
        // メソッドにログ出力コードを埋め込む。
        try
        {
            CtMethod serviceMethod = ctClass.getDeclaredMethod("service");
            if (!serviceMethod.isEmpty())
            {
                convertMethod(serviceMethod);
            }
        }
        catch (NotFoundException nfe)
        {
            SystemLogger.getInstance().debug(nfe);
        }
        setNewClassfileBuffer(ctClass.toBytecode());
    }

    /**
     * 対象メソッドを変換する。
     * 
     * @param ctMethod 変換対象メソッド
     * @throws CannotCompileException コンパイル失敗時
     */
    private void convertMethod(final CtMethod ctMethod)
        throws CannotCompileException
    {
        ctMethod.insertBefore(BEFORE);
        ctMethod.insertAfter(AFTER);
        ctMethod.addCatch(NG, throwableClass_);

        // 処理結果をログに出力する。
        logModifiedMethod("HttpServletConverter", ctMethod);
    }
    
}
