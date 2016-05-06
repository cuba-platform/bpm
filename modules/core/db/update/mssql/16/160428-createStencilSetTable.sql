-- begin BPM_STENCIL_SET
create table BPM_STENCIL_SET (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    --
    NAME varchar(255) not null,
    JSON_DATA varchar(max) not null,
    --
    primary key (ID)
)^
-- end BPM_STENCIL_SET