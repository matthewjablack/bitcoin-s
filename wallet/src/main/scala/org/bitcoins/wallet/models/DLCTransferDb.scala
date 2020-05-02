package org.bitcoins.wallet.models

import org.bitcoins.commons.jsonmodels.dlc.DLCPublicKeys
import org.bitcoins.core.currency.CurrencyUnit
import org.bitcoins.core.protocol.{BitcoinAddress, BlockStampWithFuture}
import org.bitcoins.crypto.{ECPublicKey, Sha256DigestBE}

case class DLCTransferDb(
    eventId: Sha256DigestBE,
    timeout: BlockStampWithFuture,
    buyout: CurrencyUnit,
    fee: CurrencyUnit,
    fundingKey: ECPublicKey,
    toLocalCETKey: ECPublicKey,
    finalAddress: BitcoinAddress,
    sellerFundingKeyOpt: Option[ECPublicKey])

object DLCTransferDb {

  def apply(
      eventId: Sha256DigestBE,
      timeout: BlockStampWithFuture,
      buyout: CurrencyUnit,
      fee: CurrencyUnit,
      pubKeys: DLCPublicKeys,
      sellerFundingKeyOpt: Option[ECPublicKey]): DLCTransferDb = {
    DLCTransferDb(eventId,
                  timeout,
                  buyout,
                  fee,
                  pubKeys.fundingKey,
                  pubKeys.toLocalCETKey,
                  pubKeys.finalAddress,
                  sellerFundingKeyOpt)
  }
}
