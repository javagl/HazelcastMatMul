package de.javagl.hazelcast.matmul;

import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;


public class MatmulTest extends TestCase
{
	public void testSimple()
	{
        MatrixMultiplicator multiplicator0 = MatrixMultiplicators.createSimple();
		
        MutableFloatMatrix2D m0 = Matrices.createFloatMatrix2D(2, 3);
        Matrices.fillContiguous(m0);
        MutableFloatMatrix2D m1 = Matrices.createFloatMatrix2D(3, 4);
        Matrices.fillContiguous(m1);
        MutableFloatMatrix2D result = 
           	Matrices.createFloatMatrix2D(m0.getNumRows(), m1.getNumColumns());
		multiplicator0.multiply(result, m0, m1);
		
        MutableFloatMatrix2D ref = 
        	Matrices.createFloatMatrix2D(2, 4);
        ref.set(0,0,10.0f);
        ref.set(0,1,28.0f);
        ref.set(0,2,46.0f);
        ref.set(0,3,64.0f);
        ref.set(1,0,13.0f);
        ref.set(1,1,40.0f);
        ref.set(1,2,67.0f);
        ref.set(1,3,94.0f);
		
        assertTrue(Matrices.equal(result, ref, 1e-5f));
	}
	
    public void testParallel()
    {
        MutableFloatMatrix2D m0 = Matrices.createFloatMatrix2D(20, 30);
        Matrices.fillContiguous(m0);
        MutableFloatMatrix2D m1 = Matrices.createFloatMatrix2D(30, 40);
        Matrices.fillContiguous(m1);
        MutableFloatMatrix2D result0 = 
            Matrices.createFloatMatrix2D(m0.getNumRows(), m1.getNumColumns());
        MutableFloatMatrix2D result1 = 
            Matrices.createFloatMatrix2D(m0.getNumRows(), m1.getNumColumns());

        MatrixMultiplicator multiplicator0 = 
            MatrixMultiplicators.createSimple();
        multiplicator0.multiply(result0, m0, m1);

        MatrixMultiplicator multiplicator1 = 
            MatrixMultiplicators.createParallelDefault(10);
        multiplicator1.multiply(result1, m0, m1);
        
        assertTrue(Matrices.equal(result0, result1, 1e-5f));
    }
    
    public void testParallelNested()
    {
        MutableFloatMatrix2D m0 = Matrices.createFloatMatrix2D(20, 30);
        Matrices.fillContiguous(m0);
        MutableFloatMatrix2D m1 = Matrices.createFloatMatrix2D(30, 40);
        Matrices.fillContiguous(m1);
        MutableFloatMatrix2D result0 = 
            Matrices.createFloatMatrix2D(m0.getNumRows(), m1.getNumColumns());
        MutableFloatMatrix2D result1 = 
            Matrices.createFloatMatrix2D(m0.getNumRows(), m1.getNumColumns());

        MatrixMultiplicator multiplicator0 = 
            MatrixMultiplicators.createSimple();
        multiplicator0.multiply(result0, m0, m1);

        Factory<ExecutorService> executorServiceFactory = 
            new Factory<ExecutorService>()
        {
            @Override
            public ExecutorService create()
            {
                return ExecutorExtensions.newExceptionAwareFixedThreadPool(2);
            }
        };
        MatrixMultiplicator multiplicator1 = 
            MatrixMultiplicators.createParallel(executorServiceFactory, 
                10, MatrixMultiplicators.createSimpleFactory());
        multiplicator1.multiply(result1, m0, m1);
        
        assertTrue(Matrices.equal(result0, result1, 1e-5f));
    }
}
