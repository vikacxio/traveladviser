package com.kahanchale.splitexpense.service;

import com.kahanchale.splitexpense.dto.UserDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final JdbcTemplate travelAppJdbcTemplate;

    public UserService(@Qualifier("travelAppJdbcTemplate") JdbcTemplate travelAppJdbcTemplate) {
        this.travelAppJdbcTemplate = travelAppJdbcTemplate;
    }

    /**
     * Get all users from travel_app database excluding the current user
     */
    public List<UserDTO> getAllUsersExcludingCurrent(Long currentUserId) {
        String sql = "SELECT id, email, name FROM users WHERE id != ? ORDER BY name";
        return travelAppJdbcTemplate.query(
            sql,
            new Object[]{currentUserId},
            (rs, rowNum) -> new UserDTO(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("name")
            )
        );
    }

    /**
     * Get all users from travel_app database
     */
    public List<UserDTO> getAllUsers() {
        String sql = "SELECT id, email, name FROM users ORDER BY name";
        return travelAppJdbcTemplate.query(
            sql,
            (rs, rowNum) -> new UserDTO(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("name")
            )
        );
    }

    /**
     * Get user by id
     */
    public UserDTO getUserById(Long userId) {
        String sql = "SELECT id, email, name FROM users WHERE id = ?";
        List<UserDTO> users = travelAppJdbcTemplate.query(
            sql,
            new Object[]{userId},
            (rs, rowNum) -> new UserDTO(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("name")
            )
        );
        return users.isEmpty() ? null : users.get(0);
    }
}
