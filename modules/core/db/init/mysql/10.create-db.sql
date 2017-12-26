-- begin BPM_PROC_DEFINITION
create table BPM_PROC_DEFINITION (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    --
    NAME varchar(255),
    CODE varchar(255),
    ACT_ID varchar(255),
    ACTIVE boolean,
    MODEL_ID varchar(32),
    DEPLOYMENT_DATE datetime(3),
    --
    primary key (ID)
)^-- end BPM_PROC_DEFINITION
-- begin BPM_PROC_ROLE
create table BPM_PROC_ROLE (
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
    CODE varchar(255) not null,
    PROC_DEFINITION_ID varchar(32) not null,
    ORDER_ integer,
    --
    primary key (ID)
)^-- end BPM_PROC_ROLE

-- begin BPM_PROC_INSTANCE
create table BPM_PROC_INSTANCE (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    --
    ENTITY_NAME varchar(255),
    ENTITY_ID varchar(32),
    STRING_ENTITY_ID varchar(255),
    INT_ENTITY_ID integer,
    LONG_ENTITY_ID bigint,
    ACTIVE boolean,
    CANCELLED boolean,
    ACT_PROCESS_INSTANCE_ID varchar(255),
    START_DATE datetime(3),
    END_DATE datetime(3),
    PROC_DEFINITION_ID varchar(32) not null,
    STARTED_BY_ID varchar(32),
    START_COMMENT text,
    CANCEL_COMMENT text,
    ENTITY_EDITOR_NAME varchar(255),
    DESCRIPTION text,
    --
    primary key (ID)
)^-- end BPM_PROC_INSTANCE
-- begin BPM_PROC_ACTOR
create table BPM_PROC_ACTOR (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    --
    USER_ID varchar(32),
    PROC_INSTANCE_ID varchar(32) not null,
    PROC_ROLE_ID varchar(32) not null,
    ORDER_ integer,
    --
    primary key (ID)
)^
-- end BPM_PROC_ACTOR

-- begin BPM_PROC_ATTACHMENT_TYPE
create table BPM_PROC_ATTACHMENT_TYPE (
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
    CODE varchar(255),
    --
    primary key (ID)
)^
-- end BPM_PROC_ATTACHMENT_TYPE
-- begin BPM_PROC_ATTACHMENT
create table BPM_PROC_ATTACHMENT (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    --
    FILE_ID varchar(32),
    TYPE_ID varchar(32),
    COMMENT_ text,
    PROC_INSTANCE_ID varchar(32),
    PROC_TASK_ID varchar(32),
    AUTHOR_ID varchar(32),
    --
    primary key (ID)
)^
-- end BPM_PROC_ATTACHMENT
-- begin BPM_PROC_TASK
create table BPM_PROC_TASK (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    --
    PROC_INSTANCE_ID varchar(32) not null,
    START_DATE datetime(3),
    END_DATE datetime(3),
    OUTCOME varchar(255),
    PROC_ACTOR_ID varchar(32),
    ACT_EXECUTION_ID varchar(255) not null,
    NAME varchar(255),
    ACT_TASK_ID varchar(255),
    COMMENT_ text,
    CANCELLED boolean,
    CLAIM_DATE datetime(3),
    ACT_PROCESS_DEFINITION_ID varchar(255),
    ACT_TASK_DEFINITION_KEY varchar(255),
    --
    primary key (ID)
)^-- end BPM_PROC_TASK
-- begin BPM_PROC_TASK_USER_LINK
create table BPM_PROC_TASK_USER_LINK (
    PROC_TASK_ID varchar(32),
    USER_ID varchar(32),
    primary key (PROC_TASK_ID, USER_ID)
)^
-- end BPM_PROC_TASK_USER_LINK
-- begin BPM_PROC_MODEL
create table BPM_PROC_MODEL (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    --
    NAME varchar(190) not null,
    ACT_MODEL_ID varchar(255),
    DESCRIPTION text,
    --
    primary key (ID)
)^
-- end BPM_PROC_MODEL
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
    NAME varchar(190) not null,
    JSON_DATA longtext not null,
    --
    primary key (ID)
)^
-- end BPM_STENCIL_SET