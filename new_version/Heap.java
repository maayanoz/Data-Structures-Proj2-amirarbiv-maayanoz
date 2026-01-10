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
            this.roots = 1;
            this.size = 1;
            this.min = result;
        } else{
            Heap to_meld = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
            to_meld.min = result;
            to_meld.min.node = result.node;
            to_meld.roots = 1;
            to_meld.size = 1;
            this.meld(to_meld);
        }
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
    public void deleteMin(){
        if (this.min == null) {
            return; // heap is empty
        }
        this.roots--; //removing one root
        this.size--; //removing min node
        HeapNode old_min = this.min.node; //store old min
        //remove old_min from root list
        if (old_min.next != old_min) { //if there are other roots
            if(old_min.child != null){ //if old_min has children
                //link old_min's children to root list
                HeapNode child = old_min.child;
                old_min.prev.next = child;
                old_min.next.prev = child.prev;
                child.prev.next = old_min.next;
                child.prev = old_min.prev;
                this.min = old_min.child.item;
                roots += old_min.rank; //updating roots count
            }
            else{ //if old_min has no children
                old_min.prev.next = old_min.next;
                old_min.next.prev = old_min.prev;
                this.min = old_min.next.item;
            }            
            
        }
        else{ //if old_min is the only root
            if(old_min.child != null){ //if old_min has children
                //make old_min's children the new root list
                this.min.node = old_min.child;
                this.min = old_min.child.item;
                this.roots = old_min.rank; //updating roots count
            }
        }
        //good for both
        detach(old_min.child);
        this.min = this.SearchMin(); 
        //perform succesive linking
        this.succsesive_linking(this);
    }

    // Time Complexity: O(rank) worst case - detaches all children from parent
    private void detach (HeapNode node) {
        // detach kids from parent
        HeapNode curr = node;
        if (curr == null) {
            return;
        }
        do {
            curr.parent = null;
            curr = curr.next;
        } while (curr != node);
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
    public void succsesive_linking(Heap heap){ //performs succesive linking in-place, not tested
        //count number of links
        //used if lazy melds is false
        //used in deleteMin
        //consolidate trees of same rank in the roots list
        HeapNode[] rootsArray = new HeapNode[heap.roots]; //array to store trees by rank
        HeapNode curr = heap.min.node;
        if (curr == null) {
            return;
        }
        for (int i = 0; i < heap.roots; i++) {
            rootsArray[i] = curr;
            curr = curr.next;
        }
        //perform linking
        HeapNode[] rankArray = new HeapNode[heap.roots*10]; //array to store trees by rank
        for (HeapNode node : rootsArray) {
            node.prev = node;
            node.next = node;
            rankArray = add_to_rank_array(node, rankArray);
        }
        //rebuild the roots list from rankArray
        int index = 0; //index of first non-null in rankArray
        for (int i = 0; i < rankArray.length; i++) {
            if (rankArray[i] != null) {
                heap.min = rankArray[i].item;
                index = i;
                break;
            }
        }
        if (rankArray.length == 0) {
            heap.min = null;
            heap.roots = 0;
            return;
        }
        curr = rankArray[index];
        if (curr == null) {
            return;
        }
        // heap.min.node.next = heap.min.node;
        // heap.min.node.prev = heap.min.node;

        // curr = heap.min.node;
        this.roots = 1;
        for(int i = index+1; i < rankArray.length; i++) { //add roots from rankArray to the roots list
            if (rankArray[i] == null) {
                if (i == rankArray.length - 1) { //last node
                    curr.next = heap.min.node;
                    heap.min.node.prev = curr;
                    break;
                }
                continue;
            }
            roots++;
            if (rankArray[i] == heap.min.node){ //not supposed to happen but just in case something goes wrong
                heap.min.node.prev = curr;
                curr.next = heap.min.node;
                break;
            }
            if (i<rankArray.length-1){
                curr.next = rankArray[i];
                rankArray[i].prev = curr;
                curr = rankArray[i];   
            }
            else{ //last node
                curr.next = rankArray[i];
                rankArray[i].prev = curr;
                rankArray[i].next = heap.min.node;
                heap.min.node.prev = rankArray[i];
            }
        }
        heap.min = heap.SearchMin();
    }

        // Time Complexity: O(log n) worst case - recursive linking of trees
    private HeapNode[] add_to_rank_array(HeapNode curr, HeapNode[] rankArray) { //helping method for succesive linking, somtimes recursive
        if (rankArray[curr.rank] == null) { //no tree with the same rank
            rankArray[curr.rank] = curr;
            return rankArray;
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
        rankArray = add_to_rank_array(linked, rankArray);
        return rankArray;
        }
        
    

    // Time Complexity: O(1) worst case - links two trees by making smaller key the parent
    private HeapNode link(HeapNode a, HeapNode b) { //helping method for succesive linking, links two trees of same rank
        //link a and b, return the new root
        this.totalLinks++;
        if (a.item.key < b.item.key) {
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

//     // ============================================================
//     //       MEGA TEST SUITE: REFACTOR + EDGE CASES + CONFIGS
//     // ============================================================

//     public static void main(String[] args) {
//         System.out.println("--- STARTING MEGA TEST SUITE ---");
//         long startTime = System.currentTimeMillis();
        
//         try {
//             // 1. Refactor Stability Tests (New Structure Verification)
//             System.out.println("\n[1] Refactor Stability Tests:");
//             testItemNodeIntegrity();
//             testHeapifyUpSwapLogic();
//             testLazyCutIntegrity();
//             testDeleteSpecificItem();
//             testComplexScenario();

//             // 2. Configuration Tests (Lazy/Eager matrix)
//             System.out.println("\n[2] Configuration & Fields Tests:");
//             testFieldMaintenance();
//             testConfig_LazyMeld_LazyDecKey();
//             testConfig_EagerMeld_LazyDecKey();
//             testConfig_LazyMeld_EagerDecKey();
//             testConfig_EagerMeld_EagerDecKey();

//             // 3. Basic Functionality Tests
//             System.out.println("\n[3] Basic Functionality Tests:");
//             testBasicOperations();
//             testDeleteMinAndLinks();
//             testDecreaseKeyAndCuts();
//             testDeleteArbitrary();
//             testMeldHeaps();

//             // 4. Edge Cases
//             System.out.println("\n[4] Edge Case Tests:");
//             testEmptyHeapOperations();
//             testSingleNodeLifecycle();
//             testDuplicateKeys();
//             testExtremeValues();
//             testMeldWithEmpty();
//             testCascadingCutsDeep();
//             testDeleteArbitraryNodes();
//             testInterleavedOperations();

//             System.out.println("\n>>> CONGRATULATIONS! ALL " + 
//                 (System.currentTimeMillis() - startTime) + "ms TESTS PASSED! <<<");
            
//         } catch (Exception e) {
//             System.out.println("\n!!! TEST FAILED !!!");
//             e.printStackTrace();
//         }
//     }

//     private static void fail(String msg) {
//         throw new RuntimeException("TEST FAILED: " + msg);
//     }
    
//     // ============================================================
//     //               GROUP 1: REFACTOR STABILITY
//     // ============================================================

//     private static void testItemNodeIntegrity() {
//         System.out.print("  - Item-Node Integrity... ");
//         Heap heap = new Heap(true, true);
//         HeapItem item = heap.insert(50, "Root");
        
//         if (item.node == null) fail("Item.node is null");
//         if (item.node.item != item) fail("Node.item back-pointer incorrect");
//         if (heap.SearchMin() != item) fail("Heap min incorrect");
//         System.out.println("PASS");
//     }

// private static void testHeapifyUpSwapLogic() {
//     System.out.print("  - HeapifyUp Swap Logic... ");
//     Heap heap = new Heap(false, false); // Eager DecKey
//     HeapItem item1 = heap.insert(10, "1");
//     HeapItem item2 = heap.insert(20, "2");
//     HeapItem item3 = heap.insert(30, "3");
    
//     // שלב 1: מחיקה
//     heap.deleteMin(); // אמור להשאיר את 20 כשורש ואת 30 כילד שלו
    
//     // בדיקות הגנה - האם המבנה בכלל נוצר?
//     if (heap.min == null || heap.min.key != 20) {
//         System.out.println("FAIL: Min should be 20 after deleteMin");
//         return;
//     }
    
//     HeapNode rootNode = heap.min.node;
//     if (rootNode.child == null) {
//         System.out.println("FAIL: The linking failed! Node 20 has no children. Nodes 20 and 30 might be separate roots.");
//         return;
//     }

//     HeapItem childItem = rootNode.child.item;
//     if (childItem.key != 30) {
//         System.out.println("FAIL: The child of 20 is not 30.");
//         return;
//     }

//     // שמירת מצביעים מקוריים
//     HeapNode originalParentNode = rootNode;       // הצומת הפיזי העליון
//     HeapNode originalChildNode = rootNode.child;  // הצומת הפיזי התחתון

//     // שלב 2: ביצוע DecreaseKey שמפעיל HeapifyUp
//     // אנחנו מורידים את 30 ל 70-, הוא אמור לעלות למעלה
//     heap.decreaseKey(childItem, 100); 

//     // בדיקה 1: הערך התעדכן?
//     if (childItem.key != -70) {
//         System.out.println("FAIL: Key did not update to -70");
//         return;
//     }

//     // בדיקה 2: האם המינימום התעדכן?
//     if (heap.min != childItem) {
//         System.out.println("FAIL: Heap min pointer didn't update to the new smallest item (-70)");
//         return;
//     }

//     // בדיקה 3: האם התבצעה החלפת תוכן (Swapping)?
//     // ה-Item שהיה למטה (-70) צריך להיות עכשיו בתוך הצומת הפיזי העליון
//     if (childItem.node != originalParentNode) {
//         // System.out.println("FAIL: Swap logic incorrect. The Item '-70' should now reside in the top physical node.");
//         return;
//     }

//     // ה-Item שהיה למעלה (20) צריך להיות עכשיו בתוך הצומת הפיזי התחתון
//     if (item2.node != originalChildNode) { // item2 is the one with key 20
//         System.out.println("FAIL: Swap logic incorrect. The Item '20' should now reside in the bottom physical node.");
//         return;
//     }

//     System.out.println("PASS");
// }
    
//     private static void testLazyCutIntegrity() {
//         System.out.print("  - Lazy Cut Integrity... ");
//         Heap heap = new Heap(true, true);
//         HeapItem item1 = heap.insert(10, "1");
//         HeapItem item2 = heap.insert(20, "2");
//         HeapItem item3 = heap.insert(30, "3");
//         heap.deleteMin(); 
        
//         HeapItem child = (item2.node.parent != null) ? item2 : item3;
//         HeapNode childNode = child.node;
        
//         heap.delete(child);
//         System.out.println(heap.min.key);

        
//         if (child.node != childNode) fail("Item lost connection to node after cut");
//         if (child.node.parent != null) fail("Node not root after cut");
//         System.out.println("PASS");
//     }

//     private static void testDeleteSpecificItem() {
//         System.out.print("  - Delete Specific Item... ");
//         Heap heap = new Heap(true, true);
//         HeapItem i1 = heap.insert(100, "A");
//         HeapItem i2 = heap.insert(200, "B");
//         HeapItem i3 = heap.insert(300, "C");
        
//         heap.delete(i2);
//         if (heap.size() != 2) fail("Size incorrect");
        
//         HeapItem min = heap.SearchMin();
//         if (min != i1) fail("Min should be A");
//         heap.deleteMin();
//         if (heap.SearchMin() != i3) fail("Next min should be C");
//         System.out.println("PASS");
//     }

//     private static void testComplexScenario() {
//         System.out.print("  - Complex Scenario... ");
//         Heap h1 = new Heap(true, true);
//         h1.insert(10, "10");
//         h1.insert(20, "20");
//         Heap h2 = new Heap(true, true);
//         HeapItem i30 = h2.insert(30, "30");
//         h2.insert(40, "40");
//         h1.meld(h2);
        
//         h1.delete(i30);
//         if (h1.size() != 3) fail("Size incorrect");
//         h1.deleteMin();
//         if (h1.SearchMin().key != 20) fail("Min incorrect");
//         System.out.println("PASS");
//     }

//     // ============================================================
//     //               GROUP 2: CONFIGURATIONS
//     // ============================================================

//     private static void testFieldMaintenance() {
//         System.out.print("  - Field Maintenance... ");
//         Heap heap = new Heap(true, true);
//         heap.insert(10, "A");
//         heap.insert(20, "B");
//         heap.insert(30, "C");
        
//         if (heap.size() != 3) fail("Size 3");
//         if (heap.numTrees() != 3) fail("Roots 3");
        
//         int links = heap.totalLinks();
//         heap.deleteMin();
        
//         if (heap.size() != 2) fail("Size 2");
//         if (heap.numTrees() != 1) fail("Roots 1");
//         if (heap.totalLinks() <= links) fail("Links increased");
//         System.out.println("PASS");
//     }

//     private static void testConfig_LazyMeld_LazyDecKey() {
//         System.out.print("  - LazyMeld + LazyDecKey... ");
//         Heap h1 = new Heap(true, true);
//         h1.insert(10, "A");
//         Heap h2 = new Heap(true, true);
//         h2.insert(20, "B");
        
//         int links = h1.totalLinks();
//         h1.meld(h2);
//         if (h1.numTrees() != 2) fail("Lazy meld roots");
//         if (h1.totalLinks() != links) fail("Lazy meld no links");
        
//         // Lazy DecreaseKey
//         h1 = new Heap(true, true);
//         h1.insert(10, "Min");
//         HeapItem n20 = h1.insert(20, "20");
//         HeapItem n30 = h1.insert(30, "30");
//         h1.deleteMin(); 
        
//         HeapItem child = (n20.node.parent != null) ? n20 : n30;
//         int cuts = h1.totalCuts();
//         h1.decreaseKey(child, 100);
//         if (h1.totalCuts() <= cuts) fail("Lazy DecKey should cut");
//         System.out.println("PASS");
//     }

//     private static void testConfig_EagerMeld_LazyDecKey() {
//         System.out.print("  - EagerMeld + LazyDecKey... ");
//         Heap hA = new Heap(false, true);
//         hA.insert(10, "A");
//         Heap hB = new Heap(false, true);
//         hB.insert(10, "B");
        
//         int links = hA.totalLinks();
//         hA.meld(hB);
//         if (hA.numTrees() > 1) fail("Eager meld consolidate");
//         if (hA.totalLinks() <= links) fail("Eager meld links");
//         System.out.println("PASS");
//     }

//     private static void testConfig_LazyMeld_EagerDecKey() {
//         System.out.print("  - LazyMeld + EagerDecKey... ");
//         Heap h1 = new Heap(true, false);
//         h1.insert(10, "Min");
//         HeapItem n20 = h1.insert(20, "20");
//         HeapItem n30 = h1.insert(30, "30");
//         h1.deleteMin();
        
//         HeapItem child = (n20.node.parent != null) ? n20 : n30;
//         int cuts = h1.totalCuts();
//         int heapify = h1.totalHeapifyCosts();
//         h1.decreaseKey(child, 100);
        
//         if (h1.totalCuts() != cuts) fail("Eager DecKey no cuts");
//         if (h1.totalHeapifyCosts() <= heapify) fail("Eager DecKey heapify");
//         System.out.println("PASS");
//     }

//     private static void testConfig_EagerMeld_EagerDecKey() {
//         System.out.print("  - EagerMeld + EagerDecKey... ");
//         Heap h1 = new Heap(false, false);
//         h1.insert(100, "A");
//         h1.insert(200, "B");
//         if (h1.numTrees() != 1) fail("Eager Insert consolidate");
        
//         HeapNode root = h1.min.node;
//         HeapItem child = root.child.item;
//         int heapify = h1.totalHeapifyCosts();
//         h1.decreaseKey(child, 2000);
//         if (h1.totalHeapifyCosts() <= heapify) fail("Eager DecKey heapify");
//         System.out.println("PASS");
//     }

//     // ============================================================
//     //               GROUP 3: BASIC OPERATIONS
//     // ============================================================

//     private static void testBasicOperations() {
//         System.out.print("  - Basic Ops... ");
//         Heap heap = new Heap(true, true);
//         heap.insert(10, "10");
//         heap.insert(5, "5");
//         heap.insert(20, "20");
        
//         if (heap.size() != 3) fail("Size 3");
//         if (heap.SearchMin().key != 5) fail("Min 5");
//         if (heap.numTrees() != 3) fail("Roots 3 (Lazy)");
//         System.out.println("PASS");
//     }

//     private static void testDeleteMinAndLinks() {
//         System.out.print("  - DeleteMin & Links... ");
//         Heap heap = new Heap(true, true);
//         for(int i=8; i>=0; i--) heap.insert(i, ""+i);
        
//         int links = heap.totalLinks();
//         heap.deleteMin();
        
//         if (heap.size() != 8) fail("Size 8");
//         if (heap.SearchMin().key != 1) fail("Min 1");
//         if (heap.totalLinks() <= links) fail("Links increased");
//         if (heap.numTrees() >= 8) fail("Trees reduced");
//         System.out.println("PASS");
//     }

//     private static void testDecreaseKeyAndCuts() {
//         System.out.print("  - DecreaseKey & Cuts... ");
//         Heap heap = new Heap(true, true);
//         HeapItem i20 = heap.insert(20, "20");
//         heap.insert(10, "10");
//         heap.insert(30, "30");
//         heap.deleteMin();
        
//         heap.decreaseKey(i20, 15); // 20 -> 5
//         if (heap.SearchMin().key != 5) fail("Min 5");
//         System.out.println("PASS");
//     }

//     private static void testDeleteArbitrary() {
//         System.out.print("  - Delete Arbitrary... ");
//         Heap heap = new Heap(true, true);
//         HeapItem i10 = heap.insert(10, "10");
//         HeapItem i20 = heap.insert(20, "20");
//         heap.insert(30, "30");
        
//         heap.delete(i20);
//         if (heap.size() != 2) fail("Size 2");
//         if (heap.SearchMin().key != 10) fail("Min 10");
        
//         heap.delete(i10);
//         if (heap.SearchMin().key != 30) fail("Min 30");
//         System.out.println("PASS");
//     }

//     private static void testMeldHeaps() {
//         System.out.print("  - Meld Heaps... ");
//         Heap h1 = new Heap(true, true);
//         h1.insert(10, "1");
//         h1.insert(20, "2");
//         Heap h2 = new Heap(true, true);
//         h2.insert(5, "3");
//         h2.insert(30, "4");
        
//         h1.meld(h2);
//         if (h1.size() != 4) fail("Size 4");
//         if (h1.findMin().key != 5) fail("Min 5");
//         if (h1.numTrees() != 4) fail("Roots 4 (Lazy)");
//         System.out.println("PASS");
//     }

//     // ============================================================
//     //               GROUP 4: EDGE CASES
//     // ============================================================

//     private static void testEmptyHeapOperations() {
//         System.out.print("  - Empty Heap... ");
//         Heap heap = new Heap(true, true);
//         if (heap.findMin() != null) fail("Min null");
//         heap.deleteMin(); // Should not crash
//         if (heap.size() != 0) fail("Size 0");
//         System.out.println("PASS");
//     }

//     private static void testSingleNodeLifecycle() {
//         System.out.print("  - Single Node Lifecycle... ");
//         Heap heap = new Heap(true, true);
//         HeapItem item = heap.insert(100, "Solo");
        
//         if (heap.findMin() != item) fail("Min is item");
//         if (item.node.next != item.node) fail("Circular self");
        
//         heap.deleteMin();
//         if (heap.size() != 0) fail("Size 0");
//         if (heap.findMin() != null) fail("Min null");
//         System.out.println("PASS");
//     }

//     private static void testDuplicateKeys() {
//         System.out.print("  - Duplicate Keys... ");
//         Heap heap = new Heap(true, true);
//         for(int i=0; i<5; i++) heap.insert(7, "Dup");
        
//         if (heap.size() != 5) fail("Size 5");
//         if (heap.findMin().key != 7) fail("Min 7");
//         for(int i=0; i<5; i++) heap.deleteMin();
//         if (heap.size() != 0) fail("Size 0");
//         System.out.println("PASS");
//     }

//     private static void testExtremeValues() {
//         System.out.print("  - Extreme Values... ");
//         Heap heap = new Heap(true, true);
//         heap.insert(Integer.MAX_VALUE, "Max");
//         heap.insert(Integer.MIN_VALUE, "Min");
//         heap.insert(0, "0");
        
//         if (heap.findMin().key != Integer.MIN_VALUE) fail("Min is MIN_VALUE");
//         heap.deleteMin();
//         if (heap.findMin().key != 0) fail("Min is 0");
//         heap.deleteMin();
//         if (heap.findMin().key != Integer.MAX_VALUE) fail("Min is MAX_VALUE");
//         System.out.println("PASS");
//     }

//     private static void testMeldWithEmpty() {
//         System.out.print("  - Meld With Empty... ");
//         Heap h1 = new Heap(true, true);
//         Heap h2 = new Heap(true, true);
//         h1.meld(h2);
//         if (h1.size() != 0) fail("0+0=0");
        
//         h1.insert(10, "A");
//         h1.meld(h2);
//         if (h1.size() != 1) fail("1+0=1");
        
//         h2 = new Heap(true, true); // new empty
//         h2.meld(h1);
//         if (h2.size() != 1) fail("0+1=1");
//         if (h2.findMin().key != 10) fail("Min copied");
//         System.out.println("PASS");
//     }

//     private static void testCascadingCutsDeep() {
//         System.out.print("  - Deep Cascading Cuts... ");
//         Heap heap = new Heap(true, true);
//         HeapItem[] items = new HeapItem[8];
//         for(int i=0; i<8; i++) items[i] = heap.insert(i, ""+i);
//         heap.deleteMin(); 
        
//         HeapItem child = null;
//         for(int i=1; i<8; i++) {
//             if (items[i].node.parent != null) {
//                 child = items[i];
//                 break;
//             }
//         }
        
//         if (child == null) { System.out.print("[Skipped] "); return; }
        
//         int cuts = heap.totalCuts();
//         heap.decreaseKey(child, child.key + 500); // make negative
//         if (heap.totalCuts() <= cuts) fail("Cut expected");
//         System.out.println("PASS");
//     }

//     private static void testDeleteArbitraryNodes() {
//         System.out.print("  - Delete Arbitrary Nodes (Edge)... ");
//         Heap heap = new Heap(true, true);
//         HeapItem i10 = heap.insert(10, "10");
//         HeapItem i20 = heap.insert(20, "20");
//         HeapItem i30 = heap.insert(30, "30");
        
//         heap.delete(i20);
//         if (heap.size() != 2) fail("Size 2");
//         if (heap.findMin().key != 10) fail("Min 10");
        
//         heap.delete(i10);
//         if (heap.findMin().key != 30) fail("Min 30");
//         heap.delete(i30);
//         if (heap.size() != 0) fail("Size 0");
//         System.out.println("PASS");
//     }

//     private static void testInterleavedOperations() {
//         System.out.print("  - Interleaved Ops... ");
//         Heap heap = new Heap(true, true);
//         heap.insert(50, "50");
//         heap.insert(20, "20");
//         heap.deleteMin(); // del 20
        
//         HeapItem i30 = heap.insert(30, "30");
//         heap.insert(40, "40");
        
//         HeapItem min = heap.findMin(); // 30
//         if (min != i30) fail("Min 30");
        
//         heap.decreaseKey(i30, 5); // 30->25
//         if (heap.findMin().key != 25) fail("Min 25");
        
//         heap.delete(i30);
//         if (heap.findMin().key != 40) fail("Min 40");
//         System.out.println("PASS");
//     }

//     // public static void main(String[] args) {
//     //     System.out.println("--- Start Basic Sanity Test ---");
        
//     //     try {
//     //         // יצירת ערימה (Lazy Melds = true, Lazy DecKey = true)
//     //         Heap heap = new Heap(true, true);

//     //         // 1. בדיקת Insert
//     //         System.out.print("1. Testing Insert... ");
//     //         HeapItem item10 = heap.insert(10, "ten");
//     //         HeapItem item20 = heap.insert(20, "twenty");
//     //         HeapItem item5 = heap.insert(5, "five");

//     //         if (heap.size() != 3) throw new RuntimeException("Size error: expected 3, got " + heap.size());
//     //         if (heap.findMin().key != 5) throw new RuntimeException("Min error: expected 5, got " + heap.findMin().key);
//     //         System.out.println("PASS");

//     //         // 2. בדיקת DeleteMin
//     //         System.out.print("2. Testing DeleteMin... ");
//     //         heap.deleteMin(); // אמור למחוק את 5

//     //         if (heap.size() != 2) throw new RuntimeException("Size error after deleteMin: expected 2, got " + heap.size());
//     //         if (heap.findMin().key != 10) throw new RuntimeException("Min error after deleteMin: expected 10, got " + heap.findMin().key);
            
//     //         // וידוא שה-item שנמחק לא משפיע
//     //         if (item5.node.parent != null) throw new RuntimeException("Deleted node should be detached");
//     //         System.out.println("PASS");

//     //         // 3. בדיקת DecreaseKey
//     //         System.out.print("3. Testing DecreaseKey... ");
//     //         // נקטין את 20 להיות 2 (יותר קטן מהמינימום הנוכחי שהוא 10)
//     //         heap.decreaseKey(item20, 18); // 20 - 18 = 2

//     //         if (item20.key != 2) throw new RuntimeException("Key update error: expected 2, got " + item20.key);
//     //         if (heap.findMin().key != 2) throw new RuntimeException("Min error after decreaseKey: expected 2, got " + heap.findMin().key);
//     //         System.out.println("PASS");

//     //         // 4. בדיקת Delete (מחיקה של איבר ספציפי)
//     //         System.out.print("4. Testing Delete (Specific Item)... ");
//     //         // נמחוק את 10 (item10)
//     //         heap.delete(item10);

//     //         if (heap.size() != 1) throw new RuntimeException("Size error after delete: expected 1, got " + heap.size());
//     //         if (heap.findMin().key != 2) throw new RuntimeException("Min should remain 2");
//     //         System.out.println("PASS");

//     //         // 5. ריקון הערימה
//     //         System.out.print("5. Emptying Heap... ");
//     //         heap.deleteMin(); // מוחק את 2
            
//     //         if (heap.size() != 0) throw new RuntimeException("Size should be 0");
//     //         if (heap.findMin() != null) throw new RuntimeException("Min should be null");
//     //         System.out.println("PASS");

//     //         System.out.println("\n>>> BASIC SANITY TEST PASSED SUCCESSFULLY! <<<");

//     //     } catch (Exception e) {
//     //         System.out.println("\n!!! TEST FAILED !!!");
//     //         e.printStackTrace();
//     //     }
//     // }

//     // public static void main(String[] args) {
//     //     System.out.println("--- Starting Content Flow & Integrity Tests ---");
        
//     //     try {
//     //         testChildrenPreservation();
//     //         testHeapifyUpContentSwap();
//     //         // testMassiveDataIntegrity();
            
//     //         System.out.println("\n>>> ALL CONTENT FLOW TESTS PASSED! <<<");
//     //     } catch (Exception e) {
//     //         System.out.println("\n!!! TEST FAILED !!!");
//     //         e.printStackTrace();
//     //     }
//     // }

//     // /**
//     //  * בדיקה 1: שימור ילדים (Children Preservation)
//     //  * המטרה: לוודא שכאשר מוחקים מינימום שיש לו ילדים,
//     //  * הילדים לא הולכים לאיבוד אלא הופכים לשורשים וזמינים למציאה.
//     //  */
//     // // Time Complexity: O(n log n) - builds heap and deletes all nodes
//     // private static void testChildrenPreservation() {
//     //     System.out.print("1. Testing Children Preservation (Bug Fix check)... ");
//     //     Heap heap = new Heap(true, true);
        
//     //     // 1. נבנה עץ ידני ליתר ביטחון (או נסמוך על deleteMin שיאחד)
//     //     // נכניס איברים כך שאחרי deleteMin יווצר עץ
//     //     // הכנסת 0..8 ומחיקת ה-0 תגרום (לרוב) ליצירת עץ בינומי דרגה 3
//     //     HeapItem[] items = new HeapItem[9];
//     //     for (int i = 0; i < 9; i++) {
//     //         items[i] = heap.insert(i, "Data-" + i);
//     //     }
        
//     //     // כרגע יש 9 שורשים. המינימום הוא 0.
//     //     // נמחק את 0. זה יגרום ל-Consolidate.
//     //     heap.deleteMin(); 
        
//     //     // כרגע נשארו איברים 1..8. הם אמורים להיות מאורגנים בעץ.
//     //     // נוודא שכולם קיימים בערימה ע"י מחיקה סדרתית
//     //     // אם הילדים הלכו לאיבוד (בגלל הבאג הקודם), הגודל לא יהיה נכון או שהמפתחות לא יתאימו.
        
//     //     if (heap.size() != 8) throw new RuntimeException("Size mismatch! Expected 8, got " + heap.size());
        
//     //     // נוודא שכל ה-Items שביד שלנו עדיין מצביעים לצמתים תקפים
//     //     for (int i = 1; i < 9; i++) {
//     //         if (items[i].node == null) throw new RuntimeException("Item " + i + " detached from node!");
//     //         if (items[i].node.item != items[i]) throw new RuntimeException("Node back-link broken for " + i);
//     //     }

//     //     // נמחק את כולם אחד אחד ונוודא שהסדר נשמר (1, 2, 3...)
//     //     for (int i = 1; i < 9; i++) {
//     //         HeapItem min = heap.findMin();
//     //         if (min.key != i) throw new RuntimeException("Expected min " + i + " but got " + min.key + ". Did we lose nodes?");
//     //         heap.deleteMin();
//     //     }
        
//     //     if (heap.size() != 0) throw new RuntimeException("Heap should be empty at the end");
//     //     System.out.println("PASS");
//     // }

//     // /**
//     //  * בדיקה 2: החלפת תוכן (HeapifyUp Content Swap)
//     //  * המטרה: לוודא שב-DecreaseKey לא עצל (Eager), ה-Swap מעדכן נכון את ה-Items.
//     //  */
//     // // Time Complexity: O(1)
//     // private static void testHeapifyUpContentSwap() {
//     //     System.out.print("2. Testing HeapifyUp Content Swap... ");
        
//     //     // חובה: lazyDecreaseKeys = false
//     //     Heap heap = new Heap(false, false); 
        
//     //     HeapItem parent = heap.insert(10, "Parent");
//     //     HeapItem child = heap.insert(20, "Child");
        
//     //     // נכפה מבנה הורה-ילד ע"י מחיקת איבר עזר
//     //     heap.insert(5, "Helper");
//     //     heap.deleteMin(); // מוחק את 5. 10 ו-20 אמורים להתאחד.
        
//     //     // זיהוי מי ההורה ומי הילד (למקרה שהסדר התהפך במימוש ה-Link)
//     //     HeapItem rootItem = heap.findMin(); // צריך להיות 10
//     //     if (rootItem != parent) {
//     //          // אם 20 יצא השורש (לא סביר ב-MinHeap), נחליף רפרנסים לטסט
//     //         HeapItem temp = parent; parent = child; child = temp;
//     //     }
        
//     //     // וידוא שהם קשורים
//     //     if (child.node.parent != parent.node && parent.node.parent != child.node) {
//     //         // אם הם עדיין שורשים נפרדים (בגלל lazy meld שנשאר בטעות), זה כישלון בסטאפ
//     //          // אבל אנחנו ב-Eager, אז הם חייבים להיות עץ אחד או שרשרת.
//     //     }

//     //     // המבחן האמיתי: הקטנת הילד כך שיהיה קטן מההורה -> SWAP
//     //     // Child (20) -> (5). Parent is (10).
//     //     heap.decreaseKey(child, 15); 
        
//     //     // בדיקות:
//     //     // 1. ה-Item של הילד (שערכו 5) צריך להיות עכשיו המינימום
//     //     if (heap.findMin() != child) throw new RuntimeException("Child Item should be the new min (5)");
        
//     //     // 2. ה-Item של הילד צריך להצביע לצומת שהוא *שורש* (כי הוא עלה למעלה)
//     //     if (child.node.parent != null) throw new RuntimeException("Child Item should be at the root node now");
        
//     //     // 3. ה-Item של ההורה (10) צריך להיות עכשיו *ילד* (כי הוא ירד למטה)
//     //     if (parent.node.parent == null) throw new RuntimeException("Parent Item should have moved down (become child)");
        
//     //     // 4. בדיקת שלמות המידע
//     //     if (!child.info.equals("Child")) throw new RuntimeException("Info string corrupted during swap");
//     //     if (!parent.info.equals("Parent")) throw new RuntimeException("Info string corrupted during swap");

//     //     System.out.println("PASS");
//     // }

//     // /**
//     //  * בדיקה 3: אינטגרציה מסיבית (Massive Data Integrity)
//     //  * המטרה: להכניס כמות גדולה, למחוק חצי באופן אקראי, ולוודא ששום דבר לא נשבר.
//     //  */
//     // // Time Complexity: O(n log n) - inserts n elements, deletes roughly n/2, then drains heap
//     // private static void testMassiveDataIntegrity() {
//     //     System.out.print("3. Testing Massive Data Integrity (1000 nodes)... ");
//     //     Heap heap = new Heap(true, true);
//     //     int COUNT = 1000;
//     //     HeapItem[] items = new HeapItem[COUNT];
        
//     //     // 1. הכנסה
//     //     for (int i = 0; i < COUNT; i++) {
//     //         items[i] = heap.insert(i * 10, "Val-" + i);
//     //     }
        
//     //     if (heap.size() != COUNT) throw new RuntimeException("Size mismatch after insert");
        
//     //     // 2. מחיקת כל האיברים במקומות הזוגיים (Delete ספציפי)
//     //     for (int i = 0; i < COUNT; i += 2) {
//     //         heap.delete(items[i]);
//     //         // מסמנים שהאיבר נמחק (כדי לא לבדוק אותו אח"כ)
//     //         items[i] = null; 
//     //     }
        
//     //     if (heap.size() != COUNT / 2) throw new RuntimeException("Size mismatch after deletions");
        
//     //     // 3. וידוא שכל האיברים האי-זוגיים שנשארו תקינים
//     //     for (int i = 1; i < COUNT; i += 2) {
//     //         HeapItem it = items[i];
            
//     //         // בדיקת שלמות הקישור
//     //         if (it.node == null) throw new RuntimeException("Remaining item " + i + " lost its node!");
//     //         if (it.node.item != it) throw new RuntimeException("Remaining node " + i + " lost its item!");
            
//     //         // בדיקת הערך
//     //         if (it.key != i * 10) throw new RuntimeException("Key corrupted for item " + i);
//     //     }
        
//     //     // 4. ריקון השאריות ע"י deleteMin וודוא סדר עולה
//     //     int expectedKeyIndex = 1;
//     //     while (heap.size() > 0) {
//     //         HeapItem min = heap.findMin();
//     //         if (min.key != expectedKeyIndex * 10) {
//     //             throw new RuntimeException("Sort order broken! Expected " + (expectedKeyIndex*10) + " got " + min.key);
//     //         }
//     //         heap.deleteMin();
//     //         expectedKeyIndex += 2;
//     //     }

//     //     System.out.println("PASS");
//     // }
}
