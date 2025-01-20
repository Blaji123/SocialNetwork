package com.example.guiex1.repository.paging;

import com.example.guiex1.domain.Entity;
import com.example.guiex1.repository.Repository;

public interface PagingRepository<ID, E extends Entity<ID>> extends Repository<ID, E> {
    Page<E> findAllPaged(Long userId, Pageable pageable);
}
