package com.example.nft.ticket;

import com.example.nft.core.ticket.ContractUtils;
import java.io.IOException;
import java.util.List;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class MyContractUtils extends ContractUtils {
    private static final String GANACHE_URL = "http://localhost:7545";
    private static final String CONTRACT_ADDRESS = "0x1B47825571Fa83eb28d116d1959de59cF3cC1BCB";
    private static final String OWNER_ADDRESS = "0xdF709a8770CDf5f6ea8025f80643Ec1e8528b057";

    public MyContractUtils() {
        super(GANACHE_URL, OWNER_ADDRESS, CONTRACT_ADDRESS, false);
    }

    /**
     * mintNFT 의 결과를 가져옵니다
     */
    public List<Type> getFunctionOutputsFromTransactionReceipt(String transactionHash, Function function) throws IOException {
        TransactionReceipt transactionReceipt = getReceipt(transactionHash);
        return getFunctionOutputsFromTransactionReceipt(transactionReceipt, function);
    }

    /**
     * mintNFT 의 결과를 가져옵니다
     */
    private List<Type> getFunctionOutputsFromTransactionReceipt(TransactionReceipt transactionReceipt, Function function) {
        Log log = transactionReceipt.getLogs().get(0);
        String output = log.getTopics().get(3);
        return FunctionReturnDecoder.decode(output, function.getOutputParameters());
    }
}
