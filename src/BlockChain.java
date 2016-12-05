import java.util.ArrayList;
import java.util.HashMap;

/* Block Chain should maintain only limited block nodes to satisfy the functions
   You should not have the all the blocks added to the block chain in memory 
   as it would overflow memory
 */

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    
    private ArrayList<BlockNode> heads;  
    private HashMap<ByteArrayWrapper, BlockNode> H;    
    private int height;   
    private BlockNode maxHeightBlock;    
    private TransactionPool txPool;

    // all information required in handling a block in block chain
    private class BlockNode {
        public Block b;
        public BlockNode parent;
        public ArrayList<BlockNode> children;
        public int height;
        // utxo pool for making a new block on top of this block
        private UTXOPool uPool;

        public BlockNode(Block b, BlockNode parent, UTXOPool uPool) {
            this.b = b;
            this.parent = parent;
            children = new ArrayList<BlockNode>();
            this.uPool = uPool;
            if (parent != null) {
                height = parent.height + 1;
                parent.children.add(this);
            } else {
                height = 1;
            }
        }

        public UTXOPool getUTXOPoolCopy() {
            return new UTXOPool(uPool);
        }
    }

    /* create an empty block chain with just a genesis block.
     * Assume genesis block is a valid block
     */
    public BlockChain(Block genesisBlock) {
        UTXOPool uPool = new UTXOPool();      
        Transaction coinbase = genesisBlock.getCoinbase();      
        UTXO utxoCoinbase = new UTXO(coinbase.getHash(), 0);      
        uPool.addUTXO(utxoCoinbase, coinbase.getOutput(0));      
        BlockNode genesis = new BlockNode(genesisBlock, null, uPool);      
        heads = new ArrayList<BlockNode>();      
        heads.add(genesis);      
        H = new HashMap<ByteArrayWrapper, BlockNode>();      
        H.put(new ByteArrayWrapper(genesisBlock.getHash()), genesis);      
        height = 1;      
        maxHeightBlock = genesis;      
        txPool = new TransactionPool();
    }

    /* Get the maximum height block
     */
    public Block getMaxHeightBlock() {
        return maxHeightBlock.b;
    }

    /* Get the UTXOPool for mining a new block on top of 
     * max height block
     */
    public UTXOPool getMaxHeightUTXOPool() {
        return maxHeightBlock.getUTXOPoolCopy();
    }

    /* Get the transaction pool to mine a new block
     */
    public TransactionPool getTransactionPool() {
        return txPool;
    }
    
    //valid check for block
    /* For validity, all transactions should be valid
     * and block should be at height > (maxHeight - CUT_OFF_AGE).
     * For example, you can try creating a new block over genesis block 
     * (block height 2) if blockChain height is <= CUT_OFF_AGE + 1. 
     * As soon as height > CUT_OFF_AGE + 1, you cannot create a new block at height 2.
     */
    private boolean blockValid(BlockNode b){
    	boolean isValid = true;
    	
    	// Check that height is valid
    	if (height <= maxHeightBlock.height - CUT_OFF_AGE) {
    		System.out.println("In bool check");
    		isValid = false;
    	}
    	
    	if (isValid) {
    		
		    TxHandler handler = new TxHandler(b.parent.uPool);
		    ArrayList<Transaction> blockTxs = b.b.getTransactions(); 
		    Transaction[] txs = handler.handleTxs(blockTxs.toArray(new Transaction[blockTxs.size()]));
		    if (txs.length != b.b.getTransactions().size()) {
		    	System.out.println("In handleTxs");
		    	isValid = false;
		    }
		    
		    for (Transaction tx : txs) {
		    	ArrayList<Transaction.Output> outputs = tx.getOutputs();
		    	for (int i = 0; i < outputs.size(); i++) {
		    		UTXO utxo = new UTXO(tx.getHash(), i);
		    		b.uPool.addUTXO(utxo, outputs.get(i));
		    	}
		    }
    	}
        return isValid;
    }
    
    /* Add a block to block chain if it is valid.
     * Return true of block is successfully added
     */
    public boolean addBlock(Block b) {
        
        //get previous block hash
        byte[] prevBlockHash = b.getPrevBlockHash();
        if (prevBlockHash == null) return false;
        
        //get previous block node
        BlockNode prevBlockNode = H.get(new ByteArrayWrapper(prevBlockHash));
        if (prevBlockNode == null) return false;
        
        //construct UTXOPool
        UTXOPool uPool = new UTXOPool();
        Transaction coinbase = b.getCoinbase();
        ArrayList<Transaction.Output> outputs = coinbase.getOutputs();
        for(int i = 0; i < outputs.size(); i++){
            UTXO utxoCoinbase = new UTXO(coinbase.getHash(), i);
            uPool.addUTXO(utxoCoinbase, outputs.get(i));
        }
        
        //create new BlockNode to be added
        BlockNode blockNode = new BlockNode(b, prevBlockNode, uPool);
        if(!blockValid(blockNode)) return false;
        
        //add block to block chain
        heads.add(blockNode);
        H.put(new ByteArrayWrapper(b.getHash()), blockNode);
        
        //update block chain height and max height block
        if(blockNode.height > height){
            height = blockNode.height;
            maxHeightBlock = blockNode;
        }
        
        // Remove transactions from transaction pool
        for (Transaction t : blockNode.b.getTransactions()){
            Transaction fromTx = txPool.getTransaction(t.getHash());
            if (fromTx != null)
                txPool.removeTransaction(t.getHash());
        }
        return true;
    }

    //Add a transaction in transaction pool
    public void addTransaction(Transaction tx) {
        txPool.addTransaction(tx);
        return;
    }
}