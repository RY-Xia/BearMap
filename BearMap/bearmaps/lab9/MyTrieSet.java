package bearmaps.lab9;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//Hash-table based trie!!!!!!!!!!!!
public class MyTrieSet implements TrieSet61B{
    private Node root;
    public MyTrieSet(){
        root = new Node(false);

    }

    /** Clears all items out of Trie */
    @Override
    public void clear(){
        root = new Node(false);
    }
    private Node find(String key) {
        Node curr = root;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (!curr.hashmap.containsKey(c)) {
                return null;
            }
            curr = curr.hashmap.get(c);
        }
        return curr;
    }

    /** Returns true if the Trie contains KEY, false otherwise */
    @Override
    public boolean contains(String key) {
        if (key == null || key.length() < 1) {
            throw new IllegalArgumentException();
        }
        Node n = find(key);
        return n != null && n.isBlue;
    }

    /** Inserts string KEY into Trie */
    @Override
    public void add(String key) {
        if (key == null || key.length() < 1) {
            return;
        }
        Node curr = root;
        for (int i = 0, n = key.length(); i < n; i++) {
            char c = key.charAt(i);
            if (!curr.hashmap.containsKey(c)) {
                curr.hashmap.put(c, new Node(false));
            }
            curr = curr.hashmap.get(c);
        }
        curr.isBlue = true;
    }

    private void collect(String s, List<String> x, Node n) {
        if (n == null) {
            return;
        }
        if (n.isBlue) {
            x.add(s);
        }
        for (char c : n.hashmap.keySet()) {
            collect(s + c, x, n.hashmap.get(c));
        }
    }

    /** Returns a list of all words that start with PREFIX */
    @Override
    public List<String> keysWithPrefix(String prefix) {
        List<String> keys = new ArrayList<>();
        Node n = find(prefix);
        collect(prefix, keys, n);
        return keys;
    }

    /** Returns the longest prefix of KEY that exists in the Trie
     * Not required for Lab 9. If you don't implement this, throw an
     * UnsupportedOperationException.
     */
    @Override
    public String longestPrefixOf(String key){throw new UnsupportedOperationException();}



    private class Node {
        private boolean isBlue;
        private char c;
        private HashMap<Character, Node> hashmap;

        Node(boolean isBlue) {
            this.isBlue = isBlue;
            hashmap = new HashMap<>();
        }
    }
}
