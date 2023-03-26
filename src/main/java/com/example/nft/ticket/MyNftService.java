package com.example.nft.ticket;

import com.example.nft.ticket.dto.NFT.MintNftRequest;
import com.example.nft.ticket.dto.NFT.QueryNFTResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;


/**
 * openzeppelin ERC721 기반 contract 호출 및 응답 반환 클래스
 *
 *
 */
@Service
@RequiredArgsConstructor
public class MyNftService {
    private final ObjectMapper objectMapper;
    private final MyContractUtils myContractUtils = new MyContractUtils();

    /**
     * @param req 서비스 스펙의 NFT 발행 요청
     * @return 생성된 토큰의 ID
     */
    public BigInteger mint(MintNftRequest req) throws IOException {
        Function function = createMintNftFunction(req.getRecipient(), req.getPayload());
        String txHash = myContractUtils.sendTransaction(function);

        List<Type> res = myContractUtils.getFunctionOutputsFromTransactionReceipt(txHash, function);
        return ((Uint256) res.get(0)).getValue();
    }


    /**
     * 토큰 ID로 NFT 조회
     * @return 서비스 스펙의 NFT 조회 응답
     */
    public QueryNFTResponse queryNft(BigInteger tokenId) throws IOException {
        Function queryPayloadFunction = createQueryNftFunction(tokenId);
        Function queryOwnerFunction = createQueryOwner(tokenId);

        String payload = myContractUtils.call(queryPayloadFunction).get(0).toString();
        String owner = myContractUtils.call(queryOwnerFunction).get(0).toString();

        Map<String, String> map = objectMapper.readValue(payload, Map.class);
        map.put("owner", owner);
        return new QueryNFTResponse(map);
    }

    @NotNull
    private Function createQueryOwner(BigInteger tokenId) {
        return new Function("ownerOf",
                List.of(new Uint256(tokenId)),
                List.of(new TypeReference<Address>() {
                }));
    }

    @NotNull
    private Function createQueryNftFunction(BigInteger tokenId) {
        return new Function("tokenURI",
                List.of(new Uint256(tokenId)),
                List.of(new TypeReference<Utf8String>() {
                }));
    }

    private Function createMintNftFunction(@NotNull String recipient, Map<String, String> payload)
            throws JsonProcessingException {
        String jsonPayload = objectMapper.writeValueAsString(payload);
        return new Function(
                "mintNFT",
                Arrays.asList(new Address(recipient), new Utf8String(jsonPayload)),
                List.of(new TypeReference<Uint256>() {
                }));
    }
}
