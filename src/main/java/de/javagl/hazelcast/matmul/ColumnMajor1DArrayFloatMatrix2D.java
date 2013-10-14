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
package de.javagl.hazelcast.matmul;

import java.nio.FloatBuffer;

/**
 * Implementation of a {@link MutableFloatMatrix2D} that stores the data
 * column-major in a 1D float array
 */
final class ColumnMajor1DArrayFloatMatrix2D implements MutableFloatMatrix2D
{
    /**
     * The data. Yeah, that's the data.
     */
    private final float data[];

    /**
	 * The number of rows 
	 */
    private final int numRows;
    
    /**
     * The number of columns
     */
    private final int numColumns;
    
    /**
     * Creates a new matrix with the given number of rows and columns
     * 
     * @param numRows The number of rows
     * @param numColumns The number of columns
     */
    ColumnMajor1DArrayFloatMatrix2D(int numRows, int numColumns)
    {
        this.numRows = numRows;
        this.numColumns = numColumns;
        this.data = new float[numRows * numColumns];
   }
    
    @Override
    public int getNumRows()
    {
        return numRows;
    }

    @Override
    public int getNumColumns()
    {
        return numColumns;
    }

    @Override
    public float get(int r, int c)
    {
        return data[r + c * numRows];
    }

    @Override
    public void set(int r, int c, float value)
    {
        data[r + c * numRows] = value;
    }

    @Override
    public void getDataColumnMajor(FloatBuffer values)
    {
        values.put(data);
    }

    @Override
    public void setDataColumnMajor(FloatBuffer values)
    {
        values.get(data);
    }

    @Override
    public MutableFloatMatrix2D subMatrix(int r0, int c0, int r1, int c1)
    {
        return new ColumnMajor1DArraySubFloatMatrix2D(
            data, numRows, r0, c0, r1, c1);
    }
}
