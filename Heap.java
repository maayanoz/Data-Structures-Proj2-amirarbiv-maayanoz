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
    private int roots; //number of trees in the heap
    private int size; //all nodes in the heap
    private int numMarked; //in lazy decrease keys
    private int totalLinks; //in succesive linking
    private int totalCuts; //in lazy decrease keys
    private int totalHeapifyCosts; //in non-lazy decrease keys

    
    /**
     *
     * Constructor to initialize an empty heap.
     *
     */
    public Heap(boolean lazyMelds, boolean lazyDecreaseKeys){

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
    public HeapNode insert(int key, String info){ //finished, tested
        this.size++;
        this.roots++;
        HeapNode result = new HeapNode();
        result.key = key;
        result.info = info;
        // Add the new node to the roots array
        if (this.min == null) {
            this.min = result;
        } else{
            Heap to_meld = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
            to_meld.min = result;
            this.meld(to_meld);
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
    public void deleteMin(){ //with succesive linking
        if (this.min == null) {
            return; // heap is empty
        }
        this.roots--; //removing one root
        this.size--; //removing min node
        HeapNode old_min = this.min; //store old min
        //remove old_min from root list
        if (old_min.next != old_min) { //if there are other roots
            if(old_min.child != null){ //if old_min has children
                //link old_min's children to root list
                HeapNode child = old_min.child;
                old_min.prev.next = child;
                child.prev = old_min.prev;
                old_min.next.prev = child.prev;
                child.prev.next = old_min.next;
                this.min = old_min.next;
            }
            else{ //if old_min has no children
                old_min.prev.next = old_min.next;
                old_min.next.prev = old_min.prev;
                this.min = old_min.next;
            }
            //find new min
            
            this.min = this.findMin(); 
            //perform succesive linking
            this.succsesive_linking(this);//bug here
        }
        else{ //if old_min is the only root
            this.min = null;
            if(old_min.child != null){ //if old_min has children
                //make old_min's children the new root list
                this.min = old_min.child;
                //perform succesive linking
                this.succsesive_linking(this);
            }
        }
    }

    /**
     * 
     * pre: 0<=diff<=x.key
     * 
     * Decrease the key of x by diff and fix the heap.
     * 
     */
    public void decreaseKey(HeapNode x, int diff){ //finished, tested
        x.key -= diff;
        if (x.parent == null) {
            this.min = this.findMin();
            return;
        }
        if (x.key < x.parent.key) {
            // cut x from its parent
            if (this.lazyDecreaseKeys) {
                cut(x);
                cascadingCuts(x.parent);
            }
            else{
               //non-lazy decrease key
            heapifyUp(x); 
            }
        }
        HeapNode new_min = this.findMin();
        this.min = new_min;
    }

    private void cascadingCuts(HeapNode y) { //helping method for decrease key, not tested
        if (y.parent == null) {
            return;
        }
        if (!y.marked) {
            y.marked = true;
            this.numMarked++;
        } else {
            cut(y);
            cascadingCuts(y.parent);
        }
    }

    private void heapifyUp(HeapNode x) { //helping method, tested
        // heapify up until x is smaller than two its children
        while (x.parent != null && x.key < x.parent.key) {
            swapWithParent(x);
            x = x.parent;
            totalHeapifyCosts++;
        }
    }

    private void cut(HeapNode x) { //helping method, cuts and melds back, tested
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
                x.parent = null;
                x.next = x;
                x.prev = x;
                Heap to_meld = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
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
        int x_rank = x.rank;
        x.rank = x.parent.rank;
        x.parent.rank = x_rank;
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
    public void delete(HeapNode x){ //just like in class
        decreaseKey(x, Integer.MIN_VALUE);
        deleteMin();
    }


    /**
     * 
     * Meld the heap with heap2
     * pre: heap2.lazyMelds = this.lazyMelds AND heap2.lazyDecreaseKeys = this.lazyDecreaseKeys
     *
     */
    public void meld(Heap heap2){ //finished, not tested
        this.size += heap2.size; //true for both lazy and non-lazy melds
        this.numMarked += heap2.numMarked;
        this.totalLinks += heap2.totalLinks;
        this.totalCuts += heap2.totalCuts;
        this.totalHeapifyCosts += heap2.totalHeapifyCosts;

        if (lazyMelds){
            lazyMeld(heap2);
            this.roots = this.roots + heap2.roots;
        }
        else{
            this.succsesive_linking(heap2);
        }
    }


    public void lazyMeld(Heap heap2){ //finished, tested
        //used if lazy melds is true
        //updating roots pointers
        if (this.min == null){ //if this heap is empty
            this.roots = heap2.roots;
        }
        else if (heap2.min != null){ //if heap2 is not empty
            //link the two roots lists
            HeapNode this_min_prev = this.min.prev;
            HeapNode heap2_min_prev = heap2.min.prev;

            this.min.prev = heap2_min_prev;
            heap2_min_prev.next = this.min;

            heap2.min.prev = this_min_prev;
            this_min_prev.next = heap2.min;
        }
        if (this.min == null || (heap2.min != null && heap2.min.key < this.min.key)){
            this.min = heap2.min;
        }
    }
    

    public void succsesive_linking(Heap heap){ //performs succesive linking in-place, not tested
        //count number of links
        //used if lazy melds is false
        //used in deleteMin
        //consolidate trees of same rank in the roots list
        HeapNode[] rootsArray = new HeapNode[heap.roots]; //array to store trees by rank
        HeapNode curr = heap.min;
        if (curr == null) {
            return;
        }
        for (int i = 0; i < heap.roots; i++) {
            rootsArray[i] = curr;
            curr = curr.next;
        }
        //perform linking
        HeapNode[] rankArray = new HeapNode[heap.roots*2]; //array to store trees by rank
        for (HeapNode node : rootsArray) {
            add_to_rank_array(node, rankArray);
        }
        //rebuild the roots list from rankArray
        int index = 0;
        for (int i = 0; i < rankArray.length; i++) {
            if (rankArray[i] != null) {
                heap.min = rankArray[i];
                index = i;
                break;
            }
        }
        curr = heap.min;
        if (curr == null) {
            return;
        }
        this.roots = 1;
        for(int i = index+1; i < rankArray.length; i++) {
            if (rankArray[i] == null) {
                continue;
            }
            roots++;
            if (rankArray[i] == heap.min){ //not supposed to happen but just in case something goes wrong
                heap.min.prev = curr;
                curr.next = heap.min;
                break;
            }
            curr.next = rankArray[i];
            rankArray[i].prev = curr;
            curr = rankArray[i];
            
        }

        //previous buggy attempt

        // HeapNode[] rankArray = new HeapNode[heap.size*2]; //array to store trees by rank
        // HeapNode curr = heap.min;
        // if (curr == null) {
        //     return;
        // }
        // HeapNode[] tester = new HeapNode[heap.size*2];
        // while (true) {
        //     add_to_rank_array(curr, rankArray); //bug here
        //     curr = curr.next;
        //     if (tester == rankArray) {
        //         break;
        //     }
        //     tester = rankArray;
        // }
        // //rebuild the roots list from rankArray
        // heap.min = heap.findMin();
        // if (rankArray.length == 1) {
        //     return;
        // }
        // curr = heap.min;
        // for(int i = 1; i < rankArray.length; i++) {
        //     if (rankArray[i] == null) {
        //         continue;
        //     }
        //     if (rankArray[i] == heap.min){
        //         break;
        //     }
        //     curr.next = rankArray[i];
        //     rankArray[i].prev = curr;
        //     curr = rankArray[i];
            
        // }
        // //counting roots in rankArray, without empty slots
        // this.roots = 0;
        // curr = heap.min;
        // while (true) {
        //     if (curr == null) {
        //         continue;
        //     }
        //     this.roots++;
        //     curr = curr.next;
        //     if (curr == heap.min) {
        //         break;
        //     }
        // }
    }


    private void add_to_rank_array(HeapNode curr, HeapNode[] rankArray) { //helping method for succesive linking, somtimes recursive
        if (rankArray[curr.rank] == null) { //no tree with the same rank
            rankArray[curr.rank] = curr;
            return;
        }
        //unnecessary check
        // if (rankArray[curr.rank] == curr) { //already added 
        //     return;
        // }
        //there is already a tree with the same rank
        HeapNode other = rankArray[curr.rank];
        //remove other from rankArray
        rankArray[curr.rank] = null; 
        //link curr and other
        HeapNode linked = link(curr, other);
        //create new bigger copy of rankArray if needed
        if (linked.rank >= rankArray.length){
            //create new copy
            HeapNode[] newRankArray = new HeapNode[rankArray.length * 2];
            //copy old array to new array
            for (int i = 0; i < rankArray.length; i++) {
                newRankArray[i] = rankArray[i];
            }
            rankArray = newRankArray;
        }
        
        //add linked to rankArray
        add_to_rank_array(linked, rankArray);
        }
    
    private HeapNode link(HeapNode a, HeapNode b) { //helping method for succesive linking, links two trees of same rank
        //link a and b, return the new root
        this.totalLinks++;
        if (a.key < b.key) {
            //make b a child of a
            //updating a's children pointers
            a = add_child(a, b);
            return a;

        } else {
            //make a a child of b
            //updating b's children pointers
            b = add_child(b, a);
            return b;
        }
    }
    
    public HeapNode add_child (HeapNode parent, HeapNode child){
        //add child to parent's children list
        if (parent.child == null) {
                parent.child = child;
                child_seper(parent, child);
                child.next = child;
                child.prev = child;
            } else { //if parent already has children
                child_seper(parent, child);
                parent.child.prev.next = child;
                child.prev = parent.child.prev;
                parent.child.prev = child;
                child.next = parent.child;
            }
            child.parent = parent;
            parent.rank++;
            return parent;
        }
    
        private void child_seper(HeapNode parent, HeapNode child){ //helping method for add_child
            if (child.next != child) { //if child is not alone in its list
                    child.prev.next = parent;
                    child.next.prev = parent;
                    parent.next = child.next;
                    parent.prev = child.prev;
                }
                else{ //if child is alone in its list
                    parent.next = parent;
                    parent.prev = parent;
                }
        }

    /**
     * 
     * Return the number of elements in the heap
     *   
     */
    public int size(){
        return size; // should be replaced by student code
    }


    /**
     * 
     * Return the number of trees in the heap.
     * 
     */
    public int numTrees()
    {
        return roots.length;
    }
    
    
    /**
     * 
     * Return the number of marked nodes in the heap.
     * 
     */
    public int numMarkedNodes(){
        return numMarked;
    }
    
    
    /**
     * 
     * Return the total number of links.
     * 
     */
    public int totalLinks(){
        return totalLinks;
    }
    
    
    /**
     * 
     * Return the total number of cuts.
     * 
     */
    public int totalCuts(){
        return totalCuts;
    }
    

    /**
     * 
     * Return the total heapify costs.
     * 
     */
    public int totalHeapifyCosts(){
        return totalHeapifyCosts; // should be replaced by student code
    }
    
    
    /**
     * Class implementing a node in a ExtendedFibonacci Heap.
     *  
     */
    public static class HeapNode{
        public int key;
        public String info;
        public HeapNode child;
        public HeapNode next = this;
        public HeapNode prev = this;
        public HeapNode parent;
        public int rank;
    }

    public static void main(String[] args) { //test insert and findMin
        // Heap heap = new Heap(true, false);
        // heap.insert(5, "five");
        // heap.insert(3, "three");
        // heap.insert(7, "seven");
        // HeapNode minNode = heap.findMin();
        // System.out.println("Min key: " + minNode.key + ", info: " + minNode.info); // Expected: Min key: 3, info: three
        // HeapNode start = heap.min;
        // System.out.println("Root list:");
        // while (true) {
        //     System.out.println("Key: " + start.key + ", info: " + start.info);
        //     start = start.next;
        //     if (start == heap.min) {
        //         break;
        //     }
        // }
        // heap.decreaseKey(heap.min.next, 4); // Decrease key from 5 to 1
        // HeapNode minNode2 = heap.findMin();
        // System.out.println("Min key: " + minNode2.key + ", info: " + minNode2.info); // Expected: Min key: 1, info: five
        // HeapNode start2 = heap.min;
        // System.out.println("Root list:");
        // while (true) {
        //     System.out.println("Key: " + start2.key + ", info: " + start2.info);
        //     start2 = start2.next;
        //     if (start2 == heap.min) {
        //         break;
        //     }
        // }
        //test heapifyUp
        //build a small heap manually
    }
}
