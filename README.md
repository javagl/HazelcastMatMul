HazelcastMatMul
===============

Distributed Matrix Multiplication with Hazelcast.

This is an implementation of a distributed matrix multiplication
using <a href="http://www.hazelcast.com/" target=_"blank">Hazelcast</a>.

This project should be considered as **experimental**. It is not
intended to provide a stable, public API. It is only a proof of
concept.

Usage
-----

After starting one or more instances of <code>de.javagl.hazelcast.matmul.hazelcast.MatMulServer</code>,
the <code>de.javagl.hazelcast.matmul.hazelcast.MatMulClient</code> may be used to execute a 
distributed matrix multiplication <code>A * B = C</code>.

The size of the matrices as well as basic configuration settings
may be specified in a file <code>MatMulClient.properties</code>.

With the default configuration, the client will perform the 
multiplication of a 1000x1500 and a 1500x1000 matrix. Therefore,
it will split these matrices into blocks of size 500x500. These
blocks will be distributed among all running MatMulServer instances. 
On each machine, the block matrix with size 500x500 will be subdivided
further, into blocks of size 50x50, and the multiplication of 
these blocks will be performed in parallel, using a local 
thread pool.

The general structure of this approach aims at supporting a hierarchical
distribution of the workload. It is even possible to insert additional
layers. For example, for the multiplication of two matrices with a size
of 100000x100000, the matrices could be split into sub-matrices of size
10000x10000 and distributed to local workstations. The local workstations
in turn could pass sub-matrices of size 1000x1000 to standard multi-core PCs. 
These could again be subdivided into matrices of size 100x100, which are
then multiplied in parallel by the available cores of the machine.

There are many possible tuning parameters for the distribution and
scheduling, but these have not yet been evaluated in detail.

This is just a proof of concept.

