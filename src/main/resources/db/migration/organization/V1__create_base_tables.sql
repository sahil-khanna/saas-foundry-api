-- Table: public.clients

-- DROP TABLE IF EXISTS public.clients;

-- Create table: clients
CREATE TABLE IF NOT EXISTS public.clients (
    id character varying(27) COLLATE pg_catalog."default" NOT NULL,
    name character varying(250) COLLATE pg_catalog."default",
    admin_email character varying(150) COLLATE pg_catalog."default",
    is_keycloak_realm_provisioned boolean,
    keycloak_realm_provision_attempted_on timestamp(6) with time zone,
    is_keycloak_user_provisioned boolean,
    keycloak_user_provision_attempted_on timestamp(6) with time zone,
    is_db_provisioned boolean,
    db_provision_attempted_on timestamp(6) with time zone,
    is_welcome_email_sent boolean,
    welcome_email_attempted_on timestamp(6) with time zone,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    CONSTRAINT clients_pkey PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS public.clients
    OWNER TO postgres;

CREATE INDEX IF NOT EXISTS idx_client_name
    ON public.clients USING btree
    (name COLLATE pg_catalog."default" ASC NULLS LAST);