-- begin BPM_PROC_DEFINITION
create table BPM_PROC_DEFINITION (
    ID varchar2(32 char),
    CREATE_TS timestamp,
    CREATED_BY varchar2(50 char),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50 char),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50 char),
    --
    NAME varchar2(255 char),
    CODE varchar2(255 char),
    ACT_ID varchar2(255 char),
    ACTIVE char(1),
    MODEL_ID varchar2(32 char),
    DEPLOYMENT_DATE timestamp,
    --
    primary key (ID)
)^
-- end BPM_PROC_DEFINITION
-- begin BPM_PROC_ROLE
create table BPM_PROC_ROLE (
    ID varchar2(32 char),
    CREATE_TS timestamp,
    CREATED_BY varchar2(50 char),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50 char),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50 char),
    --
    NAME varchar2(255 char) not null,
    CODE varchar2(255 char) not null,
    PROC_DEFINITION_ID varchar2(32 char) not null,
    ORDER_ integer,
    --
    primary key (ID)
)^
-- end BPM_PROC_ROLE
-- begin BPM_PROC_INSTANCE
create table BPM_PROC_INSTANCE (
    ID varchar2(32 char),
    CREATE_TS timestamp,
    CREATED_BY varchar2(50 char),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50 char),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50 char),
    --
    ENTITY_NAME varchar2(255 char),
    ENTITY_ID varchar2(32 char),
    STRING_ENTITY_ID varchar2(255 char),
    INT_ENTITY_ID integer,
    LONG_ENTITY_ID number,
    ACTIVE char(1),
    CANCELLED char(1),
    ACT_PROCESS_INSTANCE_ID varchar2(255 char),
    START_DATE timestamp,
    END_DATE timestamp,
    PROC_DEFINITION_ID varchar2(32 char) not null,
    STARTED_BY_ID varchar2(32 char),
    START_COMMENT clob,
    CANCEL_COMMENT clob,
    ENTITY_EDITOR_NAME varchar2(255 char),
    DESCRIPTION clob,
    --
    primary key (ID)
)^
-- end BPM_PROC_INSTANCE
-- begin BPM_PROC_ACTOR
create table BPM_PROC_ACTOR (
    ID varchar2(32 char),
    CREATE_TS timestamp,
    CREATED_BY varchar2(50 char),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50 char),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50 char),
    --
    USER_ID varchar2(32 char),
    PROC_INSTANCE_ID varchar2(32 char) not null,
    PROC_ROLE_ID varchar2(32 char) not null,
    ORDER_ integer,
    --
    primary key (ID)
)^
-- end BPM_PROC_ACTOR
-- begin BPM_PROC_TASK
create table BPM_PROC_TASK (
    ID varchar2(32 char),
    CREATE_TS timestamp,
    CREATED_BY varchar2(50 char),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50 char),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50 char),
    --
    PROC_INSTANCE_ID varchar2(32 char) not null,
    START_DATE timestamp,
    END_DATE timestamp,
    OUTCOME varchar2(255 char),
    PROC_ACTOR_ID varchar2(32 char),
    ACT_EXECUTION_ID varchar2(255 char) not null,
    NAME varchar2(255 char),
    ACT_TASK_ID varchar2(255 char),
    COMMENT_ clob,
    CANCELLED char(1),
    CLAIM_DATE timestamp,
    ACT_PROCESS_DEFINITION_ID varchar2(255 char),
    ACT_TASK_DEFINITION_KEY varchar2(255 char),
    --
    primary key (ID)
)^
-- end BPM_PROC_TASK
-- begin BPM_PROC_ATTACHMENT_TYPE
create table BPM_PROC_ATTACHMENT_TYPE (
    ID varchar2(32 char),
    CREATE_TS timestamp,
    CREATED_BY varchar2(50 char),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50 char),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50 char),
    --
    NAME varchar2(255 char) not null,
    CODE varchar2(255 char),
    --
    primary key (ID)
)^
-- end BPM_PROC_ATTACHMENT_TYPE
-- begin BPM_PROC_ATTACHMENT
create table BPM_PROC_ATTACHMENT (
    ID varchar2(32 char),
    CREATE_TS timestamp,
    CREATED_BY varchar2(50 char),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50 char),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50 char),
    --
    FILE_ID varchar2(32 char),
    TYPE_ID varchar2(32 char),
    COMMENT_ clob,
    PROC_INSTANCE_ID varchar2(32 char),
    PROC_TASK_ID varchar2(32 char),
    AUTHOR_ID varchar2(32 char),
    --
    primary key (ID)
)^
-- end BPM_PROC_ATTACHMENT
-- begin BPM_PROC_MODEL
create table BPM_PROC_MODEL (
    ID varchar2(32 char),
    CREATE_TS timestamp,
    CREATED_BY varchar2(50 char),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50 char),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50 char),
    --
    NAME varchar2(255 char) not null,
    ACT_MODEL_ID varchar2(255 char),
    DESCRIPTION clob,
    --
    primary key (ID)
)^
-- end BPM_PROC_MODEL
-- begin BPM_PROC_TASK_USER_LINK
create table BPM_PROC_TASK_USER_LINK (
    PROC_TASK_ID varchar2(32 char),
    USER_ID varchar2(32 char),
    primary key (PROC_TASK_ID, USER_ID)
)^
-- end BPM_PROC_TASK_USER_LINK
-- begin BPM_STENCIL_SET
create table BPM_STENCIL_SET (
    ID varchar2(32 char),
    CREATE_TS timestamp,
    CREATED_BY varchar2(50 char),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50 char),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50 char),
    --
    NAME varchar2(255 char) not null,
    JSON_DATA clob not null,
    --
    primary key (ID)
)^
-- end BPM_STENCIL_SET
