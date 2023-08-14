package ru.practicum.main_service.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_service.dto.user.UserDto;
import ru.practicum.main_service.exceptions.NameAlreadyExistException;
import ru.practicum.main_service.mapper.UserMapper;
import ru.practicum.main_service.repository.UserRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByName(userDto.getName())) {
            log.warn(String.format(
                    "Can't create user with name: %s, the name was used by another user", userDto.getName()));
            throw new NameAlreadyExistException(String.format(
                    "Can't create user with name: %s, the name was used by another user",
                    userDto.getName()));
        }
        log.debug(String.format("The user with name %s was created", userDto.getName()));
        return userMapper.toUserDto(userRepository.save(userMapper.toUserModel(userDto)));
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.debug("Received users");
        Pageable page = PageRequest.of(from / size, size);
        return ids != null && !ids.isEmpty() ? userMapper.toUserDtoList(userRepository.findAllById(ids))
                : userMapper.toUserDtoList(userRepository.findAll(page).toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.debug("User with id: {} was deleted ", id);
        userRepository.deleteById(id);
    }
}
