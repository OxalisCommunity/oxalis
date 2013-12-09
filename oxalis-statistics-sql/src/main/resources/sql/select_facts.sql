/**
 * Selects all aggregate message facts from the database within a given date and time range
 */
SELECT
    time_dimension.datum,
    time_dimension.year,
    time_dimension.month,
    time_dimension.day,
    time_dimension.hour,
    ap_dimension.ap_code,
    ppid_dimension.ppid,
    document_dimension.document_type,
    document_dimension.localname,
    document_dimension.root_name_space,
    document_dimension.customization,
    document_dimension.version,
    profile_dimension.profile,
    channel_dimension.channel,
    direction,
    counter
FROM
    message_fact AS fact
JOIN
    time_dimension
ON
    fact.time_id = time_dimension.time_id
JOIN
    ap_dimension
ON
    fact.ap_id = ap_dimension.ap_id
JOIN
    ppid_dimension
ON
    fact.ppid_id = ppid_dimension.ppid_id
JOIN
    document_dimension
ON
    fact.document_id = document_dimension.document_id
/* Profile or process might become optional in the future, so use outer join */
left outer JOIN
    profile_dimension
ON
    fact.profile_id = profile_dimension.profile_id
/* Channel is very much optional so use outer join */
 left outer JOIN
    channel_dimension
ON
    fact.channel_id = channel_dimension.channel_id