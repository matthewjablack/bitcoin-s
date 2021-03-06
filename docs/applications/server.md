---
id: server
title: Application Server
---


### App server

The [server](../../app/server) project is the aggregation of these three sub projects

1. [Wallet](../wallet/wallet.md)
2. [Chain](../chain/chain.md)
3. [Node](../node/node.md)

The server project provides a away to access information from these three projects via a JSON RPC.

### Building the server

You can build the server with the [sbt native packager](https://github.com/sbt/sbt-native-packager).
The native packager offers [numerous ways to package the project](https://github.com/sbt/sbt-native-packager#examples).

In this example we are going to use `stage` which will produce bash scripts we can easily execute. You can stage the server with the following command

```bash
 $ sbt appServer/universal:stage
```

This will produce a script to execute bitcoin-s which you can start with

```bash
$ ./app/server/target/universal/stage/bin/bitcoin-s-server
```

If you would like to pass in a custom datadir for your server, you can do

```bash
./app/server/target/universal/stage/bin/bitcoin-s-server --datadir /path/to/datadir/
```

You can also pass in a custom `rpcport` to bind to

```bash
./app/server/target/universal/stage/bin/bitcoin-s-server --rpcport 12345
```

For more information on configuring the server please see our [configuration](../config/configuration.md) document

For more information on how to use our built in `cli` to interact with the server please see [cli.md](cli.md)

### Server Endpoints

#### Blockchain

 - `getblockcount` - Get the current block height
 - `getfiltercount` - Get the number of filters
 - `getfilterheadercount` - Get the number of filter headers
 - `getbestblockhash` - Get the best block hash

#### Wallet
 - `rescan` `[options]` - Rescan for wallet UTXOs
    - `--force` - Clears existing wallet records. Warning! Use with caution!
    - `--batch-size <value>` - Number of filters that can be matched in one batch
    - `--start <value>` - Start height
    - `--end <value>` - End height
    - `--ignorecreationtime` - Ignores the wallet creation date and will instead do a full rescan
 - `isempty` - Checks if the wallet contains any data
 - `getbalance` `[options]` - Get the wallet balance
    - `--sats ` - Display balance in satoshis
 - `getconfirmedbalance` `[options]` - Get the wallet balance of confirmed utxos
    - `--sats ` - Display balance in satoshis
 - `getunconfirmedbalance` `[options]` - Get the wallet balance of unconfirmed utxos
    - `--sats ` - Display balance in satoshis
 - `getutxos` - Returns list of all wallet utxos
 - `getaddresses` - Returns list of all wallet addresses currently being watched
 - `getspentaddresses` - Returns list of all wallet addresses that have received funds and been spent
 - `getfundedaddresses` - Returns list of all wallet addresses that are holding funds
 - `getunusedaddresses` - Returns list of all wallet addresses that have not been used
 - `getaccounts` - Returns list of all wallet accounts
 - `createnewaccount` - Creates a new wallet account
 - `getaddressinfo` `address` - Returns list of all wallet accounts
    - `address` - Address to get information about
 - `getnewaddress` - Get a new address
 - `sendtoaddress` `address` `amount` `[options]` - Send money to the given address
    - `address` - Address to send to
    - `amount` - Amount to send in BTC
    - `--feerate <value>` - Fee rate in sats per virtual byte
 - `sendfromoutpoints` `outpoints` `address` `amount` `[options]` - Send money to the given address
    - `outpoints` - Out Points to send from
    - `address` - Address to send to
    - `amount` - Amount to send in BTC
    - `--feerate <value>` - Fee rate in sats per virtual byte
 - `sendwithalgo` `address` `amount` `algo` `[options]` - Send money to the given address using a specific coin selection algo
    - `address` - Address to send to
    - `amount` - Amount to send in BTC
    - `algo` - Coin selection algo
    - `--feerate <value>` - Fee rate in sats per virtual byte
 - `opreturncommit` `message` `[options]` - Creates OP_RETURN commitment transaction
    - `message` - message to put into OP_RETURN commitment
    - `--hashMessage` - should the message be hashed before commitment
    - `--feerate <value>` - Fee rate in sats per virtual byte

#### Network
 - `getpeers` - List the connected peers
 - `stop` - Request a graceful shutdown of Bitcoin-S
 - `sendrawtransaction` `tx` `Broadcasts the raw transaction`
    - `tx` - Transaction serialized in hex

#### PSBT
 - `combinepsbts` `psbts` - Combines all the given PSBTs
    - `psbts` - PSBTs serialized in hex or base64 format
 - `joinpsbts` `psbts` - Combines all the given PSBTs
    - `psbts` - PSBTs serialized in hex or base64 format
 - `finalizepsbt` `psbt` - Finalizes the given PSBT if it can
    - `psbt` - PSBT serialized in hex or base64 format
 - `extractfrompsbt` `psbt` - Extracts a transaction from the given PSBT if it can
    - `psbt` - PSBT serialized in hex or base64 format
 - `converttopsbt` `unsignedTx` - Creates an empty psbt from the given transaction
    - `unsignedTx` - serialized unsigned transaction in hex
