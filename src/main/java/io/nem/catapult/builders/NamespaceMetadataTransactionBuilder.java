/**
 * ** Copyright (c) 2016-present, ** Jaguar0625, gimre, BloodyRookie, Tech Bureau, Corp. All rights
 * reserved. ** ** This file is part of Catapult. ** ** Catapult is free software: you can
 * redistribute it and/or modify ** it under the terms of the GNU Lesser General Public License as
 * published by ** the Free Software Foundation, either version 3 of the License, or ** (at your
 * option) any later version. ** ** Catapult is distributed in the hope that it will be useful, **
 * but WITHOUT ANY WARRANTY; without even the implied warranty of ** MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the ** GNU Lesser General Public License for more details. ** ** You
 * should have received a copy of the GNU Lesser General Public License ** along with Catapult. If
 * not, see <http://www.gnu.org/licenses/>.
 **/

package io.nem.catapult.builders;

import java.io.DataInput;
import java.nio.ByteBuffer;

/**
 * Binary layout for a non-embedded namespace metadata transaction.
 */
public final class NamespaceMetadataTransactionBuilder extends TransactionBuilder {

    /**
     * Namespace metadata transaction body.
     */
    private final NamespaceMetadataTransactionBodyBuilder namespaceMetadataTransactionBody;

    /**
     * Constructor - Creates an object from stream.
     *
     * @param stream Byte stream to use to serialize the object.
     */
    protected NamespaceMetadataTransactionBuilder(final DataInput stream) {
        super(stream);
        this.namespaceMetadataTransactionBody = NamespaceMetadataTransactionBodyBuilder
            .loadFromBinary(stream);
    }

    /**
     * Constructor.
     *
     * @param signature Entity signature.
     * @param signer Entity signer's public key.
     * @param version Entity version.
     * @param type Entity type.
     * @param fee Transaction fee.
     * @param deadline Transaction deadline.
     * @param targetPublicKey Metadata target public key.
     * @param scopedMetadataKey Metadata key scoped to source, target and type.
     * @param targetNamespaceId Target namespace identifier.
     * @param valueSizeDelta Change in value size in bytes.
     * @param value Difference between existing value and new value \note when there is no existing
     * value, new value is same this value \note when there is an existing value, new value is
     * calculated as xor(previous-value, value).
     */
    protected NamespaceMetadataTransactionBuilder(final SignatureDto signature, final KeyDto signer,
        final short version, final EntityTypeDto type, final AmountDto fee,
        final TimestampDto deadline, final KeyDto targetPublicKey, final long scopedMetadataKey,
        final NamespaceIdDto targetNamespaceId, final short valueSizeDelta,
        final ByteBuffer value) {
        super(signature, signer, version, type, fee, deadline);
        this.namespaceMetadataTransactionBody = NamespaceMetadataTransactionBodyBuilder
            .create(targetPublicKey, scopedMetadataKey, targetNamespaceId, valueSizeDelta, value);
    }

    /**
     * Creates an instance of NamespaceMetadataTransactionBuilder.
     *
     * @param signature Entity signature.
     * @param signer Entity signer's public key.
     * @param version Entity version.
     * @param type Entity type.
     * @param fee Transaction fee.
     * @param deadline Transaction deadline.
     * @param targetPublicKey Metadata target public key.
     * @param scopedMetadataKey Metadata key scoped to source, target and type.
     * @param targetNamespaceId Target namespace identifier.
     * @param valueSizeDelta Change in value size in bytes.
     * @param value Difference between existing value and new value \note when there is no existing
     * value, new value is same this value \note when there is an existing value, new value is
     * calculated as xor(previous-value, value).
     * @return Instance of NamespaceMetadataTransactionBuilder.
     */
    public static NamespaceMetadataTransactionBuilder create(final SignatureDto signature,
        final KeyDto signer, final short version, final EntityTypeDto type, final AmountDto fee,
        final TimestampDto deadline, final KeyDto targetPublicKey, final long scopedMetadataKey,
        final NamespaceIdDto targetNamespaceId, final short valueSizeDelta,
        final ByteBuffer value) {
        return new NamespaceMetadataTransactionBuilder(signature, signer, version, type, fee,
            deadline, targetPublicKey, scopedMetadataKey, targetNamespaceId, valueSizeDelta, value);
    }

    /**
     * Creates an instance of NamespaceMetadataTransactionBuilder from a stream.
     *
     * @param stream Byte stream to use to serialize the object.
     * @return Instance of NamespaceMetadataTransactionBuilder.
     */
    public static NamespaceMetadataTransactionBuilder loadFromBinary(final DataInput stream) {
        return new NamespaceMetadataTransactionBuilder(stream);
    }

    /**
     * Gets metadata target public key.
     *
     * @return Metadata target public key.
     */
    public KeyDto getTargetPublicKey() {
        return this.namespaceMetadataTransactionBody.getTargetPublicKey();
    }

    /**
     * Gets metadata key scoped to source, target and type.
     *
     * @return Metadata key scoped to source, target and type.
     */
    public long getScopedMetadataKey() {
        return this.namespaceMetadataTransactionBody.getScopedMetadataKey();
    }

    /**
     * Gets target namespace identifier.
     *
     * @return Target namespace identifier.
     */
    public NamespaceIdDto getTargetNamespaceId() {
        return this.namespaceMetadataTransactionBody.getTargetNamespaceId();
    }

    /**
     * Gets change in value size in bytes.
     *
     * @return Change in value size in bytes.
     */
    public short getValueSizeDelta() {
        return this.namespaceMetadataTransactionBody.getValueSizeDelta();
    }

    /**
     * Gets difference between existing value and new value \note when there is no existing value,
     * new value is same this value \note when there is an existing value, new value is calculated
     * as xor(previous-value, value).
     *
     * @return Difference between existing value and new value \note when there is no existing
     * value, new value is same this value \note when there is an existing value, new value is
     * calculated as xor(previous-value, value).
     */
    public ByteBuffer getValue() {
        return this.namespaceMetadataTransactionBody.getValue();
    }

    /**
     * Gets the size of the object.
     *
     * @return Size in bytes.
     */
    @Override
    public int getSize() {
        int size = super.getSize();
        size += this.namespaceMetadataTransactionBody.getSize();
        return size;
    }

    /**
     * Serializes an object to bytes.
     *
     * @return Serialized bytes.
     */
    public byte[] serialize() {
        return GeneratorUtils.serialize(dataOutputStream -> {
            final byte[] superBytes = super.serialize();
            dataOutputStream.write(superBytes, 0, superBytes.length);
            final byte[] namespaceMetadataTransactionBodyBytes = this.namespaceMetadataTransactionBody
                .serialize();
            dataOutputStream.write(namespaceMetadataTransactionBodyBytes, 0,
                namespaceMetadataTransactionBodyBytes.length);
        });
    }
}