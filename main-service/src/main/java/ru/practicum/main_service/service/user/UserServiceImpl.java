package ru.practicum.main_service.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_service.dto.user.UserDto;
import ru.practicum.main_service.exceptions.ConflictException;
import ru.practicum.main_service.exceptions.NotFoundException;
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
        if (Boolean.TRUE.equals(userRepository.existsByName(userDto.getName()))) {
            log.warn("Can't create user with name: {}. Name is already taken.", userDto.getName());
            throw new ConflictException(String.format(
                    "Can't create user with name: %s, the name was used by another user",
                    userDto.getName()));
        }
        log.info("Creating user with name: {}", userDto.getName());
        return userMapper.toUserDto(userRepository.save(userMapper.toUserModel(userDto)));
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Fetching users from index {} with size {}.", from, size);
        Pageable page = PageRequest.of(from / size, size);
        return ids != null && !ids.isEmpty()
                ? userMapper.toUserDtoList(userRepository.findAllById(ids))
                : userMapper.toUserDtoList(userRepository.findAll(page).toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            log.warn("Attempted to delete a non-existent user with id: {}.", id);
            throw new NotFoundException("User with id: " + id + " does not exist.");
        }

        log.info("Deleting user with id: {}.", id);
        userRepository.deleteById(id);
    }
}
