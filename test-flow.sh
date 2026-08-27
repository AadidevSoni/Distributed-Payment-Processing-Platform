#!/bin/bash
# Visa-Sim full flow test script
# Requires: jq (brew install jq)
# Usage: ./test-flow.sh

set -e  # stop immediately if any command fails

BASE_URL="http://localhost:8081"

echo "=================================================="
echo "1. Creating User 1"
echo "=================================================="
USER_1_RESPONSE=$(curl -s -X POST "$BASE_URL/users" \
  -H "Content-Type: application/json" \
  -d "{\"fullName\": \"Aadidev Sharma\", \"email\": \"test.$(date +%s).1@example.com\"}")
echo "$USER_1_RESPONSE" | jq .
USER_ID_1=$(echo "$USER_1_RESPONSE" | jq -r '.id')
echo "-> USER_ID_1 = $USER_ID_1"
echo ""

echo "=================================================="
echo "2. Creating User 2"
echo "=================================================="
USER_2_RESPONSE=$(curl -s -X POST "$BASE_URL/users" \
  -H "Content-Type: application/json" \
  -d "{\"fullName\": \"Test Receiver\", \"email\": \"test.$(date +%s).2@example.com\"}")
echo "$USER_2_RESPONSE" | jq .
USER_ID_2=$(echo "$USER_2_RESPONSE" | jq -r '.id')
echo "-> USER_ID_2 = $USER_ID_2"
echo ""

echo "=================================================="
echo "3. Creating Wallet for User 1"
echo "=================================================="
WALLET_1_RESPONSE=$(curl -s -X POST "$BASE_URL/wallets/users/$USER_ID_1")
echo "$WALLET_1_RESPONSE" | jq .
WALLET_ID_1=$(echo "$WALLET_1_RESPONSE" | jq -r '.id')
echo "-> WALLET_ID_1 = $WALLET_ID_1"
echo ""

echo "=================================================="
echo "4. Creating Wallet for User 2"
echo "=================================================="
WALLET_2_RESPONSE=$(curl -s -X POST "$BASE_URL/wallets/users/$USER_ID_2")
echo "$WALLET_2_RESPONSE" | jq .
WALLET_ID_2=$(echo "$WALLET_2_RESPONSE" | jq -r '.id')
echo "-> WALLET_ID_2 = $WALLET_ID_2"
echo ""

echo "=================================================="
echo "5. Crediting Wallet 1 with 100.00"
echo "=================================================="
curl -s -X POST "$BASE_URL/wallets/$WALLET_ID_1/credit" \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.00}' | jq .
echo ""

echo "=================================================="
echo "6. Transferring 25.00 from Wallet 1 -> Wallet 2"
echo "=================================================="
curl -s -X POST "$BASE_URL/transactions/transfer" \
  -H "Content-Type: application/json" \
  -d "{\"fromWalletId\": \"$WALLET_ID_1\", \"toWalletId\": \"$WALLET_ID_2\", \"amount\": 25.00, \"idempotencyKey\": \"$(uuidgen)\"}" | jq .
echo ""

echo "=================================================="
echo "7. Final balances"
echo "=================================================="
echo "Wallet 1 (should be 75.0000):"
curl -s "$BASE_URL/wallets/users/$USER_ID_1" | jq .
echo ""
echo "Wallet 2 (should be 25.0000):"
curl -s "$BASE_URL/wallets/users/$USER_ID_2" | jq .
echo ""

echo "=================================================="
echo "DONE. Check user-service and notification-service"
echo "logs for the Kafka event round-trip, and check"
echo "fraud-postgres for the fraud_checks audit row."
echo "=================================================="