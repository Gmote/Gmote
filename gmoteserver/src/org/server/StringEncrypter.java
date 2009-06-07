/**
 * Copyright 2009 Marc Stogaitis and Mimi Sun
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

package org.gmote.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.gmote.server.settings.SystemPaths;


//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;

/**
 * Based on the example code found at:
 * http://www.devx.com/Java/10MinuteSolution/21385/1954 from Javid Jamae
 * 
 * Allows us to encrypt our passwords when storing it to a file. This is not
 * secure since the key can be reverse engineering out of this file but we
 * assume that the user's computer is reasonably secure.
 * 
 */
public class StringEncrypter {
  private static Logger LOGGER = Logger.getLogger(StringEncrypter.class.getName());

  public static final String DES_NAME = "DES";
  /**
   * Key used during encryption. This could easily be reversed engineered.
   */
  public static final String SECRET = "ABKDIEKF3Ikdiekdjfow FKEIDKSI fkeijklas2f";

  private KeySpec keySpec;
  private SecretKeyFactory keyFactory;
  private Cipher cipher;

  private static final String UNICODE_FORMAT = "UTF8";

  public static void writePasswordToFile(String password) throws EncryptionException {

    try {
    StringEncrypter se = new StringEncrypter();
    byte[] cipherText = se.encrypt(password);

    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SystemPaths.PASSWORD.getFullPath()));
    oos.writeObject(new ByteContainer(cipherText));
    oos.close();
    
    } catch (FileNotFoundException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      throw new EncryptionException(e);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      throw new EncryptionException(e);
    } catch (InvalidKeyException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      throw new EncryptionException(e);
    } catch (NoSuchAlgorithmException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      throw new EncryptionException(e);
    } catch (NoSuchPaddingException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      throw new EncryptionException(e);
    } catch (InvalidKeySpecException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      throw new EncryptionException(e);
    } catch (IllegalBlockSizeException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      throw new EncryptionException(e);
    } catch (BadPaddingException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
      throw new EncryptionException(e);
    }
  }

  public static synchronized String readPasswordFromFile() {
    
    ObjectInputStream is;
    try {
      is = new ObjectInputStream(new FileInputStream(SystemPaths.PASSWORD.getFullPath()));
      
      
      ByteContainer cipherText;
      cipherText = (ByteContainer) is.readObject();
      is.close();
      StringEncrypter se = new StringEncrypter();
      return se.decrypt(cipherText.getCipherText());
    } catch (FileNotFoundException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    } catch (InvalidKeyException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    } catch (NoSuchAlgorithmException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    } catch (NoSuchPaddingException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    } catch (InvalidKeySpecException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    } catch (IllegalBlockSizeException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    } catch (BadPaddingException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    } catch (ClassNotFoundException e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
    return "";
  }

  public StringEncrypter() throws UnsupportedEncodingException, InvalidKeyException,
      NoSuchAlgorithmException, NoSuchPaddingException {

    byte[] keyAsBytes = SECRET.getBytes(UNICODE_FORMAT);

    keySpec = new DESKeySpec(keyAsBytes);

    keyFactory = SecretKeyFactory.getInstance(DES_NAME);
    cipher = Cipher.getInstance(DES_NAME);

  }

  private byte[] encrypt(String unencryptedString) throws InvalidKeySpecException,
      InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException,
      BadPaddingException {

    SecretKey key = keyFactory.generateSecret(keySpec);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    byte[] cleartext = unencryptedString.getBytes(UNICODE_FORMAT);
    byte[] ciphertext = cipher.doFinal(cleartext);

    return ciphertext;

  }

  private String decrypt(byte[] ciphertext) throws InvalidKeySpecException,
      InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {

    SecretKey key = keyFactory.generateSecret(keySpec);
    cipher.init(Cipher.DECRYPT_MODE, key);

    byte[] cleartext = cipher.doFinal(ciphertext);

    return bytes2String(cleartext);
  }

  private static String bytes2String(byte[] bytes) {
    StringBuffer stringBuffer = new StringBuffer();
    for (int i = 0; i < bytes.length; i++) {
      stringBuffer.append((char) bytes[i]);
    }
    return stringBuffer.toString();
  }
}