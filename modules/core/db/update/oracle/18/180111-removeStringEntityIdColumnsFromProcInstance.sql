BEGIN
   EXECUTE IMMEDIATE 'alter table BPM_PROC_INSTANCE drop column STRING_ENTITY_ID';
EXCEPTION
   WHEN OTHERS THEN
    NULL;
END;