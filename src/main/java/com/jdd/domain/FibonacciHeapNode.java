package com.jdd.domain;

public class FibonacciHeapNode<T> {
    /**
     * Node data.
     */
    public T data;

    /**
     * first child node
     */
    FibonacciHeapNode<T> child;

    /**
     * left sibling node
     */
    FibonacciHeapNode<T> left;

    /**
     * parent node
     */
    FibonacciHeapNode<T> parent;

    /**
     * right sibling node
     */
    FibonacciHeapNode<T> right;

    /**
     * true if this node has had a child removed since this node was added to its parent
     */
    boolean mark;

    /**
     * key value for this node
     */
    public int key;

    /**
     * number of children of this node (does not count grandchildren)
     */
    int degree;

    /**
     * Constructs a new node.
     *
     * @param data data for this node
     */
    public FibonacciHeapNode(T data) {
        this.data = data;
    }

    /**
     * Return the string representation of this object.
     *
     * @return string representing this object
     */
    @Override
    public String toString() {
        return Double.toString(key);
    }

    // toString

    public T getData() {
        return data;
    }

    public int getKey() {
        return key;
    }
}
