/**
 * Heap
 *
 * An implementation of Fibonacci heap over positive integers 
 * with the possibility of not performing lazy melds and 
 * the possibility of not performing lazy decrease keys.
 *
 * fix rebuilding the roots list after succesive linking
 */
public class Heap
{
    public final boolean lazyMelds;
    public final boolean lazyDecreaseKeys;
    public HeapItem min;
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
    // Time Complexity: O(1) worst case
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
    // Time Complexity: O(1) amortized with lazy melds; O(n) worst case when eager consolidation runs
    public HeapItem insert(int key, String info) {
        HeapNode node = new HeapNode();
        HeapItem result = new HeapItem();
        result.key = key;
        result.info = info;
        result.node = node;
        node.item = result;

        // Add the new node to the roots array
        if (this.min == null) {
            this.min = result;
            node.next = node;
            node.prev = node;
            this.roots = 1;
        } else{
            //link new node to root list
            HeapNode minNode = this.min.node;
            HeapNode minPrev = minNode.prev;

            node.next = minNode;
            node.prev = minPrev;
            minPrev.next = node;
            minNode.prev = node;

            if (key < this.min.key) {
                this.min = result;
            }
            this.roots++;
        }
        this.size++;
        return result;
    }

    /**
     * 
     * Return the minimal HeapNode, null if empty.
     *
     */
    // Time Complexity: O(1) worst case
    public HeapItem findMin(){
        return this.min;
    }

    // Time Complexity: O(n) worst case - traverses root list to find min
    public HeapItem SearchMin(){
        int curr_min = Integer.MAX_VALUE;
        if (this.min == null) {
            return null; // heap is empty
        }
        HeapNode min_node = this.min.node;
        HeapNode curr = this.min.node;

        while (true) {
            if (curr.item.key < curr_min) {
                curr_min = curr.item.key;
                min_node = curr;
            }
            curr = curr.next;
            if (curr == this.min.node) {
                break; // we have traversed the entire root list
            }
        }
        return min_node.item; 
    }


    /**
     * 
     * Delete the minimal item.
     *
     */
    // Time Complexity: O(n) worst case - moves children to roots then consolidates
    public void deleteMin() {
        if (this.min == null) return;

        HeapNode oldMin = this.min.node;
        int kids = oldMin.rank;

        // 1. Update global stats immediately
        this.size--;
        // Net change in roots: -1 (remove min) + kids (children become roots)
        this.roots = this.roots - 1 + kids;

        // 2. Promote children to the root list
        if (kids > 0) {
            HeapNode firstChild = oldMin.child;
            // Detach parent pointers for all children
            HeapNode currChild = firstChild;
            do {
                currChild.parent = null;
                currChild = currChild.next;
            } while (currChild != firstChild);

            // Splice children into the root list
            if (this.roots == kids) { 
                // Case: oldMin was the ONLY root. The children BECOME the list.
                this.min = firstChild.item; // Temporary min, will be fixed in linking
            } else {
                // Case: There are other roots. Splice children between oldMin.prev and oldMin.next
                HeapNode prevRoot = oldMin.prev;
                HeapNode nextRoot = oldMin.next;
                HeapNode lastChild = firstChild.prev;

                prevRoot.next = firstChild;
                firstChild.prev = prevRoot;
                lastChild.next = nextRoot;
                nextRoot.prev = lastChild;
                
                this.min = nextRoot.item; // Temporary min
            }
        } else {
            // No children
            if (this.roots == 0) {
                this.min = null;
                return;
            }
            // Close the gap in the list
            oldMin.prev.next = oldMin.next;
            oldMin.next.prev = oldMin.prev;
            this.min = oldMin.next.item;
        }
    
    // 3. Consolidate
    succsesive_linking(this);
}

    /**
     * 
     * pre: 0<=diff<=x.key
     * 
     * Decrease the key of x by diff and fix the heap.
     * 
     */
    // Time Complexity: O(n) worst case with lazy cuts (cascading cuts); O(log n) worst case with eager heapify
    public void decreaseKey(HeapItem x, int diff) {
        x.key -= diff;
        HeapNode x_node = x.node;
        if (x_node.parent == null) {
            if (x.key < this.min.key) {
                this.min = x;
            }
            return;
        }
        if (x.key < x_node.parent.item.key) {
            // cut x from its parent
            if (this.lazyDecreaseKeys) {
                HeapNode x_p = x_node.parent;
                cut(x_node);
                cascadingCuts(x_p);
            }
            else{
               //non-lazy decrease key
            heapifyUp(x.node); 
            }
        }
        if (x.key < this.min.key) {
            this.min = x;
        }
    }

    // Time Complexity: O(log n) worst case - walks parent chain and swaps content
    private void heapifyUp(HeapNode x) { //helping method for decrease key, not tested
        HeapNode curr = x;
        while (curr.parent != null) {
            if (curr.item.key < curr.parent.item.key) {
                swapWithParent(curr);
                curr = curr.parent;
                this.totalHeapifyCosts++;
            } else {
                break;
            }
        }
    }

    // Time Complexity: O(1) worst case
    private void swapWithParent(HeapNode child) {
        HeapNode parent = child.parent;
        HeapItem childItem = child.item;
        HeapItem parentItem = parent.item;
        //swap items
        child.item = parentItem;
        parent.item = childItem;
    }

    // Time Complexity: O(n) worst case - may cascade up to the root
    private void cascadingCuts(HeapNode y) { //helping method for decrease key, not tested
        if (y.parent == null) {
            return;
        }
        if (!y.marked) {
            y.marked = true;
            this.numMarked++;
        } else {
            HeapNode y_p = y.parent;
            cut(y);
            y.marked = false;
            this.numMarked--;
            cascadingCuts(y_p);
        }
    }


        // Time Complexity: O(1) worst case - removes node and performs lazy meld
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
                x.parent.rank = x.parent.rank - 1;
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
                to_meld.min = x.item;
                to_meld.min.node = x;
                to_meld.roots = 1;
                to_meld.size = 0; //size stays the same
                this.meld(to_meld);
            }


    /**
     * 
     * Delete the x from the heap.
     *
     */
    // Time Complexity: O(n) worst case - delegates to decreaseKey and deleteMin
    public void delete(HeapItem x) {    
        decreaseKey(x, Integer.MIN_VALUE);
        deleteMin();
    }


    /**
     * 
     * Meld the heap with heap2
     * pre: heap2.lazyMelds = this.lazyMelds AND heap2.lazyDecreaseKeys = this.lazyDecreaseKeys
     *
     */
    // Time Complexity: O(1) worst case when lazy melds; O(n) worst case when eager consolidation triggers
    public void meld(Heap heap2){
        this.size += heap2.size; //true for both lazy and non-lazy melds
        this.numMarked += heap2.numMarked;
        this.totalLinks += heap2.totalLinks;
        this.totalCuts += heap2.totalCuts;
        this.totalHeapifyCosts += heap2.totalHeapifyCosts;
        this.roots = this.roots + heap2.roots;

        lazyMeld(heap2);
        if (!lazyMelds){ //eager meld
            this.succsesive_linking(this);
            
        }
    }

        // Time Complexity: O(1) worst case - simply concatenates root lists
    public void lazyMeld(Heap heap2){ //finished, tested
        //used if lazy melds is true
        //updating roots pointers
        if (this.min == null){ //if this heap is empty
            this.roots = heap2.roots;
            this.min = heap2.min;
        }
        else if (heap2.min != null){ //if heap2 is not empty
            //link the two roots lists
            HeapNode this_min_prev = this.min.node.prev;
            HeapNode heap2_min_prev = heap2.min.node.prev;

            this.min.node.prev = heap2_min_prev;
            heap2_min_prev.next = this.min.node;

            heap2.min.node.prev = this_min_prev;
            this_min_prev.next = heap2.min.node;
        }
        if (this.min == null || (heap2.min != null && heap2.min.key < this.min.key)){
            this.min = heap2.min;
        }
    }
    
// Time Complexity: O(n) worst case - consolidates trees by rank
    public void succsesive_linking(Heap heap) {
        if (heap.min == null) return;

        // 1. Create a snapshot of the current roots
        // We trust heap.roots to be accurate from deleteMin
        int numRoots = heap.roots;
        HeapNode[] rootsArray = new HeapNode[numRoots];
        HeapNode curr = heap.min.node;
        for (int i = 0; i < numRoots; i++) {
            rootsArray[i] = curr;
            curr = curr.next;
        }

        // 2. Consolidate trees (The core fix)
        // Use fixed size 64 (sufficient for any int-sized heap)
        HeapNode[] rankArray = new HeapNode[64]; 
            
        for (HeapNode node : rootsArray) {
            // Isolate the node to avoid pointer messes during linking
            node.next = node;
            node.prev = node;
                
            HeapNode active = node;
            int d = active.rank;
                
            // While there is a conflict at this rank, link them
            while (rankArray[d] != null) {
                HeapNode other = rankArray[d];
                active = link(active, other); // link returns the surviving root
                rankArray[d] = null; // Clear the slot
                d++;
            }
            // Place the result in the new rank slot
            rankArray[d] = active;
        }

        // 3. Rebuild the root list from the bucket array
        heap.min = null;
        heap.roots = 0;
            
        for (HeapNode node : rankArray) {
            if (node != null) {
                if (heap.min == null) {
                    heap.min = node.item;
                    node.next = node;
                    node.prev = node;
                    heap.roots = 1;
                } else {
                    addToRootList(heap, node);
                    if (node.item.key < heap.min.key) {
                        heap.min = node.item;
                    }
                    heap.roots++;
                }
            }
        }
    }

    // Helper for the rebuild phase
    private void addToRootList(Heap heap, HeapNode node) {
        HeapNode minNode = heap.min.node;
        HeapNode minPrev = minNode.prev;
        node.next = minNode;
        node.prev = minPrev;
        minPrev.next = node;
        minNode.prev = node;
    }


    // Time Complexity: O(1) worst case - links two trees by making smaller key the parent
    private HeapNode link(HeapNode a, HeapNode b) {
        this.totalLinks++;
        if (a.item.key < b.item.key) {
            a = add_child(a, b);
            return a; // a is the winner
        } else {
            b = add_child(b, a);
            return b; // b is the winner
        }
    }
    
    // Time Complexity: O(1) worst case - adds child to parent's child list
    public HeapNode add_child (HeapNode parent, HeapNode child){
        //add child to parent's children list
        if (parent.child == null) {
                parent.child = child;
                // child_seper(parent, child);
                child.next = child;
                child.prev = child;
            } else { //if parent already has children
                // child_seper(parent, child);
                parent.child.prev.next = child;
                child.prev = parent.child.prev;
                parent.child.prev = child;
                child.next = parent.child;
            }
            child.parent = parent;
            parent.rank++;
            return parent;
        }

    
    /**
     * 
     * Return the number of elements in the heap
     *   
     */
    // Time Complexity: O(1) worst case
    public int size()
    {
        return size;
    }


    /**
     * 
     * Return the number of trees in the heap.
     * 
     */
    // Time Complexity: O(1) worst case
    public int numTrees()
    {
        return roots;
    }
    
    
    /**
     * 
     * Return the number of marked nodes in the heap.
     * 
     */
    // Time Complexity: O(1) worst case
    public int numMarkedNodes()
    {
        return numMarked;
    }
    
    
    /**
     * 
     * Return the total number of links.
     * 
     */
    // Time Complexity: O(1) worst case
    public int totalLinks()
    {
        return totalLinks;
    }
    
    
    /**
     * 
     * Return the total number of cuts.
     * 
     */
    // Time Complexity: O(1) worst case
    public int totalCuts()
    {
        return totalCuts;
    }
    

    /**
     * 
     * Return the total heapify costs.
     * 
     */
    // Time Complexity: O(1) worst case
    public int totalHeapifyCosts()
    {
        return totalHeapifyCosts;
    }
    
    
    /**
     * Class implementing a node in a Heap.
     *  
     */
    public static class HeapNode{
        public HeapItem item;
        public HeapNode child;
        public HeapNode next = this;
        public HeapNode prev = this;
        public HeapNode parent;
        public int rank;
        public boolean marked = false;
    }
    
    /**
     * Class implementing an item in a Heap.
     *  
     */
    public static class HeapItem{
        public HeapNode node;
        public int key;
        public String info;
    }

}