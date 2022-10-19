package com.example.torrent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.libtorrent4j.Sha1Hash;
import org.libtorrent4j.swig.byte_vector;
import org.libtorrent4j.swig.sha1_hash;
import org.libtorrent4j.swig.sha256_hash;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Hash {
    private byte[] data;

    public static Hash of(sha256_hash hash) {
        return Hash.of(hash.to_bytes());
    }

    public static Hash of(sha1_hash hash) {
        return Hash.of(hash.to_bytes());
    }

    public static Hash of(Sha1Hash sha1Hash) {
        return Hash.of(sha1Hash.swig());
    }

    public static Hash of(byte_vector vector) {
        byte[] data = new byte[vector.size()];

        for (int i = 0; i < vector.size(); i++) {
            data[i] = vector.get(i);
        }

        return Hash.of(data);
    }

    public String toString() {
        return Hex.encodeHexString(data);
    }
}
