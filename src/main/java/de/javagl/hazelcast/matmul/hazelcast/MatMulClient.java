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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

import de.javagl.hazelcast.matmul.Factory;
import de.javagl.hazelcast.matmul.MatMulResult;
import de.javagl.hazelcast.matmul.MatMulTask;
import de.javagl.hazelcast.matmul.Matrices;
import de.javagl.hazelcast.matmul.MatrixMultiplicator;
import de.javagl.hazelcast.matmul.MatrixMultiplicators;
import de.javagl.hazelcast.matmul.MutableFloatMatrix2D;
import de.javagl.hazelcast.matmul.util.LoggerUtil;

/**
 * A Hazelcast matrix multiplication client
 */
public class MatMulClient
{
    /**
     * Start the client
     * 
     * @param args Not used
     */
    public static void main(String[] args)
    {
        LoggerUtil.configureDefault(Logger.getLogger("com"));
        LoggerUtil.configureDefault(Logger.getLogger("de"));
        
        final ExecutorService executorService = createHazeclastExecutorService();
        //runBasicTest(executorService);
        
        MatrixMultiplicator multiplicator0 = 
            MatrixMultiplicators.createParallelDefault(100);
        MatrixMultiplicator multiplicator1 =
            createHazelcastMatrixMultiplicator(executorService);
        
        final int rA = 1000;
        final int cArB = 1500;
        final int cB = 1000;
        MatMulTests.runBasicTest(
            rA, cArB, cB, multiplicator0, multiplicator1);
    }
    
    /**
     * Create a {@link MatrixMultiplicator} using Hazelcast
     * 
     * @param hazelcastExecutorService The Hazelcast ExecutorService
     * @return The {@link MatrixMultiplicator}
     */
    private static MatrixMultiplicator createHazelcastMatrixMultiplicator(
        final ExecutorService hazelcastExecutorService)
    {
        Factory<ExecutorService> executorServiceFactory = 
            new Factory<ExecutorService>()
        {
            @Override
            public ExecutorService create()
            {
                return hazelcastExecutorService;
            }
        };
        
        // Creates a MatrixMultiplicator that will dispatch MatMulTask objects 
        // to the executor service. Each MatMulTask will be the multiplication 
        // of a matrix with size 'clusterNodeBlockSize'. These tasks will be 
        // dispatched to the Hazelcast nodes. Each node will perform the
        // multiplication of the matrices with size 'clusterNodeBlockSize'
        // by splitting them into blocks of size 'nodeProcessorBlockSize'
        // and passing MatMulTasks to multiply these blocks to a local 
        // thread pool executor.
        final int clusterNodeBlockSize = 500;
        final int nodeProcessorBlockSize = 50;
        Factory<MatrixMultiplicator> subMatrixMultiplicatorFactory =
            MatrixMultiplicators.createParallelFactory(nodeProcessorBlockSize);
        MatrixMultiplicator multiplicator = 
            MatrixMultiplicators.createParallel(
                executorServiceFactory, clusterNodeBlockSize,
                subMatrixMultiplicatorFactory);
        return multiplicator;
    }
    
    
    /**
     * Returns the ExecutorService that is obtained from a Hazelcast client
     * that connects to localhost
     * 
     * @return The ExecutorService
     */
    public static ExecutorService createHazeclastExecutorService()
    {
        ClientConfig clientConfig = new ClientConfig();
        MatMulUtils.initSerializers(clientConfig.getSerializationConfig());
        clientConfig.addAddress("127.0.0.1:5701");
        HazelcastInstance hazelcastInstance = 
            HazelcastClient.newHazelcastClient(clientConfig);
        IExecutorService executorService = 
            hazelcastInstance.getExecutorService("matMulExecutorService");
        return executorService;
    }
    
    /**
     * Performs a single, simple matrix multiplication with the given
     * ExecutorService
     *  
     * @param executorService The ExecutorService
     */
    private static void runBasicTest(ExecutorService executorService)
    {
        MutableFloatMatrix2D m0 = Matrices.createFloatMatrix2D(2, 3);
        MutableFloatMatrix2D m1 = Matrices.createFloatMatrix2D(3, 4);
        
        Matrices.fillRandom(m0);
        Matrices.fillRandom(m1);
        
        Future<MatMulResult> future = 
            executorService.submit(new MatMulTask(0, 0, m0, m1, null));
        MatMulResult result;
        try
        {
            result = future.get();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            return;
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException("Error", e);
        }
        System.out.println("Result:\n"+Matrices.toString(result.getMatrix()));
    }
    
}
