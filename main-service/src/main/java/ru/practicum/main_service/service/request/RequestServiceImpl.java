package ru.practicum.main_service.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_service.dto.request.RequestDto;
import ru.practicum.main_service.dto.request.RequestStatusUpdateDto;
import ru.practicum.main_service.dto.request.RequestStatusUpdateResult;
import ru.practicum.main_service.entity.Event;
import ru.practicum.main_service.entity.Request;
import ru.practicum.main_service.enums.RequestStatus;
import ru.practicum.main_service.enums.RequestStatusToUpdate;
import ru.practicum.main_service.exceptions.ConflictException;
import ru.practicum.main_service.exceptions.NotFoundException;
import ru.practicum.main_service.mapper.RequestMapper;
import ru.practicum.main_service.repository.EventRepository;
import ru.practicum.main_service.repository.RequestRepository;
import ru.practicum.main_service.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;
    private final UserRepository userRepository;

    @Override
    public List<RequestDto> getRequestsByOwnerOfEvent(Long userId, Long eventId) {
        log.info("Fetching requests by owner of event. User ID: {}, Event ID: {}", userId, eventId);
        return requestMapper.toRequestDtoList(requestRepository.findAllByEventWithInitiator(userId, eventId));
    }

    @Override
    @Transactional
    public RequestDto createRequest(Long userId, Long eventId) {
        log.info("Creating request. User ID: {}, Event ID: {}", userId, eventId);

        if (Boolean.TRUE.equals(requestRepository.existsByRequesterAndEvent(userId, eventId))) {
            log.error("Conflict: Request already exists");
            throw new ConflictException("Request already exists");
        }
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            log.error("Event with ID {} not found", eventId);
            return new NotFoundException("Event not found");
        });

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Can't create request by initiator");
        }

        if (event.getPublishedOn() == null) {
            throw new ConflictException("Event is not published yet");
        }

        List<Request> requests = requestRepository.findAllByEvent(eventId);

        if (Boolean.TRUE.equals(!event.getRequestModeration()) && requests.size() >= event.getParticipantLimit()) {
            throw new ConflictException("Member limit exceeded ");
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
    public RequestStatusUpdateResult updateRequests(
            Long userId,
            Long eventId,
            RequestStatusUpdateDto requestStatusUpdateDto) {
        log.info("Updating requests for User ID: {}, Event ID: {}", userId, eventId);

        Event event = eventRepository.findById(eventId).orElseThrow(()
                -> new NotFoundException("Event doesn't exist"));
        RequestStatusUpdateResult result = new RequestStatusUpdateResult();

        if (Boolean.TRUE.equals(!event.getRequestModeration()) || event.getParticipantLimit() == 0) {
            return result;
        }

        List<Request> requestsToUpdate = requestRepository.findAllByEventWithInitiatorAndIds(
                userId,
                eventId,
                requestStatusUpdateDto.getRequestIds());

        if (requestsToUpdate.stream().anyMatch(request -> request.getStatus().equals(RequestStatus.CONFIRMED)
                && requestStatusUpdateDto.getStatus().equals(RequestStatusToUpdate.REJECTED))) {
            throw new ConflictException("Request already confirmed");
        }

        if (event.getConfirmedRequests() + requestsToUpdate.size() > event.getParticipantLimit()
                && requestStatusUpdateDto.getStatus().equals(RequestStatusToUpdate.CONFIRMED)) {
            throw new ConflictException("Limit of participants exceeded");
        }

        for (Request request : requestsToUpdate) {
            request.setStatus(RequestStatus.valueOf(requestStatusUpdateDto.getStatus().toString()));
        }

        if (requestStatusUpdateDto.getStatus().equals(RequestStatusToUpdate.CONFIRMED)) {
            event.setConfirmedRequests(event.getConfirmedRequests() + requestsToUpdate.size());
        }

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
        log.info("Fetching current user's requests. User ID: {}", userId);
        userRepository.findById(userId).orElseThrow(() -> {
            log.error("User with ID {} not found", userId);
            return new NotFoundException(String.format("User with id=%s was not found", userId));
        });
        return requestMapper.toRequestDtoList(requestRepository.findAllByRequester(userId));
    }

    @Override
    public RequestDto cancelRequests(Long userId, Long requestId) {
        log.info("Cancelling request. User ID: {}, Request ID: {}", userId, requestId);

        Request request = requestRepository.findByRequesterAndId(userId, requestId).orElseThrow(() -> {
            log.error("Request with ID {} not found", requestId);
            return new NotFoundException(String.format("Request with id=%s was not found", requestId));
        });
        request.setStatus(RequestStatus.CANCELED);
        return requestMapper.toRequestDto(requestRepository.save(request));
    }
}