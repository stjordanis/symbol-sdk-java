/*
 * Copyright 2018 NEM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nem.core.crypto;

import io.nem.core.test.Utils;
import java.math.BigInteger;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

public class SignerTest {

    @Test
    public void canCreateSignerFromKeyPair() {
        // Act:
        Signer signer = new Signer(new KeyPair());

        // Assert
        Assertions.assertNotNull(signer);
    }

    @Test
    public void canCreateSignerFromSigner() {
        // Arrange:
        final SignerContext context = new SignerContext();

        // Act:
        Signer signer = new Signer(context.dsaSigner);

        // Assert
        Assertions.assertNotNull(signer);
    }

    @Test
    public void ctorDelegatesToEngineCreateDsaSigner() {
        // Arrange:
        final KeyPair keyPair = new KeyPair();
        final CryptoEngine engine = Mockito.mock(CryptoEngine.class);

        // Act:
        new Signer(keyPair, engine);

        // Assert:
        Mockito.verify(engine, Mockito.times(1)).createDsaSigner(keyPair);
    }

    @Test
    public void signDelegatesToDsaSigner() {
        // Assert:
        final SignerContext context = new SignerContext();
        final Signer signer = new Signer(context.dsaSigner);

        // Act:
        signer.sign(context.data);

        // Assert:
        Mockito.verify(context.dsaSigner, Mockito.times(1)).sign(context.data);
    }

    @Test
    public void verifyDelegatesToDsaSigner() {
        // Assert:
        final SignerContext context = new SignerContext();
        final Signer signer = new Signer(context.dsaSigner);

        // Act:
        signer.verify(context.data, context.signature);

        // Assert:
        Mockito.verify(context.dsaSigner, Mockito.times(1)).verify(context.data, context.signature);
    }

    @Test
    public void isCanonicalSignatureDelegatesToDsaSigner() {
        // Assert:
        final SignerContext context = new SignerContext();
        final Signer signer = new Signer(context.dsaSigner);

        // Act:
        signer.isCanonicalSignature(context.signature);

        // Assert:
        Mockito.verify(context.dsaSigner, Mockito.times(1)).isCanonicalSignature(context.signature);
    }

    @Test
    public void makeSignatureCanonicalDelegatesToDsaSigner() {
        // Assert:
        final SignerContext context = new SignerContext();
        final Signer signer = new Signer(context.dsaSigner);

        // Act:
        signer.makeSignatureCanonical(context.signature);

        // Assert:
        Mockito.verify(context.dsaSigner, Mockito.times(1))
            .makeSignatureCanonical(context.signature);
    }

    private class SignerContext {

        private final KeyAnalyzer analyzer = Mockito.mock(KeyAnalyzer.class);
        private final DsaSigner dsaSigner = Mockito.mock(DsaSigner.class);
        private final byte[] data = Utils.generateRandomBytes();
        private final Signature signature = new Signature(BigInteger.ONE, BigInteger.ONE);

        private SignerContext() {
            Mockito.when(this.analyzer.isKeyCompressed(Mockito.any())).thenReturn(true);
            Mockito.when(this.dsaSigner.isCanonicalSignature(this.signature)).thenReturn(true);
            Mockito.when(this.dsaSigner.makeSignatureCanonical(this.signature))
                .thenReturn(this.signature);
        }
    }
}
