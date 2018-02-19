package org.bitcoins.core.wallet.builder

sealed abstract class TxBuilderError




object TxBuilderError {
  /** This error indicates that the transaction failed to pass the invariants the user wanted to hold
    * true after the signing process was complete. An example of this is the transaction is too big,
    * or the fee level was too high or low.
    */
  case object FailedUserInvariants extends TxBuilderError

  /** Means that we gave too many keys for the TxBuilder to use during the signing process for a
    * utxo. An example of this occurring is if we gave 2 private keys to sign a p2pkh spk.
    * A p2pkh only requires one private key to sign the utxo.
    */
  case object TooManyKeys extends TxBuilderError

  /** Means that you are using the wrong [[org.bitcoins.core.wallet.signer.Signer]] to
    * sign the given [[org.bitcoins.core.protocol.script.ScriptPubKey]]
    */
  case object WrongSigner extends TxBuilderError

  /** Can occurr when we are trying to sign a [[org.bitcoins.core.protocol.script.P2SHScriptPubKey]] but
    * we do not have a redeem script for that p2sh spk.
    */
  case object NoRedeemScript extends TxBuilderError

  /** Can occurr when we are trying to sign a [[org.bitcoins.core.protocol.script.WitnessScriptPubKey]]
    * but we do not have a [[org.bitcoins.core.protocol.script.ScriptWitness]] for that witness spk
    */
  case object NoWitness extends TxBuilderError

  /** We do not know how to sign [[org.bitcoins.core.protocol.script.NonStandardScriptPubKey]] types */
  case object NonStandardSPK extends TxBuilderError

  /** We expected a [[org.bitcoins.core.protocol.script.WitnessScriptPubKeyV0]], but got a non witness spk type */
  case object NonWitnessSPK extends TxBuilderError

  /** We cannot have a [[org.bitcoins.core.protocol.script.WitnessScriptPubKey]] nested inside of another [[org.bitcoins.core.protocol.script.WitnessScriptPubKey]] */
  case object NestedWitnessSPK extends TxBuilderError

}