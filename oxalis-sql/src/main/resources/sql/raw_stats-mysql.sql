drop table if exists raw_stats;

create table raw_stats(
  id integer auto_increment primary key,
  ap varchar(35) not null,
  tstamp timestamp default current_timestamp,
  direction enum('IN','OUT'),
  sender varchar(35) not null,
  receiver varchar(35) not null,
  doc_type varchar(255) not null,
  profile varchar(255) ,
  channel varchar(255)
);
