package shadowsocks.crypto;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Utils{

    /**
     * Thanks go to Ola Bini for releasing this source on his blog.
     * The source was obtained from <a href="http://olabini.com/blog/tag/evp_bytestokey/">here</a> .
     */
    private static byte[][] EVP_BytesToKey(int key_len, int iv_len, MessageDigest md, byte[] salt, byte[] data, int count) {
        byte[][] both = new byte[2][];
        byte[] key = new byte[key_len];
        int key_ix = 0;
        byte[] iv = new byte[iv_len];
        int iv_ix = 0;
        both[0] = key;
        both[1] = iv;
        byte[] md_buf = null;
        int nkey = key_len;
        int niv = iv_len;
        int i = 0;
        if (data == null) {
            return both;
        }
        int addmd = 0;
        for (;;) {
            md.reset();
            if (addmd++ > 0) {
                md.update(md_buf);
            }
            md.update(data);
            if (null != salt) {
                md.update(salt, 0, 8);
            }
            md_buf = md.digest();
            for (i = 1; i < count; i++) {
                md.reset();
                md.update(md_buf);
                md_buf = md.digest();
            }
            i = 0;
            if (nkey > 0) {
                for (;;) {
                    if (nkey == 0)
                        break;
                    if (i == md_buf.length)
                        break;
                    key[key_ix++] = md_buf[i];
                    nkey--;
                    i++;
                }
            }
            if (niv > 0 && i != md_buf.length) {
                for (;;) {
                    if (niv == 0)
                        break;
                    if (i == md_buf.length)
                        break;
                    iv[iv_ix++] = md_buf[i];
                    niv--;
                    i++;
                }
            }
            if (nkey == 0 && niv == 0) {
                break;
            }
        }
        for (i = 0; i < md_buf.length; i++) {
            md_buf[i] = 0;
        }
        return both;
    }


    public static byte[] getKey(String password, int keyLen, int ivLen) throws CryptoException
    {
        MessageDigest md = null;
        byte[] passwordBytes = null;
        byte[][] keyAndIV = null;
        int i = 0;

        try{
            md = MessageDigest.getInstance("MD5");
            passwordBytes = password.getBytes("ASCII");
        }catch(NoSuchAlgorithmException | UnsupportedEncodingException e){
            throw new CryptoException(e);
        }

        //This key should equal EVP_BytesToKey with no salt and count = 1
        keyAndIV = EVP_BytesToKey(keyLen, ivLen, md, null, passwordBytes, 1);

        //Discard the iv.
        return keyAndIV[0];
    }

    public static byte[] randomBytes(int size) {
        byte[] bytes = new byte[size];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }


    public static byte[] md5(byte[] source) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(source);
        } catch (Exception e) {
            // 抛出去
            throw new RuntimeException(e);
        }
    }

}
