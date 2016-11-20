
drop table if exists outbound_message_queue_error;
drop table if exists outbound_message_queue;
drop table if exists message;
drop table if exists account_receiver;
drop table if exists account_role;
drop table if exists account;
drop table if exists customer;
drop table if exists raw_stats;

drop user if exists skrue;

/** To create TEST database : Create a new database called oxalis_test and run this script */
/** To create PROD database : Create a new database called oxalis and run this script */

/** Customer paying for the connection */
CREATE TABLE `customer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL COMMENT 'Name of paying customer',
  `external_ref` int(11) DEFAULT NULL COMMENT 'External system identification',
  `created_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `address1` varchar(254) DEFAULT NULL,
  `address2` varchar(254) DEFAULT NULL,
  `zip` varchar(8) DEFAULT NULL,
  `city` varchar(64) DEFAULT NULL,
  `country` varchar(64) DEFAULT NULL,
  `contact_person` varchar(64) DEFAULT NULL COMMENT 'Contact person name',
  `contact_email` varchar(64) DEFAULT NULL,
  `contact_phone` varchar(64) DEFAULT NULL,
  `org_no` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ;

/** Each customer can have multiple accounts, details here is used for JAAS authentication etc */
CREATE TABLE `account` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `customer_id` int(11) NOT NULL COMMENT 'FK to customer',
  `name` varchar(128) NOT NULL COMMENT 'Name of account',
  `username` varchar(128) NOT NULL COMMENT 'Username used for logging in',
  `password` varchar(128) DEFAULT NULL COMMENT 'Password used for Basic Authentication',
  `created_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `validate_upload` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Should invoices be validated',
  `send_notification` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Should email notifications be sent',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  CONSTRAINT `account_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`)
) ;

/** The JAAS security roles for the accounts */
CREATE TABLE `account_role` (
  `username` varchar(128) NOT NULL,
  `role_name`  varchar(16) NOT NULL DEFAULT 'client',
  PRIMARY KEY (`username`,`role_name`),
  constraint unique_roles check(role_name in ('client','admin')),
  CONSTRAINT `account_role_ibfk_1` FOREIGN KEY (`username`) REFERENCES `account` (`username`) ON DELETE CASCADE
) ;

/** Which PEPPOL participantid belong to which account */
CREATE TABLE `account_receiver` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `participant_id` varchar(32) DEFAULT NULL COMMENT 'PEPPOL prefix and orgno',
  `account_id` int(11) NOT NULL COMMENT 'FK to account',
  PRIMARY KEY (`id`),
  UNIQUE KEY `participant_id` (`participant_id`),
  CONSTRAINT `account_receiver_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE
) ;

/** Holds the message metadata and references to the payload and associated evidence files */
CREATE TABLE `message` (
  `msg_no` int(11) NOT NULL AUTO_INCREMENT,
  `account_id` int(11) DEFAULT NULL COMMENT 'account sending or receiving a xmlMessage',
  `direction` varchar(3) NOT NULL,
  `received` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'TS when received by AP',
  `delivered` datetime DEFAULT NULL COMMENT 'TS when delivered to destination',
  `sender` varchar(32) NOT NULL COMMENT 'PPID of sender',
  `receiver` varchar(32) NOT NULL COMMENT 'PPID of receiver',
  `channel` varchar(128) NOT NULL comment 'Channel in which message was received',
  `message_uuid` varchar(36) not NULL COMMENT 'UUID assigned when message is received by us, not matter what the source is.',
  `document_id` varchar(256) NOT NULL COMMENT 'document type id',
  `process_id` varchar(128) DEFAULT NULL COMMENT 'Process type id',
  `remote_host` varchar(128) DEFAULT NULL COMMENT 'Senders AS2-From header',
  `ap_name` varchar(128) DEFAULT NULL comment 'CN of certificate of sending access point' ,
  `payload_url` varchar(256) not null comment 'The URL of the message payload, the xml document',
  `generic_evidence_url` varchar(256) default null comment 'URL of the transport evidence (REM evidence)',
  `native_evidence_url` varchar(256) default null comment 'URL of the native receipt i.e. AS2 MDN',
  PRIMARY KEY (`msg_no`),
  /* A message sent and received at the same access point, will have two entries having different transfer direction */
  constraint unique_message_uuid UNIQUE (direction, `message_uuid`),
  CONSTRAINT unique_direction check(direction in ('IN','OUT')),
  CONSTRAINT `message_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
) ;

/** The oubound queue implementation */
CREATE TABLE `outbound_message_queue` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `msg_no` int(11) DEFAULT NULL COMMENT 'FK to message table',
  `state` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT unique_state check(state in ('QUEUED','IN_PROGRESS','EXTERNAL','OK','AOD','CBU','CBO')),
  CONSTRAINT `outbound_message_queue_ibfk_1` FOREIGN KEY (`msg_no`) REFERENCES `message` (`msg_no`) ON DELETE CASCADE
) ;

/** The oubound error queue implementation */
CREATE TABLE `outbound_message_queue_error` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `queue_id` int(11) NOT NULL COMMENT 'FK to queue table',
  `message` varchar(256) DEFAULT NULL,
  `details` text,
  `stacktrace` text,
  `create_dt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `outbound_message_queue_error_ibfk_1` FOREIGN KEY (`queue_id`) REFERENCES `outbound_message_queue` (`id`) ON DELETE CASCADE
) ;

/**
 * Creates the table to hold the raw statistics, which everybody needs.
 */
create table if not exists raw_stats(
  id integer auto_increment primary key,
  ap varchar(35) not null,
  tstamp timestamp not null default current_timestamp,
  direction varchar(8),
  sender varchar(35) not null,
  receiver varchar(35) not null,
  doc_type varchar(255) not null,
  profile varchar(255) ,
  channel varchar(255),
  CONSTRAINT unique_direction_stats check(direction in ('IN','OUT')),

);

/* ============= INSERT ONE DEFAULT CUSTOMER WITH A SINGE ACCOUNT AND CLIENT ROLE =============== */

/* Creates the user for the application itself, not meant to be used for login by customers etc. */
create user skrue PASSWORD 'vable';
grant all on outbound_message_queue_error, outbound_message_queue, message,
  account_receiver, account_role, account,customer, raw_stats to skrue;

insert into customer (id, name, external_ref, org_no) values (1, 'SendRegning AS', 279, '976098897');

/* Salted password is "ringo1" in cleartext. Salted and hashed password was created with $TOMCAT_HOME/bin/digest.sh  */
insert into account (id, customer_id, name, username, password) values (1, 1, 'SendRegning User', 'sr', 'd0fc73ba7d0e6becc0fbd49c65493b6eb99912ba119a9637117ac7f636475d7e$20000$988a1f4e8c2162a4fc31814e2b3ba89f849e069d');
insert into account_role values ('sr', 'client');
insert into account_role values ('sr', 'admin');
insert into account_receiver (participant_id, account_id) values ('9908:976098897', 1);
