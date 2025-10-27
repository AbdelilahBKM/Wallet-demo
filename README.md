WEB3 Wallet example
======================================================

Overview
--------
This project is a demo backend for a multi-currency wallet application that integrates with Ethereum using Web3j. The API lets you create users, create wallets for users, load credentials from the database (or keystore), check balances, send Ether, and transfer ERC20 tokens.

Base URL
--------
All endpoints are under the /api prefix. Example base URL for local testing:

http://localhost:8080/api

Important configuration
-----------------------
The application expects a Web3 provider and a datasource to be configured. If no datasource is provided, Spring Boot tries to configure one and fails ("Failed to configure a DataSource"). For quick local testing, use an embedded H2 DB and a local or remote Ethereum node (Ganache, geth, Infura, etc.).

Add the following to src/main/resources/application.properties for local testing (example):

# Web3j HTTP client (default http://localhost:8545)
web3j.client-address=http://localhost:8545

# H2 in-memory DB to satisfy Spring Data JPA and let the app start
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true

Notes on wallets and credentials
--------------------------------
- The project contains a `WalletProperties` config class which allows pre-loading static wallets via properties under the `wallets` prefix, but the application also supports creating wallets dynamically via the `UserService`/`WalletService` (the controller endpoints create and store wallets).
- Credentials (private keys / keystores) should NOT be stored in plaintext in application.properties in a real system. For the demo you can store them in the DB via JPA entities (the current code is structured to use JPA-backed `WalletEntity` and `UserAccount`).

Swagger / OpenAPI (recommended)
--------------------------------
To explore endpoints with a UI add the Springdoc OpenAPI dependency to your pom.xml:

<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-ui</artifactId>
  <version>1.7.0</version>
</dependency>

After adding it and restarting the app, open:
- http://localhost:8080/swagger-ui.html or
- http://localhost:8080/swagger-ui/index.html

Endpoints (extracted from `BlockchainController`)
-------------------------------------------------
All paths are beneath /api.

1) Create a user
- Method: POST
- Path: /api/users
- Request JSON: { "username": "alice", "email": "alice@example.com" }
- Success: 200 OK with created UserAccount JSON
- Errors: 400 Bad Request if required fields missing

2) Get user by ID
- Method: GET
- Path: /api/users/{userId}
- Success: 200 OK with UserAccount JSON
- 404 Not Found if user does not exist

3) Get user by username
- Method: GET
- Path: /api/users/username/{username}
- Success: 200 OK with UserAccount JSON
- 404 Not Found if user does not exist

4) Create wallet for a user
- Method: POST
- Path: /api/users/{userId}/wallets
- Request JSON: { "password": "walletPassword", "walletName": "Primary" }
- Success: 200 OK with WalletEntity JSON (address, id, name, etc.)
- Errors: 400 Bad Request on missing fields or failure to create wallet

5) List user wallets
- Method: GET
- Path: /api/users/{userId}/wallets
- Success: 200 OK with array of WalletEntity

6) Rename a wallet
- Method: PUT
- Path: /api/wallets/{walletId}/name
- Request JSON: { "walletName": "New Name" }
- Success: 200 OK with updated WalletEntity
- 404 Not Found if wallet does not exist

7) Delete a wallet
- Method: DELETE
- Path: /api/wallets/{walletId}
- Success: 200 OK on deletion
- Errors: 400 Bad Request with message if deletion fails

8) Get wallet balance (by wallet ID)
- Method: GET
- Path: /api/wallets/{walletId}/balance
- Success: 200 OK with JSON: { "balance": <BigDecimal>, "unit": "ETH" }
- Errors: 400 Bad Request on failure

9) Get balance for an arbitrary address
- Method: GET
- Path: /api/balance/{address}
- Success: 200 OK with JSON: { "address": "0x...", "balance": <BigDecimal>, "unit": "ETH" }
- Errors: 400 Bad Request if invalid address or failure

10) Send Ether from a wallet
- Method: POST
- Path: /api/wallets/{walletId}/send-ether
- Request JSON: { "password": "walletPassword", "toAddress": "0x..", "amount": "0.01" }
  - amount can be provided as a string or number; controller converts to BigDecimal
- Success: 200 OK with JSON: { "transactionHash": "0x..", "status": "0x1", "gasUsed": <gas> }
- Errors: 400 Bad Request on invalid inputs or transaction failure

11) Transfer ERC20 tokens
- Method: POST
- Path: /api/wallets/{walletId}/transfer-erc20
- Request JSON: { "password": "walletPassword", "contractAddress": "0x..", "toAddress": "0x..", "amount": "1000" }
  - amount is the token amount as integer (BigInteger) depending on token decimals
- Success: 200 OK with transaction data same as ETH send
- Errors: 400 Bad Request on invalid inputs or transaction failure

12) Validate an Ethereum address
- Method: GET
- Path: /api/validate-address/{address}
- Success: 200 OK with JSON: { "valid": true|false }

Quick curl examples
-------------------
Create a user:

curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"alice\",\"email\":\"alice@example.com\"}"

Create a wallet for user 1:

curl -X POST http://localhost:8080/api/users/1/wallets \
  -H "Content-Type: application/json" \
  -d "{\"password\":\"secret\",\"walletName\":\"Main\"}"

Send 0.01 ETH from wallet 1:

curl -X POST http://localhost:8080/api/wallets/1/send-ether \
  -H "Content-Type: application/json" \
  -d "{\"password\":\"secret\",\"toAddress\":\"0x...\",\"amount\":\"0.01\"}"

Notes on common errors and troubleshooting
-----------------------------------------
- DataSource errors (e.g. "Failed to configure a DataSource: 'url' attribute is not specified"): ensure you have datasource properties or add H2 as shown above.
- Missing repository beans at startup (e.g. "Required a bean of type '...UserAccountRepository' that could not be found"): ensure Spring Boot scans the repository package (default when using @SpringBootApplication at package root). If your main class is not in the root package, move it or configure @EnableJpaRepositories with the correct base package.
- Cannot resolve web3j symbols / Transfer.sendFunds complaints: ensure you use a compatible Web3j version and import correct classes; check pom.xml for web3j-core dependency.

Next steps (recommended)
------------------------
- Add `springdoc-openapi-ui` dependency to expose Swagger UI.
- Configure a persistent database (Postgres/MySQL) for real testing (and never store raw private keys unencrypted in production).
- Add integration tests for the controller endpoints using MockMvc and an embedded test node (or mock Web3j responses).

File created: README.md

If you want, I can:
- add the H2 application.properties example to your repo as src/main/resources/application.properties (backup any existing file first),
- or add the Springdoc dependency to your pom.xml and re-run the app so you can use Swagger UI right away.

