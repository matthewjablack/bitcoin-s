package org.bitcoins.wallet.api

import org.bitcoins.core.currency.{CurrencyUnit, CurrencyUnits}
import org.bitcoins.core.protocol.transaction.TransactionOutput
import org.bitcoins.core.wallet.fee.FeeRate
import org.bitcoins.wallet.models.SpendingInfoDb

import scala.annotation.tailrec

/** Implements algorithms for selecting from a UTXO set to spend to an output set at a given fee rate. */
trait CoinSelector {

  /**
    * Greedily selects from walletUtxos starting with the largest outputs, skipping outputs with values
    * below their fees. Better for high fee environments than accumulateSmallestViable.
    */
  def accumulateLargest(
      walletUtxos: Vector[SpendingInfoDb],
      outputs: Vector[TransactionOutput],
      feeRate: FeeRate): Vector[SpendingInfoDb] = {
    val sortedUtxos =
      walletUtxos.sortBy(_.output.value).reverse

    accumulate(sortedUtxos, outputs, feeRate)
  }

  /**
    * Greedily selects from walletUtxos starting with the smallest outputs, skipping outputs with values
    * below their fees. Good for low fee environments to consolidate UTXOs.
    *
    * Has the potential privacy breach of connecting a ton of UTXOs to one address.
    */
  def accumulateSmallestViable(
      walletUtxos: Vector[SpendingInfoDb],
      outputs: Vector[TransactionOutput],
      feeRate: FeeRate): Vector[SpendingInfoDb] = {
    val sortedUtxos = walletUtxos.sortBy(_.output.value)

    accumulate(sortedUtxos, outputs, feeRate)
  }

  /** Greedily selects from walletUtxos in order, skipping outputs with values below their fees */
  def accumulate(
      walletUtxos: Vector[SpendingInfoDb],
      outputs: Vector[TransactionOutput],
      feeRate: FeeRate): Vector[SpendingInfoDb] = {
    val totalValue = outputs.foldLeft(CurrencyUnits.zero) {
      case (totVal, output) => totVal + output.value
    }

    @tailrec
    def addUtxos(
        alreadyAdded: Vector[SpendingInfoDb],
        valueSoFar: CurrencyUnit,
        bytesSoFar: Long,
        utxosLeft: Vector[SpendingInfoDb]): Vector[SpendingInfoDb] = {
      val fee = feeRate.baseAmount * bytesSoFar
      if (valueSoFar.satoshis.toDouble > totalValue.satoshis.toDouble + fee) {
        alreadyAdded
      } else if (utxosLeft.isEmpty) {
        throw new RuntimeException(
          s"Not enough value in given outputs ($valueSoFar) to make transaction spending $totalValue")
      } else {
        val nextUtxo = utxosLeft.head
        val approxUtxoSize = CoinSelector.approximateUtxoSize(nextUtxo)
        val nextUtxoFee = feeRate.baseAmount * approxUtxoSize
        if (nextUtxo.output.value.satoshis.toLong < nextUtxoFee) {
          addUtxos(alreadyAdded, valueSoFar, bytesSoFar, utxosLeft.tail)
        } else {
          val newAdded = alreadyAdded.:+(nextUtxo)
          val newValue = valueSoFar + nextUtxo.output.value

          addUtxos(newAdded,
                   newValue,
                   bytesSoFar + approxUtxoSize,
                   utxosLeft.tail)
        }
      }
    }

    addUtxos(Vector.empty, CurrencyUnits.zero, bytesSoFar = 0L, walletUtxos)
  }
}

object CoinSelector extends CoinSelector {

  /** Cribbed from [[https://github.com/bitcoinjs/coinselect/blob/master/utils.js]] */
  def approximateUtxoSize(utxo: SpendingInfoDb): Long = {
    val inputBase = 32 + 4 + 1 + 4
    val scriptSize = utxo.redeemScriptOpt match {
      case Some(script) => script.bytes.length
      case None =>
        utxo.scriptWitnessOpt match {
          case Some(script) => script.bytes.length
          case None         => 25 // PUBKEYHASH
        }
    }

    inputBase + scriptSize
  }
}
