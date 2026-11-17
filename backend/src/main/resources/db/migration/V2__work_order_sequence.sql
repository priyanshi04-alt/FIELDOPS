-- ==============================================================================
-- FIELDOPS Work Order Number Sequence Migration (V2)
-- Description: Creates persistent database sequence for atomic work order numbering
-- ==============================================================================

CREATE SEQUENCE IF NOT EXISTS work_order_number_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
