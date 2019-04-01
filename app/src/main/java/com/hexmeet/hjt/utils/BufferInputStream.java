package com.hexmeet.hjt.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class BufferInputStream extends InputStream {
    private ByteBuffer buf;

    public BufferInputStream(ByteBuffer buf) {
        super();
        this.buf = buf;
    }

    /* (non-Javadoc)
     * @see java.io.Reader#close()
     */
    @Override
    public void close() throws IOException {
        buf.clear();
    }

    /* (non-Javadoc)
     * @see java.io.Reader#read(char[], int, int)
     */
    @Override
    public int read(byte[] dst, int offset, int count) throws IOException {
        if (buf.hasRemaining()) {
            int size = Math.min(buf.remaining(), count);
            buf.get(dst, offset, size);
            return size;
        } else {
            return -1;
        }
    }

    @Override
    public int read() throws IOException {
        if (buf.hasRemaining()) {
            return buf.get() & 0xff;
        } else {
            return -1;
        }
    }

    @Override
    public int available() throws IOException {
        return buf.remaining();
    }
}
