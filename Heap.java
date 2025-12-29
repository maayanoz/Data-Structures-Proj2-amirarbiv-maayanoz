/**
 * Heap
 *
 * An implementation of Fibonacci heap over positive integers 
 * with the possibility of not performing lazy melds and 
 * the possibility of not performing lazy decrease keys.
 *
 * 
 * TODO: meld, deleteMin, insert, decreaseKey, delete
 */
public class Heap
{
    public final boolean lazyMelds;
    public final boolean lazyDecreaseKeys;
    public HeapNode min;
    private int roots;
    private int size;
    private int numMarked;
    private int totalLinks;
    private int totalCuts;
    private int totalHeapifyCosts;

    
    /**
     *
     * Constructor to initialize an empty heap.
     *
     */
    public Heap(boolean lazyMelds, boolean lazyDecreaseKeys)
    {
        this.lazyMelds = lazyMelds;
        this.lazyDecreaseKeys = lazyDecreaseKeys;
        this.min = null;
        this.roots = 0;
        this.size = 0;
        this.numMarked = 0;
        this.totalLinks = 0;
        this.totalCuts = 0;
        this.totalHeapifyCosts = 0;
    }

    /**
     * 
     * pre: key > 0
     *
     * Insert (key,info) into the heap and return the newly generated HeapNode.
     *
     */
    public HeapNode insert(int key, String info) { //finished, tested
        this.size++;
        this.roots++;
        HeapNode result = new HeapNode();
        result.key = key;
        result.info = info;
        // Add the new node to the roots array
        if (this.min == null) {
            this.min = result;
        } else{
            this.min.prev.next = result;
            result.prev = this.min.prev;
            this.min.prev = result;
            result.next = this.min;
        }
        if (key < this.min.key) {
            this.min = result;
        }
        return result;
    }

    /**
     * 
     * Return the minimal HeapNode, null if empty.
     *
     */
    public HeapNode findMin(){ //finished, tested
        int curr_min = Integer.MAX_VALUE;
        HeapNode min_node = this.min;
        HeapNode curr = this.min;
        if (curr == null) {
            return null;
        }
        while (true) {
            if (curr.key < curr_min) {
                curr_min = curr.key;
                min_node = curr;
            }
            curr = curr.next;
            if (curr == this.min) {
                break; // we have traversed the entire root list
            }
        }
        return min_node; 
    }

    /**
     * 
     * Delete the minimal item.
     *
     */
    public void deleteMin()
    {
        return; // should be replaced by student code
    }

    /**
     * 
     * pre: 0<=diff<=x.key
     * 
     * Decrease the key of x by diff and fix the heap.
     * 
     */
    public void decreaseKey(HeapNode x, int diff){ //finished, not tested
        x.key -= diff;
        if (x.parent == null) {
            this.min = this.findMin();
            return;
        }
        if (x.key < x.parent.key) {
            // cut x from its parent
            if (this.lazyDecreaseKeys) {
                cut(x);
            }
            else{
               //non-lazy decrease key
            heapifyUp(x); 
            }
        }
        HeapNode new_min = this.findMin();
        this.min = new_min;
    }

    private void heapifyUp(HeapNode x) { //helping method, tested
        // heapify up until x is smaller than two its children
        while (x.parent != null && x.key < x.parent.key) {
            swapWithParent(x);
            x = x.parent;
            totalHeapifyCosts++;
        }
    }

    private void cut(HeapNode x) { //helping method, cuts and melds back
        // cut x from its parent and add it to the root list
                this.totalCuts++;
                //taking care of current heap
                //updating x's siblings pointers
                if (x.next != x) { //if x is not alone in its list
                    if(x.prev != x.next){ //if x has more than one sibling
                    x.next.prev = x.prev;
                    x.prev.next = x.next;
                    }
                else{ //if x has only one sibling
                    x.next.next = x.next;
                    x.next.prev = x.next;
                }
                }
                else{ //if x is alone in its list
                    //do nothing
                }
                //updating x's parent's child pointer
                x.parent.rank = x.parent.rank - 1 - x.rank;
                if (x.parent.child == x) { //if x is a child
                    if (x.next != x) { //if x has siblings
                        x.parent.child = x.next;
                    }
                    else{ //if x has no siblings
                        x.parent.child = null;
                    }
                }
                //melding cutted tree
                Heap to_meld = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
                to_meld.roots[0] = x;
                to_meld.min = x;
                this.meld(to_meld);
            }
    

    /**
     * 
     * @param x
     * 
     * pre: x is a node in the heap
     * swap x with his parent
     * 
    */ 
    private void swapWithParent(HeapNode x) {
        int p_key = x.parent.key;
        String p_val = x.parent.info;
        int x_key = x.key;
        String x_val = x.info;
        x.parent.key = x_key;
        x.parent.info = x_val;
        x.key = p_key;
        x.info = p_val;
    }



    /**
     * @param x
     * Delete the x from the heap.
     *
     */
    public void delete(HeapNode x) 
    {    
        return; // should be replaced by student code
    }


    /**
     * 
     * Meld the heap with heap2
     * pre: heap2.lazyMelds = this.lazyMelds AND heap2.lazyDecreaseKeys = this.lazyDecreaseKeys
     *
     */
    public void meld(Heap heap2)
    {
        return; // should be replaced by student code           
    }
    
    
    /**
     * 
     * Return the number of elements in the heap
     *   
     */
    public int size()
    {
        return 46; // should be replaced by student code
    }


    /**
     * 
     * Return the number of trees in the heap.
     * 
     */
    public int numTrees()
    {
        return 46; // should be replaced by student code
    }
    
    
    /**
     * 
     * Return the number of marked nodes in the heap.
     * 
     */
    public int numMarkedNodes()
    {
        return 46; // should be replaced by student code
    }
    
    
    /**
     * 
     * Return the total number of links.
     * 
     */
    public int totalLinks()
    {
        return 46; // should be replaced by student code
    }
    
    
    /**
     * 
     * Return the total number of cuts.
     * 
     */
    public int totalCuts()
    {
        return 46; // should be replaced by student code
    }
    

    /**
     * 
     * Return the total heapify costs.
     * 
     */
    public int totalHeapifyCosts()
    {
        return 46; // should be replaced by student code
    }
    
    
    /**
     * Class implementing a node in a ExtendedFibonacci Heap.
     *  
     */
    public static class HeapNode{
        public int key;
        public String info;
        public HeapNode child;
        public HeapNode next;
        public HeapNode prev;
        public HeapNode parent;
        public int rank;
    }
}
