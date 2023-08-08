package ru.practicum.main_service.service.request;

import ru.practicum.main_service.dto.request.RequestDto;
import ru.practicum.main_service.dto.request.RequestStatusUpdateDto;
import ru.practicum.main_service.dto.request.RequestStatusUpdateResult;

import java.util.List;

public interface RequestService {

    RequestDto createRequest(Long userId, Long eventId);

    List<RequestDto> getRequestsByOwnerOfEvent(Long userId, Long eventId);

    RequestStatusUpdateResult updateRequests(Long userId, Long eventId, RequestStatusUpdateDto requestStatusUpdateDto);

    List<RequestDto> getCurrentUserRequests(Long userId);

    RequestDto cancelRequests(Long userId, Long requestId);
}
