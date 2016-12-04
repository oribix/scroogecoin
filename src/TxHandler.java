import java.util.ArrayList;
import java.util.HashSet;

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
		
		double txInputSum = 0.0;
        double txOutputSum = 0.0;
		
		//create a list of claimed outputs
		ArrayList<UTXO> claimedOutputs = new ArrayList<UTXO>();
		for (Transaction.Input input : tx.getInputs()) {
            UTXO claimedOutput = new UTXO(input.prevTxHash, input.outputIndex);
            claimedOutputs.add(claimedOutput);
        }
		
		// (1) all outputs claimed by tx are in the current UTXO pool,
		for (UTXO claimedOutput : claimedOutputs) {
		    if(!publicLedger.contains(claimedOutput)){
//		    	System.out.println("In 1");
		        isValid = false;
		        break;
		    }
        }
		
		// (2) the signatures on each input of tx are valid
		if (isValid) {
			ArrayList<Transaction.Input> inputs = tx.getInputs();
			for(int i = 0; i < inputs.size(); i++) {
				RSAKey publicKey = publicLedger.getTxOutput(claimedOutputs.get(i)).address;
				if (!publicKey.verifySignature(tx.getRawDataToSign(i), inputs.get(i).signature)) {
//					System.out.println("In 2");
					isValid = false;
					break;
				}
			}
		}
		
		// (3) no UTXO is claimed multiple times by tx,
		if (isValid){
		    //Convert to set to remove repetitions
		    HashSet<UTXO> utxoSet = new HashSet<>();
		    for(UTXO claimedOutput : claimedOutputs){
		        utxoSet.add(claimedOutput);
		    }
		    if(utxoSet.size() < claimedOutputs.size()) {
//		    	System.out.println("In 3");
		    	isValid = false;
		    }
		}
		
		// (4) all of tx's output values are non-negative
		if(isValid){
		    for (Transaction.Output output : tx.getOutputs()){
	            txOutputSum += output.value;
	            if (output.value < 0.0){
//	            	System.out.println("In 4");
	                isValid = false;
	                break;
	            }
	        }
		}
		
		// (5) the sum of tx's input values is greater than or equal to the sum of   
		//     its output values;
		if (isValid){
		    //get sum of transaction inputs
	        for(UTXO claimedOutput : claimedOutputs){
	            txInputSum += publicLedger.getTxOutput(claimedOutput).value;
	        }
	        
            if(txInputSum < txOutputSum) {
//            	System.out.println("In 5");
            	isValid = false;
            }
        }
		
		return isValid;
	}

	/* Handles each epoch by receiving an unordered array of proposed 
	 * transactions, checking each transaction for correctness, 
	 * returning a mutually valid array of accepted transactions, 
	 * and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
	    //TODO: Still need to update UTXO pool "as necessary"
		ArrayList<Transaction> acceptedTxs = new ArrayList<Transaction>(); 
		for(Transaction tx : possibleTxs){
		    if (isValidTx(tx)) {
                //add tx to list of accepted transactions
		        acceptedTxs.add(tx);
		        
		        //remove claimed UTXOs from the public ledger
                for (Transaction.Input input : tx.getInputs()) {
                    UTXO claimedOutput = new UTXO(input.prevTxHash, input.outputIndex);
                    publicLedger.removeUTXO(claimedOutput);
                }
                
                //add new UTXOs to the public ledger
                byte[] txHash = tx.getHash();
                ArrayList<Transaction.Output> txOutputs = tx.getOutputs(); 
                for(int i = 0; i < txOutputs.size(); i++) {
                    UTXO utxo = new UTXO(txHash, i);
                    publicLedger.addUTXO(utxo, txOutputs.get(i));
                }
            }
		}
		return acceptedTxs.toArray(new Transaction[acceptedTxs.size()]);
	}
	
	/* Returns the current UTXO pool.If no outstanding UTXOs, returns an empty (non-null) UTXOPool object. */
	public UTXOPool getUTXOPool() {
	    return (!publicLedger.getAllUTXO().isEmpty()) ? publicLedger : new UTXOPool();
    }

} 