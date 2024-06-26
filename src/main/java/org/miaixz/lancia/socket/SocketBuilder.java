/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.lancia.socket;

import org.miaixz.bus.logger.Logger;
import org.miaixz.lancia.Builder;
import org.miaixz.lancia.worker.exception.SocketException;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Represents one end (client or server) of a single SocketBuilder connection. Takes care of the
 * "handshake" phase, then allows for easy sending of text frames, and receiving frames through an
 * event-based model.
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class SocketBuilder implements WebSocket {

    /**
     * The default port of WebSockets, as defined in the spec. If the nullary constructor is used,
     * DEFAULT_PORT will be the port the WebSocketServer is binded to. Note that ports under 1024
     * usually require root permissions.
     */
    public static final int DEFAULT_PORT = 80;

    /**
     * The default wss port of WebSockets, as defined in the spec. If the nullary constructor is used,
     * DEFAULT_WSS_PORT will be the port the WebSocketServer is binded to. Note that ports under 1024
     * usually require root permissions.
     */
    public static final int DEFAULT_WSS_PORT = 443;

    /**
     * Initial buffer size
     */
    public static final int RCVBUF = 16384;
    /**
     * Queue of buffers that need to be sent to the client.
     */
    public final BlockingQueue<ByteBuffer> outQueue;
    /**
     * Queue of buffers that need to be processed
     */
    public final BlockingQueue<ByteBuffer> inQueue;
    /**
     * The listener to notify of WebSocket events.
     */
    private final SocketListener listener;
    /**
     * Attribut to synchronize the write
     */
    private final Object synchronizeWriteObject = new Object();
    /**
     * When true no further frames may be submitted to be sent
     */
    private boolean flushandclosestate = false;
    /**
     * The current state of the connection
     */
    private volatile ReadyState readyState = ReadyState.NOT_YET_CONNECTED;
    /**
     * The draft which is used by this websocket
     */
    private Draft_6455 draft = null;
    /**
     * the bytes of an incomplete received handshake
     */
    private ByteBuffer tmpHandshakeBytes = ByteBuffer.allocate(0);
    /**
     * stores the handshake sent by this websocket ( Role.CLIENT only )
     */
    private HandshakeBuilder handshakeRequest = null;
    private Integer closeCode = null;
    private Boolean closeRemote = null;
    private String closeMessage = null;
    private String descriptor = null;
    /**
     * Attribute, when the last pong was received
     */
    private long lastPong = System.nanoTime();
    /**
     * Attribute to store connection attachment
     */
    private Object attachment;

    /**
     * creates a websocket with client role
     *
     * @param listener The listener for this instance
     * @param draft    The draft which should be used
     */
    public SocketBuilder(SocketListener listener, Draft_6455 draft) {
        // socket can be null because we want do be able to create the object without already having a bound channel
        if ((draft == null)) {
            throw new IllegalArgumentException("parameters must not be null");
        }
        this.outQueue = new LinkedBlockingQueue<>();
        inQueue = new LinkedBlockingQueue<>();
        this.listener = listener;
        if (draft != null) {
            this.draft = draft.copyInstance();
        }
    }

    /**
     * Method to decode the provided ByteBuffer
     *
     * @param socketBuffer the ByteBuffer to decode
     */
    public void decode(ByteBuffer socketBuffer) {
        assert (socketBuffer.hasRemaining());
        Logger.trace("process({}): ({})", socketBuffer.remaining(),
                (socketBuffer.remaining() > 1000 ? "too big to display"
                        : new String(socketBuffer.array(), socketBuffer.position(), socketBuffer.remaining())));

        if (readyState != ReadyState.NOT_YET_CONNECTED) {
            if (readyState == ReadyState.OPEN) {
                decodeFrames(socketBuffer);
            }
        } else {
            if (decodeHandshake(socketBuffer) && (!isClosing() && !isClosed())) {
                assert (tmpHandshakeBytes.hasRemaining() != socketBuffer.hasRemaining() || !socketBuffer
                        .hasRemaining()); // the buffers will never have remaining bytes at the same time
                if (socketBuffer.hasRemaining()) {
                    decodeFrames(socketBuffer);
                } else if (tmpHandshakeBytes.hasRemaining()) {
                    decodeFrames(tmpHandshakeBytes);
                }
            }
        }
    }

    /**
     * Returns whether the handshake phase has is completed. In case of a broken handshake this will
     * be never the case.
     **/
    private boolean decodeHandshake(ByteBuffer socketBufferNew) {
        ByteBuffer socketBuffer;
        if (tmpHandshakeBytes.capacity() == 0) {
            socketBuffer = socketBufferNew;
        } else {
            if (tmpHandshakeBytes.remaining() < socketBufferNew.remaining()) {
                ByteBuffer buf = ByteBuffer
                        .allocate(tmpHandshakeBytes.capacity() + socketBufferNew.remaining());
                tmpHandshakeBytes.flip();
                buf.put(tmpHandshakeBytes);
                tmpHandshakeBytes = buf;
            }

            tmpHandshakeBytes.put(socketBufferNew);
            tmpHandshakeBytes.flip();
            socketBuffer = tmpHandshakeBytes;
        }
        socketBuffer.mark();
        try {
            HandshakeBuilder tmphandshake = draft.translateHandshake(socketBuffer);
            if (Builder.MATCHED == draft.acceptHandshakeAsClient(handshakeRequest, tmphandshake)) {
                try {
                    this.listener.onWebsocketHandshakeReceivedAsClient(this, handshakeRequest, tmphandshake);
                } catch (SocketException e) {
                    Logger.trace("Closing due to invalid data exception. Possible handshake rejection", e);
                    flushAndClose(e.getValue(), e.getMessage(), false);
                    return false;
                } catch (RuntimeException e) {
                    Logger.error("Closing since client was never connected", e);
                    this.listener.onWebsocketError(this, e);
                    flushAndClose(Framedata.NEVER_CONNECTED, e.getMessage(), false);
                    return false;
                }
                open(tmphandshake);
                return true;
            } else {
                Logger.trace("Closing due to protocol error: draft {} refuses handshake", draft);
                close(Framedata.PROTOCOL_ERROR, "draft " + draft + " refuses handshake");
            }

        } catch (SocketException e) {
            Logger.trace("Closing due to invalid handshake", e);
            close(e);
        }
        return false;
    }

    private void decodeFrames(ByteBuffer socketBuffer) {
        List<Framedata> frames;
        try {
            frames = draft.translateFrame(socketBuffer);
            for (Framedata f : frames) {
                Logger.trace("matched frame: {}", f);
                draft.processFrame(this, f);
            }
        } catch (SocketException e) {
            Logger.error("Closing due to invalid data in frame", e);
            listener.onWebsocketError(this, e);
            close(e);
        } catch (VirtualMachineError | ThreadDeath | LinkageError e) {
            Logger.error("Got fatal error during frame processing");
            throw e;
        } catch (Error e) {
            Logger.error("Closing web socket due to an error during frame processing");
            Exception exception = new Exception(e);
            listener.onWebsocketError(this, exception);
            String errorMessage = "Got error " + e.getClass().getName();
            close(Framedata.UNEXPECTED_CONDITION, errorMessage);
        }
    }

    @Override
    public void close(int code) {
        close(code, "", false);
    }

    public void close(SocketException e) {
        close(e.getValue(), e.getMessage(), false);
    }

    @Override
    public void close(int code, String message) {
        close(code, message, false);
    }

    public synchronized void close(int code, String message, boolean remote) {
        if (readyState != ReadyState.CLOSING && readyState != ReadyState.CLOSED) {
            if (readyState == ReadyState.OPEN) {
                if (code == Framedata.ABNORMAL_CLOSE) {
                    assert (!remote);
                    readyState = ReadyState.CLOSING;
                    flushAndClose(code, message, false);
                    return;
                }
                if (!Builder.NONE.equals(draft.getCloseHandshakeType())) {
                    try {
                        if (!remote) {
                            try {
                                listener.onWebsocketCloseInitiated(this, code, message);
                            } catch (RuntimeException e) {
                                listener.onWebsocketError(this, e);
                            }
                        }
                        if (isOpen()) {
                            Framedata closeFrame = new Framedata(Builder.CLOSING);
                            closeFrame.setReason(message);
                            closeFrame.setCode(code);
                            closeFrame.isValid();
                            sendFrame(closeFrame);
                        }
                    } catch (SocketException e) {
                        Logger.error("generated frame is invalid", e);
                        listener.onWebsocketError(this, e);
                        flushAndClose(Framedata.ABNORMAL_CLOSE, "generated frame is invalid", false);
                    }
                }
                flushAndClose(code, message, remote);
            } else if (code == Framedata.FLASHPOLICY) {
                assert (remote);
                flushAndClose(Framedata.FLASHPOLICY, message, true);
            } else if (code == Framedata.PROTOCOL_ERROR) { // this endpoint found a PROTOCOL_ERROR
                flushAndClose(code, message, remote);
            } else {
                flushAndClose(Framedata.NEVER_CONNECTED, message, false);
            }
            readyState = ReadyState.CLOSING;
            tmpHandshakeBytes = null;
        }
    }

    public void closeConnection() {
        if (closeRemote == null) {
            throw new IllegalStateException("this method must be used in conjunction with flushAndClose");
        }
        closeConnection(closeCode, this.closeMessage, closeRemote);
    }

    protected void closeConnection(int code, boolean remote) {
        closeConnection(code, "", remote);
    }

    public void closeConnection(int code, String message) {
        closeConnection(code, message, false);
    }

    /**
     * This will close the connection immediately without a proper close handshake. The code and the
     * message therefore won't be transferred over the wire also they will be forwarded to
     * onClose/onWebsocketClose.
     *
     * @param code    the closing code
     * @param message the closing message
     * @param remote  Indicates who "generated" <code>code</code>.<br>
     *                <code>true</code> means that this endpoint received the <code>code</code> from
     *                the other endpoint.<br> false means this endpoint decided to send the given
     *                code,<br>
     *                <code>remote</code> may also be true if this endpoint started the closing
     *                handshake since the other endpoint may not simply echo the <code>code</code> but
     *                close the connection the same time this endpoint does do but with an other
     *                <code>code</code>. <br>
     **/
    public synchronized void closeConnection(int code, String message, boolean remote) {
        if (readyState == ReadyState.CLOSED) {
            return;
        }
        //Methods like eot() call this method without calling onClose(). Due to that reason we have to adjust the ReadyState manually
        if (readyState == ReadyState.OPEN) {
            if (code == Framedata.ABNORMAL_CLOSE) {
                readyState = ReadyState.CLOSING;
            }
        }
        try {
            this.listener.onWebsocketClose(this, code, message, remote);
        } catch (RuntimeException e) {

            listener.onWebsocketError(this, e);
        }
        if (draft != null) {
            draft.reset();
        }
        handshakeRequest = null;
        readyState = ReadyState.CLOSED;
    }

    public synchronized void flushAndClose(int code, String message, boolean remote) {
        if (this.flushandclosestate) {
            return;
        }
        this.closeCode = code;
        this.closeMessage = message;
        this.closeRemote = remote;

        this.flushandclosestate = true;

        this.listener.onWriteDemand(
                this); // ensures that all outgoing frames are flushed before closing the connection
        try {
            listener.onWebsocketClosing(this, code, message, remote);
        } catch (RuntimeException e) {
            Logger.error("Exception in onWebsocketClosing", e);
            listener.onWebsocketError(this, e);
        }
        if (draft != null) {
            draft.reset();
        }
        this.handshakeRequest = null;
    }

    public void eot() {
        if (readyState == ReadyState.NOT_YET_CONNECTED) {
            closeConnection(Framedata.NEVER_CONNECTED, true);
        } else if (flushandclosestate) {
            closeConnection(closeCode, this.closeMessage, closeRemote);
        } else if (Builder.NONE.equals(draft.getCloseHandshakeType())) {
            closeConnection(Framedata.NORMAL, true);
        } else if (Builder.ONEWAY.equals(draft.getCloseHandshakeType())) {
            closeConnection(Framedata.NORMAL, true);
        } else {
            closeConnection(Framedata.ABNORMAL_CLOSE, true);
        }
    }


    /**
     * Send Text data to the other end.
     */
    @Override
    public void send(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Cannot send 'null' data to a SocketBuilder.");
        }
        send(draft.createFrames(text));
    }

    private void send(Collection<Framedata> frames) {
        if (!isOpen()) {
            throw new RuntimeException("readyState is close");
        }
        if (frames == null) {
            throw new IllegalArgumentException();
        }
        ArrayList<ByteBuffer> outgoingFrames = new ArrayList<>();
        for (Framedata f : frames) {
            Logger.trace("send frame: {}", f);
            outgoingFrames.add(draft.createBinaryFrame(f));
        }
        write(outgoingFrames);
    }

    @Override
    public void sendFragmentedFrame(String op, ByteBuffer buffer, boolean fin) {
        send(draft.continuousFrame(op, buffer, fin));
    }

    @Override
    public void sendFrame(Collection<Framedata> frames) {
        send(frames);
    }

    @Override
    public void sendFrame(Framedata framedata) {
        send(Collections.singletonList(framedata));
    }

    public void sendPing() throws NullPointerException {

    }

    @Override
    public boolean hasBufferedData() {
        return !this.outQueue.isEmpty();
    }

    public void startHandshake(HandshakeBuilder handshake) throws SocketException {
        // Store the HandshakeBuilder Request we are about to send
        this.handshakeRequest = draft.postProcessHandshakeRequestAsClient(handshake);

        descriptor = handshake.getDescriptor();
        assert (descriptor != null);

        // Notify Listener
        try {
            listener.onWebsocketHandshakeSentAsClient(this, this.handshakeRequest);
        } catch (SocketException e) {
            // Stop if the client code throws an exception
            throw new SocketException(Framedata.PROTOCOL_ERROR, "HandshakeBuilder data rejected by client.");
        } catch (RuntimeException e) {
            Logger.error("Exception in startHandshake", e);
            listener.onWebsocketError(this, e);
            throw new SocketException(Framedata.PROTOCOL_ERROR, "rejected because of " + e);
        }

        // Send
        write(draft.createHandshake(this.handshakeRequest));
    }

    private void write(ByteBuffer buf) {
        Logger.trace("write({}): {}", buf.remaining(),
                buf.remaining() > 1000 ? "too big to display" : new String(buf.array()));

        outQueue.add(buf);
        listener.onWriteDemand(this);
    }

    /**
     * Write a list of bytebuffer (frames in binary form) into the outgoing queue
     *
     * @param bufs the list of bytebuffer
     */
    private void write(List<ByteBuffer> bufs) {
        synchronized (synchronizeWriteObject) {
            for (ByteBuffer b : bufs) {
                write(b);
            }
        }
    }

    private void open(HandshakeBuilder d) {
        Logger.trace("open using draft: {}", draft);
        readyState = ReadyState.OPEN;
        updateLastPong();
        try {
            listener.onWebsocketOpen(this, d);
        } catch (RuntimeException e) {
            listener.onWebsocketError(this, e);
        }
    }

    @Override
    public boolean isOpen() {
        return readyState == ReadyState.OPEN;
    }

    @Override
    public boolean isClosing() {
        return readyState == ReadyState.CLOSING;
    }

    @Override
    public boolean isFlushAndClose() {
        return flushandclosestate;
    }

    @Override
    public boolean isClosed() {
        return readyState == ReadyState.CLOSED;
    }

    @Override
    public ReadyState getReadyState() {
        return readyState;
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress() {
        return listener.getRemoteSocketAddress(this);
    }

    @Override
    public InetSocketAddress getLocalSocketAddress() {
        return listener.getLocalSocketAddress(this);
    }

    @Override
    public void close() {
        close(Framedata.NORMAL);
    }

    /**
     * Getter for the last pong received
     *
     * @return the timestamp for the last received pong
     */
    long getLastPong() {
        return lastPong;
    }

    /**
     * Update the timestamp when the last pong was received
     */
    public void updateLastPong() {
        this.lastPong = System.nanoTime();
    }

    /**
     * Getter for the websocket listener
     *
     * @return the websocket listener associated with this instance
     */
    public SocketListener getListener() {
        return listener;
    }

    @Override
    public <T> T getAttachment() {
        return (T) attachment;
    }

    @Override
    public <T> void setAttachment(T attachment) {
        this.attachment = attachment;
    }

}
