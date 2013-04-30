/**********************************/
/* テーブル名: Javelinログ */
/**********************************/
CREATE TABLE JAVELIN_LOG(
  LOG_ID BIGINT DEFAULT NEXTVAL('SEQ_LOG_ID') NOT NULL,
  SESSION_ID BIGINT DEFAULT NEXTVAL('SEQ_SESSION_ID') NOT NULL,
  SEQUENCE_ID INT NOT NULL,
  JAVELIN_LOG BYTEA NOT NULL,
  LOG_FILE_NAME VARCHAR,
  START_TIME TIMESTAMP,
  END_TIME TIMESTAMP,
  SESSION_DESC VARCHAR,
  LOG_TYPE INT,
  CALLEE_NAME VARCHAR,
  CALLEE_SIGNATURE VARCHAR,
  CALLEE_CLASS VARCHAR,
  CALLEE_FIELD_TYPE VARCHAR,
  CALLEE_OBJECTID INT,
  CALLER_NAME VARCHAR,
  CALLER_SIGNATURE VARCHAR,
  CALLER_CLASS VARCHAR,
  CALLER_OBJECTID INT,
  EVENT_LEVEL SMALLINT,
  ELAPSED_TIME BIGINT,
  MODIFIER VARCHAR,
  THREAD_NAME VARCHAR,
  THREAD_CLASS VARCHAR,
  THREAD_OBJECTID INT
);

/**********************************/
/* テーブル名: Javelin計測項目 */
/**********************************/
CREATE UNLOGGED TABLE JAVELIN_MEASUREMENT_ITEM(
  MEASUREMENT_ITEM_ID INT DEFAULT NEXTVAL('SEQ_MEASUREMENT_ITEM_ID') NOT NULL,
  MEASUREMENT_ITEM_NAME VARCHAR NOT NULL,
  LAST_INSERTED TIMESTAMP NOT NULL
);

/**********************************/
/* テーブル名: Javelin 計測値 */
/**********************************/
CREATE UNLOGGED TABLE MEASUREMENT_VALUE(
  MEASUREMENT_TIME TIMESTAMP NOT NULL,
  MEASUREMENT_ITEM_ID INT NOT NULL,
  MEASUREMENT_VALUE VARCHAR NOT NULL
);

/**********************************/
/* テーブル名: 計測対象ホスト情報 */
/**********************************/
CREATE TABLE HOST_INFO(
  HOST_ID INT DEFAULT NEXTVAL('SEQ_HOST_ID') NOT NULL,
  HOST_NAME VARCHAR,
  IP_ADDRESS VARCHAR NOT NULL,
  PORT INT NOT NULL,
  DESCRIPTION VARCHAR
);

ALTER TABLE JAVELIN_LOG ADD PRIMARY KEY (LOG_ID);
CREATE INDEX IDX_JAVELIN_LOG_START_TIME ON JAVELIN_LOG (START_TIME);
CREATE INDEX IDX_JAVELIN_LOG_END_TIME ON JAVELIN_LOG (END_TIME);
CREATE INDEX IDX_JAVELIN_LOG_SESSION_ID ON JAVELIN_LOG (SESSION_ID);
CREATE INDEX IDX_JAVELIN_LOG_LOG_FILE_NAME ON JAVELIN_LOG (LOG_FILE_NAME);

ALTER TABLE JAVELIN_MEASUREMENT_ITEM ADD PRIMARY KEY (MEASUREMENT_ITEM_ID);

ALTER TABLE MEASUREMENT_VALUE ADD PRIMARY KEY (MEASUREMENT_TIME, MEASUREMENT_ITEM_ID);
ALTER TABLE MEASUREMENT_VALUE ADD CONSTRAINT FK_MEASUREMENT_VALUE_0 FOREIGN KEY (MEASUREMENT_ITEM_ID) REFERENCES JAVELIN_MEASUREMENT_ITEM (MEASUREMENT_ITEM_ID);
