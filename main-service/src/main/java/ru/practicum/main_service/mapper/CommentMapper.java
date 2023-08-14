package ru.practicum.main_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.main_service.dto.comment.CommentDto;
import ru.practicum.main_service.entity.Comment;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "author.name", target = "authorName")
    @Mapping(source = "event.id", target = "eventId")
    CommentDto toCommentDto(Comment comment);

    List<CommentDto> toCommentDtos(List<Comment> comment);
}
