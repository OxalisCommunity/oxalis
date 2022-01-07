
drop table if exists raw_stats;


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
  CONSTRAINT unique_direction_stats check(direction in ('IN','OUT'))

);

