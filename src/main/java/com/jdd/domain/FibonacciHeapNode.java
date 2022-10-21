package com.jdd.domain;

import lombok.Getter;

public class FibonacciHeapNode<T> {
    /**
     * Node data.
     */
    @Getter
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
    @Getter
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
}
