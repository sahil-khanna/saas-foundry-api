-- Create databases
CREATE DATABASE keycloak;
CREATE DATABASE saas;

-- Connect to saas database
\connect saas

-- Table: organizations
CREATE TABLE IF NOT EXISTS public.organizations
(
    id character varying(27) COLLATE pg_catalog."default" NOT NULL,
    name character varying(250) COLLATE pg_catalog."default",
    admin_email character varying(150) COLLATE pg_catalog."default",
    is_keycloak_user_provisioned boolean,
    keycloak_user_provision_attempted_on timestamp(6) with time zone,
    is_welcome_email_sent boolean,
    welcome_email_attempted_on timestamp(6) with time zone,
    is_db_provisioned boolean DEFAULT false,
    db_provision_attempted_on timestamp(6) with time zone,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    CONSTRAINT organizations_pkey PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS public.organizations
    OWNER to postgres;

CREATE INDEX IF NOT EXISTS idx_organizations_name
    ON public.organizations USING btree
    (name COLLATE pg_catalog."default" ASC NULLS LAST);