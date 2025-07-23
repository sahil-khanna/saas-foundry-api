#!/bin/bash

# ========= CONFIGURATION ========= #
KEYCLOAK_URL="http://localhost:8080"
MASTER_REALM="master"
MASTER_REALM_CLIENT_ID="saas-admin"
APPLICATION_REALM="${KEYCLOAK_APPLICATION_REALM}"
MASTER_REALM_ADMIN_USERNAME="${KEYCLOAK_MASTER_REALM_ADMIN_USERNAME}"
MASTER_REALM_ADMIN_PASSWORD="${KEYCLOAK_MASTER_REALM_ADMIN_PASSWORD}"
APPLICATION_ADMIN_USERNAME="${KEYCLOAK_APPLICATION_ADMIN_USERNAME}"
APPLICATION_ADMIN_PASSWORD="${KEYCLOAK_APPLICATION_ADMIN_PASSWORD}"
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

echo "🆕 Creating realm '$APPLICATION_REALM'..."
/opt/keycloak/bin/kcadm.sh create realms -s realm="$APPLICATION_REALM" -s enabled=true

if [[ $? -ne 0 ]]; then
  echo "❌ Failed to create realm '$APPLICATION_REALM'."
  exit 1
fi

echo "✅ Realm '$APPLICATION_REALM' created."

echo "📦 Creating client '$MASTER_REALM_CLIENT_ID' in realm '$MASTER_REALM'..."
/opt/keycloak/bin/kcadm.sh create clients -r "$MASTER_REALM" -s clientId="$MASTER_REALM_CLIENT_ID" \
  -s enabled=true \
  -s 'publicClient=false' \
  -s 'serviceAccountsEnabled=true' \
  -s 'standardFlowEnabled=false' \
  -s 'directAccessGrantsEnabled=false'

if [[ $? -ne 0 ]]; then
  echo "❌ Failed to create client '$MASTER_REALM_CLIENT_ID'."
  exit 1
fi

echo "✅ Client '$MASTER_REALM_CLIENT_ID' created in realm '$MASTER_REALM'."

# Get client UUID
CLIENT_UUID=$(/opt/keycloak/bin/kcadm.sh get clients -r "$MASTER_REALM" --fields id,clientId | \
  grep -B1 "\"clientId\" : \"$MASTER_REALM_CLIENT_ID\"" | \
  grep '"id"' | sed -E 's/.*"id" : "([^"]+)".*/\1/')

if [[ -z "$CLIENT_UUID" ]]; then
  echo "❌ Client '$MASTER_REALM_CLIENT_ID' not found."
  exit 1
fi

echo "✅ '$MASTER_REALM_CLIENT_ID' UUID: $CLIENT_UUID"

# Get service account user ID
SERVICE_ACCOUNT_USER_ID=$(/opt/keycloak/bin/kcadm.sh get users -r "$MASTER_REALM" -q username="service-account-$MASTER_REALM_CLIENT_ID" --fields id | \
  grep '"id"' | sed -E 's/.*"id" : "([^"]+)".*/\1/')

if [[ -z "$SERVICE_ACCOUNT_USER_ID" ]]; then
  echo "❌ Failed to retrieve service account user for '$MASTER_REALM_CLIENT_ID'."
  exit 1
fi

echo "👤 Service account user ID: $SERVICE_ACCOUNT_USER_ID"

echo "🔐 Assigning 'admin' and 'create-realm' realm roles in '$MASTER_REALM'..."

/opt/keycloak/bin/kcadm.sh add-roles -r "$MASTER_REALM" --uusername "service-account-$MASTER_REALM_CLIENT_ID" \
  --rolename "admin"

/opt/keycloak/bin/kcadm.sh add-roles -r "$MASTER_REALM" --uusername "service-account-$MASTER_REALM_CLIENT_ID" \
  --rolename "create-realm"

echo "✅ Roles assigned to service account in '$MASTER_REALM'."

echo "👤 Creating user '$APPLICATION_ADMIN_USERNAME' in realm '$MASTER_REALM'..."

# Create the user
/opt/keycloak/bin/kcadm.sh create users -r "$MASTER_REALM" \
  -s username="$APPLICATION_ADMIN_USERNAME" \
  -s enabled=true

if [[ $? -ne 0 ]]; then
  echo "❌ Failed to create user '$APPLICATION_ADMIN_USERNAME'."
  exit 1
fi

# Get the user ID
USER_ID=$(/opt/keycloak/bin/kcadm.sh get users -r "$MASTER_REALM" -q username="$APPLICATION_ADMIN_USERNAME" --fields id | \
  grep '"id"' | sed -E 's/.*"id" : "([^"]+)".*/\1/')

if [[ -z "$USER_ID" ]]; then
  echo "❌ Could not retrieve ID for user '$APPLICATION_ADMIN_USERNAME'."
  exit 1
fi

# Set user password
echo "🔐 Setting password for user '$APPLICATION_ADMIN_USERNAME'..."
/opt/keycloak/bin/kcadm.sh set-password -r "$MASTER_REALM" --userid "$USER_ID" --new-password "$APPLICATION_ADMIN_PASSWORD" --temporary=false

if [[ $? -ne 0 ]]; then
  echo "❌ Failed to set password for user '$APPLICATION_ADMIN_USERNAME'."
  exit 1
fi

echo "✅ User '$APPLICATION_ADMIN_USERNAME' created with password in realm '$MASTER_REALM'."