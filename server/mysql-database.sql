DROP DATABASE IF EXISTS MIRTHDB;

CREATE DATABASE MIRTHDB; 

USE MIRTHDB;

DROP TABLE IF EXISTS SCHEMA_INFO;

CREATE TABLE SCHEMA_INFO
	(VERSION VARCHAR(40));

DROP TABLE IF EXISTS EVENT;

CREATE TABLE EVENT
	(ID INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
	DATE_CREATED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	EVENT LONGTEXT NOT NULL,
	EVENT_LEVEL VARCHAR(40) NOT NULL,
	DESCRIPTION LONGTEXT,
	ATTRIBUTES LONGTEXT);

ALTER TABLE MESSAGE DROP FOREIGN KEY CHANNEL_ID_FK;

ALTER TABLE CHANNEL_STATISTICS DROP FOREIGN KEY CHANNEL_STATS_ID_FK;

DROP TABLE IF EXISTS CHANNEL;

CREATE TABLE CHANNEL
	(ID VARCHAR(255) NOT NULL PRIMARY KEY,
	NAME VARCHAR(40) NOT NULL,
	DESCRIPTION LONGTEXT,
	IS_ENABLED SMALLINT,
	VERSION VARCHAR(40),
	REVISION INTEGER,
	SOURCE_CONNECTOR LONGTEXT,
	DESTINATION_CONNECTORS LONGTEXT,
	PROPERTIES LONGTEXT,
	PREPROCESSING_SCRIPT LONGTEXT,
	POSTPROCESSING_SCRIPT LONGTEXT,
	DEPLOY_SCRIPT LONGTEXT,
	SHUTDOWN_SCRIPT LONGTEXT);

DROP TABLE IF EXISTS CHANNEL_STATISTICS;

CREATE TABLE CHANNEL_STATISTICS
	(SERVER_ID VARCHAR(255) NOT NULL,
	CHANNEL_ID VARCHAR(255) NOT NULL,
	RECEIVED INTEGER,
	FILTERED INTEGER,
	SENT INTEGER,
	ERROR INTEGER,
	QUEUED INTEGER,
	PRIMARY KEY(SERVER_ID, CHANNEL_ID),
	CONSTRAINT CHANNEL_STATS_ID_FK FOREIGN KEY(CHANNEL_ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE);

DROP TABLE ATTACHMENT;

CREATE TABLE ATTACHMENT
    (ID VARCHAR(255) NOT NULL PRIMARY KEY,
     MESSAGE_ID VARCHAR(255) NOT NULL,
     DATA BLOB,
     SIZE INTEGER,
     TYPE VARCHAR(40));

DROP TABLE IF EXISTS MESSAGE;

CREATE TABLE MESSAGE
	(SEQUENCE_ID INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
	ID VARCHAR(255) NOT NULL,
	SERVER_ID VARCHAR(255) NOT NULL,
	CHANNEL_ID VARCHAR(255) NOT NULL,
	SOURCE VARCHAR(255),
	TYPE VARCHAR(255),
	DATE_CREATED TIMESTAMP NOT NULL,
	VERSION VARCHAR(40),
	IS_ENCRYPTED SMALLINT NOT NULL,
	STATUS VARCHAR(40),
	RAW_DATA LONGTEXT,
	RAW_DATA_PROTOCOL VARCHAR(40),
	TRANSFORMED_DATA LONGTEXT,
	TRANSFORMED_DATA_PROTOCOL VARCHAR(40),
	ENCODED_DATA LONGTEXT,
	ENCODED_DATA_PROTOCOL VARCHAR(40),
	CONNECTOR_MAP LONGTEXT,
	CHANNEL_MAP LONGTEXT,
	RESPONSE_MAP LONGTEXT,
	CONNECTOR_NAME VARCHAR(255),
	ERRORS LONGTEXT,
	CORRELATION_ID VARCHAR(255),
    ATTACHMENT SMALLINT,
	UNIQUE (ID),
	CONSTRAINT CHANNEL_ID_FK FOREIGN KEY(CHANNEL_ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE);

CREATE INDEX MESSAGE_INDEX1 ON MESSAGE(CHANNEL_ID, DATE_CREATED);

CREATE INDEX MESSAGE_INDEX2 ON MESSAGE(CHANNEL_ID, DATE_CREATED, CONNECTOR_NAME);

CREATE INDEX MESSAGE_INDEX3 ON MESSAGE(CHANNEL_ID, DATE_CREATED, RAW_DATA_PROTOCOL);

CREATE INDEX MESSAGE_INDEX4 ON MESSAGE(CHANNEL_ID, DATE_CREATED, SOURCE);

CREATE INDEX MESSAGE_INDEX5 ON MESSAGE(CHANNEL_ID, DATE_CREATED, STATUS);

CREATE INDEX MESSAGE_INDEX6 ON MESSAGE(CHANNEL_ID, DATE_CREATED, TYPE);
	
DROP TABLE IF EXISTS SCRIPT;

CREATE TABLE SCRIPT
	(ID VARCHAR(255) NOT NULL PRIMARY KEY,
	SCRIPT LONGTEXT);

DROP TABLE IF EXISTS TEMPLATE;

CREATE TABLE TEMPLATE
	(ID VARCHAR(255) NOT NULL PRIMARY KEY,
	TEMPLATE LONGTEXT);
	
DROP TABLE IF EXISTS PERSON;

CREATE TABLE PERSON
	(ID INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
	USERNAME VARCHAR(40) NOT NULL,
	PASSWORD VARCHAR(40) NOT NULL,
	SALT VARCHAR(40) NOT NULL,
	FULLNAME VARCHAR(255),
	EMAIL VARCHAR(255),
	PHONENUMBER VARCHAR(40),
	DESCRIPTION VARCHAR(255),
	LOGGED_IN SMALLINT NOT NULL);

DROP TABLE IF EXISTS ALERT;

CREATE TABLE ALERT
	(ID VARCHAR(255) NOT NULL PRIMARY KEY,
	NAME VARCHAR(40) NOT NULL,
	IS_ENABLED SMALLINT NOT NULL,
	EXPRESSION LONGTEXT,
	TEMPLATE LONGTEXT);
	
DROP TABLE IF EXISTS CHANNEL_ALERT;

CREATE TABLE CHANNEL_ALERT
	(CHANNEL_ID VARCHAR(255) NOT NULL,
	ALERT_ID VARCHAR(255) NOT NULL,
	CONSTRAINT ALERT_ID_CA_FK FOREIGN KEY(ALERT_ID) REFERENCES ALERT(ID) ON DELETE CASCADE);

DROP TABLE IF EXISTS ALERT_EMAIL;

CREATE TABLE ALERT_EMAIL
	(ALERT_ID VARCHAR(255) NOT NULL,
	EMAIL VARCHAR(255) NOT NULL,
	CONSTRAINT ALERT_ID_AE_FK FOREIGN KEY(ALERT_ID) REFERENCES ALERT(ID) ON DELETE CASCADE);

DROP TABLE IF EXISTS CONFIGURATION;

CREATE TABLE CONFIGURATION
	(ID INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
	DATE_CREATED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	DATA LONGTEXT NOT NULL);
	
DROP TABLE IF EXISTS ENCRYPTION_KEY;

CREATE TABLE ENCRYPTION_KEY
	(DATA LONGTEXT NOT NULL);

INSERT INTO PERSON (USERNAME, PASSWORD, SALT, LOGGED_IN) VALUES('admin', 'NdgB6ojoGb/uFa5amMEyBNG16mE=', 'Np+FZYzu4M0=', 0);
INSERT INTO SCHEMA_INFO (VERSION) VALUES ('1');