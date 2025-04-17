package accommodationbookingservice.mapper;

import accommodationbookingservice.config.MapperConfig;
import accommodationbookingservice.dto.user.request.UserRegistrationRequestDto;
import accommodationbookingservice.dto.user.request.UserUpdateRequestDto;
import accommodationbookingservice.dto.user.response.UserResponseDto;
import accommodationbookingservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponseDto intoDto(User user);

    User intoModel(UserRegistrationRequestDto userRequestDto);

    void updateModelFromDto(UserUpdateRequestDto userRequestDto, @MappingTarget User user);
}
