const BASE_URL = import.meta.env.VITE_API_URL ?? '/api/policies';

export class ApiError extends Error {
  constructor(message, { status = 0, fieldErrors = [] } = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

async function request(url, options = {}) {
  let response;
  try {
    response = await fetch(url, {
      ...options,
      headers: { Accept: 'application/json', ...(options.body && { 'Content-Type': 'application/json' }) },
    });
  } catch (error) {
    if (error.name === 'AbortError') throw error;
    throw new ApiError('Cannot reach the server. Check that the backend is running and try again.');
  }

  if (response.status === 204) return null;

  const body = await response.json().catch(() => null);
  if (!response.ok) {
    throw new ApiError(body?.message ?? `Request failed (${response.status})`, {
      status: response.status,
      fieldErrors: body?.fieldErrors ?? [],
    });
  }
  return body;
}

export function listPolicies({ search, status, page, limit, sort, direction }, signal) {
  const params = new URLSearchParams();
  if (search) params.set('search', search);
  if (status) params.set('status', status);
  params.set('page', page);
  params.set('limit', limit);
  if (sort) params.set('sort', sort);
  if (direction) params.set('direction', direction);
  return request(`${BASE_URL}?${params}`, { signal });
}

export const createPolicy = (policy) =>
  request(BASE_URL, { method: 'POST', body: JSON.stringify(policy) });

export const updatePolicy = (id, policy) =>
  request(`${BASE_URL}/${id}`, { method: 'PUT', body: JSON.stringify(policy) });

export const deletePolicy = (id) => request(`${BASE_URL}/${id}`, { method: 'DELETE' });
