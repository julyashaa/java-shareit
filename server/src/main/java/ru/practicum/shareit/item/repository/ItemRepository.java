package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerIdOrderByIdAsc(Long ownerId);

    List<Item> findAllByRequest_IdIn(Collection<Long> requestIds);

    List<Item> findAllByRequest_Id(Long requestId);

    @Query("""
            select i
            from Item i
            where i.available = true
              and (lower(i.name) like lower(concat('%', :text, '%'))
                or lower(i.description) like lower(concat('%', :text, '%')))
            order by i.id
            """)
    List<Item> searchAvailable(@Param("text") String text);
}
