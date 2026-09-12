package au.edu.adelaide.stt.service;

/**
* A simple interface used to shut down the server.
 *
 * If our code directly killed the Java program (e.g., using System.exit()), 
 * it would accidentally kill our automated tests running that code
 * 
 * By putting the shutdown action behind this interface, our tests can swap 
 * a "fake" version that does nothing. This lets us verify the server 
 * correctly decides when to shut down, without actually crashing the tests.
 *
 * NB: Because it only has one method, it can easily be mocked with a quick 
 * lambda expression in our test files (like () -> {})
 */
public interface ServerTerminator {

    void terminate();
}