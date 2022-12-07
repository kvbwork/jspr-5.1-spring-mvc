package ru.netology.mapper;

import org.mapstruct.Mapper;
import ru.netology.dto.PostDto;
import ru.netology.model.Post;

@Mapper(componentModel = "spring")
public interface PostMapper {

    PostDto postToPostDto(Post post);

    Post postDtoToPost(PostDto dto);

}
