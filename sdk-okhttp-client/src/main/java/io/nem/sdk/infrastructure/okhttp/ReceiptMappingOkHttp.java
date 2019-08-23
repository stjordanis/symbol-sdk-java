/*
 *  Copyright 2019 NEM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nem.sdk.infrastructure.okhttp;

import io.nem.sdk.model.account.Address;
import io.nem.sdk.model.account.PublicAccount;
import io.nem.sdk.model.blockchain.NetworkType;
import io.nem.sdk.model.mosaic.MosaicId;
import io.nem.sdk.model.namespace.AddressAlias;
import io.nem.sdk.model.namespace.MosaicAlias;
import io.nem.sdk.model.receipt.ArtifactExpiryReceipt;
import io.nem.sdk.model.receipt.BalanceChangeReceipt;
import io.nem.sdk.model.receipt.BalanceTransferReceipt;
import io.nem.sdk.model.receipt.InflationReceipt;
import io.nem.sdk.model.receipt.Receipt;
import io.nem.sdk.model.receipt.ReceiptSource;
import io.nem.sdk.model.receipt.ReceiptType;
import io.nem.sdk.model.receipt.ReceiptVersion;
import io.nem.sdk.model.receipt.ResolutionEntry;
import io.nem.sdk.model.receipt.ResolutionStatement;
import io.nem.sdk.model.receipt.Statement;
import io.nem.sdk.model.receipt.TransactionStatement;
import io.nem.sdk.model.transaction.JsonHelper;
import io.nem.sdk.model.transaction.UInt64;
import io.nem.sdk.openapi.okhttp_gson.model.ArtifactExpiryReceiptDTO;
import io.nem.sdk.openapi.okhttp_gson.model.BalanceChangeReceiptDTO;
import io.nem.sdk.openapi.okhttp_gson.model.BalanceTransferReceiptDTO;
import io.nem.sdk.openapi.okhttp_gson.model.InflationReceiptDTO;
import io.nem.sdk.openapi.okhttp_gson.model.ResolutionStatementDTO;
import io.nem.sdk.openapi.okhttp_gson.model.StatementsDTO;
import io.nem.sdk.openapi.okhttp_gson.model.TransactionStatementDTO;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;


public class ReceiptMappingOkHttp {

    private final JsonHelper jsonHelper;

    public ReceiptMappingOkHttp(JsonHelper jsonHelper) {
        this.jsonHelper = jsonHelper;
    }

    public Statement createStatementFromDto(StatementsDTO input, NetworkType networkType) {
        List<TransactionStatement> transactionStatements =
            input.getTransactionStatements().stream()
                .map(receiptDto -> createTransactionStatement(receiptDto, networkType))
                .collect(Collectors.toList());
        List<ResolutionStatement<Address>> addressResolutionStatements =
            input.getAddressResolutionStatements().stream()
                .map(this::createAddressResolutionStatementFromDto)
                .collect(Collectors.toList());
        List<ResolutionStatement<MosaicId>> mosaicResolutionStatements =
            input.getMosaicResolutionStatements().stream()
                .map(this::createMosaicResolutionStatementFromDto)
                .collect(Collectors.toList());
        return new Statement(
            transactionStatements, addressResolutionStatements, mosaicResolutionStatements);
    }


    public ResolutionStatement<Address> createAddressResolutionStatementFromDto(
        ResolutionStatementDTO receiptDto) {
        return new ResolutionStatement<>(
            extractBigInteger(receiptDto.getHeight()),
            createAddressFromNumber(receiptDto.getUnresolved()),
            receiptDto.getResolutionEntries().stream()
                .map(
                    entry ->
                        new ResolutionEntry<>(
                            new AddressAlias(
                                createAddressFromNumber(entry.getResolved())),
                            new ReceiptSource(
                                entry.getSource().getPrimaryId(),
                                entry.getSource().getSecondaryId()),
                            ReceiptType.Address_Alias_Resolution))
                .collect(Collectors.toList()));
    }

    private Address createAddressFromNumber(Object object) {
        return Address.createFromEncoded(object.toString());
    }

    public ResolutionStatement<MosaicId> createMosaicResolutionStatementFromDto(
        ResolutionStatementDTO receiptDto) {
        return new ResolutionStatement<>(
            extractBigInteger(receiptDto.getHeight()),
            new MosaicId(extractBigInteger((List<Long>) receiptDto.getUnresolved())),
            receiptDto.getResolutionEntries().stream()
                .map(
                    entry ->
                        new ResolutionEntry<>(
                            new MosaicAlias(new MosaicId(extractBigInteger(
                                (List<Long>) entry.getResolved()))),
                            new ReceiptSource(
                                entry.getSource().getPrimaryId(),
                                entry.getSource().getSecondaryId()),
                            ReceiptType.Mosaic_Alias_Resolution))
                .collect(Collectors.toList()));
    }


    public TransactionStatement createTransactionStatement(
        TransactionStatementDTO input, NetworkType networkType) {
        return new TransactionStatement(
            extractBigInteger(input.getHeight()),
            new ReceiptSource(
                input.getSource().getPrimaryId(),
                input.getSource().getSecondaryId()),
            input.getReceipts().stream()
                .map(receipt -> createReceiptFromDto(receipt, networkType))
                .collect(Collectors.toList()));
    }

    public Receipt createReceiptFromDto(Object receiptDto, NetworkType networkType) {
        ReceiptType type = ReceiptType.rawValueOf(jsonHelper.getInteger(receiptDto, "type"));
        switch (type) {
            case Harvest_Fee:
            case LockHash_Created:
            case LockHash_Completed:
            case LockHash_Expired:
            case LockSecret_Created:
            case LockSecret_Completed:
            case LockSecret_Expired:
                return createBalanceChangeReceipt(
                    jsonHelper.convert(receiptDto, BalanceChangeReceiptDTO.class), networkType);
            case Mosaic_Levy:
            case Mosaic_Rental_Fee:
            case Namespace_Rental_Fee:
                return createBalanceTransferRecipient(
                    jsonHelper.convert(receiptDto, BalanceTransferReceiptDTO.class), networkType);
            case Mosaic_Expired:
            case Namespace_Expired:
                return createArtifactExpiryReceipt(
                    jsonHelper.convert(receiptDto, ArtifactExpiryReceiptDTO.class), type);
            case Inflation:
                return createInflationReceipt(
                    jsonHelper.convert(receiptDto, InflationReceiptDTO.class));
            default:
                throw new IllegalArgumentException("Receipt type: " + type.name() + " not valid");
        }
    }


    public ArtifactExpiryReceipt<MosaicId> createArtifactExpiryReceipt(
        ArtifactExpiryReceiptDTO receipt,
        ReceiptType type) {
        return new ArtifactExpiryReceipt<>(
            new MosaicId(extractBigInteger((List<Long>) receipt.getArtifactId())),
            type,
            ReceiptVersion.ARTIFACT_EXPIRY);
    }


    public BalanceChangeReceipt createBalanceChangeReceipt(
        BalanceChangeReceiptDTO receipt, NetworkType networkType) {
        return new BalanceChangeReceipt(
            PublicAccount.createFromPublicKey(receipt.getAccount(), networkType),
            new MosaicId(extractBigInteger(receipt.getMosaicId())),
            extractBigInteger(receipt.getAmount()),
            ReceiptType.rawValueOf(receipt.getType().getValue()),
            ReceiptVersion.BALANCE_CHANGE);
    }

    public BalanceTransferReceipt<Address> createBalanceTransferRecipient(
        BalanceTransferReceiptDTO receipt, NetworkType networkType) {
        return new BalanceTransferReceipt<>(
            PublicAccount.createFromPublicKey(receipt.getSender(), networkType),
            Address.createFromEncoded(receipt.getRecipient()),
            new MosaicId(extractBigInteger(receipt.getMosaicId())),
            extractBigInteger(receipt.getAmount()),
            ReceiptType.rawValueOf(receipt.getType().getValue()),
            ReceiptVersion.BALANCE_TRANSFER);
    }

    public InflationReceipt createInflationReceipt(InflationReceiptDTO receipt) {
        return new InflationReceipt(
            new MosaicId(extractBigInteger(receipt.getMosaicId())),
            extractBigInteger(receipt.getAmount()),
            ReceiptType.rawValueOf(receipt.getType().getValue()),
            ReceiptVersion.INFLATION_RECEIPT);
    }


    private static BigInteger extractBigInteger(List<Long> input) {
        return UInt64.extractBigInteger(input);
    }

}