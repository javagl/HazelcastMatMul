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
 * Implementation of a {@link MutableFloatMatrix2D} that is a sub-matrix
 * of a {@link ColumnMajor1DArrayFloatMatrix2D}
 */
final class ColumnMajor1DArraySubFloatMatrix2D implements MutableFloatMatrix2D
{
	/**
	 * The data. This is a reference to the data stored in the matrix
	 * that this matrix is a sub-matrix of.
	 */
    private final float data[];
    
    /**
     * The actual number of rows for the data. This is the number of
     * rows of the "root" matrix, that is, of the 
     * {@link ColumnMajor1DArrayFloatMatrix2D}
     */
    private final int actualNumRows;
    
    /**
     * The start row, inclusive
     */
    private final int r0;
    
    /**
     * The start column, inclusive
     */
    private final int c0;

    /**
     * The end row, exclusive
     */
    private final int r1;
    
    /**
     * The end column, exclusive
     */
    private final int c1;
    
    /**
     * Creates a new sub-matrix based on the given data
     * 
     * @param data The data from the owning matrix that this matrix is a 
     * sub-matrix of. A reference to this data will be stored 
     * @param actualNumRows The number of rows for the data. This is the 
     * number of rows of the "root" matrix, that is, of the 
     * {@link ColumnMajor1DArrayFloatMatrix2D}
     * @param r0 The start row, inclusive
     * @param c0 The start column, inclusive
     * @param r1 The end row, exclusive
     * @param c1 The end column, exclusive
     */
    ColumnMajor1DArraySubFloatMatrix2D(float data[], 
        int actualNumRows, int r0, int c0, int r1, int c1)
    {
        this.data = data;
        this.actualNumRows = actualNumRows;
        this.r0 = r0;
        this.c0 = c0;
        this.r1 = r1;
        this.c1 = c1;
    }
    
    
    @Override
    public int getNumRows()
    {
        return r1-r0;
    }

    @Override
    public int getNumColumns()
    {
        return c1-c0;
    }

    @Override
    public float get(int r, int c)
    {
        int ar = r + r0;
        int ac = c + c0;
        return data[ar + ac * actualNumRows];
    }
    
    @Override
    public void set(int r, int c, float value)
    {
        int ar = r + r0;
        int ac = c + c0;
        data[ar + ac * actualNumRows] = value;
    }

    @Override
    public void getDataColumnMajor(FloatBuffer values)
    {
        for (int c=c0; c<c1; c++)
        {
            values.put(data, r0+c*actualNumRows, getNumRows());
        }
    }

    @Override
    public void setDataColumnMajor(FloatBuffer values)
    {
        for (int c=c0; c<c1; c++)
        {
            values.get(data, r0+c*actualNumRows, getNumRows());
        }
    }

    @Override
    public MutableFloatMatrix2D subMatrix(int r0, int c0, int r1, int c1)
    {
        return new ColumnMajor1DArraySubFloatMatrix2D(data, actualNumRows,
            this.r0 + r0, this.c0 + c0, 
            this.r0 + r1, this.c0 + c1); 
    }

}
