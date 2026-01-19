public class HeapTest {

    public static void main(String[] args) {
        System.out.println("=== STARTING HEAP TESTS ===");
        boolean allPassed = true;

        allPassed &= testBasicInsertDelete();
        allPassed &= testConsolidationLogic();
        allPassed &= testLazyMeld();
        allPassed &= testEagerMeld();
        allPassed &= testDecreaseKey();
        allPassed &= testCascadingCuts();
        allPassed &= testDelete();
        allPassed &= testEmptyHeapEdgeCases();

        if (allPassed) {
            System.out.println("\n>>> [SUCCESS] ALL TESTS PASSED! Your Heap is solid. <<<");
        } else {
            System.out.println("\n>>> [FAILURE] SOME TESTS FAILED. Check logs above. <<<");
        }
    }

    // 1. Basic Sanity Check
    private static boolean testBasicInsertDelete() {
        System.out.print("Test 1: Basic Insert/Delete... ");
        Heap heap = new Heap(true, true);
        heap.insert(10, "A");
        heap.insert(5, "B");
        heap.insert(20, "C");

        if (heap.size() != 3) return fail("Size should be 3, got " + heap.size());
        if (heap.findMin().key != 5) return fail("Min should be 5, got " + heap.findMin().key);

        heap.deleteMin(); // Remove 5
        if (heap.size() != 2) return fail("Size after delete should be 2, got " + heap.size());
        if (heap.findMin().key != 10) return fail("New min should be 10, got " + heap.findMin().key);

        System.out.println("PASSED");
        return true;
    }

    // 2. Test Successive Linking (The Core Fix)
    private static boolean testConsolidationLogic() {
        System.out.print("Test 2: Successive Linking (Consolidation)... ");
        Heap heap = new Heap(true, true); 
        
        // Insert 8 nodes. In a lazy heap, this creates 8 roots.
        for (int i = 0; i < 8; i++) {
            heap.insert(i + 10, "val");
        }
        
        // Delete min (10). This triggers successive_linking on the remaining 7 nodes.
        // 7 nodes (binary 111) usually consolidate into 3 trees (ranks 0, 1, 2) or similar.
        heap.deleteMin(); 

        if (heap.size() != 7) return fail("Size should be 7");
        
        // We verify that links actually happened. 
        if (heap.numTrees() > 3) return fail("Consolidation failed! Too many roots left: " + heap.numTrees());
        if (heap.totalLinks() == 0) return fail("No links performed during consolidation.");

        System.out.println("PASSED");
        return true;
    }

    // 3. Test Lazy Meld
    private static boolean testLazyMeld() {
        System.out.print("Test 3: Lazy Meld... ");
        Heap h1 = new Heap(true, true);
        h1.insert(10, "A");
        
        Heap h2 = new Heap(true, true);
        h2.insert(20, "B");
        h2.insert(30, "C");

        // Lazy meld should just concat lists. 1 root + 2 roots = 3 roots.
        h1.meld(h2);

        if (h1.size() != 3) return fail("Size mismatch");
        if (h1.numTrees() != 3) return fail("Lazy meld shouldn't link trees. Expected 3 roots, got " + h1.numTrees());
        
        System.out.println("PASSED");
        return true;
    }

    // 4. Test Eager Meld
    private static boolean testEagerMeld() {
        System.out.print("Test 4: Eager Meld... ");
        Heap h1 = new Heap(false, false); // Eager!
        h1.insert(10, "A"); // 1 tree
        
        Heap h2 = new Heap(false, false);
        h2.insert(10, "B"); // 1 tree (same rank 0)

        // Eager meld calls successive_linking immediately.
        // Rank 0 + Rank 0 should link to become 1 tree of Rank 1.
        h1.meld(h2);

        if (h1.numTrees() != 1) return fail("Eager meld did not consolidate. Roots: " + h1.numTrees());
        if (h1.totalLinks() < 1) return fail("Eager meld recorded no links.");

        System.out.println("PASSED");
        return true;
    }

    // 5. Test Decrease Key (Simple)
    private static boolean testDecreaseKey() {
        System.out.print("Test 5: Decrease Key (Simple)... ");
        Heap heap = new Heap(true, true);
        Heap.HeapItem item = heap.insert(20, "A");
        heap.insert(10, "B"); // Min is 10
        
        // Decrease 20 -> 5. Should become new min.
        heap.decreaseKey(item, 15);
        
        if (item.key != 5) return fail("Key not updated");
        if (heap.findMin().key != 5) return fail("Min ptr not updated");
        
        System.out.println("PASSED");
        return true;
    }

    // 6. Test Cascading Cuts
    private static boolean testCascadingCuts() {
        System.out.print("Test 6: Cascading Cuts... ");
        Heap heap = new Heap(true, true);
        
        // We need to create a tree structure where a node has children.
        // Strategy: Insert nodes, delete min to force consolidation.
        Heap.HeapItem i1 = heap.insert(10, "1");
        Heap.HeapItem i2 = heap.insert(20, "2");
        Heap.HeapItem i3 = heap.insert(30, "3");
        
        heap.deleteMin(); // Removes 10. Links 20 and 30. 
        // 20 should be parent of 30 (since 20 < 30).
        
        // To verify cuts, we check totalCuts.
        int initialCuts = heap.totalCuts();
        
        // Find the node that is the child (30)
        Heap.HeapItem child = (heap.findMin().key == 20) ? i3 : i2; 
        
        // Decrease child to be smaller than parent (e.g., 30 -> 15)
        heap.decreaseKey(child, 15); 
        
        if (heap.totalCuts() <= initialCuts) return fail("No cuts occurred.");
        if (heap.findMin().key != 15) return fail("New min incorrect after cut");

        System.out.println("PASSED");
        return true;
    }
    
    // 7. Test Delete
    private static boolean testDelete() {
        System.out.print("Test 7: Delete arbitrary... ");
        Heap heap = new Heap(true, true);
        Heap.HeapItem target = heap.insert(100, "Target");
        heap.insert(10, "Min");
        
        heap.delete(target);
        
        if (heap.size() != 1) return fail("Size incorrect");
        if (heap.findMin().key != 10) return fail("Min incorrect");
        
        System.out.println("PASSED");
        return true;
    }

    // 8. Edge Cases
    private static boolean testEmptyHeapEdgeCases() {
        System.out.print("Test 8: Empty Heap Edge Cases... ");
        Heap heap = new Heap(true, true);
        
        // Should not crash
        heap.deleteMin(); 
        if (heap.findMin() != null) return fail("Min not null on empty");
        
        heap.insert(1, "One");
        heap.deleteMin();
        if (heap.size() != 0) return fail("Not empty after deleting single element");
        if (heap.numTrees() != 0) return fail("Roots not 0 after empty");

        System.out.println("PASSED");
        return true;
    }

    private static boolean fail(String msg) {
        System.out.println("FAILED: " + msg);
        return false;
    }
}