drop table if exists time_dimension;
drop table if exists ap_dimension;
drop table if exists ppid_dimension;
drop table if exists document_dimension;
drop table if exists profile_dimension;
drop table if exists channel_dimension;

drop table if exists message_fact;

create table if not exists time_dimension  (
  time_id integer auto_increment primary key,
  day_of_week integer,
  day_of_month integer,
  day_of_year integer,
  month integer,
  year integer,
  holiday integer,
  weekend integer
);

create table if not exists ap_dimension (
  ap_id integer auto_increment primary key comment 'auto gen primary key',
  ap_code varchar(32) comment 'Unique access point code',
  ap_name varchar(128) comment 'Name of access point',
  ap_country varchar(64) comment 'ISO 3166-1 alpha2 code of country',
  ap_reg_auth varchar (128) comment 'name of regional authority'
);

create table if not exists ppid_dimension (
  ppid_id integer auto_increment primary key comment 'Surrogate primary key',
  ppid varchar (64) not null comment 'Peppol participant id'
);


create table if not exists document_dimension (
  document_id integer auto_increment primary key comment 'Surrogate primary key',
  document_type varchar (255)  comment 'PEPPOL document type identifier',
  localname varchar(32) comment 'Local name of document type',
  root_name_space varchar(128) comment 'Root name space',
  customization varchar (128) comment 'CEN/BII customization id',
  version  varchar (16)
);

create table if not exists profile_dimension (
  profile_id integer auto_increment primary key comment 'Surrogate primary key',
  profile varchar(255)
) ;


create table if not exists channel_dimension (
  channel_id integer auto_increment primary key comment 'Surrogate primary key',
  channel varchar(128)
) ;


create table if not exists message_fact (
  id integer auto_increment primary key,
  time_id integer not null,
  ap_id integer not null,
  ppid_id integer not null,
  document_id integer not null,
  profile_id integer null,
  channel_id integer null,
  direction enum ('IN','OUT') not null,
  counter integer not null,
  constraint fk_msg_2_time foreign key (time_id) references time_dimension(time_id) on delete cascade,
  constraint fk_msg_2_ap foreign key (ap_id) references ap_dimension(ap_id) on delete cascade ,
  constraint fk_msg_2_ppid foreign key (ppid_id) references ppid_dimension(ppid_id) on delete cascade ,
  constraint fk_msg_2_document foreign key (document_id) references document_dimension(document_id) on delete cascade,
  constraint fk_msg_2_profile foreign key (profile_id) references profile_dimension(profile_id) on delete cascade ,
  constraint fk_msg_2_channel foreign key (channel_id) references channel_dimension(channel_id) on delete cascade
);