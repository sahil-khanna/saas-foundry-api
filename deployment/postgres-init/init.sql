-- Create databases
CREATE DATABASE keycloak;
CREATE DATABASE saas;

-- Connect to saas database
\connect saas

-- Table: organizations
CREATE TABLE IF NOT EXISTS public.organizations
(
    id character varying(27) COLLATE pg_catalog."default" NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    admin_email character varying(150) COLLATE pg_catalog."default",
    is_keycloak_user_provisioned boolean,
    is_welcome_email_sent boolean,
    keycloak_user_provision_attempted_on timestamp(6) with time zone,
    name character varying(250) COLLATE pg_catalog."default",
    welcome_email_attempted_on timestamp(6) with time zone,
    CONSTRAINT organizations_pkey PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS public.organizations
    OWNER to postgres;

CREATE INDEX IF NOT EXISTS idx_organizations_name
    ON public.organizations USING btree
    (name COLLATE pg_catalog."default" ASC NULLS LAST);

-- Table: clients
CREATE TABLE IF NOT EXISTS public.clients
(
    id character varying(27) COLLATE pg_catalog."default" NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    admin_email character varying(150) COLLATE pg_catalog."default",
    db_provision_attempted_on timestamp(6) with time zone,
    is_db_provisioned boolean,
    is_keycloak_realm_provisioned boolean,
    is_keycloak_user_provisioned boolean,
    is_welcome_email_sent boolean,
    keycloak_realm_provision_attempted_on timestamp(6) with time zone,
    keycloak_user_provision_attempted_on timestamp(6) with time zone,
    name character varying(250) COLLATE pg_catalog."default",
    welcome_email_attempted_on timestamp(6) with time zone,
    org_uid character varying(27) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT clients_pkey PRIMARY KEY (id),
    CONSTRAINT fknbo7cegfgehfj17mo4t3wt9l0 FOREIGN KEY (org_uid)
        REFERENCES public.organizations (id)
);

ALTER TABLE IF EXISTS public.clients
    OWNER to postgres;

CREATE INDEX IF NOT EXISTS idx_client_name
    ON public.clients USING btree
    (name COLLATE pg_catalog."default" ASC NULLS LAST);

CREATE INDEX IF NOT EXISTS idx_client_org_uid
    ON public.clients USING btree
    (org_uid COLLATE pg_catalog."default" ASC NULLS LAST);
