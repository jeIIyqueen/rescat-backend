package com.sopt.rescat.domain.log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sopt.rescat.domain.BaseTime;
import com.sopt.rescat.domain.Notification;
import com.sopt.rescat.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;


@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserNotificationLog extends BaseTime {

    @Id
    @GeneratedValue
    private Long idx;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_user_notification_log_user_receiving_idx"))
    @JsonIgnore
    private User receivingUser;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_user_notification_log_notification_idx"))
    private Notification notification;

    @Column
    @Range(min = 0, max = 1)
    // 0: 안읽음, 1: 읽음
    private Integer isChecked;

    public void updateIsChecked() {
        this.isChecked = 1;
    }
}
