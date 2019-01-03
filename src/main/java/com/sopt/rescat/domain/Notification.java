package com.sopt.rescat.domain;

import lombok.Builder;
import lombok.Cleanup;
import lombok.Getter;

import javax.persistence.*;
import java.util.List;

@Getter
@Entity
public class Notification extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @OneToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_notification_receiving_user_idx"))
    private User receivingUser;

    @Column
    private Long relatedPostIdx;

    @Column
    private String contents;

    public Notification (User receivingUser, String contents){
        this.receivingUser = receivingUser;
        this.contents = contents;
    }

    public Notification (User receivingUser, Long relatedPostIdx , String contents){
        this.receivingUser = receivingUser;
        this.relatedPostIdx = relatedPostIdx;
        this.contents = contents;
    }

}
