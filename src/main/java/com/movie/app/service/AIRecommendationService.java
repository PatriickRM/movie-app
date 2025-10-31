package com.movie.app.service;

import com.movie.app.model.dto.ai.request.AIRecommendationRequest;
import com.movie.app.model.dto.ai.response.AIRecommendationResponse;
import com.movie.app.model.dto.auth.response.AILimitResponse;

public interface AIRecommendationService {
    AIRecommendationResponse getRecommendation(Long userId, AIRecommendationRequest request);
    AILimitResponse canRequestAI(Long userId);
}