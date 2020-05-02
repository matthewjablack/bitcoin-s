package org.bitcoins.wallet.models

import org.bitcoins.core.psbt.InputPSBTRecord.PartialSignature
import org.bitcoins.crypto.{ECDigitalSignature, ECPublicKey, Sha256DigestBE}

case class DLCCETSignatureDb(
    eventId: Sha256DigestBE,
    outcomeHash: Sha256DigestBE,
    pubkey: ECPublicKey,
    signature: ECDigitalSignature) {

  def toTuple: (Sha256DigestBE, PartialSignature) =
    (outcomeHash, PartialSignature(pubkey, signature))
}

object DLCCETSignatureDb {

  def apply(
      eventId: Sha256DigestBE,
      outcomeHash: Sha256DigestBE,
      signature: PartialSignature): DLCCETSignatureDb =
    DLCCETSignatureDb(eventId,
                      outcomeHash,
                      signature.pubKey,
                      signature.signature)
}
