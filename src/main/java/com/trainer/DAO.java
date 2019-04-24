package com.trainer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO {
    private Connection connection;

    public DAO(){
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:dbExam");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getQuestion(int id) {

        try {
            Statement statement = connection.createStatement();
            ResultSet questionSet = statement.executeQuery(String.format("select * from Questions where id=%d", id));
            questionSet.next();
            String data = questionSet.getString("data");
            statement.close();
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Answer> getAnswers(int questionId) {
        List<Answer> answers = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet answerSet = statement.executeQuery(String.format("select * from answers where question_id=%d",
                    questionId));
            Answer answer;
            while (answerSet.next()){
                answer = new Answer(
                        answerSet.getString("data"),
                        answerSet.getInt("is_true") == 1
                );
                answers.add(answer);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }

    public int getMaxId() {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select MAX(id) as id from Questions;");
            resultSet.next();
            int id = resultSet.getInt("id");
            statement.close();
            return id;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public Connection getConnection() {
        return connection;
    }
}
