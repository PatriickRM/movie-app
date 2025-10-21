package com.movie.app.service.impl;

import com.movie.app.exception.BadRequestException;
import com.movie.app.exception.ForbiddenException;
import com.movie.app.exception.ResourceNotFoundException;
import com.movie.app.model.dto.common.PageResponse;
import com.movie.app.model.dto.customlist.request.AddMovieToListRequest;
import com.movie.app.model.dto.customlist.request.CreateListRequest;
import com.movie.app.model.dto.customlist.request.UpdateListRequest;
import com.movie.app.model.dto.customlist.response.CustomListDetailResponse;
import com.movie.app.model.dto.customlist.response.CustomListResponse;
import com.movie.app.model.dto.customlist.response.ListMovieResponse;
import com.movie.app.model.entity.CustomList;
import com.movie.app.model.entity.ListMovie;
import com.movie.app.model.entity.User;
import com.movie.app.model.entity.UserPlan;
import com.movie.app.repository.CustomListRepository;
import com.movie.app.repository.ListMovieRepository;
import com.movie.app.repository.UserRepository;
import com.movie.app.service.CustomListService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomListServiceImpl implements CustomListService {
    private final CustomListRepository customListRepository;
    private final ListMovieRepository listMovieRepository;
    private final UserRepository userRepository;

    //Crear nueva lista (solo Premium)
    @Transactional
    public CustomListResponse createList(Long userId, CreateListRequest request) {
        log.info("Creando nueva lista para usuario {}", userId);

        User user = findUserById(userId);

        // Verificar que sea usuario premium
        if (user.getPlan() != UserPlan.PREMIUM) {
            throw new ForbiddenException("Las listas personalizadas son una característica Premium");
        }

        // Crear lista
        CustomList customList = CustomList.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .isPublic(request.getIsPublic())
                .build();

        customList = customListRepository.save(customList);

        log.info("Lista '{}' creada exitosamente", customList.getName());

        return mapToCustomListResponse(customList);
    }

    //Actualizar lista
    @Transactional
    public CustomListResponse updateList(Long userId, Long listId, UpdateListRequest request) {
        log.info("Actualizando lista {} del usuario {}", listId, userId);

        CustomList customList = findListByIdAndUserId(listId, userId);

        //Actualizar campos si están presentes
        if (request.getName() != null) {
            customList.setName(request.getName());
        }
        if (request.getDescription() != null) {
            customList.setDescription(request.getDescription());
        }
        if (request.getIsPublic() != null) {
            customList.setIsPublic(request.getIsPublic());
        }

        customList = customListRepository.save(customList);

        log.info("Lista actualizada exitosamente");

        return mapToCustomListResponse(customList);
    }

    //Eliminar lista
    @Transactional
    public void deleteList(Long userId, Long listId) {
        log.info("Eliminando lista {} del usuario {}", listId, userId);

        CustomList customList = findListByIdAndUserId(listId, userId);

        // Eliminar todas las películas de la lista primero
        listMovieRepository.deleteByCustomListId(listId);

        // Eliminar la lista
        customListRepository.delete(customList);

        log.info("Lista eliminada exitosamente");
    }

    //Agregar pelicula a una lista
    @Transactional
    public void addMovieToList(Long userId, Long listId, AddMovieToListRequest request) {
        log.info("Agregando película {} a lista {} del usuario {}", request.getMovieId(), listId, userId);

        CustomList customList = findListByIdAndUserId(listId, userId);

        // Verificar si la película ya está en la lista
        if (listMovieRepository.existsByCustomListIdAndMovieId(listId, request.getMovieId())) {
            throw new BadRequestException("La película ya está en esta lista");
        }

        // Agregar película
        ListMovie listMovie = ListMovie.builder()
                .customList(customList)
                .movieId(request.getMovieId())
                .movieTitle(request.getMovieTitle())
                .moviePoster(request.getMoviePoster())
                .build();

        listMovieRepository.save(listMovie);

        log.info("Película agregada a la lista exitosamente");
    }

    //Eliminar una pelicula de una lista
    @Transactional
    public void removeMovieFromList(Long userId, Long listId, Integer movieId) {
        log.info("Eliminando película {} de lista {} del usuario {}", movieId, listId, userId);

        // Verificar que la lista pertenezca al usuario
        findListByIdAndUserId(listId, userId);

        // Verificar que la película esté en la lista
        if (!listMovieRepository.existsByCustomListIdAndMovieId(listId, movieId)) {
            throw new ResourceNotFoundException("La película no está en esta lista");
        }

        listMovieRepository.deleteByCustomListIdAndMovieId(listId, movieId);

        log.info("Película eliminada de la lista exitosamente");
    }

    //Obtener listas del usuario por paginación
    @Transactional(readOnly = true)
    public PageResponse<CustomListResponse> getUserLists(Long userId, int page, int size) {
        log.info("Obteniendo listas del usuario {} - Página: {}, Tamaño: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CustomList> listsPage = customListRepository.findByUserId(userId, pageable);

        List<CustomListResponse> lists = listsPage.getContent().stream()
                .map(this::mapToCustomListResponse)
                .collect(Collectors.toList());

        return PageResponse.<CustomListResponse>builder()
                .content(lists)
                .page(page)
                .size(size)
                .totalElements(listsPage.getTotalElements())
                .totalPages(listsPage.getTotalPages())
                .isLast(listsPage.isLast())
                .build();
    }


    //Obtener detalles de una lista con sus películas
    @Transactional(readOnly = true)
    public CustomListDetailResponse getListDetails(Long userId, Long listId) {
        log.info("Obteniendo detalles de lista {} del usuario {}", listId, userId);

        CustomList customList = findListByIdAndUserId(listId, userId);

        // Obtener películas de la lista
        List<ListMovie> listMovies = listMovieRepository.findByCustomListId(listId);

        List<ListMovieResponse> movies = listMovies.stream()
                .map(this::mapToListMovieResponse)
                .collect(Collectors.toList());

        return CustomListDetailResponse.builder()
                .id(customList.getId())
                .name(customList.getName())
                .description(customList.getDescription())
                .isPublic(customList.getIsPublic())
                .movies(movies)
                .createdAt(customList.getCreatedAt())
                .updatedAt(customList.getUpdatedAt())
                .build();
    }

    //Obtener listas publicas
    @Transactional(readOnly = true)
    public PageResponse<CustomListResponse> getPublicLists(int page, int size) {
        log.info("Obteniendo listas públicas - Página: {}, Tamaño: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CustomList> listsPage = customListRepository.findByIsPublicTrue(pageable);

        List<CustomListResponse> lists = listsPage.getContent().stream()
                .map(this::mapToCustomListResponse)
                .collect(Collectors.toList());

        return PageResponse.<CustomListResponse>builder()
                .content(lists)
                .page(page)
                .size(size)
                .totalElements(listsPage.getTotalElements())
                .totalPages(listsPage.getTotalPages())
                .isLast(listsPage.isLast())
                .build();
    }

    //Buscar usuario por id
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
    }

    //Buscar lista por ID y verificar que pertenezca al usuario
    private CustomList findListByIdAndUserId(Long listId, Long userId) {
        return customListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada o no pertenece al usuario"));
    }

    //Mapear CustomList a CustomListResponse
    private CustomListResponse mapToCustomListResponse(CustomList customList) {
        long movieCount = listMovieRepository.countByCustomListId(customList.getId());

        return CustomListResponse.builder()
                .id(customList.getId())
                .name(customList.getName())
                .description(customList.getDescription())
                .isPublic(customList.getIsPublic())
                .movieCount((int) movieCount)
                .createdAt(customList.getCreatedAt())
                .updatedAt(customList.getUpdatedAt())
                .build();
    }

    //Mapear ListMovie a ListMovieResponse
    private ListMovieResponse mapToListMovieResponse(ListMovie listMovie) {
        return ListMovieResponse.builder()
                .movieId(listMovie.getMovieId())
                .movieTitle(listMovie.getMovieTitle())
                .moviePoster(listMovie.getMoviePoster())
                .addedAt(listMovie.getAddedAt())
                .build();
    }
}