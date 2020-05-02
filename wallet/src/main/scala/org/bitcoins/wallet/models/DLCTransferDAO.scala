package org.bitcoins.wallet.models

import org.bitcoins.core.currency.CurrencyUnit
import org.bitcoins.core.protocol.{BitcoinAddress, BlockStampWithFuture}
import org.bitcoins.crypto.{ECPublicKey, Sha256Digest, Sha256DigestBE}
import org.bitcoins.db.{CRUD, SlickUtil}
import org.bitcoins.wallet.config._
import slick.lifted.{PrimaryKey, ProvenShape}

import scala.concurrent.{ExecutionContext, Future}

case class DLCTransferDAO()(
    implicit val ec: ExecutionContext,
    override val appConfig: WalletAppConfig)
    extends CRUD[DLCTransferDb, Sha256DigestBE]
    with SlickUtil[DLCTransferDb, Sha256DigestBE] {
  import org.bitcoins.db.DbCommonsColumnMappers._
  import profile.api._

  override val table: TableQuery[DLCTransferTable] =
    TableQuery[DLCTransferTable]

  override def createAll(
      ts: Vector[DLCTransferDb]): Future[Vector[DLCTransferDb]] =
    createAllNoAutoInc(ts, safeDatabase)

  override protected def findByPrimaryKeys(ids: Vector[Sha256DigestBE]): Query[
    DLCTransferTable,
    DLCTransferDb,
    Seq] =
    table.filter(_.eventId.inSet(ids))

  override def findByPrimaryKey(
      id: Sha256DigestBE): Query[DLCTransferTable, DLCTransferDb, Seq] = {
    table
      .filter(_.eventId === id)
  }

  override def findAll(dlcs: Vector[DLCTransferDb]): Query[
    DLCTransferTable,
    DLCTransferDb,
    Seq] =
    findByPrimaryKeys(dlcs.map(_.eventId))

  def findByEventId(eventId: Sha256DigestBE): Future[Option[DLCTransferDb]] = {
    val q = table.filter(_.eventId === eventId)

    safeDatabase.run(q.result).map {
      case h +: Vector() =>
        Some(h)
      case Vector() =>
        None
      case dlcs: Vector[DLCTransferDb] =>
        throw new RuntimeException(
          s"More than one DLC Transfer per eventId ($eventId), got: $dlcs")
    }
  }

  def findByEventId(eventId: Sha256Digest): Future[Option[DLCTransferDb]] =
    findByEventId(eventId.flip)

  class DLCTransferTable(tag: Tag)
      extends Table[DLCTransferDb](tag, "wallet_dlc_transfers") {

    import org.bitcoins.db.DbCommonsColumnMappers._

    def eventId: Rep[Sha256DigestBE] = column("eventId", O.Unique)

    def timeout: Rep[BlockStampWithFuture] = column("timeout")

    def buyout: Rep[CurrencyUnit] = column("buyout")

    def fee: Rep[CurrencyUnit] = column("fee")

    def fundingKey: Rep[ECPublicKey] = column("fundingKey")

    def toLocalCETKey: Rep[ECPublicKey] = column("toLocalCETKey")

    def finalAddress: Rep[BitcoinAddress] = column("finalAddress")

    def sellerFundingKeyOpt: Rep[Option[ECPublicKey]] =
      column("sellerFundingKey")

    private type DLCTuple =
      (
          Sha256DigestBE,
          BlockStampWithFuture,
          CurrencyUnit,
          CurrencyUnit,
          ECPublicKey,
          ECPublicKey,
          BitcoinAddress,
          Option[ECPublicKey])

    private val fromTuple: DLCTuple => DLCTransferDb = {
      case (eventId,
            timeout,
            buyout,
            fee,
            fundingKey,
            toLocalCETKey,
            finalAddress,
            sellerFundingKeyOpt) =>
        DLCTransferDb(
          eventId,
          timeout,
          buyout,
          fee,
          fundingKey,
          toLocalCETKey,
          finalAddress,
          sellerFundingKeyOpt
        )
    }

    private val toTuple: DLCTransferDb => Option[DLCTuple] = dlc =>
      Some(
        (dlc.eventId,
         dlc.timeout,
         dlc.buyout,
         dlc.fee,
         dlc.fundingKey,
         dlc.toLocalCETKey,
         dlc.finalAddress,
         dlc.sellerFundingKeyOpt))

    def * : ProvenShape[DLCTransferDb] =
      (eventId,
       timeout,
       buyout,
       fee,
       fundingKey,
       toLocalCETKey,
       finalAddress,
       sellerFundingKeyOpt) <> (fromTuple, toTuple)

    def primaryKey: PrimaryKey =
      primaryKey(name = "pk_dlc", sourceColumns = eventId)
  }
}
