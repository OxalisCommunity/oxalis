SELECT
  ap,
  'OUT' direction,
  date_format(tstamp,'%Y-%m-%d') period,
  sender ppid,
  doc_type,
  profile,
  channel,
  COUNT(*) count
FROM
  raw_stats
WHERE
  direction = 'OUT'
  and tstamp between '2013-02-23 12' and '2013-02-24 23'
GROUP BY 1,2, 3, 4, 5,6
union
SELECT
  ap,
  'IN' direction,
  date_format(tstamp,'%Y-%m-%d') period,
  receiver ppid,
  doc_type,
  profile,
  channel,
  COUNT(*) count
FROM
  raw_stats
WHERE
  direction = 'IN'
  and tstamp between '2013-01-01' and '2013-02-28 12'

GROUP BY 1,2, 3, 4, 5,6

order by period, ap
;