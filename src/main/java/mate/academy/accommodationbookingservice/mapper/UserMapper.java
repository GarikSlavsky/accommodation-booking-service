package mate.academy.accommodationbookingservice.mapper;

import mate.academy.accommodationbookingservice.config.MapperConfig;
import mate.academy.accommodationbookingservice.dto.user.request.UserRegistrationRequestDto;
import mate.academy.accommodationbookingservice.dto.user.request.UserUpdateRequestDto;
import mate.academy.accommodationbookingservice.dto.user.response.UserResponseDto;
import mate.academy.accommodationbookingservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponseDto intoDto(User user);

    User intoModel(UserRegistrationRequestDto userRequestDto);

    void updateModelFromDto(UserUpdateRequestDto userRequestDto, @MappingTarget User user);
}
