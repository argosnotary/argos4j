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

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.input.UnixLineEndingInputStream;
import org.bouncycastle.util.encoders.Hex;

import com.argosnotary.argos.argos4j.Argos4jError;
import com.argosnotary.argos.argos4j.rest.api.model.HashAlgorithm;

public class HashUtil {
	
	private HashUtil() {}

    
    public static String calculateHashedPassphrase(String keyId, String passphrase) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(HashAlgorithm.SHA512.getValue());
        } catch (NoSuchAlgorithmException e) {
            throw new Argos4jError(e.getMessage());
        }
        byte[] passphraseHash = md.digest(passphrase.getBytes());
        byte [] keyIdBytes = keyId.getBytes();
        // salt with keyId
        md.update(keyIdBytes);        
        return Hex.toHexString(md.digest(passphraseHash));
    }
    
    public static String createHash(InputStream inputStream, String filename, boolean normalizeLineEndings) {
        MessageDigest digest = DigestUtils.getSha256Digest();
        try {
            InputStream file = normalizeLineEndings ? new UnixLineEndingInputStream(inputStream, false) : inputStream;
            byte[] buffer = new byte[2048];
            int len;
            while ((len = file.read(buffer)) > -1) {
                digest.update(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new Argos4jError("The file " + filename + " couldn't be recorded: " + e.getMessage());
        }
        return Hex.toHexString(digest.digest());
    }
}
