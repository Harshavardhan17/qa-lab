package com.bank.qalab.web.ui;

import com.bank.qalab.dto.AccountDtos.AccountResponse;
import com.bank.qalab.dto.BeneficiaryDtos.BeneficiaryRequest;
import com.bank.qalab.dto.TransferDtos.CreateTransferRequest;
import com.bank.qalab.dto.TransferDtos.TransferResponse;
import com.bank.qalab.security.AppUserPrincipal;
import com.bank.qalab.service.AccountService;
import com.bank.qalab.service.BeneficiaryService;
import com.bank.qalab.service.TransferService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Server-rendered banking pages (session/form-login secured). This is the surface the QA will
 * automate with Selenium. Every interactive element has a stable id/class for locators.
 */
@Controller
public class WebUiController {

    private final AccountService accountService;
    private final TransferService transferService;
    private final BeneficiaryService beneficiaryService;

    public WebUiController(AccountService accountService, TransferService transferService,
                           BeneficiaryService beneficiaryService) {
        this.accountService = accountService;
        this.transferService = transferService;
        this.beneficiaryService = beneficiaryService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        List<AccountResponse> accounts = accountService.list(principal, null, null, PageRequest.of(0, 100)).content();
        BigDecimal total = accounts.stream()
                .map(AccountResponse::balance)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("accounts", accounts);
        model.addAttribute("totalBalance", total);
        return "dashboard";
    }

    @GetMapping("/accounts")
    public String accounts(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("accounts",
                accountService.list(principal, null, null, PageRequest.of(0, 100)).content());
        return "accounts";
    }

    @GetMapping("/accounts/{id}")
    public String accountDetail(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal,
                                Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("account", accountService.get(id, principal));
            model.addAttribute("transactions",
                    accountService.transactions(id, null, null, PageRequest.of(0, 100), principal).content());
            return "account-detail";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/accounts";
        }
    }

    @GetMapping("/transfer")
    public String transferForm(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("accounts",
                accountService.list(principal, null, null, PageRequest.of(0, 100)).content());
        model.addAttribute("beneficiaries",
                principal.getCustomerId() != null ? beneficiaryService.list(principal) : List.of());
        return "transfer";
    }

    @PostMapping("/transfer")
    public String doTransfer(@AuthenticationPrincipal AppUserPrincipal principal,
                             @RequestParam Long fromAccountId,
                             @RequestParam(required = false) String toAccountNumber,
                             @RequestParam(required = false) Long beneficiaryId,
                             @RequestParam BigDecimal amount,
                             @RequestParam(required = false) String description,
                             RedirectAttributes ra) {
        try {
            CreateTransferRequest req = new CreateTransferRequest(
                    fromAccountId, emptyToNull(toAccountNumber), beneficiaryId, amount, description);
            TransferResponse res = transferService.create(req, principal);
            ra.addFlashAttribute("success", "Transfer " + res.reference() + " " + res.status().toLowerCase() + ".");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transfer";
    }

    @GetMapping("/beneficiaries")
    public String beneficiaries(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("beneficiaries",
                principal.getCustomerId() != null ? beneficiaryService.list(principal) : List.of());
        return "beneficiaries";
    }

    @PostMapping("/beneficiaries")
    public String addBeneficiary(@AuthenticationPrincipal AppUserPrincipal principal,
                                 @RequestParam String name,
                                 @RequestParam String accountNumber,
                                 @RequestParam(required = false) String nickname,
                                 @RequestParam(required = false) String bankName,
                                 @RequestParam(required = false) String ifsc,
                                 RedirectAttributes ra) {
        try {
            beneficiaryService.create(new BeneficiaryRequest(name, nickname, bankName, accountNumber, ifsc), principal);
            ra.addFlashAttribute("success", "Beneficiary added: " + name);
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/beneficiaries";
    }

    @PostMapping("/beneficiaries/{id}/delete")
    public String deleteBeneficiary(@AuthenticationPrincipal AppUserPrincipal principal,
                                    @PathVariable Long id, RedirectAttributes ra) {
        try {
            beneficiaryService.delete(id, principal);
            ra.addFlashAttribute("success", "Beneficiary removed.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/beneficiaries";
    }

    private static String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
