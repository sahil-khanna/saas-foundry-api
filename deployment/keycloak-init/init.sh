#!/bin/bash

# ========= CONFIGURATION ========= #
KEYCLOAK_URL="http://localhost:8080"

MASTER_REALM="master"
MASTER_REALM_CLIENT_ID="saas-admin"
MASTER_REALM_ADMIN_USERNAME="${KEYCLOAK_MASTER_REALM_ADMIN_USERNAME}"
MASTER_REALM_ADMIN_PASSWORD="${KEYCLOAK_MASTER_REALM_ADMIN_PASSWORD}"

SAAS_ADMIN_USERNAME="${KEYCLOAK_SAAS_ADMIN_USERNAME}"
SAAS_ADMIN_PASSWORD="${KEYCLOAK_SAAS_ADMIN_PASSWORD}"

ORGANIZATION_REALM="${KEYCLOAK_ORGANIZATION_REALM}"
ORGANIZATION_REALM_CLIENT_ID="organization-admin"
# ================================= #

echo "👉 Logging in to Keycloak Admin CLI..."
/opt/keycloak/bin/kcadm.sh config credentials \
  --server "$KEYCLOAK_URL" \
  --realm "$MASTER_REALM" \
  --user "$MASTER_REALM_ADMIN_USERNAME" \
  --password "$MASTER_REALM_ADMIN_PASSWORD"

if [[ $? -ne 0 ]]; then
  echo "❌ Failed to log in. Check if Keycloak is running and credentials are correct."
  exit 1
fi

echo "✅ Logged in."


# --------------------------
# 🔐 MASTER REALM SETUP
# --------------------------

echo "📦 Creating client '$MASTER_REALM_CLIENT_ID' in realm '$MASTER_REALM'..."
/opt/keycloak/bin/kcadm.sh create clients -r "$MASTER_REALM" \
  -s clientId="$MASTER_REALM_CLIENT_ID" \
  -s enabled=true \
  -s publicClient=true \
  -s standardFlowEnabled=false \
  -s directAccessGrantsEnabled=true

if [[ $? -ne 0 ]]; then
  echo "❌ Failed to create client '$MASTER_REALM_CLIENT_ID'."
  exit 1
fi

echo "✅ Client '$MASTER_REALM_CLIENT_ID' created."

CLIENT_UUID=$(/opt/keycloak/bin/kcadm.sh get clients -r "$MASTER_REALM" --fields id,clientId | \
  grep -B1 "\"clientId\" : \"$MASTER_REALM_CLIENT_ID\"" | \
  grep '"id"' | sed -E 's/.*"id" : "([^"]+)".*/\1/')

if [[ -z "$CLIENT_UUID" ]]; then
  echo "❌ Client UUID not found."
  exit 1
fi

echo "✅ '$MASTER_REALM_CLIENT_ID' UUID: $CLIENT_UUID"

echo "👤 Creating user '$SAAS_ADMIN_USERNAME' in realm '$MASTER_REALM'..."
/opt/keycloak/bin/kcadm.sh create users -r "$MASTER_REALM" \
  -s username="$SAAS_ADMIN_USERNAME" \
  -s enabled=true

if [[ $? -ne 0 ]]; then
  echo "❌ Failed to create user '$SAAS_ADMIN_USERNAME'."
  exit 1
fi

USER_ID=$(/opt/keycloak/bin/kcadm.sh get users -r "$MASTER_REALM" -q username="$SAAS_ADMIN_USERNAME" --fields id | \
  grep '"id"' | sed -E 's/.*"id" : "([^"]+)".*/\1/')

if [[ -z "$USER_ID" ]]; then
  echo "❌ Could not retrieve ID for user '$SAAS_ADMIN_USERNAME'."
  exit 1
fi

echo "🔐 Setting password for user '$SAAS_ADMIN_USERNAME'..."
/opt/keycloak/bin/kcadm.sh set-password -r "$MASTER_REALM" --userid "$USER_ID" --new-password "$SAAS_ADMIN_PASSWORD" --temporary=false

echo "✅ Password set for user '$SAAS_ADMIN_USERNAME'."

echo "⏳ Setting Access Token Lifespan to 5 minutes for realm '$MASTER_REALM'..."
/opt/keycloak/bin/kcadm.sh update realms/"$MASTER_REALM" -s accessTokenLifespan=300
echo "✅ Access Token Lifespan set."

echo "⚙️ Enabling service account for 'admin-cli' in '$MASTER_REALM'..."
ADMIN_CLI_CLIENT_ID=$(/opt/keycloak/bin/kcadm.sh get clients -r "$MASTER_REALM" --fields id,clientId | \
  grep -B1 "\"clientId\" : \"admin-cli\"" | \
  grep '"id"' | sed -E 's/.*"id" : "([^"]+)".*/\1/')

/opt/keycloak/bin/kcadm.sh update clients/"$ADMIN_CLI_CLIENT_ID" -r "$MASTER_REALM" \
  -s serviceAccountsEnabled=true \
  -s publicClient=false

echo "🔑 Assigning 'create-realm' role to 'admin-cli' service account..."
/opt/keycloak/bin/kcadm.sh add-roles -r "$MASTER_REALM" --uusername "service-account-admin-cli" \
  --rolename "create-realm" \
  --rolename "admin"

echo "✅ Master realm setup complete."


# --------------------------
# 🏢 ORGANIZATION REALM SETUP
# --------------------------

echo "🆕 Creating realm '$ORGANIZATION_REALM'..."
/opt/keycloak/bin/kcadm.sh create realms -s realm="$ORGANIZATION_REALM" -s enabled=true -s displayName="SaaS Organization"

if [[ $? -ne 0 ]]; then
  echo "❌ Failed to create realm '$ORGANIZATION_REALM'."
  exit 1
fi

echo "✅ Realm '$ORGANIZATION_REALM' created."

echo "⚙️ Updating 'admin-cli' in '$ORGANIZATION_REALM' to disable direct access grants..."
ORG_ADMIN_CLI_ID=$(/opt/keycloak/bin/kcadm.sh get clients -r "$ORGANIZATION_REALM" --fields id,clientId | \
  grep -B1 "\"clientId\" : \"admin-cli\"" | \
  grep '"id"' | sed -E 's/.*"id" : "([^"]+)".*/\1/')

if [[ -n "$ORG_ADMIN_CLI_ID" ]]; then
  /opt/keycloak/bin/kcadm.sh update clients/"$ORG_ADMIN_CLI_ID" -r "$ORGANIZATION_REALM" \
    -s directAccessGrantsEnabled=false
  echo "✅ 'admin-cli' updated in '$ORGANIZATION_REALM'."
else
  echo "⚠️ 'admin-cli' not found in '$ORGANIZATION_REALM'. Skipping."
fi

echo "📦 Creating client '$ORGANIZATION_REALM_CLIENT_ID' in realm '$ORGANIZATION_REALM'..."
/opt/keycloak/bin/kcadm.sh create clients -r "$ORGANIZATION_REALM" \
  -s clientId="$ORGANIZATION_REALM_CLIENT_ID" \
  -s enabled=true \
  -s publicClient=true \
  -s standardFlowEnabled=true \
  -s directAccessGrantsEnabled=true

if [[ $? -ne 0 ]]; then
  echo "❌ Failed to create client '$ORGANIZATION_REALM_CLIENT_ID'."
  exit 1
fi

echo "✅ Client '$ORGANIZATION_REALM_CLIENT_ID' created."
