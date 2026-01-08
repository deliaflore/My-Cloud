package org.distributed.stumatchdistributed.roommate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.distributed.stumatchdistributed.auth.entity.UserAccount;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoommateMatch {
    private UserAccount user;
    private RoommateProfile profile;
    private Double compatibilityScore;
    private String matchReason;
}
