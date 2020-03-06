package pony.xcode.media.utils;

import java.io.FileInputStream;
import java.security.MessageDigest;

public class Digest {
    /**
     * 获取文件MD5值
     */
    public static String computeToQMD5(FileInputStream inputStream) {
        return computeToQ(inputStream);
    }

    private static String computeToQ(FileInputStream fis) {

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024 * 1024];
            for (int bytesRead; (bytesRead = fis.read(buffer)) != -1; ) {
                messageDigest.update(buffer, 0, bytesRead);
            }

            return bytesToHex(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(byteToHex(b));
        }

        return sb.toString();
    }

    private static String byteToHex(byte b) {
        String hex = Integer.toHexString(0xFF & b);
        return b >= 0 && b <= 15 ? '0' + hex : hex;
    }
}
