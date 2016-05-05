-- begin BPM_STENCIL_SET
create table BPM_STENCIL_SET (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255) not null,
    JSON_DATA text not null,
    --
    primary key (ID)
)^
-- end BPM_STENCIL_SET
