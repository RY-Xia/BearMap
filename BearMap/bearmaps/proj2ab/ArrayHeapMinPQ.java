package bearmaps.proj2ab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class ArrayHeapMinPQ<T> implements ExtrinsicMinPQ<T>{

    private ArrayList<priorityNode> items;
    private int n;           //number of items
    private HashMap<T, Integer> hashmap;


    public ArrayHeapMinPQ(){
        hashmap = new HashMap<>();
        items = new ArrayList<>();
        n = 0;

    }

    public ArrayHeapMinPQ(int size, ArrayList<priorityNode> items){
        size = items.size();
        items = new ArrayList<>(size);
    }
    /* Adds an item with the given priority value. Throws an
     * IllegalArgumentExceptionb if item is already present.
     * You may assume that item is never null. */
    @Override
    public void add(T item, double priority){
        if (item == null || contains(item)){
            throw new IllegalArgumentException();
        }else {
            items.add(new priorityNode(item,priority));
            hashmap.put(item, size()-1);
            swim((size()-1));
        }
    }

    //return the left child of the specific item
    private int leftChild(int k){
        return k*2+1;
    }
    //return the right child of the specific item
    private int rightChild(int k){
        return k*2+2;
    }

    //return the parent key of the specific item
    private int parent(int k){
        return (k-1)/2;
    }

    private void swap(int i, int j){
        priorityNode temp = items.get(i);
        items.set(i,items.get(j));
        items.set(j,temp);
        hashmap.put(items.get(i).getItem(), i);
        hashmap.put(items.get(j).getItem(), j);
    }
    private void swim(int k){
        if (items.get(k).getPriority() < items.get(parent(k)).getPriority() && k > 1){
            swap(k,parent(k));
            swim(parent(k));
        }
    }
    private void sink(int k){
        /*while (rightChild(k) < size()){
            int i = 2*k;
            if (i < size() && greater(i, i+1)){i++;}
            if (!greater(k,i)){break;}
            swap(k,i);
            k = i;
        }
        while (leftChild(k) < size()){
            int l = leftChild(k);
            if (rightChild(k) < size() && greater(l, rightChild(k))){l=rightChild(k);}
            swap (k,l);
            k = l;
        }*/
        if (rightChild(k) < size() &&leftChild(k)<size()&& greater(leftChild(k), rightChild(k))){
            swap (leftChild(k), rightChild(k));
        }
        if (leftChild(k) < size()){
            swap(k, leftChild(k));
            sink(leftChild(k));
        }
    }
    private boolean greater(int i, int j){
        return items.get(i).getPriority() > items.get(j).getPriority();
    }

    private boolean isEmpty(){
        return size() == 0;
    }


    /* Returns true if the PQ contains the given item.Runtime must be O(logN) */
    @Override
    public boolean contains(T item){
        long start = System.currentTimeMillis();
        if (isEmpty()){
            return false;
        }
        long end = System.currentTimeMillis();
        System.out.print("Total time elapsed: " + (end - start)/1000.0 +  " seconds.");
        return hashmap.containsKey(item);
    }
    /* Returns the minimum item. Throws NoSuchElementException if the PQ is empty.
    * runtime must be O(logN)!!!!
    *  */
    @Override
    public T getSmallest(){
        if (isEmpty()){
            throw new NoSuchElementException();
        }
        //int height = (int)(Math.log(size()+2)/Math.log(2) - 1);
        return items.get(0).getItem();
    }
    /* Removes and returns the minimum item. Throws NoSuchElementException if the PQ is empty. */
    @Override
    public T removeSmallest(){
        T smallest = getSmallest();
        if (isEmpty()){throw new NoSuchElementException();}
        swap (0,size()-1);
        hashmap.remove(smallest);
        items.remove(size()-1);
        sink(0);
        return smallest;
    }
    /* Returns the number of items in the PQ. */
    @Override
    public int size(){
        return items.size();
    }
    /* Changes the priority of the given item. Throws NoSuchElementException if the item
     * doesn't exist. */
    @Override
    public void changePriority(T item, double priority){
        if (!contains(item)){
            throw new NoSuchElementException();
        }
        int index = hashmap.get(item);
        double oldP = items.get(index).getPriority();
        if (oldP < priority){
            sink(index);
        }else {
            swim(index);
        }
    }


// The pair of each element of the arrayList
    private class priorityNode implements Comparable<priorityNode>{
        private T item;
        private double priority;

        private priorityNode(){}

        private priorityNode(T item, double priority){
            this.item = item;
            this.priority = priority;
        }
        private T getItem(){
            return item;
        }
        private void setItem(T item){
            this.item = item;
        }
        private double getPriority(){
            return this.priority;
        }
        private void setPriority(double priority){
            this.priority = priority;
        }
        @Override
        public int compareTo(priorityNode other){
            if (other == null) return -1;
            return Double.compare(this.getPriority(), other.getPriority());
        }
        @Override
        public boolean equals(Object o){
            if (o == null || o.getClass() != this.getClass()) return false;
            else return ((priorityNode) o).getItem().equals(getItem());
        }
    }


}
