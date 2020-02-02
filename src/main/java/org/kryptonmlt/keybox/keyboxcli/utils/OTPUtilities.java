package org.kryptonmlt.keybox.keyboxcli.utils;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.kryptonmlt.keybox.keyboxcli.utils.Base32String.DecodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OTPUtilities {

  private final static Logger LOGGER = LoggerFactory.getLogger(OTPUtilities.class);
  private final String type = "TOTP";
  private final static int digits = 6;
  private final static String algo = "SHA1";
  private final static int period = 30;
  private static long counter = 0;

  public String generateOTPAccessCode(String qrCodeText) {
    long cur = System.currentTimeMillis();

    switch (type) {
      case "HOTP":
        return getHOTP(qrCodeText, counter++);
      case "TOTP":
        long counter = cur / 1000 / period;
        return getHOTP(qrCodeText, counter);
    }

    return "";
  }

  private String getHOTP(String qrCodeText, long counter) {
    // Encode counter in network byte order
    ByteBuffer bb = ByteBuffer.allocate(8);
    bb.putLong(counter);

    // Create digits divisor
    int div = 1;
    for (int i = digits; i > 0; i--) {
      div *= 10;
    }

    byte[] secret = new byte[0];
    try {
      secret = Base32String.decode(qrCodeText);
    } catch (DecodingException e) {
      LOGGER.error("Error decoding qrCode text ", e);
    }
    // Create the HMAC
    try {
      Mac mac = Mac.getInstance("Hmac" + algo);
      mac.init(new SecretKeySpec(secret, "Hmac" + algo));

      // Do the hashing
      byte[] digest = mac.doFinal(bb.array());

      // Truncate
      int binary;
      int off = digest[digest.length - 1] & 0xf;
      binary = (digest[off] & 0x7f) << 0x18;
      binary |= (digest[off + 1] & 0xff) << 0x10;
      binary |= (digest[off + 2] & 0xff) << 0x08;
      binary |= (digest[off + 3] & 0xff);

      String hotp = "";
      binary = binary % div;

      // Zero pad
      hotp = Integer.toString(binary);
      while (hotp.length() != digits) {
        hotp = "0" + hotp;
      }

      return hotp;
    } catch (InvalidKeyException e) {
      LOGGER.error("Error creating key", e);
    } catch (NoSuchAlgorithmException e) {
      LOGGER.error("Error creating key no algorithm", e);
    }
    return "";
  }
}
