package com.capgemini.test.code.repository;

import com.capgemini.test.code.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}