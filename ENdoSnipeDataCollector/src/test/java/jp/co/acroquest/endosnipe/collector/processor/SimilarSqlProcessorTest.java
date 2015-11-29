/*******************************************************************************
 * ENdoSnipe 5.0 - (https://github.com/endosnipe)
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Acroquest Technology Co.,Ltd.
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
package jp.co.acroquest.endosnipe.collector.processor;

import java.lang.reflect.Method;

import junit.framework.TestCase;

public class SimilarSqlProcessorTest extends TestCase
{
    private SimilarSqlProcessor similarSqlProcessor_;

    @Override
    public void setUp()
    {
        similarSqlProcessor_ = new SimilarSqlProcessor();
    }

    /**
     * 同一SQL
     * @throws Exception 例外が発生した場合
     */
    public void test_isSameSqlWithoutParameter_SameSqlNoparameter()
        throws Exception
    {
        // 準備
        Method convertSameSqlMethod =
            similarSqlProcessor_.getClass().getDeclaredMethod("isSameSqlWithoutParameter",
                                                              String.class, String.class);
        convertSameSqlMethod.setAccessible(true);

        String sql1 = "Select * from Test";
        String sql2 = "Select * from Test";

        boolean result = (Boolean)convertSameSqlMethod.invoke(similarSqlProcessor_, sql1, sql2);
        assertTrue(result);
    }

    /**
     * 同一SQL
     * @throws Exception 例外が発生した場合
     */
    public void test_isSameSqlWithoutParameter_SameSqlHasparameter()
        throws Exception
    {
        // 準備
        Method convertSameSqlMethod =
            similarSqlProcessor_.getClass().getDeclaredMethod("isSameSqlWithoutParameter",
                                                              String.class, String.class);
        convertSameSqlMethod.setAccessible(true);

        String sql1 = "Select * from Test where SAMPLE = 1";
        String sql2 = "Select * from Test where SAMPLE = 1";

        boolean result = (Boolean)convertSameSqlMethod.invoke(similarSqlProcessor_, sql1, sql2);
        assertTrue(result);
    }

    /**
     * 同一SQL
     * @throws Exception 例外が発生した場合
     */
    public void test_isSameSqlWithoutParameter_SameSqlDifferparameter()
        throws Exception
    {
        // 準備
        Method convertSameSqlMethod =
            similarSqlProcessor_.getClass().getDeclaredMethod("isSameSqlWithoutParameter",
                                                              String.class, String.class);
        convertSameSqlMethod.setAccessible(true);

        String sql1 = "Select * from Test where SAMPLE = 1";
        String sql2 = "Select * from Test where SAMPLE = 2";

        boolean result = (Boolean)convertSameSqlMethod.invoke(similarSqlProcessor_, sql1, sql2);
        assertTrue(result);
    }

    /**
     * 同一SQL
     * @throws Exception 例外が発生した場合
     */
    public void test_isSameSqlWithoutParameter_SameSqlDifferCondition()
        throws Exception
    {
        // 準備
        Method convertSameSqlMethod =
            similarSqlProcessor_.getClass().getDeclaredMethod("isSameSqlWithoutParameter",
                                                              String.class, String.class);
        convertSameSqlMethod.setAccessible(true);

        String sql1 = "Select * from Test where SAMPLE = 1";
        String sql2 = "Select * from Test where TARGET = 2";

        boolean result = (Boolean)convertSameSqlMethod.invoke(similarSqlProcessor_, sql1, sql2);
        assertFalse(result);
    }

    /**
     * 同一SQL
     * @throws Exception 例外が発生した場合
     */
    public void test_isSameSqlWithoutParameter_SameSqlDifferConditionIn()
        throws Exception
    {
        // 準備
        Method convertSameSqlMethod =
            similarSqlProcessor_.getClass().getDeclaredMethod("isSameSqlWithoutParameter",
                                                              String.class, String.class);
        convertSameSqlMethod.setAccessible(true);

        String sql1 = "Select * from Test where SAMPLE in (1,2,3)";
        String sql2 = "Select * from Test where SAMPLE in (1)";

        boolean result = (Boolean)convertSameSqlMethod.invoke(similarSqlProcessor_, sql1, sql2);
        assertTrue(result);
    }

    /**
     * 同一SQL
     * @throws Exception 例外が発生した場合
     */
    public void test_isSameSqlWithoutParameter_SameSqlDifferConditionString()
        throws Exception
    {
        // 準備
        Method convertSameSqlMethod =
            similarSqlProcessor_.getClass().getDeclaredMethod("isSameSqlWithoutParameter",
                                                              String.class, String.class);
        convertSameSqlMethod.setAccessible(true);

        String sql1 = "Select * from Test where SAMPLE = 'Test(test=1)'";
        String sql2 = "Select * from Test where SAMPLE = 'Sample(sample=1)'";

        boolean result = (Boolean)convertSameSqlMethod.invoke(similarSqlProcessor_, sql1, sql2);
        assertTrue(result);
    }

    /**
     * 同一SQL
     * @throws Exception 例外が発生した場合
     */
    public void test_isSameSqlWithoutParameter_SameSqlDifferConditionStringIn()
        throws Exception
    {
        // 準備
        Method convertSameSqlMethod =
            similarSqlProcessor_.getClass().getDeclaredMethod("isSameSqlWithoutParameter",
                                                              String.class, String.class);
        convertSameSqlMethod.setAccessible(true);

        String sql1 = "Select * from Test where SAMPLE in ('one','two','three')";
        String sql2 = "Select * from Test where SAMPLE in ('four')";

        boolean result = (Boolean)convertSameSqlMethod.invoke(similarSqlProcessor_, sql1, sql2);
        assertTrue(result);
    }

    /**
     * 同一SQL
     * @throws Exception 例外が発生した場合
     */
    public void test_isSameSqlWithoutParameter_SameSqlDifferPreparedStatement()
        throws Exception
    {
        // 準備
        Method convertSameSqlMethod =
            similarSqlProcessor_.getClass().getDeclaredMethod("isSameSqlWithoutParameter",
                                                              String.class, String.class);
        convertSameSqlMethod.setAccessible(true);

        String sql1 = "Select * from Test where ? = SAMPLE";
        String sql2 = "Select * from Test where ? = TEST";

        boolean result = (Boolean)convertSameSqlMethod.invoke(similarSqlProcessor_, sql1, sql2);
        assertFalse(result);
    }

    /**
     * 同一SQL
     * @throws Exception 例外が発生した場合
     */
    public void test_isSameSqlWithoutParameter_SameSqlSubquery()
        throws Exception
    {
        // 準備
        Method convertSameSqlMethod =
            similarSqlProcessor_.getClass().getDeclaredMethod("isSameSqlWithoutParameter",
                                                              String.class, String.class);
        convertSameSqlMethod.setAccessible(true);

        String sql1 =
            "Select * from Test where SAMPLE = ( Select sample from TEST2 where condition = '1')";
        String sql2 =
            "Select * from Test where SAMPLE = ( Select sample from TEST2 where condition = 'test')";

        boolean result = (Boolean)convertSameSqlMethod.invoke(similarSqlProcessor_, sql1, sql2);
        assertTrue(result);
    }

    /**
     * 同一SQL
     * @throws Exception 例外が発生した場合
     */
    public void test_isSameSqlWithoutParameter_SameSqlSubquery2()
        throws Exception
    {
        // 準備
        Method convertSameSqlMethod =
            similarSqlProcessor_.getClass().getDeclaredMethod("isSameSqlWithoutParameter",
                                                              String.class, String.class);
        convertSameSqlMethod.setAccessible(true);

        String sql1 =
            "Select * from Test where SAMPLE = ( Select sample from TEST2 where condition = '1') and TEST = ? and ID = 500";
        String sql2 =
            "Select * from Test where SAMPLE = ( Select sample from TEST2 where condition = 'test') and TEST = ? and ID = 100 ";

        boolean result = (Boolean)convertSameSqlMethod.invoke(similarSqlProcessor_, sql1, sql2);
        assertTrue(result);
    }

}
