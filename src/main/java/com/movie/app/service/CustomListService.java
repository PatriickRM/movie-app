package com.movie.app.service;

import com.movie.app.model.dto.common.PageResponse;
import com.movie.app.model.dto.customlist.request.AddMovieToListRequest;
import com.movie.app.model.dto.customlist.request.CreateListRequest;
import com.movie.app.model.dto.customlist.request.UpdateListRequest;
import com.movie.app.model.dto.customlist.response.CustomListDetailResponse;
import com.movie.app.model.dto.customlist.response.CustomListResponse;

public interface CustomListService {
    CustomListResponse createList(Long userId, CreateListRequest request);
    CustomListResponse updateList(Long userId, Long listId, UpdateListRequest request);
    void deleteList(Long userId, Long listId);
    void addMovieToList(Long userId, Long listId, AddMovieToListRequest request);
    void removeMovieFromList(Long userId, Long listId, Integer movieId);
    PageResponse<CustomListResponse> getUserLists(Long userId, int page, int size);
    CustomListDetailResponse getListDetails(Long userId, Long listId);
    PageResponse<CustomListResponse> getPublicLists(int page, int size);
}
