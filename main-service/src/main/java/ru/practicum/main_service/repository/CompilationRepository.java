package ru.practicum.main_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.main_service.entity.Compilation;


@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {

}
