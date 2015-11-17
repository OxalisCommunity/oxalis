-- ==========================================
-- ORACLE ADAPTED SQL  (raw_stats-oracle.sql)
-- ==========================================

-- drop trigger oxa_raw_stats_trg;
-- drop table /* if exists */ oxa_raw_stats;
-- drop sequence oxa_raw_stats_seq;

create sequence oxa_raw_stats_seq start with 1 increment by 1 nocache;

create table oxa_raw_stats (
  id integer /* auto_increment */ primary key,
  ap varchar(35) not null,
  tstamp timestamp default current_timestamp,
  direction /* enum */ VARCHAR2(8) CHECK( direction IN ('IN','OUT') ),
  sender varchar(35) not null,
  receiver varchar(35) not null,
  doc_type varchar(255) not null,
  profile varchar(255),
  channel varchar(255),
  messageUID varchar(255)
);

create table oxa_messages (
  id varchar(255) primary key,
  messageId varchar(255) not null,
  documentTypeIdentifier varchar(255) not null,
  profileTypeIdentifier varchar(255),
  sendingAccessPoint varchar(255) not null,
  receivingAccessPoint varchar(255) not null,
  recipientId varchar(255) not null,
  recipientSchemeId varchar(30) not null,
  senderId varchar(255) not null,
  senderSchemeId varchar(30) not null,
  protocol varchar(255),
  userAgent varchar(255),
  userAgentVersion varchar(255),
  sendersTimeStamp TIMESTAMP,
  receivedTimeStamp TIMESTAMP,
  sendingAccessPointPrincipal varchar(255),
  transmissionId varchar(255),
  buildUser varchar(255),
  buildDescription varchar(255),
  buildTimeStamp varchar(255),
  oxalis varchar(255),
  content blob
);

alter table oxa_raw_stats add constraint fk_messageUID foreign key (messageUID) references oxa_messages(id);

CREATE OR REPLACE TRIGGER oxa_raw_stats_trg
  BEFORE INSERT ON oxa_raw_stats FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
      SELECT oxa_raw_stats_seq.NEXTVAL INTO :NEW.id FROM DUAL;
    END IF;
END;