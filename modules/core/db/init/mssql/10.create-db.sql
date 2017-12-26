-- begin BPM_PROC_DEFINITION
create table BPM_PROC_DEFINITION (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    --
    NAME varchar(255),
    CODE varchar(255),
    ACT_ID varchar(255),
    ACTIVE tinyint,
    MODEL_ID uniqueidentifier,
    DEPLOYMENT_DATE datetime,
    --
    primary key nonclustered (ID)
)^
-- end BPM_PROC_DEFINITION
-- begin BPM_PROC_ROLE
create table BPM_PROC_ROLE (
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
    CODE varchar(255) not null,
    PROC_DEFINITION_ID uniqueidentifier not null,
    ORDER_ integer,
    --
    primary key nonclustered (ID)
)^
-- end BPM_PROC_ROLE
-- begin BPM_PROC_INSTANCE
create table BPM_PROC_INSTANCE (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    --
    ENTITY_NAME varchar(255),
    ENTITY_ID uniqueidentifier,
    STRING_ENTITY_ID varchar(255),
    INT_ENTITY_ID integer,
    LONG_ENTITY_ID bigint,
    ACTIVE tinyint,
    CANCELLED tinyint,
    ACT_PROCESS_INSTANCE_ID varchar(255),
    START_DATE datetime,
    END_DATE datetime,
    PROC_DEFINITION_ID uniqueidentifier not null,
    STARTED_BY_ID uniqueidentifier,
    START_COMMENT varchar(max),
    CANCEL_COMMENT varchar(max),
    ENTITY_EDITOR_NAME varchar(255),
    DESCRIPTION varchar(max),
    --
    primary key nonclustered (ID)
)^
-- end BPM_PROC_INSTANCE
-- begin BPM_PROC_ACTOR
create table BPM_PROC_ACTOR (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    --
    USER_ID uniqueidentifier,
    PROC_INSTANCE_ID uniqueidentifier not null,
    PROC_ROLE_ID uniqueidentifier not null,
    ORDER_ integer,
    --
    primary key nonclustered (ID)
)^
-- end BPM_PROC_ACTOR
-- begin BPM_PROC_TASK
create table BPM_PROC_TASK (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    --
    PROC_INSTANCE_ID uniqueidentifier not null,
    START_DATE datetime,
    END_DATE datetime,
    OUTCOME varchar(255),
    PROC_ACTOR_ID uniqueidentifier,
    ACT_EXECUTION_ID varchar(255) not null,
    NAME varchar(255),
    ACT_TASK_ID varchar(255),
    COMMENT_ varchar(max),
    CANCELLED tinyint,
    CLAIM_DATE datetime,
    ACT_PROCESS_DEFINITION_ID varchar(255),
    ACT_TASK_DEFINITION_KEY varchar(255),
    --
    primary key nonclustered (ID)
)^
-- end BPM_PROC_TASK
-- begin BPM_PROC_ATTACHMENT_TYPE
create table BPM_PROC_ATTACHMENT_TYPE (
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
    CODE varchar(255),
    --
    primary key nonclustered (ID)
)^
-- end BPM_PROC_ATTACHMENT_TYPE
-- begin BPM_PROC_ATTACHMENT
create table BPM_PROC_ATTACHMENT (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    --
    FILE_ID uniqueidentifier,
    TYPE_ID uniqueidentifier,
    COMMENT_ varchar(max),
    PROC_INSTANCE_ID uniqueidentifier,
    PROC_TASK_ID uniqueidentifier,
    AUTHOR_ID uniqueidentifier,
    --
    primary key nonclustered (ID)
)^
-- end BPM_PROC_ATTACHMENT
-- begin BPM_PROC_MODEL
create table BPM_PROC_MODEL (
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
    ACT_MODEL_ID varchar(255),
    DESCRIPTION varchar(max),
    --
    primary key nonclustered (ID)
)^
-- end BPM_PROC_MODEL
-- begin BPM_PROC_TASK_USER_LINK
create table BPM_PROC_TASK_USER_LINK (
    PROC_TASK_ID uniqueidentifier,
    USER_ID uniqueidentifier,
    primary key (PROC_TASK_ID, USER_ID)
)^
-- end BPM_PROC_TASK_USER_LINK
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