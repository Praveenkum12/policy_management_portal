import { useCallback, useEffect, useState } from 'react';
import { listPolicies } from '../api/policyApi';

const INITIAL_QUERY = {
  search: '',
  status: '',
  page: 0,
  limit: 10,
  sort: 'createdAt',
  direction: 'desc',
};

const SEARCH_DEBOUNCE_MS = 350;

export function usePolicies() {
  const [query, setQuery] = useState(INITIAL_QUERY);
  const [searchInput, setSearchInput] = useState('');
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [reloadToken, setReloadToken] = useState(0);

  useEffect(() => {
    const timer = setTimeout(() => {
      const search = searchInput.trim();
      setQuery((q) => (q.search === search ? q : { ...q, search, page: 0 }));
    }, SEARCH_DEBOUNCE_MS);
    return () => clearTimeout(timer);
  }, [searchInput]);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError(null);

    listPolicies(query, controller.signal)
      .then((result) => {
        setData(result);
        if (result.content.length === 0 && result.page > 0 && result.totalPages > 0) {
          setQuery((q) => ({ ...q, page: result.totalPages - 1 }));
        }
      })
      .catch((err) => {
        if (err.name !== 'AbortError') setError(err);
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });

    return () => controller.abort();
  }, [query, reloadToken]);

  const setStatus = useCallback((status) => setQuery((q) => ({ ...q, status, page: 0 })), []);
  const setPage = useCallback((page) => setQuery((q) => ({ ...q, page })), []);
  const setLimit = useCallback((limit) => setQuery((q) => ({ ...q, limit, page: 0 })), []);

  const toggleSort = useCallback(
    (field) =>
      setQuery((q) => ({
        ...q,
        sort: field,
        direction: q.sort === field && q.direction === 'asc' ? 'desc' : 'asc',
        page: 0,
      })),
    [],
  );

  const clearFilters = useCallback(() => {
    setSearchInput('');
    setQuery((q) => ({ ...q, search: '', status: '', page: 0 }));
  }, []);

  const reload = useCallback(() => setReloadToken((token) => token + 1), []);

  return {
    query,
    data,
    loading,
    error,
    searchInput,
    setSearchInput,
    setStatus,
    setPage,
    setLimit,
    toggleSort,
    clearFilters,
    reload,
  };
}
