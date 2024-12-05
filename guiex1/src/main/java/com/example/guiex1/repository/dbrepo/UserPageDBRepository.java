package com.example.guiex1.repository.dbrepo;

import com.example.guiex1.domain.User;
import com.example.guiex1.domain.validators.Validator;
import com.example.guiex1.repository.paging.Page;
import com.example.guiex1.repository.paging.Pageable;
import com.example.guiex1.repository.paging.PagingRepository;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class UserPageDBRepository extends UserDbRepository implements PagingRepository<Long, User> {
    public UserPageDBRepository(String url, String username, String password, Validator<User> validator) {
        super(url, username, password, validator);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        Set<User> users = new HashSet<>();

        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users offset ? limit ?");
            PreparedStatement countStatement = connection.prepareStatement("SELECT COUNT(*) AS count FROM users")){

            statement.setInt(1, pageable.getPageSize() * pageable.getPageNumber());
            statement.setInt(2, pageable.getPageSize());

            ResultSet resultSet = statement.executeQuery();
            ResultSet countResultSet = countStatement.executeQuery();

            while(resultSet.next()){
                Long id = resultSet.getLong("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");
                User user = new User(first_name, last_name, email, password);
                user.setId(id);
                users.add(user);
            }
            int totalCount = 0;

            if(countResultSet.next()){
                totalCount = countResultSet.getInt("count");
            }

            return new Page<>(users, totalCount);

        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
}
