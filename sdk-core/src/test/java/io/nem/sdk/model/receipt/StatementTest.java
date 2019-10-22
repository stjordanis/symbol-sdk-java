package io.nem.sdk.model.receipt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.nem.sdk.model.account.Address;
import io.nem.sdk.model.blockchain.NetworkType;
import io.nem.sdk.model.mosaic.MosaicId;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class StatementTest {

    static List<TransactionStatement> transactionStatements = new ArrayList<>();
    static List<AddressResolutionStatement> addressResolutionStatements = new ArrayList<>();
    static List<MosaicResolutionStatement> mosaicResolutionStatements = new ArrayList<>();

    @BeforeAll
    public static void setup() {
        ReceiptSource receiptSource = new ReceiptSource(1, 1);
        MosaicId mosaicId = new MosaicId("85BBEA6CC462B244");
        Address address =
            new Address("SDGLFW-DSHILT-IUHGIB-H5UGX2-VYF5VN-JEKCCD-BR26", NetworkType.MIJIN_TEST);

        receiptSource = new ReceiptSource(1, 1);
        ResolutionEntry<Address> addressAliasResolutionEntry =
            new ResolutionEntry<>(address, receiptSource, ReceiptType.ADDRESS_ALIAS_RESOLUTION);
        ResolutionEntry<MosaicId> mosaicIdResolutionEntry =
            new ResolutionEntry<>(mosaicId, receiptSource, ReceiptType.MOSAIC_ALIAS_RESOLUTION);

        List<ResolutionEntry<Address>> addressEntries = new ArrayList<>();
        addressEntries.add(addressAliasResolutionEntry);

        List<ResolutionEntry<MosaicId>> mosaicEntries = new ArrayList<>();
        mosaicEntries.add(mosaicIdResolutionEntry);
        ArtifactExpiryReceipt<MosaicId> mosaicExpiryReceipt =
            new ArtifactExpiryReceipt<>(
                mosaicId, ReceiptType.MOSAIC_EXPIRED, ReceiptVersion.ARTIFACT_EXPIRY);
        List<Receipt> receipts = new ArrayList<>();
        receipts.add(mosaicExpiryReceipt);

        transactionStatements
            .add(new TransactionStatement(BigInteger.TEN, receiptSource, receipts));
        addressResolutionStatements.add(
            new AddressResolutionStatement(BigInteger.TEN, address, addressEntries));
        mosaicResolutionStatements.add(
            new MosaicResolutionStatement(BigInteger.TEN, mosaicId, mosaicEntries));
    }

    @Test
    void shouldCreateAddressResolutionTransactionStatement() {
        Statement statement =
            new Statement(
                transactionStatements, addressResolutionStatements, mosaicResolutionStatements);
        assertEquals(statement.getAddressResolutionStatements(), addressResolutionStatements);
        assertEquals(statement.getMosaicResolutionStatement(), mosaicResolutionStatements);
        assertEquals(statement.getTransactionStatements(), transactionStatements);
    }
}