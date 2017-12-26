-- begin BPM_PROC_DEFINITION
create table BPM_PROC_DEFINITION (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255),
    CODE varchar(255),
    ACT_ID varchar(255),
    ACTIVE boolean,
    MODEL_ID varchar(36),
    DEPLOYMENT_DATE timestamp,
    --
    primary key (ID)
)^
-- end BPM_PROC_DEFINITION
-- begin BPM_PROC_ROLE
create table BPM_PROC_ROLE (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255) not null,
    CODE varchar(255) not null,
    PROC_DEFINITION_ID varchar(36) not null,
    ORDER_ integer,
    --
    primary key (ID)
)^
-- end BPM_PROC_ROLE
-- begin BPM_PROC_INSTANCE
create table BPM_PROC_INSTANCE (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    ENTITY_NAME varchar(255),
    ENTITY_ID varchar(36),
    STRING_ENTITY_ID varchar(255),
    INT_ENTITY_ID integer,
    LONG_ENTITY_ID bigint,
    ACTIVE boolean,
    CANCELLED boolean,
    ACT_PROCESS_INSTANCE_ID varchar(255),
    START_DATE timestamp,
    END_DATE timestamp,
    PROC_DEFINITION_ID varchar(36) not null,
    STARTED_BY_ID varchar(36),
    START_COMMENT longvarchar,
    CANCEL_COMMENT longvarchar,
    ENTITY_EDITOR_NAME varchar(255),
    DESCRIPTION longvarchar,
    --
    primary key (ID)
)^
-- end BPM_PROC_INSTANCE
-- begin BPM_PROC_ACTOR
create table BPM_PROC_ACTOR (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    USER_ID varchar(36),
    PROC_INSTANCE_ID varchar(36) not null,
    PROC_ROLE_ID varchar(36) not null,
    ORDER_ integer,
    --
    primary key (ID)
)^
-- end BPM_PROC_ACTOR
-- begin BPM_PROC_TASK
create table BPM_PROC_TASK (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    PROC_INSTANCE_ID varchar(36) not null,
    START_DATE timestamp,
    END_DATE timestamp,
    OUTCOME varchar(255),
    PROC_ACTOR_ID varchar(36),
    ACT_EXECUTION_ID varchar(255) not null,
    NAME varchar(255),
    ACT_TASK_ID varchar(255),
    COMMENT_ longvarchar,
    CANCELLED boolean,
    CLAIM_DATE timestamp,
    ACT_PROCESS_DEFINITION_ID varchar(255),
    ACT_TASK_DEFINITION_KEY varchar(255),
    --
    primary key (ID)
)^
-- end BPM_PROC_TASK
-- begin BPM_PROC_ATTACHMENT_TYPE
create table BPM_PROC_ATTACHMENT_TYPE (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255) not null,
    CODE varchar(255),
    --
    primary key (ID)
)^
-- end BPM_PROC_ATTACHMENT_TYPE
-- begin BPM_PROC_ATTACHMENT
create table BPM_PROC_ATTACHMENT (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    FILE_ID varchar(36),
    TYPE_ID varchar(36),
    COMMENT_ longvarchar,
    PROC_INSTANCE_ID varchar(36),
    PROC_TASK_ID varchar(36),
    AUTHOR_ID varchar(36),
    --
    primary key (ID)
)^
-- end BPM_PROC_ATTACHMENT
-- begin BPM_PROC_MODEL
create table BPM_PROC_MODEL (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255) not null,
    ACT_MODEL_ID varchar(255),
    DESCRIPTION longvarchar,
    --
    primary key (ID)
)^
-- end BPM_PROC_MODEL
-- begin BPM_PROC_TASK_USER_LINK
create table BPM_PROC_TASK_USER_LINK (
    PROC_TASK_ID varchar(36) not null,
    USER_ID varchar(36) not null,
    primary key (PROC_TASK_ID, USER_ID)
)^
-- end BPM_PROC_TASK_USER_LINK
-- begin BPM_STENCIL_SET
create table BPM_STENCIL_SET (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255) not null,
    JSON_DATA longvarchar not null,
    --
    primary key (ID)
)^
-- end BPM_STENCIL_SET
