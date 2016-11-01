-- ==========================================
-- ORACLE ADAPTED SQL  (raw_stats-oracle.sql)
-- ==========================================

-- drop trigger raw_stats_trg;
-- drop table /* if exists */ raw_stats;
-- drop sequence raw_stats_seq;

create sequence raw_stats_seq start with 1 increment by 1 nocache;

create table raw_stats (
  id integer /* auto_increment */ primary key,
  ap varchar(35) not null,
  tstamp timestamp default current_timestamp,
  direction /* enum */ VARCHAR2(8) CHECK( direction IN ('IN','OUT') ),
  sender varchar(35) not null,
  receiver varchar(35) not null,
  doc_type varchar(255) not null,
  profile varchar(255),
  channel varchar(255)
);

CREATE OR REPLACE TRIGGER raw_stats_trg
  BEFORE INSERT ON raw_stats FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
      SELECT raw_stats_seq.NEXTVAL INTO :NEW.id FROM DUAL;
    END IF;
END;

-- desc raw_stats;
-- insert into raw_stats (ap, direction, sender, receiver, doc_type) values ('ap', 'OUT', 'sender', 'receiver', 'invoice');












