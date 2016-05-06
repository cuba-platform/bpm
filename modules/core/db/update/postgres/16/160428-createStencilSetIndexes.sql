-- begin BPM_STENCIL_SET
create unique index IDX_BPM_STENCIL_SET_UNIQ_NAME on BPM_STENCIL_SET (NAME) where DELETE_TS is null ^
-- end BPM_STENCIL_SET
