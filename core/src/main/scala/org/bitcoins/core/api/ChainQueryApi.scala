package org.bitcoins.core.api

import org.bitcoins.core.crypto.DoubleSha256DigestBE

import scala.concurrent.{ExecutionContext, Future}

/**
  * This trait provides methods to query various types of blockchain data.
  */
trait ChainQueryApi {

  /** Gets the height of the given block */
  def getBlockHeight(blockHash: DoubleSha256DigestBE)(
      implicit ec: ExecutionContext): Future[Option[Int]]

  /** Gets the hash of the block that is what we consider "best" */
  def getBestBlockHash(
      implicit ec: ExecutionContext): Future[DoubleSha256DigestBE]

}