-- begin BPM_STENCIL_SET
create table BPM_STENCIL_SET (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    --
    NAME varchar(255) not null,
    JSON_DATA longtext not null,
    --
    primary key (ID)
)^
-- end BPM_STENCIL_SET