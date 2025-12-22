package com.example.backend.seller.service;

import com.example.backend.seller.dto.SellerRequestResponse;
import com.example.backend.auth.dto.Responses.MessageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface SellerService {

    SellerRequestResponse requestSeller(String userEmail, String storeName,String reason , MultipartFile document) throws IOException;

    MessageResponse approveRequest(Long requestId, String adminEmail);

    MessageResponse rejectRequest(Long requestId, String adminEmail, String reason);

    List<SellerRequestResponse> getPendingRequests();
}
