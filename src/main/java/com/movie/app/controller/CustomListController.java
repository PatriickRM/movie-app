package com.movie.app.controller;

import com.movie.app.model.dto.common.ApiResponse;
import com.movie.app.model.dto.common.PageResponse;
import com.movie.app.model.dto.customlist.request.AddMovieToListRequest;
import com.movie.app.model.dto.customlist.request.CreateListRequest;
import com.movie.app.model.dto.customlist.request.UpdateListRequest;
import com.movie.app.model.dto.customlist.response.CustomListDetailResponse;
import com.movie.app.model.dto.customlist.response.CustomListResponse;
import com.movie.app.security.UserPrincipal;
import com.movie.app.service.CustomListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lists")
@RequiredArgsConstructor
@Slf4j
public class CustomListController {
    private final CustomListService customListService;

    //Crear una nueva lista
    @PostMapping
    public ResponseEntity<ApiResponse<CustomListResponse>> createList(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                      @Valid @RequestBody CreateListRequest request) {

        log.info("POST /api/lists - User: {}, List: {}", userPrincipal.getId(), request.getName());

        CustomListResponse list = customListService.createList(userPrincipal.getId(), request);
        ApiResponse<CustomListResponse> response = ApiResponse.<CustomListResponse>builder()
                .success(true)
                .message("Lista creada exitosamente")
                .data(list)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    //Obtener listas del usuario con paginación
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CustomListResponse>>> getUserLists(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                                      @RequestParam(defaultValue = "0") int page,
                                                                                      @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/lists - User: {}, Page: {}, Size: {}", userPrincipal.getId(), page, size);

        PageResponse<CustomListResponse> lists = customListService.getUserLists(
                userPrincipal.getId(), page, size
        );
        ApiResponse<PageResponse<CustomListResponse>> response =
                ApiResponse.<PageResponse<CustomListResponse>>builder()
                        .success(true)
                        .data(lists)
                        .build();

        return ResponseEntity.ok(response);
    }


    //Obtener detalles de una lista con sus películas
    @GetMapping("/{listId}")
    public ResponseEntity<ApiResponse<CustomListDetailResponse>> getListDetails(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                                @PathVariable Long listId) {
        log.info("GET /api/lists/{} - User: {}", listId, userPrincipal.getId());

        CustomListDetailResponse listDetails = customListService.getListDetails(userPrincipal.getId(), listId);
        ApiResponse<CustomListDetailResponse> response =
                ApiResponse.<CustomListDetailResponse>builder()
                        .success(true)
                        .data(listDetails)
                        .build();

        return ResponseEntity.ok(response);
    }

    //Obtener listas publicas
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<PageResponse<CustomListResponse>>> getPublicLists(@RequestParam(defaultValue = "0") int page,
                                                                                        @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/lists/public - Page: {}, Size: {}", page, size);

        PageResponse<CustomListResponse> lists = customListService.getPublicLists(page, size);
        ApiResponse<PageResponse<CustomListResponse>> response =
                ApiResponse.<PageResponse<CustomListResponse>>builder()
                        .success(true)
                        .data(lists)
                        .build();

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{listId}")
    public ResponseEntity<ApiResponse<CustomListResponse>> updateList(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                      @PathVariable Long listId,
                                                                      @Valid @RequestBody UpdateListRequest request) {

        log.info("PUT /api/lists/{} - User: {}", listId, userPrincipal.getId());
        CustomListResponse list = customListService.updateList(userPrincipal.getId(), listId, request);

        ApiResponse<CustomListResponse> response = ApiResponse.<CustomListResponse>builder()
                .success(true)
                .message("Lista actualizada exitosamente")
                .data(list)
                .build();

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{listId}")
    public ResponseEntity<ApiResponse<Void>> deleteList(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Long listId) {
        log.info("DELETE /api/lists/{} - User: {}", listId, userPrincipal.getId());

        customListService.deleteList(userPrincipal.getId(), listId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Lista eliminada exitosamente")
                .build();

        return ResponseEntity.ok(response);
    }


    //Agregar película a una lista
    @PostMapping("/{listId}/movies")
    public ResponseEntity<ApiResponse<Void>> addMovieToList(@AuthenticationPrincipal UserPrincipal userPrincipal,@PathVariable Long listId,
                                                            @Valid @RequestBody AddMovieToListRequest request) {

        log.info("POST /api/lists/{}/movies - User: {}, Movie: {}", listId, userPrincipal.getId(), request.getMovieId());
        customListService.addMovieToList(userPrincipal.getId(), listId, request);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Película agregada a la lista")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @DeleteMapping("/{listId}/movies/{movieId}")
    public ResponseEntity<ApiResponse<Void>> removeMovieFromList(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                 @PathVariable Long listId,
                                                                 @PathVariable Integer movieId) {

        log.info("DELETE /api/lists/{}/movies/{} - User: {}", listId, movieId, userPrincipal.getId());

        customListService.removeMovieFromList(userPrincipal.getId(), listId, movieId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Película eliminada de la lista")
                .build();
        return ResponseEntity.ok(response);
    }
}