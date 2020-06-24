CREATE TABLE "wallet_dlcs" ("id" SERIAL UNIQUE,"eventId" TEXT NOT NULL UNIQUE,"isInitiator" BOOLEAN NOT NULL, "account" TEXT NOT NULL, "keyIndex" INTEGER NOT NULL,"initiatorWinSig" TEXT,"initiatorLoseSig" TEXT,"initiatorRefundSig" TEXT,"oracleSig" TEXT, constraint "pk_dlc" primary key("eventId"));
CREATE INDEX "wallet_dlcs_eventId_index" on "wallet_dlcs" ("eventId");

CREATE TABLE "wallet_dlc_offers" ("id" SERIAL UNIQUE,"eventId" TEXT NOT NULL UNIQUE,"network" TEXT NOT NULL,"oraclePubKey" TEXT NOT NULL,"oracleRValue" TEXT NOT NULL,"contractInfo" TEXT NOT NULL,"penaltyTimeout" TEXT NOT NULL,"contractMaturity" TEXT NOT NULL,"contractTimeout" TEXT NOT NULL,"fundingKey" TEXT NOT NULL,"toLocalCETKey" TEXT NOT NULL,"finalAddress" TEXT NOT NULL,"totalCollateral" INTEGER NOT NULL,"feeRate" TEXT,"changeAddress" TEXT NOT NULL,constraint "pk_dlc_offer" primary key("eventId"),constraint "fk_eventId" foreign key("eventId") references "wallet_dlcs"("eventId")on update NO ACTION on delete NO ACTION);
CREATE INDEX "wallet_dlc_offers_eventId_index" on "wallet_dlc_offers" ("eventId");

CREATE TABLE "wallet_dlc_accepts" ("id" SERIAL UNIQUE,"eventId" TEXT NOT NULL UNIQUE,"fundingKey" TEXT NOT NULL,"toLocalCETKey" TEXT NOT NULL,"finalAddress" TEXT NOT NULL,"totalCollateral" INTEGER NOT NULL,"winSig" TEXT NOT NULL,"loseSig" TEXT NOT NULL,"refundSig" TEXT NOT NULL,"changeAddress" TEXT NOT NULL,constraint "pk_dlc_accept" primary key("eventId"), constraint "fk_eventId" foreign key("eventId") references "wallet_dlcs"("eventId") on update NO ACTION on delete NO ACTION);
CREATE INDEX "wallet_dlc_accepts_eventId_index" on "wallet_dlc_accepts" ("eventId");

CREATE TABLE "wallet_dlc_funding_inputs" ("id" SERIAL UNIQUE,"eventId" TEXT NOT NULL,"isInitiator" BOOLEAN NOT NULL,"outPoint" TEXT NOT NULL UNIQUE,"output" TEXT NOT NULL,"sigs" TEXT,constraint "pk_dlcInput" primary key("outPoint"), constraint "fk_eventId" foreign key("eventId") references "wallet_dlcs"("eventId") on update NO ACTION on delete NO ACTION);