package com.example.nft.core;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

/**
 * Contract 의 함수를 호출하기 위한 유틸리티 클래스입니다
 *
 * GANACHE_URL: ganache-cli 주소
 * CONTRACT_ADDRESS: openzeppelin ERC721 기반 contract 주소
 * OWNER_ADDRESS: contract 소유자 주소
 */
public class ContractUtils {
    private final Web3j web3j;
    private final String OWNER_ADDRESS;
    private final String CONTRACT_ADDRESS;
    private final StaticGasProvider gasProvider;

    private boolean useDefaultGasProvider = true;

    public ContractUtils(String serverURL, String ownerAddress, String contractAddress, boolean useDefaultGasProvider) {
        this(serverURL, ownerAddress, contractAddress);
        this.useDefaultGasProvider = useDefaultGasProvider;
    }

    public ContractUtils(String serverURL, String ownerAddress, String contractAddress) {
        this.web3j = Web3j.build(new HttpService(serverURL));
        this.OWNER_ADDRESS = ownerAddress;
        this.CONTRACT_ADDRESS = contractAddress;
        this.gasProvider = new DefaultGasProvider();
    }

    /**
     * 가스를 필요로 하는 함수를 호출할 때 사용합니다
     * @param function 호출할 함수
     * @return 트랜잭션 해시
     */
    public String sendTransaction(Function function) throws IOException {
        Transaction transaction = createTransaction(function);
        EthSendTransaction res = web3j.ethSendTransaction(transaction).send();
        return res.getTransactionHash();
    }

    /**
     * read-only 함수를 호출합니다
     * @param function 호출할 함수
     */
    public List<Type> call(Function function) throws IOException {
        Transaction transaction = createTransaction(function);
        EthCall res = web3j.ethCall(transaction, DefaultBlockParameter.valueOf("latest")).send();
        return FunctionReturnDecoder.decode(res.getResult(), function.getOutputParameters());
    }

    protected TransactionReceipt getReceipt(String transactionHash) throws IOException {
        EthGetTransactionReceipt ethGetTransactionReceipt = this.web3j.ethGetTransactionReceipt(transactionHash).send();
        Optional<TransactionReceipt> transactionReceipt = ethGetTransactionReceipt.getTransactionReceipt();
        if(transactionReceipt.isEmpty()) {
            throw new RuntimeException("Transaction processed not yet");
        }
        return transactionReceipt.get();
    }

    @NotNull
    private Transaction createTransaction(Function function) throws IOException {
        String encodedFunction = FunctionEncoder.encode(function);
        return Transaction.createFunctionCallTransaction(
                OWNER_ADDRESS,
                getNonce(),
                getGasPrice(),
                getGasLimit(),
                CONTRACT_ADDRESS,
                encodedFunction);
    }

    /**
     * @return 가스당 지불할 이더리움 수량
     */
    private BigInteger getGasPrice() {
        if (useDefaultGasProvider) {
            return gasProvider.getGasPrice();
        }
        return Contract.GAS_PRICE;
    }

    /**
     * @return 한 트랜잭션을 처리하는데 사용할 가스의 최대량
     */
    private BigInteger getGasLimit() {
        if (useDefaultGasProvider) {
            return gasProvider.getGasLimit();
        }
        return Contract.GAS_LIMIT;
    }

    /**
     * 트랜잭션을 전송하기 전에 트랜잭션을 전송할 계정의 nonce 값을 가져옵니다
     * nonce 값은 네트워크에서 동일 트랜잭션의 중복 처리를 방지하기 위해 사용됩니다
     * @return
     * @throws IOException
     */
    private BigInteger getNonce() throws IOException {
        EthGetTransactionCount ethGetTransactionCount =
                this.web3j
                .ethGetTransactionCount(OWNER_ADDRESS, DefaultBlockParameter.valueOf("latest"))
                .send();

        return ethGetTransactionCount.getTransactionCount();
    }
}
