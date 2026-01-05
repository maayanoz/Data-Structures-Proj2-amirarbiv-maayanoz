/**
 * Heap
 *
 * An implementation of Fibonacci heap over positive integers 
 * with the possibility of not performing lazy melds and 
 * the possibility of not performing lazy decrease keys.
 *
 * 
 * TODO: run tests
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
     * Time Complexity: O(1) amortized
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
     * Time Complexity: O(log n) - traverses root list which has at most log n nodes
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
     * Time Complexity: O(log n) amortized - includes successive linking
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
            this.succsesive_linking(this);
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
     * Time Complexity: O(1) amortized with lazy decrease keys, O(log n) with non-lazy
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
                HeapNode x_p = x.parent;
                cut(x);
                cascadingCuts(x_p);
            }
            else{
               //non-lazy decrease key
            heapifyUp(x); 
            }
        }
        HeapNode new_min = this.findMin();
        this.min = new_min;
    }

    // Time Complexity: O(1) amortized - constant work per cut with amortized analysis
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

    // Time Complexity: O(log n) - swaps with parent until heap property is satisfied
    private void heapifyUp(HeapNode x) { //helping method, tested
        // heapify up until x is smaller than two its children
        while (x.parent != null && x.key < x.parent.key) {
            swapWithParent(x);
            x = x.parent;
            totalHeapifyCosts++;
        }
    }

    // Time Complexity: O(1) amortized - removes node and performs lazy meld
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
                to_meld.min = x;
                to_meld.roots = 1;
                this.meld(to_meld);
            }
    

    /**
     * 
     * @param x
     * 
     * pre: x is a node in the heap
     * swap x with his parent
     * 
     * Time Complexity: O(1)
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
     * Time Complexity: O(log n) amortized
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
     * Time Complexity: O(1) with lazy melds, O(log n) with eager melds
     *
     */
    public void meld(Heap heap2){ //finished, not tested
        this.size += heap2.size; //true for both lazy and non-lazy melds
        this.numMarked += heap2.numMarked;
        this.totalLinks += heap2.totalLinks;
        this.totalCuts += heap2.totalCuts;
        this.totalHeapifyCosts += heap2.totalHeapifyCosts;
        this.roots = this.roots + heap2.roots;

        if (lazyMelds){
            lazyMeld(heap2);
        }
        else{ //eager meld
            lazyMeld(heap2);//first link the two roots lists
            this.succsesive_linking(this); //then perform succesive linking
        }
    }


    // Time Complexity: O(1) - simply concatenates root lists
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
    

    // Time Complexity: O(log n) amortized - consolidates trees by rank
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
            node.prev = node;
            node.next = node;
            add_to_rank_array(node, rankArray);
        }
        //rebuild the roots list from rankArray
        int index = 0; //index of first non-null in rankArray
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
        for(int i = index+1; i < rankArray.length; i++) { //add roots from rankArray to the roots list
            if (rankArray[i] == null) {
                if (i == rankArray.length - 1) { //last node
                    curr.next = heap.min;
                    heap.min.prev = curr;
                    break;
                }
                continue;
            }
            roots++;
            if (rankArray[i] == heap.min){ //not supposed to happen but just in case something goes wrong
                heap.min.prev = curr;
                curr.next = heap.min;
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
                rankArray[i].next = heap.min;
                heap.min.prev = rankArray[i];
            }
        }
        heap.min = heap.findMin();

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


    // Time Complexity: O(log n) amortized - recursive linking of trees
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
    
    // Time Complexity: O(1) - links two trees by making smaller key the parent
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
    
    // Time Complexity: O(1) - adds child to parent's child list
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
    // maybe unnecessary
    
        // private void child_seper(HeapNode parent, HeapNode child){ //helping method for add_child
        //     if (child.next != child) { //if child is not alone in its list
        //             child.prev.next = parent;
        //             child.next.prev = parent;
        //             parent.next = child.next;
        //             parent.prev = child.prev;
        //         }
        //         else{ //if child is alone in its list
        //             parent.next = parent;
        //             parent.prev = parent;
        //         }
        // }

    /**
     * 
     * Return the number of elements in the heap
     * 
     * Time Complexity: O(1)
     *   
     */
    public int size(){
        return size; // should be replaced by student code
    }


    /**
     * 
     * Return the number of trees in the heap.
     * 
     * Time Complexity: O(1)
     * 
     */
    public int numTrees()
    {
        return roots;
    }
    
    
    /**
     * 
     * Return the number of marked nodes in the heap.
     * 
     * Time Complexity: O(1)
     * 
     */
    public int numMarkedNodes(){
        return numMarked;
    }
    
    
    /**
     * 
     * Return the total number of links.
     * 
     * Time Complexity: O(1)
     * 
     */
    public int totalLinks(){
        return totalLinks;
    }
    
    
    /**
     * 
     * Return the total number of cuts.
     * 
     * Time Complexity: O(1)
     * 
     */
    public int totalCuts(){
        return totalCuts;
    }
    

    /**
     * 
     * Return the total heapify costs.
     * 
     * Time Complexity: O(1)
     * 
     */
    public int totalHeapifyCosts(){
        return totalHeapifyCosts; // should be replaced by student code
    }
    
    
    /**
     * Class implementing a node in a ExtendedFibonacci Heap.
     *  
     */
    public class HeapNode{
        public int key;
        public String info;
        public HeapNode child;
        public HeapNode next = this;
        public HeapNode prev = this;
        public HeapNode parent;
        public int rank;
        public boolean marked = false;
    }

    // // ============================================================
    // //               EDGE CASE TESTING SUITE
    // // ============================================================

    // public static void main(String[] args) {
    //     System.out.println("--- Starting EDGE CASE Tests ---");
    //     try {
    //         testEmptyHeapOperations();
    //         testSingleNodeLifecycle();
    //         testDuplicateKeys();
    //         testExtremeValues();
    //         testMeldWithEmpty();
    //         testCascadingCutsDeep(); 
    //         testDeleteArbitraryNodes();
    //         testInterleavedOperations();
            
    //         System.out.println("\n>>> CONGRATULATIONS! ALL EDGE CASE TESTS PASSED! <<<");
    //     } catch (Exception e) {
    //         System.out.println("\n!!! TEST FAILED WITH EXCEPTION !!!");
    //         e.printStackTrace();
    //     }
    // }

    // /**
    //  * מקרה קצה 1: פעולות על ערימה ריקה
    //  * האם הקוד קורס כשמנסים למחוק מכלום?
    //  */
    // private static void testEmptyHeapOperations() {
    //     System.out.print("Edge Case 1: Empty Heap Operations... ");
    //     Heap heap = new Heap(true, true);
        
    //     if (heap.findMin() != null) fail("Min should be null for empty heap");
    //     if (heap.size() != 0) fail("Size should be 0");
    //     if (heap.numTrees() != 0) fail("Roots should be 0");
        
    //     // לא אמור לזרוק שגיאה
    //     heap.deleteMin(); 
        
    //     if (heap.size() != 0) fail("Size should remain 0 after deleteMin on empty");

    //     System.out.println("PASS");
    // }

    // /**
    //  * מקרה קצה 2: מחזור חיים של איבר בודד
    //  * הכנסה -> בדיקה -> מחיקה -> בדיקה שחזרנו ל-0 נקי
    //  */
    // private static void testSingleNodeLifecycle() {
    //     System.out.print("Edge Case 2: Single Node Lifecycle... ");
    //     Heap heap = new Heap(true, true);
        
    //     HeapNode node = heap.insert(100, "Solo");
        
    //     if (heap.findMin() != node) fail("Min should be the single node");
    //     if (node.next != node || node.prev != node) fail("Single node should point to itself");
        
    //     heap.deleteMin();
        
    //     if (heap.size() != 0) fail("Size should be 0 after deleting single node");
    //     if (heap.min != null) fail("Min should be null");
    //     if (heap.numTrees() != 0) fail("Roots should be 0");

    //     System.out.println("PASS");
    // }

    // /**
    //  * מקרה קצה 3: כפילויות (Duplicates)
    //  * ערימה עם מפתחות זהים. האם הסדר נשמר? האם כולם נמחקים?
    //  */
    // private static void testDuplicateKeys() {
    //     System.out.print("Edge Case 3: Duplicate Keys... ");
    //     Heap heap = new Heap(true, true);
        
    //     // הכנסת 5 איברים זהים
    //     for (int i = 0; i < 5; i++) {
    //         heap.insert(7, "Dup" + i);
    //     }
        
    //     if (heap.size() != 5) fail("Size should be 5");
    //     if (heap.findMin().key != 7) fail("Min key should be 7");
        
    //     // מחיקת כולם אחד אחד
    //     for (int i = 0; i < 5; i++) {
    //         heap.deleteMin();
    //     }
        
    //     if (heap.size() != 0) fail("Heap should be empty after deleting all duplicates");
    //     if (heap.min != null) fail("Min should be null");

    //     System.out.println("PASS");
    // }

    // /**
    //  * מקרה קצה 4: ערכי קיצון (Integer.MAX_VALUE / MIN_VALUE)
    //  * האם חישובים מתמטיים (כמו חיסור) גורמים ל-Overflow?
    //  */
    // private static void testExtremeValues() {
    //     System.out.print("Edge Case 4: Extreme Values (Int boundaries)... ");
    //     Heap heap = new Heap(true, true);
        
    //     heap.insert(Integer.MAX_VALUE, "Max");
    //     heap.insert(Integer.MIN_VALUE, "Min"); // אם המימוש תומך בשליליים
    //     heap.insert(0, "Zero");
        
    //     if (heap.findMin().key != Integer.MIN_VALUE) fail("Min should be Integer.MIN_VALUE");
        
    //     heap.deleteMin(); // Remove MIN_VALUE
    //     if (heap.findMin().key != 0) fail("Next min should be 0");
        
    //     heap.deleteMin(); // Remove 0
    //     if (heap.findMin().key != Integer.MAX_VALUE) fail("Last min should be MAX_VALUE");

    //     System.out.println("PASS");
    // }

    // /**
    //  * מקרה קצה 5: Meld עם ערימות ריקות
    //  * ריק+מלא, מלא+ריק, ריק+ריק
    //  */
    // private static void testMeldWithEmpty() {
    //     System.out.print("Edge Case 5: Meld with Empty Heaps... ");
    //     Heap h1 = new Heap(true, true);
    //     Heap hEmpty = new Heap(true, true);
        
    //     // 1. Meld empty into empty
    //     h1.meld(hEmpty);
    //     if (h1.size() != 0) fail("Empty+Empty size should be 0");
        
    //     // 2. Meld empty into full
    //     h1.insert(10, "A");
    //     h1.meld(hEmpty);
    //     if (h1.size() != 1) fail("Full+Empty size should be 1");
        
    //     // 3. Meld full into empty (should update min)
    //     Heap h2 = new Heap(true, true);
    //     h2.meld(h1); // h2 is empty, h1 has 1 node
    //     if (h2.size() != 1) fail("Empty+Full size should be 1");
    //     if (h2.findMin().key != 10) fail("Min should be updated after meld");

    //     System.out.println("PASS");
    // }

    // /**
    //  * מקרה קצה 6: Cascading Cuts אמיתי (עומק העץ)
    //  * יוצרים עץ עמוק, ומתחילים לקצוץ מלמטה למעלה כדי להפעיל את מנגנון הסימון.
    //  */
    // private static void testCascadingCutsDeep() {
    //     System.out.print("Edge Case 6: Deep Cascading Cuts logic... ");
    //     Heap heap = new Heap(true, true); // Lazy
        
    //     // בניית עץ עם 8 איברים
    //     HeapNode[] nodes = new HeapNode[8];
    //     for (int i = 0; i < 8; i++) {
    //         nodes[i] = heap.insert(i, "#" + i);
    //     }
        
    //     // מחיקת המינימום גורמת לאיחוד העצים לעץ בינומי (או כמה עצים)
    //     heap.deleteMin(); 
        
    //     // כעת נחפש צומת שהוא *לא* שורש (כלומר, יש לו הורה).
    //     // בערימה מאוחדת בגודל 7, חייבים להיות צמתים כאלה.
    //     HeapNode nodeToCut = null;
    //     for (int i = 1; i < 8; i++) { // מתחילים מ-1 כי 0 נמחק
    //         if (nodes[i].parent != null) {
    //             nodeToCut = nodes[i];
    //             break; 
    //         }
    //     }
        
    //     if (nodeToCut == null) {
    //         // זה מצב לא הגיוני מתמטית עבור ערימה מאוחדת בגודל 7, אבל ליתר ביטחון:
    //         System.out.println("[Skipped: Could not find child node] ");
    //         return;
    //     }

    //     int initialCuts = heap.totalCuts();
        
    //     // נקטין את המפתח שלו בצורה אגרסיבית כדי שיהיה קטן מהאבא שלו בוודאות
    //     // (הופכים אותו לשלילי, האבא בטוח חיובי)
    //     heap.decreaseKey(nodeToCut, nodeToCut.key + 500); 
        
    //     if (heap.totalCuts() <= initialCuts) {
    //         fail("Should have performed a cut (Child Key: " + nodeToCut.key + ", Parent Key: " + (nodeToCut.parent != null ? nodeToCut.parent.key : "null") + ")");
    //     }

    //     System.out.println("PASS");
    // }

    // /**
    //  * מקרה קצה 7: מחיקה של איבר שהוא לא המינימום (Arbitrary Delete)
    //  * כולל מחיקה של שורש שאינו מינימום, ומחיקה של עלה.
    //  */
    // private static void testDeleteArbitraryNodes() {
    //     System.out.print("Edge Case 7: Arbitrary Delete... ");
    //     Heap heap = new Heap(true, true);
        
    //     HeapNode n10 = heap.insert(10, "A");
    //     HeapNode n20 = heap.insert(20, "B");
    //     HeapNode n30 = heap.insert(30, "C");
        
    //     // 1. מחיקת איבר אמצעי (20)
    //     heap.delete(n20);
    //     if (heap.size() != 2) fail("Size should be 2");
    //     if (heap.findMin().key != 10) fail("Min should still be 10");
        
    //     // 2. מחיקת המינימום דרך delete (לא deleteMin)
    //     heap.delete(n10);
    //     if (heap.findMin().key != 30) fail("Min should be 30");
        
    //     // 3. מחיקת האיבר האחרון
    //     heap.delete(n30);
    //     if (heap.min != null) fail("Heap should be empty");

    //     System.out.println("PASS");
    // }
    
    // /**
    //  * מקרה קצה 8: פעולות מעורבבות (Interleaved)
    //  * לוודא שהמצב הפנימי נשאר יציב
    //  */
    // private static void testInterleavedOperations() {
    //     System.out.print("Edge Case 8: Interleaved Ops (Stress)... ");
    //     Heap heap = new Heap(true, true);
        
    //     heap.insert(50, "50");
    //     HeapNode n20 = heap.insert(20, "20");
    //     heap.deleteMin(); // del 20
        
    //     heap.insert(30, "30");
    //     heap.insert(40, "40");
    //     heap.decreaseKey(n20, 100); // זהירות! n20 כבר נמחק! המערכת לא אמורה לקרוס (תלוי במימוש)
    //     // בדרך כלל אסור לגשת לצומת מחוק. אם המימוש שלך לא מגן מזה, נקפוץ על הבדיקה הזו.
    //     // נבדוק משהו חוקי:
        
    //     HeapNode n30 = heap.findMin(); // 30
    //     heap.decreaseKey(n30, 5); // 25
    //     if (heap.findMin().key != 25) fail("Min should be 25");
        
    //     heap.delete(n30); // delete the new min
    //     if (heap.findMin().key != 40) fail("Min should be 40"); // 50 is left

    //     System.out.println("PASS");
    // }

    // // פונקציית עזר לזריקת שגיאות
    // private static void fail(String message) {
    //     throw new RuntimeException(message);
    // }

    // // ============================================================
    // //               TESTING SECTION (Main & Helpers)
    // // ============================================================

    // public static void main(String[] args) {
    //     System.out.println("--- Starting Internal Tests for Heap ---");
        
    //     testBasicOperations();
    //     testDeleteMinAndLinks();
    //     testDecreaseKeyAndCuts();
    //     testDeleteArbitrary();
    //     testMeldHeaps();
        
    //     System.out.println("\n--- All Tests Finished ---");
    // }

    // private static void testBasicOperations() {
    //     System.out.print("Test 1: Basic Insert, Min, Size... ");
    //     // Initialize with both lazy flags true (standard Fibonacci Heap behavior)
    //     Heap heap = new Heap(true, true);
        
    //     if (heap.min != null) {
    //         printFail("Heap should be empty initially"); return;
    //     }

    //     heap.insert(10, "ten");
    //     HeapNode five = heap.insert(5, "five");
    //     heap.insert(20, "twenty");

    //     if (heap.size() != 3) {
    //         printFail("Size should be 3, got " + heap.size()); return;
    //     }
        
    //     if (heap.findMin().key != 5) {
    //         printFail("Min should be 5"); return;
    //     }
        
    //     if (heap.numTrees() != 3) {
    //         // In lazy insert, every insert adds a new tree until consolidate
    //         printFail("Should have 3 trees (roots) before any consolidation"); return;
    //     }

    //     System.out.println("PASS");
    // }

    // private static void testDeleteMinAndLinks() {
    //     System.out.print("Test 2: DeleteMin & Successive Linking... ");
    //     Heap heap = new Heap(true, true);
    //     // Inserting 9 elements (0 to 8)
    //     for (int i = 8; i >= 0; i--) {
    //         heap.insert(i, "val" + i);
    //     }
    //     // Current state: 9 roots, min is 0.
    //     int initialLinks = heap.totalLinks();
    //     heap.deleteMin(); // Deletes 0. Should trigger successive linking.
    //     if (heap.size() != 8) {
    //         printFail("Size should be 8 after deleteMin"); return;
    //     }
    //     if (heap.findMin().key != 1) {
    //         printFail("New min should be 1"); return;
    //     }
    //     // Check if linking happened
    //     if (heap.totalLinks() <= initialLinks) {
    //         printFail("Total links should increase after deleteMin (consolidation)"); return;
    //     }
    //     // For 8 nodes, typically we expect 1 tree (binomial tree B3) if fully consolidated
    //     // Depending on implementation details, it might vary, but roots should decrease significantly
    //     if (heap.numTrees() >= 8) {
    //         printFail("Number of trees should decrease after consolidation"); return;
    //     }

    //     System.out.println("PASS");
    // }

    // private static void testDecreaseKeyAndCuts() {
    //     System.out.print("Test 3: DecreaseKey & Cuts... ");
    //     Heap heap = new Heap(true, true); // Lazy
        
    //     HeapNode n100 = heap.insert(100, "100");
    //     HeapNode n50 = heap.insert(50, "50");
    //     HeapNode n10 = heap.insert(10, "10"); // Min
        
    //     // Force a structure where n100 is child of n50 (requires deleteMin)
    //     heap.deleteMin(); // deletes 10. n100 and n50 should consolidate.
        
    //     // We need to find who is the parent. 
    //     // Logic: 50 < 100, so 50 should be root, 100 should be child.
    //     if (n100.parent != n50) {
    //          // In case the linking order is different or they didn't merge yet (depends on size)
    //          // Let's force a scenario we can control better or just trust the decreaseKey logic
    //          // If n100 is not a child, we can't test "Cut".
    //     }
        
    //     // Let's create a simpler scenario for decrease key cuts
    //     // We will insert elements, delete min to build a tree, then decrease a child's key
        
    //     heap = new Heap(true, true);
    //     HeapNode a = heap.insert(20, "a");
    //     HeapNode b = heap.insert(10, "b"); // min
    //     heap.insert(30, "c");
        
    //     heap.deleteMin(); // deletes 10. 20 and 30 should merge. 20 becomes parent of 30.
        
    //     // Assume 30 is child of 20 (since 20 < 30)
    //     HeapNode childNode = null;
    //     if (a.child != null) childNode = a.child; // The child of 20
    //     else if (heap.findMin() == a && a.next != a) {
    //          // Maybe they didn't merge? (e.g. if rank array logic skipped)
    //          // Retry with guaranteed merge size
    //     }
        
    //     // We'll proceed with a standard decreaseKey test that simply checks values
    //     // and ensures no crash, verifying cuts count if possible.
    //     int cutsBefore = heap.totalCuts();
        
    //     // Decrease 20 to 5 (making it new min)
    //     heap.decreaseKey(a, 15); // 20 - 15 = 5
        
    //     if (heap.findMin().key != 5) {
    //         printFail("Min should be 5 after decreaseKey"); return;
    //     }
    //     if (a.key != 5) {
    //         printFail("Node key should be 5"); return;
    //     }
        
    //     // Note: This didn't necessarily trigger a cut because 20 was a root. 
    //     // Testing actual cuts requires ensuring parent-child relationship first.
        
    //     System.out.println("PASS");
    // }

    // private static void testDeleteArbitrary() {
    //     System.out.print("Test 4: Delete arbitrary node... ");
    //     Heap heap = new Heap(true, true);
    //     HeapNode n10 = heap.insert(10, "10");
    //     HeapNode n20 = heap.insert(20, "20");
    //     HeapNode n30 = heap.insert(30, "30");
        
    //     // Delete 20 (middle element)
    //     heap.delete(n20);
        
    //     if (heap.size() != 2) {
    //         printFail("Size should be 2 after delete"); return;
    //     }
    //     // Verify min is still 10
    //     if (heap.findMin().key != 10) {
    //         printFail("Min should be 10"); return;
    //     }
        
    //     // Delete 10 (min)
    //     heap.delete(n10);
    //     if (heap.findMin().key != 30) {
    //         printFail("Min should be 30 after deleting 10"); return;
    //     }
        
    //     System.out.println("PASS");
    // }

    // private static void testMeldHeaps() {
    //     System.out.print("Test 5: Meld... ");
    //     Heap h1 = new Heap(true, true);
    //     h1.insert(10, "h1-1");
    //     h1.insert(20, "h1-2");
        
    //     Heap h2 = new Heap(true, true);
    //     h2.insert(5, "h2-1"); // Smaller than h1 min
    //     h2.insert(30, "h2-2");
        
    //     int h1Size = h1.size();
    //     int h2Size = h2.size();
        
    //     h1.meld(h2);
        
    //     if (h1.size() != h1Size + h2Size) {
    //         printFail("Size mismatch after meld. Expected " + (h1Size + h2Size) + ", got " + h1.size()); return;
    //     }
        
    //     if (h1.findMin().key != 5) {
    //         printFail("Min should be 5 after meld"); return;
    //     }
        
    //     // Check structural change (roots count should sum up in lazy meld)
    //     // h1 had 2 roots, h2 had 2 roots -> Total 4 roots
    //     if (h1.numTrees() != 4) {
    //         printFail("In lazy meld, roots should simply be concatenated (expected 4)"); return;
    //     }
        
    //     System.out.println("PASS");
    // }

    // private static void printFail(String msg) {
    //     System.out.println("FAIL: " + msg);
    // }

// ============================================================
//     //     BASIC TESTS: INSERT & FINDMIN + DECREASEKEY & CUT 
// ============================================================

//     public static void main(String[] args) { //test insert and findMin
//         //test cut
//         //build a small heap manually
//         Heap heap = new Heap(true, true);
//         HeapNode n1 = new HeapNode();
//         n1.key = 10;
//         n1.info = "ten";
//         HeapNode n2 = new HeapNode();
//         n2.key = 20;   
//         n2.info = "twenty";
//         HeapNode n3 = new HeapNode();
//         n3.key = 30;
//         n3.info = "thirty";
//         HeapNode n4 = new HeapNode();
//         n4.key = 40;
//         n4.info = "forty";
//         //link nodes to form a heap
//         heap.min = n1;
//         heap.min.child = n2;
//         n1.rank = 1;
//         //link n2 and n3 as children of n1
//         n2.parent = n1;
//         n3.parent = n1;
//         n2.next = n3;
//         n3.prev = n2;
//         n3.next = n2;
//         n2.prev = n3;
//         //link n4 as child of n2
//         n2.child = n4;
//         n2.rank = 1;
//         n4.parent = n2;
//         n4.next = n4;
//         n4.prev = n4;
//         //cut n2
//         heap.decreaseKey(n2, 15); //decrease key from 20 to 5, should cut n2
//         //print the root list
//         System.out.println("Root list after cutting n2:");
//         HeapNode start = heap.min;
//         while (true) {
//             System.out.println("Key: " + start.key + ", info: " + start.info);
//             start = start.next;
//             if (start == heap.min) {
//                 break;
//             }
//         }
//         //print n2's children
//         System.out.println("Children of n2 after cutting:");
//         System.out.println("Key: " + n2.child.key + ", info: " + n2.child.info);
//         System.out.println("Key: " + n2.child.next.key + ", info: " + n2.child.next.info);
//         //print n1's children
//         System.out.println("Children of n1 after cutting n2:");
//         System.out.println("Key: " + n1.child.key + ", info: " + n1.child.info);
//         System.out.println("Key: " + n1.child.next.key + ", info: " + n1.child.next.info);




//         // Heap heap = new Heap(true, false);
//         // heap.insert(5, "five");
//         // heap.insert(3, "three");
//         // heap.insert(7, "seven");
//         // HeapNode minNode = heap.findMin();
//         // System.out.println("Min key: " + minNode.key + ", info: " + minNode.info); // Expected: Min key: 3, info: three
//         // HeapNode start = heap.min;
//         // System.out.println("Root list:");
//         // while (true) {
//         //     System.out.println("Key: " + start.key + ", info: " + start.info);
//         //     start = start.next;
//         //     if (start == heap.min) {
//         //         break;
//         //     }
//         // }
//         // heap.decreaseKey(heap.min.next, 4); // Decrease key from 5 to 1
//         // HeapNode minNode2 = heap.findMin();
//         // System.out.println("Min key: " + minNode2.key + ", info: " + minNode2.info); // Expected: Min key: 1, info: five
//         // HeapNode start2 = heap.min;
//         // System.out.println("Root list:");
//         // while (true) {
//         //     System.out.println("Key: " + start2.key + ", info: " + start2.info);
//         //     start2 = start2.next;
//         //     if (start2 == heap.min) {
//         //         break;
//         //     }
//         // }
//         //test heapifyUp
//         //build a small heap manually
//     }
// //     ============================================================
// //            ADVANCED TESTS: FIELDS & CONFIGURATIONS
// //     ============================================================

//     public static void main(String[] args) {
//         System.out.println("--- Starting Internal Tests for Heap ---");
        
//         testAdvancedScenarios();
//     }
//     // ============================================================
//     //        ADVANCED TESTS: FIELDS & CONFIGURATIONS
//     // ============================================================

//     public static void testAdvancedScenarios() {
//         System.out.println("--- Starting Advanced Field & Config Tests ---");
        
//         testFieldMaintenance();
//         testConfig_LazyMeld_LazyDecKey();       // (true, true)
//         testConfig_EagerMeld_LazyDecKey();      // (false, true)
//         testConfig_LazyMeld_EagerDecKey();      // (true, false)
//         testConfig_EagerMeld_EagerDecKey();     // (false, false)
        
//         System.out.println("\n--- All Advanced Tests Finished Successfully ---");
//     }

//     private static void testFieldMaintenance() {
//         System.out.print("Test 1: Field Maintenance (Size, Roots, Links)... ");
//         Heap heap = new Heap(true, true);

//         heap.insert(10, "A");
//         heap.insert(20, "B");
//         heap.insert(30, "C");
        
//         if (heap.size() != 3) { printFail("Size should be 3"); return; }
//         if (heap.numTrees() != 3) { printFail("Roots should be 3 (Lazy Insert)"); return; }

//         int linksBefore = heap.totalLinks();
//         heap.deleteMin(); // מוחק 10
        
//         if (heap.size() != 2) { printFail("Size should be 2 after delete"); return; }
//         if (heap.numTrees() != 1) { printFail("Roots should be 1 after consolidate"); return; }
//         if (heap.totalLinks() <= linksBefore) { printFail("TotalLinks should increase after consolidate"); return; }

//         System.out.println("PASS");
//     }

//     private static void testConfig_LazyMeld_LazyDecKey() {
//         System.out.print("Test 2: Config [LazyMelds=T, LazyDecKey=T]... ");
        
//         // Check Lazy Meld
//         Heap h1 = new Heap(true, true);
//         h1.insert(10, "A");
//         Heap h2 = new Heap(true, true);
//         h2.insert(20, "B");
        
//         int linksBefore = h1.totalLinks();
//         h1.meld(h2);
        
//         if (h1.numTrees() != 2) { printFail("Lazy Meld should just add roots"); return; }
//         if (h1.totalLinks() != linksBefore) { printFail("Lazy Meld should NOT perform links"); return; }

//         // Check Lazy DecreaseKey (Cuts)
//         h1 = new Heap(true, true);
//         h1.insert(10, "min");
//         HeapNode n20 = h1.insert(20, "n20");
//         HeapNode n30 = h1.insert(30, "n30");
//         h1.deleteMin(); // 10 deleted, 20 and 30 merge
        
//         HeapNode childNode = (n20.parent != null) ? n20 : n30;
//         int cutsBefore = h1.totalCuts();
//         int heapifyBefore = h1.totalHeapifyCosts();
        
//         h1.decreaseKey(childNode, 100); 
        
//         if (h1.totalCuts() <= cutsBefore) { printFail("Lazy DecreaseKey should perform Cut"); return; }
//         if (h1.totalHeapifyCosts() != heapifyBefore) { printFail("Lazy DecreaseKey should NOT perform HeapifyUp"); return; }

//         System.out.println("PASS");
//     }

//     private static void testConfig_EagerMeld_LazyDecKey() {
//         System.out.print("Test 3: Config [LazyMelds=F, LazyDecKey=T]... ");
        
//         Heap hA = new Heap(false, true);
//         hA.insert(10, "A"); 
        
//         Heap hB = new Heap(false, true);
//         hB.insert(20, "B"); 
        
//         int linksBefore = hA.totalLinks();
//         hA.meld(hB); 
//         if (hA.size() != 2) { printFail("Size check failed"); return; }
//         if (hA.numTrees() > 1) { printFail("Eager Meld should consolidate trees"); return; }
//         if (hA.totalLinks() <= linksBefore) { printFail("Eager Meld should perform links"); return; }
        
//         System.out.println("PASS");
//     }

//     private static void testConfig_LazyMeld_EagerDecKey() {
//         System.out.print("Test 4: Config [LazyMelds=T, LazyDecKey=F]... ");
        
//         Heap h1 = new Heap(true, false);
//         h1.insert(10, "min");
//         HeapNode n20 = h1.insert(20, "parent");
//         HeapNode n30 = h1.insert(30, "child");
//         h1.deleteMin(); 
        
//         HeapNode child = (n20.parent != null) ? n20 : n30;
//         int cutsBefore = h1.totalCuts();
//         int heapifyBefore = h1.totalHeapifyCosts();
        
//         h1.decreaseKey(child, 100); 
        
//         if (h1.totalCuts() != cutsBefore) { printFail("Eager DecreaseKey should NOT perform Cuts"); return; }
//         if (h1.totalHeapifyCosts() <= heapifyBefore) { printFail("Eager DecreaseKey should perform HeapifyUp"); return; }

//         System.out.println("PASS");
//     }

//     private static void testConfig_EagerMeld_EagerDecKey() {
//         System.out.print("Test 5: Config [LazyMelds=F, LazyDecKey=F]... ");
        
//         Heap h1 = new Heap(false, false);
//         h1.insert(100, "A");
//         h1.insert(200, "B"); 
        
//         if (h1.numTrees() != 1) { printFail("Eager Insert/Meld should result in 1 tree"); return; }
        
//         HeapNode child = h1.min.child; 
//         if (child == null && h1.min.child != null) child = h1.min.child;
        
//         if (child == null) {
//              // אם המבנה הפוך ממה שחשבנו (תלוי ב-Link), ננסה למצוא אותו ידנית
//              // לצורך הטסט נניח שהם חוברו
//         } else {
//             int heapifyBefore = h1.totalHeapifyCosts();
//             h1.decreaseKey(child, 2000); 
//             if (h1.totalHeapifyCosts() <= heapifyBefore) { printFail("Should perform HeapifyUp"); return; }
//         }
        
//         System.out.println("PASS");
//     }

//     // זו הפונקציה שהייתה חסרה:
//     private static void printFail(String msg) {
//         System.out.println("FAIL: " + msg);
//     }
    }
