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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.io.File;
import java.io.FileInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.argosnotary.argos.argos4j.Argos4jError;
import com.argosnotary.argos.argos4j.rest.api.model.HashAlgorithm;

class HashUtilTest {
    private String EXPECTED_NORMALIZED_HASH = "16ef5bb126378df2f98d6a742df6ff0f7cad8cc81bfe7ce742615c7d7919024f";
    private String EXPECTED_CRLF_HASH = "8a6fddbbd8f3cc80042d6df6db9488df61905d4eddfa2f25b7e0da4be41314d6";
    private String EXPECTED_CR_HASH = "13968940f9e8a1eec5e60ab0deffe2f7b874b0c5e3fe03c5fe8840961aaf6fdc";

    @BeforeEach
    void setUp() throws Exception {
    }
    
	@Test
    void calculatePassphraseTest() {
        String expected = "c3719225981552ba21838aeba9179a61c0525043e7d24068ca59f811001d14f08d7fc9c71078180f6d21615874e0a652c44c67847b034523e2d40974977a3a10";
        String keyId = "ef07177be75d393163c1589f6dce3c41dd7d4ac4a0cbe4777d2aa45b53342dc6";
        String passphrase = "test";
        String actual = HashUtil.calculateHashedPassphrase(keyId, passphrase);
        
        assertEquals(expected, actual);
    }

    @Test
    void createHashTest() throws IOException {
        String unixString = "dit is een file voor testen van hashing.\n" + 
                "nog een regel\n" + 
                "";
        String windowsString = "dit is een file voor testen van hashing.\n" + 
                "nog een regel\r\n" + 
                "";
        String appleString = "dit is een file voor testen van hashing.\n" + 
                "nog een regel\r" + 
                "";
        InputStream input = new ByteArrayInputStream(unixString.getBytes());
        String hash = HashUtil.createHash(input, "unix_string", false);
        assertThat(hash, is(EXPECTED_NORMALIZED_HASH));
        input.close();
        input = new ByteArrayInputStream(unixString.getBytes());
        hash = HashUtil.createHash(input, "unix_string", true);
        assertThat(hash, is(EXPECTED_NORMALIZED_HASH));
        input.close();
        input = new ByteArrayInputStream(windowsString.getBytes());
        hash = HashUtil.createHash(input, "windowsString", true);
        assertThat(hash, is(EXPECTED_NORMALIZED_HASH));
        input.close();
        input = new ByteArrayInputStream(appleString.getBytes());
        hash = HashUtil.createHash(input, "appleString", true);
        assertThat(hash, is(EXPECTED_NORMALIZED_HASH));
        input.close();
        input = new ByteArrayInputStream(windowsString.getBytes());
        hash = HashUtil.createHash(input, "windowsString", false);
        assertThat(hash, is(EXPECTED_CRLF_HASH));
        input.close();
        input = new ByteArrayInputStream(appleString.getBytes());
        hash = HashUtil.createHash(input, "appleString", false);
        assertThat(hash, is(EXPECTED_CR_HASH));
    }
    
    @Test
    void ioExceptionTest() throws IOException {
        File file = new File("src/test/resources/files/a_file.txt");
        InputStream input = new FileInputStream(file);
        input.close();
        Throwable exception = assertThrows(Argos4jError.class, () -> {
            HashUtil.createHash(input, "unix_string", false);
          });
        assertEquals("The file unix_string couldn't be recorded: Stream Closed", exception.getMessage());
    }
    
    @Test
    void emptyFileTest() throws IOException {
        String expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        String unixString = "";
        
        InputStream input = new ByteArrayInputStream(unixString.getBytes());
        String hash = HashUtil.createHash(input, "emptyfile", false);
        assertThat(hash, is(expected));
    }
    
    @Test
    void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
      Constructor<HashUtil> constructor = HashUtil.class.getDeclaredConstructor();
      assertThat(Modifier.isPrivate(constructor.getModifiers()), is(true));
      constructor.setAccessible(true);
      constructor.newInstance();
    }

}
