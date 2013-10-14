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

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import de.javagl.hazelcast.matmul.MatMulResult;
import de.javagl.hazelcast.matmul.MutableFloatMatrix2D;
import de.javagl.hazelcast.matmul.Point;

/**
 * A Hazelcast StreamSerializer for {@link MatMulResult} objects
 */
public final class MatMulResultSerializer 
    implements StreamSerializer<MatMulResult>
{
    @Override
    public int getTypeId()
    {
        return 34567;
    }

    @Override
    public void write(ObjectDataOutput out, MatMulResult result)
        throws IOException
    {
        out.writeInt(result.getPoint().r);
        out.writeInt(result.getPoint().c);
        FloatMatrix2DSerializer.writeImpl(out, result.getMatrix());
    }

    @Override
    public MatMulResult read(ObjectDataInput in) throws IOException
    {
        int rowBlockIndex = in.readInt();
        int columnBlockIndex = in.readInt();
        MutableFloatMatrix2D m0 = FloatMatrix2DSerializer.readImpl(in);
        Point point = new Point(rowBlockIndex, columnBlockIndex);
        return new MatMulResult(point, m0);
    }

    @Override
    public void destroy()
    {
        // Not used
    }

}
