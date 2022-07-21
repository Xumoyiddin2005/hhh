package com.company.service;

import com.company.dto.transfer.TransferCreateDTO;
import com.company.dto.transfer.TransferDTO;
import com.company.dto.transfer.TransferResponseDTO;
import com.company.entity.CardEntity;
import com.company.entity.CompanyEntity;
import com.company.entity.TransferEntity;
import com.company.enums.GeneralStatus;
import com.company.enums.TransferStatus;
import com.company.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransferService {

    @Autowired
    private TransferRepository transferRepository;
    @Autowired
    private CardService cardService;
    @Autowired
    private CompanyService companyService;

    private final double uzCardServicePercentage = 0.5;

    public TransferResponseDTO create(TransferCreateDTO transferDTO) {

        CardEntity fromCard = cardService.get(transferDTO.getFromCard());
        CardEntity toCard = cardService.get(transferDTO.getToCard());
        CompanyEntity company = companyService.get(transferDTO.getCompanyId());

        if (!fromCard.getStatus().equals(GeneralStatus.ACTIVE)) {
            return new TransferResponseDTO("failed", "Card not active");
        }

        if (!toCard.getStatus().equals(GeneralStatus.ACTIVE)) {
            return new TransferResponseDTO("failed", "Card not active");
        }

        // 10,000
        //  company.getServicePercentage()  0.3
        //  0.4 - uzcard
        double service_percentage = uzCardServicePercentage + company.getServicePercentage(); // 0.7
        double service_amount = transferDTO.getAmount() * service_percentage; // 70
        double total_amount = transferDTO.getAmount() + service_amount; // 10,070

        if (fromCard.getBalance() < total_amount) {
            return new TransferResponseDTO("failed", "Not enough money");
        }

        TransferEntity entity = new TransferEntity();
        entity.setAmount(transferDTO.getAmount());
        entity.setFromCardId(fromCard.getId());
        entity.setToCardId(toCard.getId());
        entity.setServiceAmount((long) service_amount);
        entity.setTotalAmount((long) total_amount);
        entity.setStatus(TransferStatus.IN_PROGRESS);
        // ..........
        transferRepository.save(entity);

        TransferDTO dto = new TransferDTO();
        // id, card ,... total_amount

        return new TransferResponseDTO("success", "", dto);
    }

    public void finishTransfer(TransferDTO dto) {
        // ...
        // 1. fromCard - CREDIT
        // 2. TOCARD - DEBIT
        // 3. card_id (PAYMENT,BANK) - DEBIT
        // 4. card_id  (UZ_CARD) - DEBIT

        // FROM_CARD_UPDATE
        // TO_CARD_UPDATE
        // Company_CARD_UPDATE
        // Uzcard_CARD_UPDATE

        // Transfer status update
    }

}
