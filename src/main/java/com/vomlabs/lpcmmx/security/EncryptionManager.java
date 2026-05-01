package com.vomlabs.lpcmmx.security;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

public class EncryptionManager {
    private static final int LAYER_COUNT = 10;
    private final SecureRandom random;
    private final byte[] masterKey;

    public EncryptionManager() {
        this.random = new SecureRandom();
        this.masterKey = new byte[32];
        random.nextBytes(masterKey);
    }

    public byte[] encrypt10Layers(byte[] data) {
        byte[] current = data.clone();
        for (int layer = 0; layer < LAYER_COUNT; layer++) {
            current = encryptLayer(current, layer);
        }
        return current;
    }

    public byte[] decrypt10Layers(byte[] data) {
        byte[] current = data.clone();
        for (int layer = LAYER_COUNT - 1; layer >= 0; layer--) {
            current = decryptLayer(current, layer);
        }
        return current;
    }

    public String hash10Layers(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        for (int layer = 0; layer < LAYER_COUNT; layer++) {
            data = hashLayer(data, layer);
        }
        return Hex.toHexString(data);
    }

    private byte[] encryptLayer(byte[] data, int layer) {
        switch (layer % 4) {
            case 0: return aesEncrypt(data, deriveKey("AES", layer));
            case 1: return xorLayer(data, layer);
            case 2: return aesEncrypt(data, deriveKey("AES256", layer));
            case 3: return addIntegrityLayer(data, layer);
            default: return data;
        }
    }

    private byte[] decryptLayer(byte[] data, int layer) {
        switch (layer % 4) {
            case 0: return aesDecrypt(data, deriveKey("AES", layer));
            case 1: return xorLayer(data, layer);
            case 2: return aesDecrypt(data, deriveKey("AES256", layer));
            case 3: return removeIntegrityLayer(data, layer);
            default: return data;
        }
    }

    private byte[] hashLayer(byte[] data, int layer) {
        switch (layer % 3) {
            case 0: return sha256Hash(data);
            case 1: return sha3Hash(data, 256);
            case 2: return sha512Hash(data);
            default: return data;
        }
    }

    private byte[] aesEncrypt(byte[] data, byte[] key) {
        try {
            AESEngine engine = new AESEngine();
            engine.init(true, new KeyParameter(key));
            byte[] output = new byte[data.length];
            engine.processBlock(data, 0, output, 0);
            return output;
        } catch (Exception e) {
            return data;
        }
    }

    private byte[] aesDecrypt(byte[] data, byte[] key) {
        try {
            AESEngine engine = new AESEngine();
            engine.init(false, new KeyParameter(key));
            byte[] output = new byte[data.length];
            engine.processBlock(data, 0, output, 0);
            return output;
        } catch (Exception e) {
            return data;
        }
    }

    private byte[] xorLayer(byte[] data, int layer) {
        byte[] key = deriveKey("XOR", layer);
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return result;
    }

    private byte[] addIntegrityLayer(byte[] data, int layer) {
        byte[] hash = sha256Hash(data);
        byte[] result = new byte[data.length + hash.length];
        System.arraycopy(data, 0, result, 0, data.length);
        System.arraycopy(hash, 0, result, data.length, hash.length);
        return result;
    }

    private byte[] removeIntegrityLayer(byte[] data, int layer) {
        if (data.length <= 32) return data;
        byte[] original = new byte[data.length - 32];
        System.arraycopy(data, 0, original, 0, original.length);
        return original;
    }

    private byte[] sha256Hash(byte[] data) {
        SHA256Digest digest = new SHA256Digest();
        byte[] result = new byte[digest.getDigestSize()];
        digest.update(data, 0, data.length);
        digest.doFinal(result, 0);
        return result;
    }

    private byte[] sha3Hash(byte[] data, int bitLength) {
        org.bouncycastle.crypto.digests.SHA3Digest digest = new org.bouncycastle.crypto.digests.SHA3Digest(bitLength);
        byte[] result = new byte[digest.getDigestSize()];
        digest.update(data, 0, data.length);
        digest.doFinal(result, 0);
        return result;
    }

    private byte[] sha512Hash(byte[] data) {
        return sha3Hash(data, 512);
    }

    private byte[] deriveKey(String purpose, int layer) {
        byte[] purposeBytes = purpose.getBytes(StandardCharsets.UTF_8);
        byte[] layerBytes = String.valueOf(layer).getBytes(StandardCharsets.UTF_8);
        byte[] combined = new byte[masterKey.length + purposeBytes.length + layerBytes.length];
        System.arraycopy(masterKey, 0, combined, 0, masterKey.length);
        System.arraycopy(purposeBytes, 0, combined, masterKey.length, purposeBytes.length);
        System.arraycopy(layerBytes, 0, combined, masterKey.length + purposeBytes.length, layerBytes.length);
        return sha256Hash(combined);
    }

    public String encryptConfigValue(String value) {
        byte[] encrypted = encrypt10Layers(value.getBytes(StandardCharsets.UTF_8));
        return Hex.toHexString(encrypted);
    }

    public String decryptConfigValue(String hexValue) {
        try {
            byte[] data = Hex.decode(hexValue);
            byte[] decrypted = decrypt10Layers(data);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return hexValue;
        }
    }
}
