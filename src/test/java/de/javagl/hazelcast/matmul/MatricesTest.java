package de.javagl.hazelcast.matmul;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Locale;

import junit.framework.TestCase;

public class MatricesTest extends TestCase
{
	
    public void testSubmatrix()
    {
        MutableFloatMatrix2D m = Matrices.createFloatMatrix2D(6, 6);
        Matrices.fillContiguous(m);
        
        MutableFloatMatrix2D mSub = m.subMatrix(1, 1, 5, 5);
        MutableFloatMatrix2D mRef = Matrices.createFloatMatrix2D(4, 4);
        mRef.set(0,0,  7.0f);
        mRef.set(1,0,  8.0f);
        mRef.set(2,0,  9.0f);
        mRef.set(3,0, 10.0f);
        
        mRef.set(0,1, 13.0f);
        mRef.set(1,1, 14.0f);
        mRef.set(2,1, 15.0f);
        mRef.set(3,1, 16.0f);

        mRef.set(0,2, 19.0f);
        mRef.set(1,2, 20.0f);
        mRef.set(2,2, 21.0f);
        mRef.set(3,2, 22.0f);
        
        mRef.set(0,3, 25.0f);
        mRef.set(1,3, 26.0f);
        mRef.set(2,3, 27.0f);
        mRef.set(3,3, 28.0f);

    	assertTrue( Matrices.equal(mSub, mRef, 1e-5f) );    	
    }
	
    public void testSubSubmatrix()
    {
        MutableFloatMatrix2D m = Matrices.createFloatMatrix2D(6, 6);
        Matrices.fillContiguous(m);
        
        MutableFloatMatrix2D mSub = m.subMatrix(1, 1, 5, 5);
        MutableFloatMatrix2D mSubSub = mSub.subMatrix(1, 1, 3, 3);
        
        MutableFloatMatrix2D mRef = Matrices.createFloatMatrix2D(2, 2);
        mRef.set(0,0, 14.0f);
        mRef.set(1,0, 15.0f);
        
        mRef.set(0,1, 20.0f);
        mRef.set(1,1, 21.0f);
        
    	assertTrue( Matrices.equal(mSubSub, mRef, 1e-5f) );    	
    }
    
    public void testGetBuffer()
    {
        MutableFloatMatrix2D m0 = Matrices.createFloatMatrix2D(3, 3);
        Matrices.fillContiguous(m0);
        
        FloatBuffer buffer = FloatBuffer.allocate(
        	m0.getNumRows() * m0.getNumColumns());
        m0.getDataColumnMajor(buffer);
        
        for (int i=0; i<buffer.capacity(); i++)
        {
        	assertTrue(buffer.get(i) == i); 
        }
    }
	
    public void testSetBuffer()
    {
        MutableFloatMatrix2D m0 = Matrices.createFloatMatrix2D(3, 3);
        Matrices.fillContiguous(m0);
        
        FloatBuffer buffer = FloatBuffer.allocate(
        	m0.getNumRows() * m0.getNumColumns());
        m0.getDataColumnMajor(buffer);
        
        MutableFloatMatrix2D m1 = Matrices.createFloatMatrix2D(3, 3);
        buffer.position(0);
        m1.setDataColumnMajor(buffer);
        
        assertTrue(Matrices.equal(m0, m1, 1e-5f));
    }
    
    public void testGetBufferInSubMatrix()
    {
        MutableFloatMatrix2D m = Matrices.createFloatMatrix2D(6, 6);
        Matrices.fillContiguous(m);
        MutableFloatMatrix2D mSub = m.subMatrix(1, 1, 5, 5);
        
        FloatBuffer buffer = FloatBuffer.allocate(
        	mSub.getNumRows() * mSub.getNumColumns());
        mSub.getDataColumnMajor(buffer);
        
        float ref[] = new float[]{
          	 7.0f,  8.0f,  9.0f, 10.0f, 
           	13.0f, 14.0f, 15.0f, 16.0f, 
           	19.0f, 20.0f, 21.0f, 22.0f, 
           	25.0f, 26.0f, 27.0f, 28.0f, 
        };
        float dst[] = new float[16];
        buffer.position(0);
        buffer.get(dst);
        assertTrue(Arrays.equals(ref, dst));
    }
	
    
    public void testSetBufferInSubMatrix()
    {
        MutableFloatMatrix2D m0 = Matrices.createFloatMatrix2D(6, 6);
        MutableFloatMatrix2D mSub0 = m0.subMatrix(1, 1, 5, 5);
        
        MutableFloatMatrix2D m1 = Matrices.createFloatMatrix2D(4, 4);
        Matrices.fillContiguous(m1);
        FloatBuffer buffer = FloatBuffer.allocate(
           	m1.getNumRows() * m1.getNumColumns());
        m1.getDataColumnMajor(buffer);
        buffer.position(0);
        mSub0.setDataColumnMajor(buffer);
        
        assertTrue(Matrices.equal(mSub0, m1, 1e-5f));
    }
	
    
	
    public static void main(String[] args)
    {
        MutableFloatMatrix2D m = Matrices.createFloatMatrix2D(6, 6);
        Matrices.fillContiguous(m);
        
        System.out.println(Matrices.toString(m));
        
        MutableFloatMatrix2D m0 = m.subMatrix(1, 1, 5, 5);
        System.out.println(Matrices.toString(m0));
        
        MutableFloatMatrix2D m1 = m0.subMatrix(1, 1, 3, 3);
        System.out.println(Matrices.toString(m1));
        
        printAsBuffer(m1);
    }
    
    private static void printAsBuffer(MutableFloatMatrix2D matrix)
    {
        FloatBuffer buffer = FloatBuffer.allocate(
        	matrix.getNumRows() * matrix.getNumColumns());
        matrix.getDataColumnMajor(buffer);
        buffer.position(0);
        System.out.println(createColumnMajorString(
        	buffer, matrix.getNumRows(), matrix.getNumColumns()));
        
        Matrices.fill(matrix, 0.0f);
        matrix.setDataColumnMajor(buffer);
        System.out.println(createColumnMajorString(
        	buffer, matrix.getNumRows(), matrix.getNumColumns()));
    }

    public static String createColumnMajorString(
    	FloatBuffer buffer, int rows, int cols)
    {
        return createColumnMajorString(
        	buffer, rows, cols, Locale.ENGLISH, "%6.2f");
    }
    
    public static String createColumnMajorString(
        FloatBuffer buffer, int rows, int cols, Locale locale, String format)
    {
        StringBuilder sb = new StringBuilder();
        for (int r=0; r<rows; r++)
        {
            for (int c=0; c<cols; c++)
            {
                float value = buffer.get(r+c*rows);
                sb.append(String.format(locale, format, value)+" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
}
