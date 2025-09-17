import { apiFetch } from '../../../utils/apiFetch';                       // ← ../../../ (api → profile → features → src)
import type { ApiResponse } from '../../user/types/AuthTypes';            // ← ../../ (api → profile → user)
import type { PointSummaryResponse } from '../types/point';

export async function fetchUserPoint() {
    return apiFetch<ApiResponse<PointSummaryResponse>>('/api/users/me/points');
}
