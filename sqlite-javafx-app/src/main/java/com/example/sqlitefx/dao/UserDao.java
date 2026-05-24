package com.example.sqlitefx.dao;

import com.example.sqlitefx.db.Database;
import com.example.sqlitefx.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

  public UserDao() {
    try {
      Database.initialize();
    } catch (SQLException exception) {
      throw new IllegalStateException("Failed to initialize SQLite database.", exception);
    }
  }

  public List<User> findAll() throws SQLException {
    String sql = "SELECT id, full_name, email, phone, created_at FROM users ORDER BY id DESC";
    List<User> users = new ArrayList<>();

    try (Connection connection = Database.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql)) {

      while (resultSet.next()) {
        users.add(mapRow(resultSet));
      }
    }

    return users;
  }

  public Optional<User> findById(int id) throws SQLException {
    String sql = "SELECT id, full_name, email, phone, created_at FROM users WHERE id = ?";

    try (Connection connection = Database.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, id);

      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return Optional.of(mapRow(resultSet));
        }
      }
    }

    return Optional.empty();
  }

  public User insert(User user) throws SQLException {
    String sql = "INSERT INTO users (full_name, email, phone) VALUES (?, ?, ?)";

    try (Connection connection = Database.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      statement.setString(1, user.getFullName());
      statement.setString(2, user.getEmail());
      statement.setString(3, user.getPhone());

      int affectedRows = statement.executeUpdate();
      if (affectedRows == 0) {
        throw new SQLException("Insert failed, no rows were added.");
      }

      try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          user.setId(generatedKeys.getInt(1));
        }
      }

      findById(user.getId()).ifPresent(saved -> user.setCreatedAt(saved.getCreatedAt()));
      return user;
    }
  }

  public boolean update(User user) throws SQLException {
    String sql = "UPDATE users SET full_name = ?, email = ?, phone = ? WHERE id = ?";

    try (Connection connection = Database.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, user.getFullName());
      statement.setString(2, user.getEmail());
      statement.setString(3, user.getPhone());
      statement.setInt(4, user.getId());

      return statement.executeUpdate() > 0;
    }
  }

  public boolean delete(int id) throws SQLException {
    String sql = "DELETE FROM users WHERE id = ?";

    try (Connection connection = Database.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, id);
      return statement.executeUpdate() > 0;
    }
  }

  private User mapRow(ResultSet resultSet) throws SQLException {
    return new User(
        resultSet.getInt("id"),
        resultSet.getString("full_name"),
        resultSet.getString("email"),
        resultSet.getString("phone"),
        resultSet.getString("created_at"));
  }
}
