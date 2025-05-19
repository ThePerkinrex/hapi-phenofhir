package es.upm.etsiinf.tfg.juanmahou.plugin.util;

import java.nio.charset.StandardCharsets;
import org.bouncycastle.crypto.digests.Blake2bDigest;
import org.bouncycastle.util.encoders.Hex;

public final class HashUtils {
    /**
     * Generate a short, reproducible hex-string hash suffix from the fully-qualified name.
     * Uses BLAKE2b with a configurable digest size.
     *
     * @param fqn         fully-qualified name to hash
     * @param lengthBytes number of bytes of output to produce (digest size)
     * @return lowercase hex string of lengthBytes*2
     */
    public static String hashSuffix(String fqn, int lengthBytes) {
        byte[] input = fqn.getBytes(StandardCharsets.UTF_8);
        // BLAKE2bDigest takes output size in bits
        Blake2bDigest digest = new Blake2bDigest(lengthBytes * 8);
        digest.update(input, 0, input.length);
        byte[] out = new byte[lengthBytes];
        digest.doFinal(out, 0);
        return Hex.toHexString(out);
    }

    /**
     * Generate a default 4-byte hash suffix.
     */
    public static String hashSuffix(String fqn) {
        return hashSuffix(fqn, 4);
    }
}
