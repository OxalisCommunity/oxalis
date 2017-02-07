/**
 * MICROSOFT SQL SERVER ADAPTED SQL (raw_stats-mssql.sql)
 */
create table raw_stats(
  id integer identity(1,1) primary key,
  ap varchar(35) not null,
  tstamp datetime not null default current_timestamp,
  direction varchar(8),
  sender varchar(35) not null,
  receiver varchar(35) not null,
  doc_type varchar(255) not null,
  profile varchar(255) ,
  channel varchar(255),
  CONSTRAINT unique_direction_stats check(direction in ('IN','OUT')),

);
