package org.distributed.stumatchdistributed.group.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.distributed.stumatchdistributed.auth.entity.UserAccount;
import org.distributed.stumatchdistributed.auth.service.UserContextService;
import org.distributed.stumatchdistributed.group.model.Bill;
import org.distributed.stumatchdistributed.group.model.BillSplit;
import org.distributed.stumatchdistributed.group.model.HousingGroup;
import org.distributed.stumatchdistributed.group.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GroupController {

    private final GroupService groupService;
    private final UserContextService userContextService;

    @GetMapping("/my-group")
    public ResponseEntity<?> getMyGroup(Authentication authentication) {
        try {
            log.info("API request: GET /api/groups/my-group");
            UserAccount user = userContextService.requireUserByEmail(authentication.getName());
            HousingGroup group = groupService.getMyGroup(user);
            
            if (group == null) {
                return ResponseEntity.ok(Map.of("exists", false));
            }
            
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            log.error("Failed to fetch group", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createGroup(
            @RequestBody HousingGroup group,
            Authentication authentication) {
        try {
            log.info("API request: POST /api/groups");
            UserAccount user = userContextService.requireUserByEmail(authentication.getName());
            HousingGroup created = groupService.createGroup(group, user);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Failed to create group", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{groupId}/bills")
    public ResponseEntity<?> getGroupBills(@PathVariable Long groupId) {
        try {
            log.info("API request: GET /api/groups/{}/bills", groupId);
            List<Bill> bills = groupService.getGroupBills(groupId);
            return ResponseEntity.ok(bills);
        } catch (Exception e) {
            log.error("Failed to fetch bills", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{groupId}/bills")
    public ResponseEntity<?> createBill(
            @PathVariable Long groupId,
            @RequestBody Bill bill,
            Authentication authentication) {
        try {
            log.info("API request: POST /api/groups/{}/bills", groupId);
            UserAccount user = userContextService.requireUserByEmail(authentication.getName());
            Bill created = groupService.createBill(groupId, bill);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Failed to create bill", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/bills/splits/{splitId}/pay")
    public ResponseEntity<?> markAsPaid(
            @PathVariable Long splitId,
            Authentication authentication) {
        try {
            log.info("API request: POST /api/groups/bills/splits/{}/pay", splitId);
            UserAccount user = userContextService.requireUserByEmail(authentication.getName());
            BillSplit updated = groupService.markAsPaid(splitId, user);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Failed to mark as paid", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-balance")
    public ResponseEntity<?> getMyBalance(Authentication authentication) {
        try {
            log.info("API request: GET /api/groups/my-balance");
            UserAccount user = userContextService.requireUserByEmail(authentication.getName());
            BigDecimal balance = groupService.getUserBalance(user);
            return ResponseEntity.ok(Map.of("balance", balance));
        } catch (Exception e) {
            log.error("Failed to get balance", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-splits")
    public ResponseEntity<?> getMySplits(Authentication authentication) {
        try {
            log.info("API request: GET /api/groups/my-splits");
            UserAccount user = userContextService.requireUserByEmail(authentication.getName());
            List<BillSplit> splits = groupService.getUserSplits(user);
            return ResponseEntity.ok(splits);
        } catch (Exception e) {
            log.error("Failed to get splits", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
