if exists (select *  FROM sys.objects o INNER JOIN sys.columns c ON o.object_id = c.object_id WHERE o.name = 'BPM_PROC_INSTANCE' AND c.name = 'LONG_ENTITY_ID')
    ALTER TABLE bpm_proc_instance DROP COLUMN long_entity_id^
