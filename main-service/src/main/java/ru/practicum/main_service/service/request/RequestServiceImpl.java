package ru.practicum.main_service.service.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_service.dto.request.RequestDto;
import ru.practicum.main_service.dto.request.RequestStatusUpdateDto;
import ru.practicum.main_service.dto.request.RequestStatusUpdateResult;
import ru.practicum.main_service.entity.Event;
import ru.practicum.main_service.entity.Request;
import ru.practicum.main_service.enums.RequestStatus;
import ru.practicum.main_service.enums.RequestStatusToUpdate;
import ru.practicum.main_service.exceptions.*;
import ru.practicum.main_service.mapper.RequestMapper;
import ru.practicum.main_service.repository.EventRepository;
import ru.practicum.main_service.repository.RequestRepository;
import ru.practicum.main_service.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;
    private final UserRepository userRepository;

    @Override
    public List<RequestDto> getRequestsByOwnerOfEvent(Long userId, Long eventId) {
        return requestMapper.toRequestDtoList(requestRepository.findAllByEventWithInitiator(userId, eventId));
    }

    @Override
    @Transactional
    public RequestDto createRequest(Long userId, Long eventId) {
        if (requestRepository.existsByRequesterAndEvent(userId, eventId)) {
            throw new RequestAlreadyExistException("Request already exists");
        }
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotExistException("Event doesnt exist"));
        if (event.getInitiator().getId().equals(userId)) {
            throw new WrongUserException("Can't create request by initiator");
        }

        if (event.getPublishedOn() == null) {
            throw new EventIsNotPublishedException("Event is not published yet");
        }

        List<Request> requests = requestRepository.findAllByEvent(eventId);

        if (!event.getRequestModeration() && requests.size() >= event.getParticipantLimit()) {
            throw new ParticipantLimitException("Member limit exceeded ");
        }

        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setEvent(eventId);
        request.setRequester(userId);

        request.setStatus(event.getParticipantLimit() == 0 ? RequestStatus.CONFIRMED : RequestStatus.PENDING);
        return requestMapper.toRequestDto(requestRepository.save(request));
    }

    @Transactional
    @Override
    public RequestStatusUpdateResult updateRequests(Long userId, Long eventId, RequestStatusUpdateDto requestStatusUpdateDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotExistException("Event doesn't exist"));
        RequestStatusUpdateResult result = new RequestStatusUpdateResult();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            return result;
        }

        List<Request> requests = requestRepository.findAllByEventWithInitiator(userId, eventId);
        List<Request> requestsToUpdate = requests.stream().filter(x -> requestStatusUpdateDto.getRequestIds().contains(x.getId())).collect(Collectors.toList());

        if (requestsToUpdate.stream().anyMatch(x -> x.getStatus().equals(RequestStatus.CONFIRMED) && requestStatusUpdateDto.getStatus().equals(RequestStatusToUpdate.REJECTED))) {
            throw new RequestAlreadyConfirmedException("request already confirmed");
        }

        if (event.getConfirmedRequests() + requestsToUpdate.size() > event.getParticipantLimit() && requestStatusUpdateDto.getStatus().equals(RequestStatusToUpdate.CONFIRMED)) {
            throw new ParticipantLimitException("exceeding the limit of participants");
        }

        for (Request request : requestsToUpdate) {
            request.setStatus(RequestStatus.valueOf(requestStatusUpdateDto.getStatus().toString()));
        }

        requestRepository.saveAll(requestsToUpdate);

        if (requestStatusUpdateDto.getStatus().equals(RequestStatusToUpdate.CONFIRMED)) {
            event.setConfirmedRequests(event.getConfirmedRequests() + requestsToUpdate.size());
        }

        eventRepository.save(event);

        if (requestStatusUpdateDto.getStatus().equals(RequestStatusToUpdate.CONFIRMED)) {
            result.setConfirmedRequests(requestMapper.toRequestDtoList(requestsToUpdate));
        }

        if (requestStatusUpdateDto.getStatus().equals(RequestStatusToUpdate.REJECTED)) {
            result.setRejectedRequests(requestMapper.toRequestDtoList(requestsToUpdate));
        }

        return result;
    }

    @Override
    public List<RequestDto> getCurrentUserRequests(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new UserNotExistException(String.format("User with id=%s was not found", userId)));
        return requestMapper.toRequestDtoList(requestRepository.findAllByRequester(userId));
    }

    @Override
    public RequestDto cancelRequests(Long userId, Long requestId) {
        Request request = requestRepository.findByRequesterAndId(userId, requestId).orElseThrow(() -> new RequestNotExistException(String.format("Request with id=%s was not found", requestId)));
        request.setStatus(RequestStatus.CANCELED);
        return requestMapper.toRequestDto(requestRepository.save(request));
    }
}