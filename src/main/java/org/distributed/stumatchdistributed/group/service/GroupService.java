package org.distributed.stumatchdistributed.group.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.distributed.stumatchdistributed.auth.entity.UserAccount;
import org.distributed.stumatchdistributed.group.model.Bill;
import org.distributed.stumatchdistributed.group.model.BillSplit;
import org.distributed.stumatchdistributed.group.model.HousingGroup;
import org.distributed.stumatchdistributed.group.repository.BillRepository;
import org.distributed.stumatchdistributed.group.repository.BillSplitRepository;
import org.distributed.stumatchdistributed.group.repository.HousingGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {
    
    private final HousingGroupRepository groupRepository;
    private final BillRepository billRepository;
    private final BillSplitRepository billSplitRepository;

    public HousingGroup getMyGroup(UserAccount user) {
        log.info("Fetching group for user: {}", user.getEmail());
        return groupRepository.findByMemberId(user.getId()).orElse(null);
    }

    @Transactional
    public HousingGroup createGroup(HousingGroup group, UserAccount creator) {
        log.info("Creating group: {} by user: {}", group.getName(), creator.getEmail());
        
        // Add creator as first member
        if (group.getMembers() == null) {
            group.setMembers(new ArrayList<>());
        }
        if (!group.getMembers().contains(creator)) {
            group.getMembers().add(creator);
        }
        
        return groupRepository.save(group);
    }

    @Transactional
    public HousingGroup addMember(Long groupId, UserAccount newMember, UserAccount requester) {
        log.info("Adding member {} to group {}", newMember.getEmail(), groupId);
        
        HousingGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        
        // Check if requester is a member
        if (!group.getMembers().contains(requester)) {
            throw new RuntimeException("Only group members can add new members");
        }
        
        // Check if user is already a member
        if (group.getMembers().contains(newMember)) {
            throw new RuntimeException("User is already a member");
        }
        
        group.getMembers().add(newMember);
        return groupRepository.save(group);
    }

    @Transactional
    public void removeMember(Long groupId, UserAccount memberToRemove, UserAccount requester) {
        log.info("Removing member {} from group {}", memberToRemove.getEmail(), groupId);
        
        HousingGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        
        // Users can remove themselves, or any member can remove others (simple logic)
        if (!group.getMembers().contains(requester)) {
            throw new RuntimeException("Only group members can remove members");
        }
        
        group.getMembers().remove(memberToRemove);
        
        // If no members left, delete the group
        if (group.getMembers().isEmpty()) {
            groupRepository.delete(group);
        } else {
            groupRepository.save(group);
        }
    }

    public List<Bill> getGroupBills(Long groupId) {
        log.info("Fetching bills for group: {}", groupId);
        return billRepository.findByGroupIdOrderByDueDateDesc(groupId);
    }

    @Transactional
    public Bill createBill(Long groupId, Bill bill) {
        log.info("Creating bill for group: {}", groupId);
        
        HousingGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        
        bill.setGroup(group);
        Bill savedBill = billRepository.save(bill);
        
        // Automatically split bill equally among all members
        splitBillEqually(savedBill, group.getMembers());
        
        return savedBill;
    }

    @Transactional
    private void splitBillEqually(Bill bill, List<UserAccount> members) {
        if (members.isEmpty()) {
            throw new RuntimeException("Cannot split bill with no members");
        }
        
        BigDecimal splitAmount = bill.getTotalAmount()
                .divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);
        
        List<BillSplit> splits = new ArrayList<>();
        for (UserAccount member : members) {
            BillSplit split = BillSplit.builder()
                    .bill(bill)
                    .user(member)
                    .amount(splitAmount)
                    .paid(false)
                    .build();
            splits.add(split);
        }
        
        billSplitRepository.saveAll(splits);
        bill.setSplits(splits);
    }

    @Transactional
    public BillSplit markAsPaid(Long splitId, UserAccount user) {
        log.info("Marking split {} as paid by user: {}", splitId, user.getEmail());
        
        BillSplit split = billSplitRepository.findById(splitId)
                .orElseThrow(() -> new RuntimeException("Bill split not found"));
        
        // Check if user owns this split
        if (!split.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only mark your own bills as paid");
        }
        
        split.setPaid(true);
        split.setPaidAt(LocalDateTime.now());
        return billSplitRepository.save(split);
    }

    public BigDecimal getUserBalance(UserAccount user) {
        BigDecimal unpaid = billSplitRepository.sumUnpaidAmountByUserId(user.getId());
        return unpaid != null ? unpaid : BigDecimal.ZERO;
    }

    public List<BillSplit> getUserSplits(UserAccount user) {
        return billSplitRepository.findByUserId(user.getId());
    }
}
