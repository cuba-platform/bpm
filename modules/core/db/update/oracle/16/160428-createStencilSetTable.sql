-- begin BPM_STENCIL_SET
create table BPM_STENCIL_SET (
    ID varchar2(32),
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    --
    NAME varchar2(255) not null,
    JSON_DATA clob not null,
    --
    primary key (ID)
)^
-- end BPM_STENCIL_SET