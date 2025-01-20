package com.example.guiex1.repository.dbrepo;

import com.example.guiex1.domain.Friendship;
import com.example.guiex1.domain.Tuple;
import com.example.guiex1.domain.validators.Validator;
import com.example.guiex1.repository.paging.Page;
import com.example.guiex1.repository.paging.Pageable;
import com.example.guiex1.repository.paging.PagingRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FriendshipPagingRepository extends FriendshipDBRepository implements PagingRepository<Tuple<Long, Long>, Friendship> {


    public FriendshipPagingRepository(Validator validator) {
        super(validator);
    }

    @Override
    public Page<Friendship> findAllPaged(Long userId, Pageable pageable) {
        List<Friendship> friendships = new ArrayList<>();
        int totalElementCount = 0;

        try(Connection connection = DriverManager.getConnection(url, username, password)) {

            try (PreparedStatement countStatement = connection.prepareStatement("SELECT COUNT(*) FROM friendships WHERE id1 = ? OR id2 = ?")) {
                countStatement.setLong(1, userId);
                countStatement.setLong(2, userId);
                ResultSet countResultSet = countStatement.executeQuery();
                if (countResultSet.next()) {
                    totalElementCount = countResultSet.getInt(1);
                }
            }

            String query = "SELECT * FROM friendships WHERE id1 = ? OR id2 = ? ORDER BY id1, id2 LIMIT ? OFFSET ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setLong(1, userId);
                preparedStatement.setLong(2, userId);
                preparedStatement.setInt(3, pageable.getPageSize());
                preparedStatement.setInt(4, pageable.getPageNumber() * pageable.getPageSize());
                ResultSet resultSet = preparedStatement.executeQuery();
                while(resultSet.next()){
                    Long id1 = resultSet.getLong("id1");
                    Long id2 = resultSet.getLong("id2");
                    Timestamp date = resultSet.getTimestamp("friends_from");
                    LocalDateTime friendsFrom = date.toLocalDateTime();

                    Friendship friendship = new Friendship(friendsFrom);
                    friendship.setId(new Tuple<>(id1, id2));
                    friendships.add(friendship);
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return new Page<>(friendships, totalElementCount);
    }
}
