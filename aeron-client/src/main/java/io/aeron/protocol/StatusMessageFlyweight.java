/*
 * Copyright 2014-2020 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.aeron.protocol;

import io.aeron.exceptions.AeronException;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.agrona.BitUtil.SIZE_OF_LONG;

/**
 * Flyweight for a Status Message Frame.
 * <p>
 * <a target="_blank" href="https://github.com/real-logic/aeron/wiki/Protocol-Specification#status-messages">
 *     Status Message</a> wiki page.
 */
public class StatusMessageFlyweight extends HeaderFlyweight
{
    /**
     * Length of the Status Message Frame
     */
    public static final int HEADER_LENGTH = 36;

    /**
     * Publisher should send SETUP frame
     */
    public static final short SEND_SETUP_FLAG = 0x80;

    private static final int SESSION_ID_FIELD_OFFSET = 8;
    private static final int STREAM_ID_FIELD_OFFSET = 12;
    private static final int CONSUMPTION_TERM_ID_FIELD_OFFSET = 16;
    private static final int CONSUMPTION_TERM_OFFSET_FIELD_OFFSET = 20;
    private static final int RECEIVER_WINDOW_FIELD_OFFSET = 24;
    private static final int RECEIVER_ID_FIELD_OFFSET = 28;
    private static final int APP_SPECIFIC_FEEDBACK_FIELD_OFFSET = 36;
    private static final int RECEIVER_TAG_FIELD_OFFSET = APP_SPECIFIC_FEEDBACK_FIELD_OFFSET;

    public StatusMessageFlyweight()
    {
    }

    public StatusMessageFlyweight(final ByteBuffer buffer)
    {
        super(buffer);
    }

    public StatusMessageFlyweight(final UnsafeBuffer buffer)
    {
        super(buffer);
    }

    /**
     * The session-id for the stream.
     *
     * @return session-id for the stream.
     */
    public int sessionId()
    {
        return getInt(SESSION_ID_FIELD_OFFSET, LITTLE_ENDIAN);
    }

    /**
     * Set the session-id of the stream.
     *
     * @param sessionId field value
     * @return flyweight
     */
    public StatusMessageFlyweight sessionId(final int sessionId)
    {
        putInt(SESSION_ID_FIELD_OFFSET, sessionId, LITTLE_ENDIAN);

        return this;
    }

    /**
     * The stream-id for the stream.
     *
     * @return the session-id for the stream.
     */
    public int streamId()
    {
        return getInt(STREAM_ID_FIELD_OFFSET, LITTLE_ENDIAN);
    }

    /**
     * Set the session-id for the stream.
     *
     * @param streamId field value
     * @return flyweight
     */
    public StatusMessageFlyweight streamId(final int streamId)
    {
        putInt(STREAM_ID_FIELD_OFFSET, streamId, LITTLE_ENDIAN);

        return this;
    }

    /**
     * The highest consumption offset within the term.
     *
     * @return the highest consumption offset within the term.
     */
    public int consumptionTermOffset()
    {
        return getInt(CONSUMPTION_TERM_OFFSET_FIELD_OFFSET, LITTLE_ENDIAN);
    }

    /**
     * Set the highest consumption offset within the term.
     *
     * @param termOffset field value
     * @return flyweight
     */
    public StatusMessageFlyweight consumptionTermOffset(final int termOffset)
    {
        putInt(CONSUMPTION_TERM_OFFSET_FIELD_OFFSET, termOffset, LITTLE_ENDIAN);

        return this;
    }

    /**
     * The highest consumption term-id.
     *
     * @return highest consumption term-id.
     */
    public int consumptionTermId()
    {
        return getInt(CONSUMPTION_TERM_ID_FIELD_OFFSET, LITTLE_ENDIAN);
    }

    /**
     * Set the highest consumption term-id.
     *
     * @param termId field value
     * @return flyweight
     */
    public StatusMessageFlyweight consumptionTermId(final int termId)
    {
        putInt(CONSUMPTION_TERM_ID_FIELD_OFFSET, termId, LITTLE_ENDIAN);

        return this;
    }

    /**
     * The receiver window length they will accept.
     *
     * @return receiver window length they will accept.
     */
    public int receiverWindowLength()
    {
        return getInt(RECEIVER_WINDOW_FIELD_OFFSET, LITTLE_ENDIAN);
    }

    /**
     * Set the receiver window length they will accept.
     *
     * @param receiverWindowLength field value
     * @return flyweight
     */
    public StatusMessageFlyweight receiverWindowLength(final int receiverWindowLength)
    {
        putInt(RECEIVER_WINDOW_FIELD_OFFSET, receiverWindowLength, LITTLE_ENDIAN);

        return this;
    }

    /**
     * Identifier for the receiver to distinguish them for FlowControl strategies.
     *
     * @return identifier for the receiver to distinguish them for FlowControl strategies.
     */
    public long receiverId()
    {
        return getLongUnaligned(RECEIVER_ID_FIELD_OFFSET);
    }

    /**
     * Identifier for the receiver to distinguish them for FlowControl strategies.
     *
     * @param id for the receiver to distinguish them for FlowControl strategies.
     * @return flyweight
     */
    public StatusMessageFlyweight receiverId(final long id)
    {
        return putLongUnaligned(RECEIVER_ID_FIELD_OFFSET, id);
    }

    /**
     * The length of the Application Specific Feedback (or rtag).
     *
     * @return length, in bytes, of the Application Specific Feedback (or rtag).
     */
    public int asfLength()
    {
        return (frameLength() - HEADER_LENGTH);
    }

    /**
     * The rtag (if present) from the Status Message.
     *
     * @return the rtag value or 0 if not present.
     */
    public long receiverTag()
    {
        final int frameLength = frameLength();

        if (frameLength > HEADER_LENGTH)
        {
            if (frameLength > (HEADER_LENGTH + SIZE_OF_LONG))
            {
                throw new AeronException(
                    "SM has longer application specific feedback (" + (frameLength - HEADER_LENGTH) + ") than rtag");
            }

            return getLongUnaligned(RECEIVER_TAG_FIELD_OFFSET);
        }

        return 0;
    }

    /**
     * Set the Receiver Tag for the Status Message.
     *
     * @param rtag value to set if not null
     * @return flyweight
     */
    public StatusMessageFlyweight receiverTag(final Long rtag)
    {
        if (null != rtag)
        {
            frameLength(HEADER_LENGTH + SIZE_OF_LONG);
            putLongUnaligned(RECEIVER_TAG_FIELD_OFFSET, rtag);
        }

        return this;
    }

    /**
     * Return the field offset within the flyweight for the receiver tag field.
     *
     * @return offset of receiver tag field
     */
    public static int receiverTagFieldOffset()
    {
        return RECEIVER_TAG_FIELD_OFFSET;
    }

    /**
     * Get long value from a field that is not aligned on an 8 byte boundary.
     *
     * @param offset of the field to get.
     * @return value of field.
     */
    public long getLongUnaligned(final int offset)
    {
        final long value;
        if (ByteOrder.nativeOrder() == LITTLE_ENDIAN)
        {
            value =
                (((long)getByte(offset + 7)) << 56) |
                (((long)getByte(offset + 6) & 0xFF) << 48) |
                (((long)getByte(offset + 5) & 0xFF) << 40) |
                (((long)getByte(offset + 4) & 0xFF) << 32) |
                (((long)getByte(offset + 3) & 0xFF) << 24) |
                (((long)getByte(offset + 2) & 0xFF) << 16) |
                (((long)getByte(offset + 1) & 0xFF) << 8) |
                (((long)getByte(offset) & 0xFF));
        }
        else
        {
            value =
                (((long)getByte(offset)) << 56) |
                (((long)getByte(offset + 1) & 0xFF) << 48) |
                (((long)getByte(offset + 2) & 0xFF) << 40) |
                (((long)getByte(offset + 3) & 0xFF) << 32) |
                (((long)getByte(offset + 4) & 0xFF) << 24) |
                (((long)getByte(offset + 5) & 0xFF) << 16) |
                (((long)getByte(offset + 6) & 0xFF) << 8) |
                (((long)getByte(offset + 7) & 0xFF));
        }

        return value;
    }

    /**
     * Set long value into a field that is not aligned on an 8 byte boundary.
     *
     * @param offset of the field to put.
     * @param value of the field to pu.
     * @return this for fluent API.
     */
    public StatusMessageFlyweight putLongUnaligned(final int offset, final long value)
    {
        if (ByteOrder.nativeOrder() == LITTLE_ENDIAN)
        {
            putByte(offset + 7, (byte)(value >> 56));
            putByte(offset + 6, (byte)(value >> 48));
            putByte(offset + 5, (byte)(value >> 40));
            putByte(offset + 4, (byte)(value >> 32));
            putByte(offset + 3, (byte)(value >> 24));
            putByte(offset + 2, (byte)(value >> 16));
            putByte(offset + 1, (byte)(value >> 8));
            putByte(offset, (byte)(value));
        }
        else
        {
            putByte(offset, (byte)(value >> 56));
            putByte(offset + 1, (byte)(value >> 48));
            putByte(offset + 2, (byte)(value >> 40));
            putByte(offset + 3, (byte)(value >> 32));
            putByte(offset + 4, (byte)(value >> 24));
            putByte(offset + 5, (byte)(value >> 16));
            putByte(offset + 6, (byte)(value >> 8));
            putByte(offset + 7, (byte)(value));
        }

        return this;
    }

    public String toString()
    {
        return "STATUS{" +
            "frame-length=" + frameLength() +
            " version=" + version() +
            " flags=" + String.valueOf(flagsToChars(flags())) +
            " type=" + headerType() +
            " session-id=" + sessionId() +
            " stream-id=" + streamId() +
            " consumption-term-id=" + consumptionTermId() +
            " consumption-term-offset=" + consumptionTermOffset() +
            " receiver-window-length=" + receiverWindowLength() +
            "}";
    }
}
