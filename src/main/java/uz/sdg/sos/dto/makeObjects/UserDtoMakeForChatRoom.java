package uz.sdg.sos.dto.makeObjects;

import lombok.*;
import uz.sdg.sos.dto.makeObjects.UserDtoMakeForChat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDtoMakeForChatRoom extends UserDtoMakeForChat {
    private Integer chatCount;
    private String lastMessage;
    private LocalDateTime lastChatDateTime;

    public static UserDtoMakeForChatRoom toRoom(UserDtoMakeForChat userDtoMake, int chatCount, String lastMessage,LocalDateTime lastChatDateTime,UserDtoMakeForChatRoom room ){
        room.setLastName(userDtoMake.getLastName());
        room.setFirstName(userDtoMake.getFirstName());
        room.setDateBirth(userDtoMake.getDateBirth());
        room.setPhoneNumber(userDtoMake.getPhoneNumber());
        room.setAccountType(userDtoMake.getAccountType());
        room.setChatCount(chatCount);
        room.setLastMessage(lastMessage);
        room.setLastChatDateTime(lastChatDateTime);
        room.setId(userDtoMake.getId());
        return room;
    }
}
