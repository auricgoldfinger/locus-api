/*
 * Copyright 2012, Asamm Software, s. r. o.
 *
 * This file is part of LocusAPI.
 *
 * LocusAPI is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * LocusAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public
 * License along with LocusAPI. If not, see
 * <http://www.gnu.org/licenses/lgpl.html/>.
 */

package locus.api.objects;

import java.io.DataInputStream;
import java.io.IOException;

import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;
import locus.api.utils.Logger;

@SuppressWarnings({"TryWithIdenticalCatches", "WeakerAccess", "unused"})
public abstract class Storable {

    // tag for logger
    private static final String TAG = "Storable";

    /*
     * Container for inner data
     */
    static class BodyContainer {

        // current item version
        int version;
        // data in item
        byte[] data;
    }

    /**
     * Default empty constructor.
     */
    public Storable() {}

    /**
     * Current object version used for storing
     *
     * @return get current version of object
     */
    protected abstract int getVersion();

    //*************************************************
    // READ PART
    //*************************************************

    // RAW DATA

    /**
     * Read content of certain item from byte array.
     *
     * @param data array with data
     * @throws IOException thrown in case of invalid data format
     */
    public void read(byte[] data) throws IOException {
        DataReaderBigEndian dr = new DataReaderBigEndian(data);
        read(dr);
    }

    // DATA READER

    /**
     * Read content of certain item from existing stream.
     *
     * @param dr stream to read for
     * @throws IOException thrown in case of invalid data format
     */
    public void read(DataReaderBigEndian dr) throws IOException {
        // read header
        BodyContainer bc = readData(dr);

        // read body
        readObject(bc.version, new DataReaderBigEndian(bc.data));
    }

    /**
     * Read header of object from stream.
     *
     * @param dr input stream
     * @return read data container
     * @throws IOException thrown in case of invalid data format
     */
    static BodyContainer readData(DataReaderBigEndian dr) throws IOException {
        // initialize container
        BodyContainer bc = new BodyContainer();

        // read basic data
        bc.version = dr.readInt();
        int size = dr.readInt();

        // check size to prevent OOE
        if (size < 0 || size > 20 * 1024 * 1024) {
            throw new IOException("item size too big, size:" + size + ", max: 20MB");
        }

        // read object data
        bc.data = dr.readBytes(size);

        // return filled container
        return bc;
    }

    // DATA INPUT STREAM

    /**
     * Read content of object from stream.
     *
     * @param input input stream
     * @throws IOException thrown in case of invalid data format
     */
    public void read(DataInputStream input) throws IOException {
        // read header
        BodyContainer bc = readData(input);

        // read body
        readObject(bc.version, new DataReaderBigEndian(bc.data));
    }

    /**
     * Read header of object from stream.
     *
     * @param dis input stream
     * @return read data container
     * @throws IOException thrown in case of invalid data format
     */
    private BodyContainer readData(DataInputStream dis) throws IOException {
        // initialize container
        BodyContainer bc = new BodyContainer();

        // read basic data
        bc.version = dis.readInt();
        int size = dis.readInt();

        // check size to prevent OOE
        if (size < 0 || size > 10 * 1024 * 1024) {
            throw new IOException("item size too big, size:" + size + ", max: 10MB");
        }

        // read object data
        bc.data = new byte[size];
        //noinspection ResultOfMethodCallIgnored
        dis.read(bc.data);

        // return filled container
        return bc;
    }

    /**
     * This function is called from {@link #read} function. Do not call it directly until you know,
     * what exactly are you doing.
     *
     * @param version version of loading content
     * @param dr      data reader with content
     * @throws IOException thrown in case of invalid data format
     */
    protected abstract void readObject(int version, DataReaderBigEndian dr) throws IOException;

    //*************************************************
    // WRITE PART
    //*************************************************

    /**
     * Write current object into writer.
     *
     * @param dw data writer
     * @throws IOException thrown in case of invalid data format
     */
    public void write(DataWriterBigEndian dw) throws IOException {
        // write version
        dw.writeInt(getVersion());

        // save position and write empty size
        dw.writeInt(0);
        int startSize = dw.size();

        // write object itself
        writeObject(dw);

        // return back and write 'totalSize'
        int totalSize = dw.size() - startSize;
        if (totalSize > 0) {
            dw.storePosition();
            dw.moveTo(startSize - 4);
            dw.writeInt(totalSize);
            dw.restorePosition();
        }
    }

    /**
     * This function is called from {@link #write} function. Do not call it directly until you know,
     * what exactly are you doing.
     *
     * @param dw data writer class
     * @throws IOException thrown in case of invalid data format
     */
    protected abstract void writeObject(DataWriterBigEndian dw) throws IOException;

    //*************************************************
    // TOOLS
    //*************************************************

    /**
     * Create precise copy of current object.
     * Method is that object is stored into byte stream and then restored
     * as a new object.
     *
     * @return exact clone of this object
     * @throws IOException            thrown in case of invalid data format
     * @throws InstantiationException throws if class cannot be initialized
     * @throws IllegalAccessException in case of access to class constructor is limited
     */
    public Storable getCopy() throws IOException, InstantiationException, IllegalAccessException {
        byte[] data = getAsBytes();
        return StorableUtils.read(this.getClass(), new DataReaderBigEndian(data));
    }

    /**
     * Get whole object serialized into byte array.
     *
     * @return serialized object
     */
    public byte[] getAsBytes() {
        try {
            DataWriterBigEndian dw = new DataWriterBigEndian();
            write(dw);
            return dw.toByteArray();
        } catch (IOException e) {
            Logger.logE(TAG, "getAsBytes()", e);
            return null;
        }
    }
}
