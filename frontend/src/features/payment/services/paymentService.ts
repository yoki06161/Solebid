import { apiFetch } from '../../../utils/apiFetch';
import type { PaymentsPageDto, FetchPaymentsParams } from '../types/payment';

export async function fetchPayments(params: FetchPaymentsParams) {
    const qs = new URLSearchParams();

    if (params.page != null) qs.set('page', String(params.page));
    if (params.size != null) qs.set('size', String(params.size));
    if (params.userId != null) qs.set('userId', String(params.userId));
    if (params.status && params.status !== 'ALL') qs.set('status', params.status);
    if (params.from) qs.set('from', params.from);
    if (params.to) qs.set('to', params.to);
    if (params.sort) qs.set('sort', params.sort);

    return apiFetch<PaymentsPageDto>(`/api/payments/records?${qs.toString()}`);
}
