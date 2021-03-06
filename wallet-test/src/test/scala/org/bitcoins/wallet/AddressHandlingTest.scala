package org.bitcoins.wallet

import org.bitcoins.core.currency.{Bitcoins, Satoshis}
import org.bitcoins.core.protocol.transaction.TransactionOutput
import org.bitcoins.core.wallet.fee.SatoshisPerVirtualByte
import org.bitcoins.rpc.util.AsyncUtil
import org.bitcoins.testkit.wallet.FundWalletUtil.FundedWallet
import org.bitcoins.testkit.wallet.{BitcoinSWalletTest, WalletTestUtil}
import org.scalatest.FutureOutcome

import scala.concurrent.Future

class AddressHandlingTest extends BitcoinSWalletTest {
  type FixtureParam = FundedWallet

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {
    withFundedWallet(test, getBIP39PasswordOpt())
  }

  behavior of "AddressHandling"

  it must "generate a new address for the default account and then find it" in {
    fundedWallet: FundedWallet =>
      val wallet = fundedWallet.wallet
      val addressF = wallet.getNewAddress()

      for {
        address <- addressF
        exists <- wallet.contains(address, None)
      } yield {
        assert(exists, s"Wallet must contain address after generating it")
      }
  }

  it must "generate an address for a non default account and then find it" in {
    fundedWallet: FundedWallet =>
      val wallet = fundedWallet.wallet
      val account1 = WalletTestUtil.getHdAccount1(wallet.walletConfig)
      val addressF = wallet.getNewAddress(account1)
      for {
        address <- addressF
        listAddressesForAcct <- wallet.listAddresses(account1)
        exists <- wallet.contains(address, Some(account1))
        doesNotExist <- wallet.contains(address, None)
      } yield {
        assert(listAddressesForAcct.nonEmpty)
        assert(listAddressesForAcct.map(_.address).contains(address))
        assert(exists,
               s"Wallet must contain address in specific after generating it")
        assert(
          doesNotExist,
          s"Wallet must NOT contain address in default account when address is specified")
      }
  }

  it must "generate the same unused address until it receives funds" in {
    fundedWallet: FundedWallet =>
      val wallet = fundedWallet.wallet

      for {
        address1 <- wallet.getUnusedAddress
        exists <- wallet.contains(address1, None)
        _ = assert(exists, s"Wallet must contain address after generating it")
        address2 <- wallet.getUnusedAddress
        _ = assert(address1 == address2, "Must generate same address")
        _ <- wallet.sendToAddress(address1,
                                  Satoshis(10000),
                                  Some(SatoshisPerVirtualByte.one))
        address3 <- wallet.getUnusedAddress
      } yield {
        assert(address1 != address3, "Must generate a new address")
        assert(address2 != address3, "Must generate a new address")
      }
  }

  it must "be safe to call getNewAddress multiple times in a row" in {
    fundedWallet: FundedWallet =>
      val wallet = fundedWallet.wallet
      val addressesF = Future.sequence {
        Vector.fill(10)(wallet.getNewAddress())
      }

      for {
        addresses <- addressesF
      } yield {
        assert(addresses.size == 10)
        assert(addresses.distinct.length == addresses.length,
               s"We receive an identical address!")

      }
  }

  it must "fail with an illegal state exception if the queue is full" in {
    fundedWallet: FundedWallet =>
      val wallet = fundedWallet.wallet
      //attempt to generate 50 addresses simultaneously
      //this should overwhelm our buffer size of 10
      val numAddress = 50
      val generatedF = Vector.fill(numAddress)(wallet.getNewAddress())

      //some hacking here so we don't get an ugly stack trace
      //when the thread gets killed while processing things in the queue
      //we want to make sure everything                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              is done processing before we assert
      //we failed
      val allCompletedF =
        AsyncUtil.retryUntilSatisfied(generatedF.forall(_.isCompleted))
      val addressesF = allCompletedF.flatMap { _ =>
        Future.sequence {
          generatedF
        }
      }

      recoverToSucceededIf[IllegalStateException] {
        addressesF
      }
  }

  it must "get the correct spent addresses" in { fundedWallet: FundedWallet =>
    val wallet = fundedWallet.wallet

    for {
      emptySpentAddresses <- wallet.listSpentAddresses()
      _ = assert(
        emptySpentAddresses.isEmpty,
        s"Wallet did not start with empty spent addresses, got $emptySpentAddresses")

      tempAddress <- wallet.getNewAddress()
      tx <- wallet.sendToAddress(tempAddress,
                                 Bitcoins(1),
                                 Some(SatoshisPerVirtualByte(Satoshis(3))))
      spentDbs <- wallet.spendingInfoDAO.findOutputsBeingSpent(tx)
      spentAddresses <- wallet.listSpentAddresses()
    } yield {
      val diff = spentDbs
        .map(_.output.scriptPubKey)
        .diff(spentAddresses.map(_.scriptPubKey))
      assert(diff.isEmpty, s"Extra spent addresses $diff")
    }
  }

  it must "get the correct funded addresses" in { fundedWallet: FundedWallet =>
    val wallet = fundedWallet.wallet

    for {
      unspentDbs <- wallet.spendingInfoDAO.findAllUnspent()
      fundedAddresses <- wallet.listFundedAddresses()
    } yield {
      val diff = unspentDbs
        .map(_.output)
        .diff(fundedAddresses.map(tuple =>
          TransactionOutput(tuple._2, tuple._1.scriptPubKey)))
      assert(diff.isEmpty, s"Extra funded addresses $diff")
    }
  }

  it must "get the correct unused addresses" in { fundedWallet: FundedWallet =>
    val wallet = fundedWallet.wallet

    for {
      addrDbs <- wallet.spendingInfoDAO.findAll()
      fundedAddresses <- wallet.listUnusedAddresses()
    } yield {
      val intersect = addrDbs
        .map(_.output.scriptPubKey)
        .intersect(fundedAddresses.map(_.scriptPubKey))
      assert(intersect.isEmpty, s"Returned used addresses $intersect")
    }
  }
}
