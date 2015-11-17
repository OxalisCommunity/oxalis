/**
 * Creates the table to hold the raw statistics, which everybody needs.
 */
create table if not exists oxa_raw_stats(
  id integer auto_increment primary key,
  ap varchar(35) not null,
  tstamp timestamp default current_timestamp,
  direction enum('IN','OUT'),
  sender varchar(35) not null,
  receiver varchar(35) not null,
  doc_type varchar(255) not null,
  profile varchar(255) ,
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
  sendersTimeStamp timestamp,
  receivedTimeStamp timestamp,
  sendingAccessPointPrincipal varchar(255),
  transmissionId varchar(255),
  buildUser varchar(255),
  buildDescription varchar(255),
  buildTimeStamp varchar(255),
  oxalis varchar(255),
  content longtext
);

alter table oxa_raw_stats add constraint fk_messageUID foreign key (messageUID) references oxa_messages(id);