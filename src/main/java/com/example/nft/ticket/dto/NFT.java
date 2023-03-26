package com.example.nft.ticket.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * 서비스 스펙의 NFT 관련 DTO 를 정의하는 클래스
 */
public class NFT {
    /**
     * NFT 발행 요청 클래스
     */
    public static class MintNftRequest {
        private final String recipient;
        private final Map<String, String> payLoad = new HashMap<>();

        /**
         * TODO("NFT 메타데이터 설계")
         * @param recipient NFT 발행 대상
         * @param tokenURI NFT 메타데이터
         */
        public MintNftRequest(String recipient, String tokenURI) {
            this.recipient = recipient;
            this.payLoad.put("tokenURI", tokenURI);
        }

        public String getRecipient() {
            return recipient;
        }

        public Map<String, String> getPayload() {
            return payLoad;
        }
    }


    /**
     * NFT 조회 응답 클래스
     */
    public static class QueryNFTResponse {
        private final String tokenURI;
        private final String owner;

        public QueryNFTResponse(Map<String, String> map) {
            this(map.get("tokenURI"), map.get("owner"));
        }

        public QueryNFTResponse(String tokenURI, String owner) {
            this.tokenURI = tokenURI;
            this.owner = owner;
        }

        public String getTokenURI() {
            return tokenURI;
        }

        public String getOwner() {
            return owner;
        }
    }
}
