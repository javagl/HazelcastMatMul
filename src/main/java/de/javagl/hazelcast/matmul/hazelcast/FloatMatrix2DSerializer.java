/*
 * www.javagl.de - Hazelcast Matrix Multiplication
 *
 * Copyright (c) 2013 Marco Hutter - http://www.javagl.de
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package de.javagl.hazelcast.matmul.hazelcast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import de.javagl.hazelcast.matmul.FloatMatrix2D;
import de.javagl.hazelcast.matmul.Matrices;
import de.javagl.hazelcast.matmul.MutableFloatMatrix2D;

/**
 * A Hazelcast StreamSerializer for {@link FloatMatrix2D} objects.
 */
public class FloatMatrix2DSerializer implements StreamSerializer<FloatMatrix2D>
{
    @Override
    public int getTypeId()
    {
        return 12345;
    }

    @Override
    public void write(ObjectDataOutput out, FloatMatrix2D matrix)
        throws IOException
    {
        writeImpl(out, matrix);
    }
    
    /**
     * Writes the given matrix to the given output
     * 
     * @param out The output
     * @param matrix The matrix
     * @throws IOException If an IO error occurs
     */
    static void writeImpl(ObjectDataOutput out, FloatMatrix2D matrix)
        throws IOException
    {
        out.writeInt(matrix.getNumRows());
        out.writeInt(matrix.getNumColumns());
        byte data[] = new byte[matrix.getNumRows()*matrix.getNumColumns()*4];
        FloatBuffer buffer = ByteBuffer.wrap(data).asFloatBuffer();
        matrix.getDataColumnMajor(buffer);
        out.write(data);
    }

    @Override
    public FloatMatrix2D read(ObjectDataInput in) throws IOException
    {
        return readImpl(in);
    }

    /**
     * Reads a FloatMatrix2D from the given input
     * 
     * @param in The input
     * @return The matrix
     * @throws IOException If an IO error occurs
     */
    static MutableFloatMatrix2D readImpl(ObjectDataInput in) throws IOException
    {
        int numRows = in.readInt();
        int numColumns = in.readInt();
        MutableFloatMatrix2D matrix = 
            Matrices.createFloatMatrix2D(numRows, numColumns); 
        byte data[] = new byte[matrix.getNumRows()*matrix.getNumColumns()*4];
        FloatBuffer buffer = ByteBuffer.wrap(data).asFloatBuffer();
        in.readFully(data);
        matrix.setDataColumnMajor(buffer);
        return matrix;
    }
    
    @Override
    public void destroy()
    {
        // Not used
    }

}