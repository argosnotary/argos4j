/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2021 Gerard Borst <gerard.borst@argosnotary.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.argosnotary.argos.argos4j.internal.crypto;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8EncryptorBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.bouncycastle.util.io.pem.PemObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.argos4j.Argos4jError;
import com.argosnotary.argos.argos4j.rest.api.model.KeyAlgorithm;
import com.argosnotary.argos.argos4j.rest.api.model.KeyPair;
import com.argosnotary.argos.argos4j.rest.api.model.ServiceAccountKeyPair;
import com.argosnotary.argos.argos4j.rest.api.model.Signature;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignerTest {
	
	static {
	    Security.addProvider(new BouncyCastleProvider());
	}

    private static final char[] PASSWORD = "password".toCharArray();
    private static final char[] OTHER_PASSWORD = "other password".toCharArray();

    private KeyPair pair1;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws GeneralSecurityException, IOException, OperatorCreationException {
    	pair1 = createKeyPair(PASSWORD);
    	publicKey = instance(pair1.getPublicKey());
    }

    @Test
    void sign() throws DecoderException, GeneralSecurityException {
        Signature signature = Signer.sign(pair1, PASSWORD, "string to sign");
        assertThat(signature.getKeyId(), is(pair1.getKeyId()));

        java.security.Signature signatureValidator = java.security.Signature.getInstance("SHA384withECDSA");
        signatureValidator.initVerify(publicKey);
        signatureValidator.update("string to sign".getBytes(StandardCharsets.UTF_8));

        assertTrue(signatureValidator.verify(Hex.decodeHex(signature.getSignature())));
    }
    
    @Test
    void signInvalidPassword() throws DecoderException, GeneralSecurityException {
        Throwable exception = assertThrows(Argos4jError.class, () -> {
    		Signer.sign(pair1, OTHER_PASSWORD, "string to sign");
          });
    	assertEquals("unable to read encrypted data: Error finalising cipher", exception.getMessage());
    }
    
    @Test
    void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
      Constructor<Signer> constructor = Signer.class.getDeclaredConstructor();
      assertThat(Modifier.isPrivate(constructor.getModifiers()), is(true));
      constructor.setAccessible(true);
      constructor.newInstance();
    }
    


    private java.security.PublicKey instance(byte[] encodedKey) throws GeneralSecurityException, IOException {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(encodedKey);
        KeyFactory keyFactory = null;
        try (ASN1InputStream aIn = new ASN1InputStream(encodedKey)) {
            SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(aIn.readObject());
            keyFactory = KeyFactory.getInstance(info.getAlgorithm().getAlgorithm().getId(), "BC");
        }
        return keyFactory.generatePublic(x509EncodedKeySpec);
    }
    
    public static KeyPair createKeyPair(char[] passphrase) throws NoSuchAlgorithmException, OperatorCreationException, PemGenerationException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyAlgorithm.EC.name());
		java.security.KeyPair keyPair = generator.generateKeyPair();
        JceOpenSSLPKCS8EncryptorBuilder encryptorBuilder = new JceOpenSSLPKCS8EncryptorBuilder(PKCS8Generator.AES_256_CBC).setProvider("BC");  
        OutputEncryptor encryptor = encryptorBuilder
        		.setRandom(new SecureRandom())
        		.setPasssword(passphrase).build();
      
        JcaPKCS8Generator gen2 = new JcaPKCS8Generator(keyPair.getPrivate(), encryptor);  
        PemObject obj2 = gen2.generate();
        KeyPair newKey = new KeyPair();
        newKey.setKeyId(computeKeyId(keyPair.getPublic()));
        newKey.setPublicKey(keyPair.getPublic().getEncoded());
        newKey.setEncryptedPrivateKey(obj2.getContent());
        return newKey;
	}
    
    private static String computeKeyId(PublicKey publicKey) {
        return DigestUtils.sha256Hex(publicKey.getEncoded());
    }
    
}
