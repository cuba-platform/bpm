CREATE PROCEDURE cuba_drop_column_if_exists() BEGIN
IF EXISTS ( SELECT * FROM information_schema.columns WHERE table_name = 'BPM_PROC_INSTANCE' AND column_name = 'INT_ENTITY_ID' AND table_schema = DATABASE() )
then
    ALTER TABLE BPM_PROC_INSTANCE DROP COLUMN INT_ENTITY_ID;
end if;
end;
CALL cuba_drop_column_if_exists();
DROP PROCEDURE cuba_drop_column_if_exists;    