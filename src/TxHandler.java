import java.util.ArrayList;

public class TxHandler {

	UTXOPool publicLedger;
	
	/* Creates a public ledger whose current UTXOPool (collection of unspent 
	 * transaction outputs) is utxoPool. This should make a defensive copy of 
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	public TxHandler(UTXOPool utxoPool) {
		publicLedger = new UTXOPool(utxoPool);
	}

	/* Returns true if 
	 * (1) all outputs claimed by tx are in the current UTXO pool, 
	 * (2) the signatures on each input of tx are valid, 
	 * (3) no UTXO is claimed multiple times by tx, 
	 * (4) all of tx's output values are non-negative, and
	 * (5) the sum of tx's input values is greater than or equal to the sum of   
	        its output values;
	   and false otherwise.
	 */
	public boolean isValidTx(Transaction tx) {
		boolean isValid = true;
		ArrayList<UTXO> UTXOList = publicLedger.getAllUTXO();
		ArrayList<Transaction.Output> txOutputs = tx.getOutputs();
		double txInputSum = 0;
		double txOutputSum = 0;
		
		
		// (1) Checks all outputs against public ledger's outputs
		// (4) all of tx's output values are non-negative
		for (Transaction.Output output : txOutputs) {
			boolean isNegative = output.value < 0;
			txOutputSum += output.value;
		    if (isNegative || !publicLedger.containsOutput(output))
				isValid = false;
		}
		// (2) the signatures on each input of tx are valid,
		if (isValid) {
			
		}
		// (3) no UTXO is claimed multiple times by tx,
		if (isValid){
		    
		}
		// (5) the sum of tx's input values is greater than or equal to the sum of   
		//     its output values;
		if (isValid){
            
        }
		
		return isValid;
	}

	/* Handles each epoch by receiving an unordered array of proposed 
	 * transactions, checking each transaction for correctness, 
	 * returning a mutually valid array of accepted transactions, 
	 * and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		// IMPLEMENT THIS
		return null;
	}
	
	/* Returns the current UTXO pool.If no outstanding UTXOs, returns an empty (non-null) UTXOPool object. */
	public UTXOPool getUTXOPool() {
	    return (!publicLedger.getAllUTXO().isEmpty()) ? publicLedger : new UTXOPool();
    }

} 