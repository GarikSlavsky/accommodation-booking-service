package accommodation.booking.service.mapper;

import accommodation.booking.service.config.MapperConfig;
import accommodation.booking.service.dto.user.request.UserRegistrationRequestDto;
import accommodation.booking.service.dto.user.request.UserUpdateRequestDto;
import accommodation.booking.service.dto.user.response.UserResponseDto;
import accommodation.booking.service.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponseDto intoDto(User user);

    User intoModel(UserRegistrationRequestDto userRequestDto);

    void updateModelFromDto(UserUpdateRequestDto userRequestDto, @MappingTarget User user);
}
