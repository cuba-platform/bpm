-- begin BPM_PROC_DEFINITION
create table BPM_PROC_DEFINITION (
    ID uuid,
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
    MODEL_ID uuid,
    DEPLOYMENT_DATE timestamp,
    --
    primary key (ID)
)^-- end BPM_PROC_DEFINITION
-- begin BPM_PROC_ROLE
create table BPM_PROC_ROLE (
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
    CODE varchar(255) not null,
    PROC_DEFINITION_ID uuid not null,
    ORDER_ integer,
    --
    primary key (ID)
)^-- end BPM_PROC_ROLE

-- begin BPM_PROC_INSTANCE
create table BPM_PROC_INSTANCE (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    ENTITY_NAME varchar(255),
    ENTITY_ID uuid,
    STRING_ENTITY_ID varchar(255),
    INT_ENTITY_ID integer,
    LONG_ENTITY_ID bigint,
    ACTIVE boolean,
    CANCELLED boolean,
    ACT_PROCESS_INSTANCE_ID varchar(255),
    START_DATE timestamp,
    END_DATE timestamp,
    PROC_DEFINITION_ID uuid not null,
    STARTED_BY_ID uuid,
    START_COMMENT text,
    CANCEL_COMMENT text,
    ENTITY_EDITOR_NAME varchar(255),
    DESCRIPTION text,
    --
    primary key (ID)
)^-- end BPM_PROC_INSTANCE
-- begin BPM_PROC_ACTOR
create table BPM_PROC_ACTOR (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    USER_ID uuid,
    PROC_INSTANCE_ID uuid not null,
    PROC_ROLE_ID uuid not null,
    ORDER_ integer,
    --
    primary key (ID)
)^
-- end BPM_PROC_ACTOR

-- begin BPM_PROC_ATTACHMENT_TYPE
create table BPM_PROC_ATTACHMENT_TYPE (
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
    CODE varchar(255),
    --
    primary key (ID)
)^
-- end BPM_PROC_ATTACHMENT_TYPE
-- begin BPM_PROC_ATTACHMENT
create table BPM_PROC_ATTACHMENT (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    FILE_ID uuid,
    TYPE_ID uuid,
    COMMENT_ text,
    PROC_INSTANCE_ID uuid,
    PROC_TASK_ID uuid,
    AUTHOR_ID uuid,
    --
    primary key (ID)
)^
-- end BPM_PROC_ATTACHMENT
-- begin BPM_PROC_TASK
create table BPM_PROC_TASK (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    PROC_INSTANCE_ID uuid not null,
    START_DATE timestamp,
    END_DATE timestamp,
    OUTCOME varchar(255),
    PROC_ACTOR_ID uuid,
    ACT_EXECUTION_ID varchar(255) not null,
    NAME varchar(255),
    ACT_TASK_ID varchar(255),
    COMMENT_ text,
    CANCELLED boolean,
    CLAIM_DATE timestamp,
    ACT_PROCESS_DEFINITION_ID varchar(255),
    ACT_TASK_DEFINITION_KEY varchar(255),
    --
    primary key (ID)
)^-- end BPM_PROC_TASK
-- begin BPM_PROC_TASK_USER_LINK
create table BPM_PROC_TASK_USER_LINK (
    PROC_TASK_ID uuid,
    USER_ID uuid,
    primary key (PROC_TASK_ID, USER_ID)
)^
-- end BPM_PROC_TASK_USER_LINK
-- begin BPM_PROC_MODEL
create table BPM_PROC_MODEL (
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
    ACT_MODEL_ID varchar(255),
    DESCRIPTION text,
    --
    primary key (ID)
)^
-- end BPM_PROC_MODEL
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
