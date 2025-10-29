WEB3 Wallet example
======================================================

Overview
--------
This project is a demo backend for a multi-currency wallet application that integrates with Ethereum using Web3j. The API lets you create users, create wallets for users, load credentials from the database (or keystore), check balances, send Ether, and transfer ERC20 tokens.

Base URL
--------
Example base URL for local testing:

http://localhost:8080

If your app is configured with a global `/api` prefix, prefix the paths below with `/api`.

Important configuration
-----------------------
The application expects a Web3 provider and a datasource to be configured. For quick local testing, add an H2 DB and a local Ethereum node.

Example `src/main/resources/application.properties`:

# Web3j HTTP client (default http://localhost:8545)
web3j.client-address=http://localhost:8545

# H2 in-memory DB
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true

Endpoints (current controller mappings)
--------------------------------------
The following paths are taken directly from the controllers in the project. If your project uses a global `/api` prefix, add it before these paths.

User account endpoints (`UserAccountController` - base: `/userAccount`)
1) Create a user
- Method: POST
- Path: `/userAccountcreate`
- Request JSON: `{ "username": "alice", "email": "alice@example.com" }`
- Success: 200 OK with created UserAccount JSON
- Errors: 400 Bad Request if required fields missing

2) Get user by ID
- Method: GET
- Path: `/userAccount/find/{userId}`
- Success: 200 OK with UserAccount JSON
- 404 Not Found if user does not exist

3) Get user by username
- Method: GET
- Path: `/userAccount/find/username/{username}`
- Success: 200 OK with UserAccount JSON
- 404 Not Found if user does not exist

4) Update user
- Method: PUT
- Path: `/userAccount/update/{userId}`
- Request JSON: UserAccount object
- Success: 200 OK with updated UserAccount
- 404 Not Found if user does not exist

5) Delete user
- Method: DELETE
- Path: `/userAccount/delete/{userId}`
- Success: 200 OK on deletion

Wallet endpoints (`WalletController` - base: `/wallets`)
6) Create wallet for a user
- Method: POST
- Path: `/wallets/create/{userId}/wallets`
- Request JSON: `{ "password": "walletPassword", "walletName": "Primary" }`
- Success: 200 OK with WalletEntity JSON

7) List user wallets
- Method: GET
- Path: `/wallets/users/{userId}/wallets`
- Success: 200 OK with array of WalletEntity

8) Rename a wallet
- Method: PUT
- Path: `/wallets/wallets/{walletId}/name`
- Request JSON: `{ "walletName": "New Name" }`
- Success: 200 OK with updated WalletEntity

9) Delete a wallet
- Method: DELETE
- Path: `/wallets/wallets/{walletId}`
- Success: 200 OK on deletion

Blockchain endpoints (`BlockchainController` - base: `/blockchain`)
10) Get wallet balance (by wallet ID)
- Method: GET
- Path: `/blockchain/wallets/{walletId}/balance`
- Success: 200 OK with JSON: `{ "balance": <BigDecimal>, "unit": "ETH" }`

11) Get balance for an arbitrary address
- Method: GET
- Path: `/blockchain/balance/{address}`
- Success: 200 OK with JSON: `{ "address": "0x...", "balance": <BigDecimal>, "unit": "ETH" }`

12) Send Ether from a wallet
- Method: POST
- Path: `/blockchain/wallets/{walletId}/send-ether`
- Request JSON: `{ "password": "walletPassword", "toAddress": "0x..", "amount": "0.01" }`
- Success: 200 OK with JSON: `{ "transactionHash": "0x..", "status": "0x1", "gasUsed": <gas> }`

13) Transfer ERC20 tokens
- Method: POST
- Path: `/blockchain/wallets/{walletId}/transfer-erc20`
- Request JSON: `{ "password": "walletPassword", "contractAddress": "0x..", "toAddress": "0x..", "amount": "1000" }`
- Success: 200 OK with transaction data

14) Validate an Ethereum address
- Method: GET
- Path: `/blockchain/validate-address/{address}`
- Success: 200 OK with JSON: `{ "valid": true|false }`

Quick curl examples
-------------------
Create a user:

curl -X POST http://localhost:8080/userAccountcreate \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"alice\",\"email\":\"alice@example.com\"}"

Create a wallet for user 1:

curl -X POST http://localhost:8080/wallets/create/1/wallets \
  -H "Content-Type: application/json" \
  -d "{\"password\":\"secret\",\"walletName\":\"Main\"}"

Send 0.01 ETH from wallet 1:

curl -X POST http://localhost:8080/blockchain/wallets/1/send-ether \
  -H "Content-Type: application/json" \
  -d "{\"password\":\"secret\",\"toAddress\":\"0x...\",\"amount\":\"0.01\"}"

Notes
-----
- Paths above reflect current controller mappings. Some mappings include duplicated segments (for example, `/wallets/create/{userId}/wallets` and `/wallets/wallets/{walletId}`) — these likely indicate missing or extra slashes in controller `@RequestMapping` values and can be adjusted in the controller code if desired.
- Ensure a running Web3 provider is available at the configured `web3j.client-address` (e.g., Ganache on `http://localhost:8545`) and a datasource is configured or use the H2 example above.