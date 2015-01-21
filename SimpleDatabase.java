import java.util.*;

public class SimpleDatabase {
    HashMap<String, Integer> dbHash;
    HashMap<Integer, Integer> countHash;
    Stack<Stack<Transaction>> log;
    Stack<Transaction> curTransactions;

    boolean dbRunning;

    private class Transaction {
        String key;
        Integer oldVal;
        Integer newVal;
        
        Transaction(String k, Integer o, Integer n) {
            this.key = k;
            this.oldVal = o;
            this.newVal = n;
        }
    }

    public SimpleDatabase() {
        dbHash = new HashMap<String, Integer>();
        countHash = new HashMap<Integer, Integer>();
        log = new Stack<Stack<Transaction>>();
    }

    public static void main(String[] args) {
        SimpleDatabase db = new SimpleDatabase();
        db.run();
    }

    private void error(String msg) {
        System.out.println(msg);
    }

    /* Start and run the database until terminated by user */
    public void run() {
        Scanner console = new Scanner(System.in);
        Scanner tokenizer;

        dbRunning = true;
        System.out.println("Welcome to SimpleDatabase");

        while (dbRunning && console.hasNextLine()) {
            String command;
            String argKey = null;
            Integer argVal = null;
            tokenizer = new Scanner(console.nextLine());

            if (tokenizer.hasNext()) {
                command = tokenizer.next();
                
                if (tokenizer.hasNext() && !tokenizer.hasNextInt())
                    argKey = tokenizer.next();

                if (tokenizer.hasNextInt())
                    argVal = tokenizer.nextInt();

            } else {
                System.out.println("No input");
                continue;
            }

            switch (command) {
                case "SET": {
                    if (argKey == null || argVal == null) {
                        error("invalid arguments for SET");
                        continue;
                    }
                    
                    set(argKey, argVal);
                    break;
                }
                case "UNSET": {
                    if (argKey == null || argVal != null) {
                        error("invalid arguments for UNSET");
                        continue;
                    }

                    unset(argKey);
                    break;
                }
                case "GET": {
                    if (argKey == null || argVal != null) {
                        error("invalid arguments for GET");
                        continue;
                    }

                    get(argKey);
                    break;
                }
                case "NUMEQUALTO": {
                    if (argKey != null || argVal == null) {
                        error("invalid arguments for NUMEQUALTO");
                        continue;
                    }

                    numEqualTo(argVal);
                    break;
                }
                case "BEGIN": {
                    if (argKey != null || argVal != null) {
                        error("extra arguments for BEGIN");
                        continue;
                    }

                    begin();
                    break;
                }
                case "ROLLBACK": {
                    if (argKey != null || argVal != null) {
                        error("extra arguments for ROLLBACK");
                        continue;
                    }

                    rollback();
                    break;
                }
                case "COMMIT": {
                    if (argKey != null || argVal != null) {
                        error("extra arguments for COMMIT");
                        continue;
                    }

                    commit();
                    break;
                }
                case "END": {
                    if (argKey != null || argVal != null) {
                        error("extra arguments for END");
                        continue;
                    }

                    end();
                    break;
                }
                default: {
                    error("invalid command");
                }
            }
        }
        System.out.println("Goodbye!");
    }

    /* 
     * SET command, create or update a DB entry 
     */
    private void set(String key, Integer val) {
        Integer oldVal = null;

        if (dbHash.containsKey(key)) {
            oldVal = dbHash.get(key);
            decCount(oldVal);
        }

        if (val == null) {
            dbHash.remove(key);
        } else {
            dbHash.put(key, val);
            incCount(val);
        }

        if (curTransactions != null &&
            (oldVal == null || val == null || !oldVal.equals(val)))
                curTransactions.push(new Transaction(key, oldVal, val));
    }

    /* 
     * UNSET command, delete a DB entry 
     */
    private void unset(String key) {
        set(key, null);
    }

    /* 
     * GET command, get DB entry value of key 
     */
    private void get(String key) {
        Integer value = dbHash.get(key);

        if (value == null)
            System.out.println("NULL");
        else
            System.out.println(value);
    }

    /*
     * NUMEQUALTO command, get number of entries with value
     */
    private void numEqualTo(int val) {
        Integer count = countHash.get(val);

        if (count == null)
            count = 0;

        System.out.println(count);
    }

    /*
     * BEGIN command, start a new transaction group
     */
    private void begin() {
        curTransactions = new Stack<Transaction>();
        log.push(curTransactions);
    }

    /*
     * ROLLBACK command, undo the previous transaction group
     */
    private void rollback() {
        if (curTransactions == null) {
            System.out.println("NO TRANSACTION");
            return;
        }

        for (Transaction t : curTransactions)
            undo(t);

        log.pop();
        curTransactions = log.empty() ? null : log.peek();
    }

    /*
     * COMMIT command, commit all the out-standing transaction groups
     */
    private void commit() {
        curTransactions = null;
        log = new Stack<Stack<Transaction>>();
    }

    /*
     * END command, terminate the program
     */
    private void end() {
       dbRunning = false;
    }

    /*
     * increment the count of occurrences of a given value
     */
    private void incCount(int val) {
        int oldCount = 0;

        if (countHash.containsKey(val))
            oldCount = countHash.get(val);

        countHash.put(val, ++oldCount);
    }

    /*
     * decrement the count of occurrences of a given value
     */
    private void decCount(int val) {
        int oldCount = 1;

        if (countHash.containsKey(val))
            oldCount = countHash.get(val);

        countHash.put(val, --oldCount);
    }

    /*
     * undo a given transaction record
     */
    private void undo(Transaction tr) {

        if (tr.oldVal == null) { /* if we created a new entry */
            dbHash.remove(tr.key);
            decCount(tr.newVal);
        } else if (tr.newVal == null) { /* if we deleted an entry */
            dbHash.put(tr.key, tr.oldVal);
            incCount(tr.oldVal);
        } else {    /* if we modified an entry */
            dbHash.put(tr.key, tr.oldVal);
            incCount(tr.oldVal);
            decCount(tr.newVal);
        }
    }
}
