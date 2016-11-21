
/** To create TEST database : Create a new database called oxalis_test and run this script */
/** To create PROD database : Create a new database called oxalis and run this script */

drop table if exists outbound_message_queue_error;
drop table if exists outbound_message_queue;
drop table if exists message;
drop table if exists account_receiver;
drop table if exists account_role;
drop table if exists account;
drop table if exists customer;
drop table if exists raw_stats;

drop user if exists skrue;

/* Creates the user for the application itself, not meant to be used for login by customers etc. */
create user skrue with PASSWORD='Vable2016!';

/** Customer paying for the connection */
CREATE TABLE customer (
  id int NOT NULL identity(1,1) ,
  name varchar(128) NOT NULL ,
  external_ref int DEFAULT NULL ,
  created_ts datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  address1 varchar(254) DEFAULT NULL,
  address2 varchar(254) DEFAULT NULL,
  zip varchar(8) DEFAULT NULL,
  city varchar(64) DEFAULT NULL,
  country varchar(64) DEFAULT NULL,
  contact_person varchar(64) DEFAULT NULL,
  contact_email varchar(64) DEFAULT NULL,
  contact_phone varchar(64) DEFAULT NULL,
  org_no varchar(16) DEFAULT NULL,
  PRIMARY KEY (id)
) ;
grant SELECT , INSERT , UPDATE , DELETE on customer  to skrue;

/** Each customer can have multiple accounts, details here is used for JAAS authentication etc */
CREATE TABLE account (
  id int NOT NULL IDENTITY(1,1) ,
  customer_id int NOT NULL ,
  name varchar(128) NOT NULL ,
  username varchar(128) NOT NULL ,
  password varchar(128) DEFAULT NULL ,
  created_ts DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  validate_upload tinyint NOT NULL DEFAULT '0' ,
  send_notification tinyint NOT NULL DEFAULT '1' ,
  PRIMARY KEY (id),
  constraint account_unique_name  UNIQUE(username),
  CONSTRAINT account_ibfk_1 FOREIGN KEY (customer_id) REFERENCES customer (id)
) ;
grant SELECT , INSERT , UPDATE , DELETE on account  to skrue;

/** The JAAS security roles for the accounts */
CREATE TABLE account_role (
  username varchar(128) NOT NULL,
  role_name  varchar(16) NOT NULL DEFAULT 'client',
  PRIMARY KEY (username,role_name),
  constraint unique_roles check(role_name in ('client','admin')),
  CONSTRAINT account_role_ibfk_1 FOREIGN KEY (username) REFERENCES account (username) ON DELETE CASCADE
) ;
grant SELECT , INSERT , UPDATE , DELETE on account_role to skrue;

/** Which PEPPOL participantid belong to which account */
CREATE TABLE account_receiver (
  id int NOT NULL IDENTITY ,
  participant_id varchar(32) DEFAULT NULL ,
  account_id int NOT NULL ,
  PRIMARY KEY (id),
  constraint account_receiver_unique_participant_id UNIQUE (participant_id),
  CONSTRAINT account_receiver_ibfk_1 FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE CASCADE
) ;
grant SELECT , INSERT , UPDATE , DELETE on account_receiver to skrue;

/** Holds the message metadata and references to the payload and associated evidence files */
CREATE TABLE message (
  msg_no int NOT NULL IDENTITY,
  account_id int DEFAULT NULL ,
  direction varchar(3) NOT NULL,
  received DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  delivered datetime DEFAULT NULL ,
  sender varchar(32) NOT NULL ,
  receiver varchar(32) NOT NULL ,
  channel varchar(128) NOT NULL ,
  message_uuid varchar(36) not NULL ,
  document_id varchar(256) NOT NULL ,
  process_id varchar(128) DEFAULT NULL ,
  remote_host varchar(128) DEFAULT NULL ,
  ap_name varchar(128) DEFAULT NULL  ,
  payload_url varchar(256) not null ,
  generic_evidence_url varchar(256) default null ,
  native_evidence_url varchar(256) default null ,
  PRIMARY KEY (msg_no),
  constraint unique_message_uuid UNIQUE (direction, message_uuid),
  CONSTRAINT direction_enum check(direction in ('IN','OUT')),
  CONSTRAINT message_ibfk_1 FOREIGN KEY (account_id) REFERENCES account (id)
) ;
grant SELECT , INSERT , UPDATE , DELETE on message to skrue;


/** The oubound queue implementation */
CREATE TABLE outbound_message_queue (
  id int NOT NULL IDENTITY ,
  msg_no int DEFAULT NULL ,
  state varchar(16) DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT unique_state check(state in ('QUEUED','IN_PROGRESS','EXTERNAL','OK','AOD','CBU','CBO')),
  CONSTRAINT outbound_message_queue_ibfk_1 FOREIGN KEY (msg_no) REFERENCES message (msg_no) ON DELETE CASCADE
) ;
grant SELECT , INSERT , UPDATE , DELETE on outbound_message_queue to skrue;

/** The oubound error queue implementation */
CREATE TABLE outbound_message_queue_error (
  id int NOT NULL IDENTITY ,
  queue_id int NOT NULL ,
  message varchar(256) DEFAULT NULL,
  details text,
  stacktrace text,
  create_dt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT outbound_message_queue_error_ibfk_1 FOREIGN KEY (queue_id) REFERENCES outbound_message_queue (id) ON DELETE CASCADE
) ;
grant SELECT , INSERT , UPDATE , DELETE on outbound_message_queue_error to skrue;


/**
 * Creates the table to hold the raw statistics, which everybody needs.
 */
create table raw_stats(
  id integer identity(1,1) primary key,
  ap varchar(35) not null,
  tstamp DATETIME not null default current_timestamp,
  direction varchar(8),
  sender varchar(35) not null,
  receiver varchar(35) not null,
  doc_type varchar(255) not null,
  profile varchar(255) ,
  channel varchar(255),
  CONSTRAINT unique_direction_stats check(direction in ('IN','OUT')),

);
grant SELECT , INSERT , UPDATE , DELETE on raw_stats to skrue;

/* ============= INSERT ONE DEFAULT CUSTOMER WITH A SINGE ACCOUNT AND CLIENT ROLE =============== */
insert into customer (name, external_ref, org_no) values ('SendRegning AS', 279, '976098897');

/* Salted password is "ringo1" in cleartext. Salted and hashed password was created with $TOMCAT_HOME/bin/digest.sh  */
/* scope_identity() returns the primary key value assigned to the identity column of the previous insert statement */
insert into
  account (customer_id, name, username, password)
  values (scope_identity(), 'SendRegning User', 'sr',
          'd0fc73ba7d0e6becc0fbd49c65493b6eb99912ba119a9637117ac7f636475d7e$20000$988a1f4e8c2162a4fc31814e2b3ba89f849e069d');

insert into account_role values ('sr', 'client');
insert into account_role values ('sr', 'admin');
insert into account_receiver (participant_id, account_id) values ('9908:976098897', 1);
